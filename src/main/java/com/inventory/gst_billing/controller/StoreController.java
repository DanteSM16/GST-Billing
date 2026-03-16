package com.inventory.gst_billing.controller;

import com.inventory.gst_billing.dto.*;
import com.inventory.gst_billing.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PreAuthorize("hasAnyRole('OWNER', 'DEVELOPER')")
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.createStore(request));
    }

    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @PreAuthorize("hasAnyRole('OWNER', 'DEVELOPER')")
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<StoreResponse> toggleStoreStatus(@PathVariable Integer id) {
        return ResponseEntity.ok(storeService.toggleStoreStatus(id));
    }
}