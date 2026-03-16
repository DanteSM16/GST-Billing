package com.inventory.gst_billing.controller;
import com.inventory.gst_billing.dto.SalesInvoiceRequest;
import com.inventory.gst_billing.dto.SalesInvoiceResponse;
import com.inventory.gst_billing.service.SalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    // Only Billing Staff handles retail sales POS
    @PreAuthorize("hasRole('BILLING_STAFF')")
    @PostMapping
    public ResponseEntity<SalesInvoiceResponse> createSale(@RequestBody SalesInvoiceRequest request) {
        return ResponseEntity.ok(salesService.createSalesInvoice(request));
    }
}