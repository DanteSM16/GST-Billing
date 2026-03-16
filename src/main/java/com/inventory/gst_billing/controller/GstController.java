package com.inventory.gst_billing.controller;

import com.inventory.gst_billing.dto.GstCalculationResponse;
import com.inventory.gst_billing.service.GstCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gst")
public class GstController {

    private final GstCalculationService gstService;

    public GstController(GstCalculationService gstService) {
        this.gstService = gstService;
    }

    // Only the Finance Manager can view the final Tax offset dashboard and owner
    @PreAuthorize("hasAnyRole('OWNER', 'FINANCE_MGR')")
    @GetMapping("/calculate")
    public ResponseEntity<GstCalculationResponse> calculateGst(@RequestParam String stateCode) {
        return ResponseEntity.ok(gstService.calculateGstOffset(stateCode));
    }
}