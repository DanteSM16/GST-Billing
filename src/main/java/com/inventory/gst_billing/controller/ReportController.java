package com.inventory.gst_billing.controller;
import com.inventory.gst_billing.dto.*;
import com.inventory.gst_billing.entity.PurchaseInvoice;
import com.inventory.gst_billing.entity.PurchaseItem;
import com.inventory.gst_billing.entity.SalesInvoice;
import com.inventory.gst_billing.entity.SalesItem;
import com.inventory.gst_billing.repository.PurchaseInvoiceRepository;
import com.inventory.gst_billing.repository.PurchaseItemRepository;
import com.inventory.gst_billing.repository.SalesInvoiceRepository;
import com.inventory.gst_billing.repository.SalesItemRepository;
import com.inventory.gst_billing.service.GstCalculationService;
import com.inventory.gst_billing.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final GstCalculationService gstService;
    private final SalesItemRepository salesItemRepo;
    private final SalesInvoiceRepository salesInvoiceRepo;
    private final PdfService pdfService;
    private final PurchaseInvoiceRepository purchaseInvoiceRepo;
    private final PurchaseItemRepository purchaseItemRepo;

    public ReportController(GstCalculationService gstService, SalesItemRepository salesItemRepo,
                            SalesInvoiceRepository salesInvoiceRepo, PdfService pdfService,
                            PurchaseInvoiceRepository purchaseInvoiceRepo,
                            PurchaseItemRepository purchaseItemRepo)
    {
        this.gstService = gstService;
        this.salesItemRepo = salesItemRepo;
        this.salesInvoiceRepo = salesInvoiceRepo;
        this.pdfService = pdfService;
        this.purchaseInvoiceRepo = purchaseInvoiceRepo;
        this.purchaseItemRepo = purchaseItemRepo;
    }

    // 1. PERIOD SETTLEMENT (Closes the month)
    @PreAuthorize("hasAnyRole('OWNER', 'FINANCE_MGR')")
    @PostMapping("/settle")
    public ResponseEntity<String> settleMonth(@RequestParam String stateCode, @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(gstService.settleMonth(stateCode, year, month));
    }

    // 2. HSN SUMMARY REPORT
    @PreAuthorize("hasAnyRole('OWNER', 'FINANCE_MGR')")
    @GetMapping("/hsn-summary")
    public ResponseEntity<List<HsnSummaryDto>> getHsnSummary(@RequestParam String stateCode, @RequestParam int year, @RequestParam int month) {
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.atEndOfMonth().atTime(23, 59, 59);
        return ResponseEntity.ok(salesItemRepo.getHsnSummary(stateCode, start, end));
    }

    // 3. B2B vs B2C SEGREGATION REPORT
    @PreAuthorize("hasAnyRole('OWNER', 'FINANCE_MGR')")
    @GetMapping("/b2b-b2c")
    public ResponseEntity<List<B2bB2cSummaryDto>> getB2bB2cSummary(@RequestParam String stateCode, @RequestParam int year, @RequestParam int month) {
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.atEndOfMonth().atTime(23, 59, 59);
        return ResponseEntity.ok(salesInvoiceRepo.getB2bB2cSummary(stateCode, start, end));
    }

    // 4. DOWNLOAD PDF INVOICE
    @PreAuthorize("hasAnyRole('BILLING_STAFF', 'FINANCE_MGR',  'OWNER')")
    @GetMapping("/download-invoice/{id}")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Integer id) throws Exception {

        // Fetch the  invoice and its line items from the database
        SalesInvoice invoice = salesInvoiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found!"));
        List<SalesItem> items = salesItemRepo.findBySalesInvoiceId(id);

        //  Pass the data to the HTML Template
        Map<String, Object> data = new HashMap<>();
        data.put("invoice", invoice); // Contains Customer Name, Total Tax, State Code
        data.put("items", items);     // Contains the List of Serial Numbers and Prices!

        // Generate the PDF Byte Array
        byte[] pdfBytes = pdfService.generatePdf("invoice", data);

        // Tell Postman/Browser to download this as a PDF file
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Sale_Invoice_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    // DOWNLOAD PURCHASE INVOICE / GOODS RECEIPT
    @PreAuthorize("hasAnyRole('STORE_MGR', 'FINANCE_MGR', 'OWNER')")
    @GetMapping("/download-purchase/{id}")
    public ResponseEntity<byte[]> downloadPurchaseInvoicePdf(@PathVariable Integer id) throws Exception {

        // Fetch Purchase invoice and  items
        PurchaseInvoice invoice = purchaseInvoiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Invoice not found!"));
        List<PurchaseItem> items = purchaseItemRepo.findByPurchaseInvoiceId(id);

        // Send  the data to thymeleaf html invoice template
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("invoice", invoice);
        data.put("items", items);

        // Generate PDF using the  template name: "purchase_invoice"
        byte[] pdfData = pdfService.generatePdf("purchase_invoice", data);

        // Tell Postman/Browser to download this as a PDF file
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Purchase_Record_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
    }
}