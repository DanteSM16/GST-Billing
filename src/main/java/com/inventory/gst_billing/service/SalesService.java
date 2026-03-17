package com.inventory.gst_billing.service;

import com.inventory.gst_billing.dto.*;
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
public class SalesService {

    private final SalesInvoiceRepository salesInvoiceRepo;
    private final SalesItemRepository salesItemRepo;
    private final StockRepository stockRepo;
    private final GstLedgerRepository gstLedgerRepo;
    private final UserRepository userRepo;
    private final PaymentService paymentService;

    //private final String STORE_STATE_CODE = "MH"; // Hardcoded shop location
    // was used before state and store id logic


    public SalesService(SalesInvoiceRepository salesInvoiceRepo, SalesItemRepository salesItemRepo,
                        StockRepository stockRepo, GstLedgerRepository gstLedgerRepo, UserRepository userRepo, PaymentService paymentService) {
        this.salesInvoiceRepo = salesInvoiceRepo;
        this.salesItemRepo = salesItemRepo;
        this.stockRepo = stockRepo;
        this.gstLedgerRepo = gstLedgerRepo;
        this.userRepo = userRepo;
        this.paymentService = paymentService;
    }

    @Transactional
    public SalesInvoiceResponse createSalesInvoice(SalesInvoiceRequest request) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepo.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));
        Store userStore = loggedInUser.getStore();
        if (userStore == null) throw new RuntimeException("This user is not assigned to a Store!");

        // If they claim they paid online via Razorpay, verify that using the secret key math !
        if ("RAZORPAY_ONLINE".equals(request.getPaymentMethod())) {

            if (request.getRazorpayPaymentId() == null || request.getRazorpayOrderId() == null || request.getRazorpaySignature() == null) {
                throw new RuntimeException("Missing Payment Verification Data!");
            }

            boolean isAuthentic = paymentService.verifySignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );

            if (!isAuthentic) {
                throw new RuntimeException("SECURITY BREACH: Payment Signature Verification Failed.");
            }
        }
        // If it's CASH, it skips the verification entirely and goes straight to saving the invoice!

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        // 1. Save empty Header to get ID
        SalesInvoice invoice = new SalesInvoice();
        invoice.setCustomerName(request.getCustomerName());
        invoice.setCustomerContact(request.getCustomerContact());
        invoice.setBillingAddress(request.getBillingAddress());
        invoice.setStateCode(request.getStateCode().toUpperCase());
        invoice.setStore(userStore);
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setRazorpayOrderId(request.getRazorpayOrderId());
        invoice.setRazorpayPaymentId(request.getRazorpayPaymentId());
        invoice = salesInvoiceRepo.save(invoice);

        // 2. Process each scanned item
        for (SalesItemRequest itemReq : request.getItems()) {

            // Find the physical stock. Is it real? Is it already sold?
            Stock stock = stockRepo.findById(itemReq.getSerialNo())
                    .orElseThrow(() -> new RuntimeException("Serial Number not found: " + itemReq.getSerialNo()));
            // cannot sell stock belonging to another store, will mess gst complications
            if (!stock.getStore().getId().equals(userStore.getId())) {
                throw new RuntimeException("Security issue: Serial Number " + itemReq.getSerialNo() + " belongs to a different branch!");
            }

            if (stock.getStatus() == StockStatus.SOLD) {
                throw new RuntimeException("Item already sold! Serial: " + itemReq.getSerialNo());
            }

            // Mark as SOLD
            stock.setStatus(StockStatus.SOLD);
            stockRepo.save(stock);

            // Fetch the GST Rate from the Product Template attached to this stock
            BigDecimal gstRate = stock.getProduct().getGstRate();

            // Calculate tax for this specific line item
            BigDecimal lineTaxable = itemReq.getSellingPrice();
            BigDecimal lineTax = lineTaxable.multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            totalTaxable = totalTaxable.add(lineTaxable);
            totalTax = totalTax.add(lineTax);

            // Save the Sales Item line
            SalesItem salesItem = new SalesItem();
            salesItem.setSalesInvoice(invoice);
            salesItem.setStock(stock);
            salesItem.setSellingPrice(itemReq.getSellingPrice());
            salesItemRepo.save(salesItem);
        }

        // 3. Split tax into IGST or CGST/SGST based on customer state
        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;

        if (invoice.getStateCode().equals(userStore.getStateCode())) {
            cgst = totalTax.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            sgst = totalTax.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        } else {
            igst = totalTax;
        }

        // 4. Update Invoice Totals
        invoice.setTotalTaxable(totalTaxable);
        invoice.setCgstTotal(cgst);
        invoice.setSgstTotal(sgst);
        invoice.setIgstTotal(igst);

        salesInvoiceRepo.save(invoice);

        // 5. Log OUTPUT tax liability to GST Ledger
        logGstLedger(cgst, GstComponent.CGST, invoice.getId(), userStore);
        logGstLedger(sgst, GstComponent.SGST, invoice.getId(), userStore);
        logGstLedger(igst, GstComponent.IGST, invoice.getId(), userStore);

        // 6. Return receipt data
        SalesInvoiceResponse response = new SalesInvoiceResponse();
        response.setInvoiceId(invoice.getId());
        response.setMessage("Sale successful! Stock updated and Output Tax logged.");

        response.setStoreName(userStore.getStoreName());
        response.setStoreGstin(userStore.getGstin());
        response.setStoreAddress(userStore.getAddress());
        response.setStoreStateCode(userStore.getStateCode());

        response.setTotalTaxable(totalTaxable);
        response.setCgstTotal(cgst);
        response.setSgstTotal(sgst);
        response.setIgstTotal(igst);
        response.setPaymentMethod(invoice.getPaymentMethod());
        response.setPaymentId(invoice.getRazorpayPaymentId());

        return response;
    }

    private void logGstLedger(BigDecimal amount, GstComponent component, Integer invoiceId, Store store) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            GstLedger ledger = new GstLedger();
            ledger.setComponent(component);
            ledger.setType(GstType.OUTPUT); // Sales are ALWAYS Output Tax Liability!
            ledger.setAmount(amount);
            ledger.setTransactionId(invoiceId);
            ledger.setTransactionType("SALE");
            ledger.setStore(store);
            gstLedgerRepo.save(ledger);
        }
    }
}
