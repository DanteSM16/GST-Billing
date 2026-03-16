package com.inventory.gst_billing.controller;

import com.inventory.gst_billing.dto.PurchaseInvoiceRequest;
import com.inventory.gst_billing.dto.PurchaseInvoiceResponse;
import com.inventory.gst_billing.service.PurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    // Only the Store Manager can log supplieds recieved
    @PreAuthorize("hasRole('STORE_MGR')")
    @PostMapping
    public ResponseEntity<PurchaseInvoiceResponse> createPurchase(@RequestBody PurchaseInvoiceRequest request) {
        return ResponseEntity.ok(purchaseService.createPurchaseInvoice(request));
    }
}