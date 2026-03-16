package com.inventory.gst_billing.controller;

import com.inventory.gst_billing.dto.*;
import com.inventory.gst_billing.entity.Stock;
import com.inventory.gst_billing.enums.StockStatus;
import com.inventory.gst_billing.repository.StockRepository;
import com.inventory.gst_billing.repository.UserRepository;
import com.inventory.gst_billing.service.ProductTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products") //base url
public class ProductTemplateController {

    private final ProductTemplateService productService;
    private final StockRepository stockRepo;
    private final UserRepository userRepo;

    public ProductTemplateController(ProductTemplateService productService, StockRepository stockRepo, UserRepository userRepo) {
        this.productService = productService;
        this.stockRepo = stockRepo;
        this.userRepo = userRepo;
    }

    @PreAuthorize("hasRole('STORE_MGR')")
    @PostMapping
    public ResponseEntity<ProductTemplateResponse> createProduct(@RequestBody ProductTemplateRequest request) {
        ProductTemplateResponse response = productService.createProduct(request);
        return ResponseEntity.ok(response);
    }

    // Anyone who is logged in (authenticated) can view the products
    // owner, storemgr, billing staff def need it, dev needs it to for debugging n finance mangaer for verificatino of correct rates
    @GetMapping
    public ResponseEntity<List<ProductTemplateResponse>> getAllProducts() {
        List<ProductTemplateResponse> responses = productService.getAllProducts();
        return ResponseEntity.ok(responses);
    }

    // GET ONE TEMPLATE  BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductTemplateResponse> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ONLY STORE MANAGER ALLOWED TO UPDATE THE PRODUCTS  (Store Manager Only)
    @PreAuthorize("hasRole('STORE_MGR')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductTemplateResponse> updateProductFull(
            @PathVariable Integer id,
            @RequestBody ProductTemplateRequest request) {
        return ResponseEntity.ok(productService.updateProductFull(id, request));
    }

    // PATCH PRICE (Store Manager Only), NOT USED
    @PreAuthorize("hasRole('STORE_MGR')")
    @PatchMapping("/{id}/price")
    public ResponseEntity<ProductTemplateResponse> updatePrice(
            @PathVariable Integer id,
            @RequestBody PriceUpdateRequest request) {
        return ResponseEntity.ok(productService.updatePrice(id, request));
    }

    // PATCH GST RATE (Store Manager Only), NOT USED
    @PreAuthorize("hasRole('STORE_MGR')")
    @PatchMapping("/{id}/gst")
    public ResponseEntity<ProductTemplateResponse> updateGst(
            @PathVariable Integer id,
            @RequestBody GstUpdateRequest request) {
        return ResponseEntity.ok(productService.updateGst(id, request));
    }


    // TO GET THE ALL THE AVAILABLE STOCK AT A STORE.
    // USE THE LOGGEDD IN USER STORE ID TO QUERY STORE ID, STOCK, AND PRODUCT ID.
    @PreAuthorize("hasAnyRole('STORE_MGR', 'BILLING_STAFF')")
    @GetMapping("/stock/available")
    public ResponseEntity<java.util.List<com.inventory.gst_billing .dto.StockResponse>> getAvailableStock(@RequestParam Integer productId) {

        // 1. SECURITY Feature, find out who made this request. handle exceptions
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        com.inventory.gst_billing .entity.User loggedInUser = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Get their Store ID, if no store id attached exception.
        if (loggedInUser.getStore() == null) {
            throw new RuntimeException("You are not assigned to a store, so you cannot view local stock.");
        }
        Integer userStoreId = loggedInUser.getStore().getId();

        // Get stock matching the Product AND the Status AND the Cashier's Store ID
        java.util.List<com.inventory.gst_billing .entity.Stock> availableStock = stockRepo.findByProductIdAndStatusAndStoreId(
                productId, com.inventory.gst_billing .enums.StockStatus.AVAILABLE, userStoreId
        );

        java.util.List<com.inventory.gst_billing .dto.StockResponse> response = availableStock.stream()
                .map(s -> new com.inventory.gst_billing .dto.StockResponse(s.getSerialNo(), s.getProduct().getId()))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasAnyRole('OWNER', 'DEVELOPER')")
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ProductTemplateResponse> toggleProductStatus(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.toggleProductStatus(id));
    }

    @PreAuthorize("hasAnyRole('STORE_MGR', 'BILLING_STAFF')")
    @GetMapping("/stock/{serialNo}")
    public ResponseEntity<StockResponse> getSingleStock(@PathVariable String serialNo) {

        // Fetch the specific physical item
        Stock stock = stockRepo.findById(serialNo)
                .orElseThrow(() -> new RuntimeException("Serial Number not found!"));

        // Check if it's actually available at teh store, or is not already sold
        if (stock.getStatus() != StockStatus.AVAILABLE) {
            throw new RuntimeException("Item is already sold or unavailable!");
        }

        // Return safely using DTO
        StockResponse response = new StockResponse(
                stock.getSerialNo(),
                stock.getProduct().getId()
        );

        return ResponseEntity.ok(response);
    }

}