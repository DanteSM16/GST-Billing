package com.inventory.gst_billing.controller;

import com.inventory.gst_billing.entity.GstLedger;
import com.inventory.gst_billing .entity.PurchaseInvoice;
import com.inventory.gst_billing .entity.SalesInvoice;
import com.inventory.gst_billing.repository.GstLedgerRepository;
import com.inventory.gst_billing .repository.PurchaseInvoiceRepository;
import com.inventory.gst_billing .repository.SalesInvoiceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@PreAuthorize("hasAnyRole('OWNER', 'FINANCE_MGR')") //
public class OrderHistoryController {

    private final SalesInvoiceRepository salesRepo;
    private final PurchaseInvoiceRepository purchaseRepo;
    private final GstLedgerRepository ledgerRepo;

    public OrderHistoryController(SalesInvoiceRepository salesRepo, PurchaseInvoiceRepository purchaseRepo, GstLedgerRepository ledgerRepo) {
        this.salesRepo = salesRepo;
        this.purchaseRepo = purchaseRepo;
        this.ledgerRepo = ledgerRepo;
    }

    @GetMapping("/sales")
    public ResponseEntity<List<SalesInvoice>> getAllSales() {
        return ResponseEntity.ok(salesRepo.findAll());
    }

    @GetMapping("/purchases")
    public ResponseEntity<List<PurchaseInvoice>> getAllPurchases() {
        return ResponseEntity.ok(purchaseRepo.findAll());
    }
    @GetMapping("/settlements")
    public ResponseEntity<List<com.inventory.gst_billing.entity.GstLedger>> getSettlementHistory() {
        // Fetch only the double-entry accounting rows!
        return ResponseEntity.ok(ledgerRepo.findAll().stream()
                .filter(l -> l.getTransactionType().equals("SETTLEMENT") || l.getTransactionType().equals("OPENING_BALANCE"))
                .collect(java.util.stream.Collectors.toList()));

    }
}