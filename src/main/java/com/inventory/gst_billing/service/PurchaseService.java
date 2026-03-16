package com.inventory.gst_billing.service;

import com.inventory.gst_billing.dto.PurchaseInvoiceRequest;
import com.inventory.gst_billing.dto.PurchaseInvoiceResponse;
import com.inventory.gst_billing.dto.PurchaseItemRequest;
import com.inventory.gst_billing.entity.*;
import com.inventory.gst_billing.enums.GstComponent;
import com.inventory.gst_billing.enums.GstType;
import com.inventory.gst_billing.enums.StockStatus;
import com.inventory.gst_billing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PurchaseService {

    private final PurchaseInvoiceRepository invoiceRepo;
    private final PurchaseItemRepository itemRepo;
    private final StockRepository stockRepo;
    private final ProductTemplateRepository productRepo;
    private final GstLedgerRepository gstLedgerRepo;
    private final UserRepository userRepo;

    //private final String STORE_STATE_CODE = "MH"; // Hardcoded shop location for GST logic
    // REMOVED ABOVE LATER WHEN ADDED LOGCI OF STORE ID AND STATE THROUGH STORE ENTITY.

    public PurchaseService(PurchaseInvoiceRepository invoiceRepo, PurchaseItemRepository itemRepo,
                           StockRepository stockRepo, ProductTemplateRepository productRepo,
                           GstLedgerRepository gstLedgerRepo, UserRepository userRepo) {
        this.invoiceRepo = invoiceRepo;
        this.itemRepo = itemRepo;
        this.stockRepo = stockRepo;
        this.productRepo = productRepo;
        this.gstLedgerRepo = gstLedgerRepo;
        this.userRepo = userRepo;
    }

    // @Transactional ensures if ANY step fails, the entire database reverts to ensure ACID propwrty
    @Transactional
    public PurchaseInvoiceResponse createPurchaseInvoice(PurchaseInvoiceRequest request) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepo.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));
        Store userStore = loggedInUser.getStore();
        if (userStore == null) throw new RuntimeException("This user is not assigned to a Store!");

        // Ensuring the store manager can only purchase and add stock to the store they are assigned to
        if (!userStore.getId().equals(request.getStoreId())) {
            throw new RuntimeException("You can only purchase for your own store (Store ID: " + userStore.getId() + ")");
        }

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        // 1. Create and save the empty Invoice Header first to get an ID
        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setSupplierName(request.getSupplierName());
        invoice.setSupplierGstin(request.getSupplierGstin());
        invoice.setBillingAddress(request.getBillingAddress());
        invoice.setContactNumber(request.getContactNumber());
        invoice.setStateCode(request.getStateCode().toUpperCase());
        invoice.setStore(userStore);
        invoice = invoiceRepo.save(invoice);

        // 2. Loop through the "Shopping Cart" items
        for (PurchaseItemRequest itemReq : request.getItems()) {

            // Validate serial numbers match quantity
            if (itemReq.getSerialNumbers().size() != itemReq.getQuantity()) {
                throw new RuntimeException("Quantity does not match number of Serial Numbers provided!");
            }

            ProductTemplate product = productRepo.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found!"));

            // Taxable amount/total for this line = Price * Quantity
            BigDecimal lineTaxable = itemReq.getPurchasePrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            // Tax for this line = LineTaxable * (GstRate / 100)
            BigDecimal lineTax = lineTaxable.multiply(product.getGstRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            totalTaxable = totalTaxable.add(lineTaxable);
            totalTax = totalTax.add(lineTax);

            // Save the PurchaseItem
            PurchaseItem item = new PurchaseItem();
            item.setPurchaseInvoice(invoice);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPurchasePrice(itemReq.getPurchasePrice());
            itemRepo.save(item);

            // 3. Auto-generate the individual Stock (Serial Numbers)
            for (String serialNo : itemReq.getSerialNumbers()) {
                if (stockRepo.existsById(serialNo)) {
                    throw new RuntimeException("Duplicate Serial Number detected: " + serialNo);
                }
                Stock stock = new Stock();
                stock.setSerialNo(serialNo);
                stock.setProduct(product);
                stock.setPurchaseInvoice(invoice);
                stock.setStatus(StockStatus.AVAILABLE);
                stock.setStore(userStore);
                stockRepo.save(stock);
            }
        }

        // 4. Calculate IGST vs CGST/SGST based on State Code
        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;

        //boolean isIntraState = invoice.getStateCode().equals(STORE_STATE_CODE);
        // the above was prev version with hardcoded string
        boolean isIntraState = invoice.getStateCode().equals(userStore.getStateCode());

        if (isIntraState) {
            cgst = totalTax.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            sgst = totalTax.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        } else {
            igst = totalTax;
        }

        // Update the Invoice with final totals
        invoice.setTotalTaxable(totalTaxable);
        invoice.setCgstTotal(cgst);
        invoice.setSgstTotal(sgst);
        invoice.setIgstTotal(igst);
        invoiceRepo.save(invoice);

        // 5. Auto-log the Input Tax Credit (ITC) to the GST Ledger
        logGstLedger(cgst, GstComponent.CGST, invoice.getId(), userStore);
        logGstLedger(sgst, GstComponent.SGST, invoice.getId(), userStore);
        logGstLedger(igst, GstComponent.IGST, invoice.getId(), userStore);

        // 6. Build the Response
        PurchaseInvoiceResponse response = new PurchaseInvoiceResponse();
        response.setInvoiceId(invoice.getId());
        response.setMessage("Purchase Invoice created, Stock added, and ITC logged successfully!");
        response.setStoreName(userStore.getStoreName());
        response.setStoreGstin(userStore.getGstin());
        response.setStoreAddress(userStore.getAddress());
        response.setStoreStateCode(userStore.getStateCode());
        response.setTotalTaxable(totalTaxable);
        response.setCgstTotal(cgst);
        response.setSgstTotal(sgst);
        response.setIgstTotal(igst);

        return response;
    }

    // Helper method to write to the GST Ledger
    private void logGstLedger(BigDecimal amount, GstComponent component, Integer invoiceId, Store store) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            GstLedger ledger = new GstLedger();
            ledger.setComponent(component);
            ledger.setType(GstType.INPUT); // Purchases are ALWAYS Input Tax Credit
            ledger.setAmount(amount);
            ledger.setTransactionId(invoiceId);
            ledger.setTransactionType("PURCHASE");
            ledger.setStore(store);
            gstLedgerRepo.save(ledger);
        }
    }
}