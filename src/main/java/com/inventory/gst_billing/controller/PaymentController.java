package com.inventory.gst_billing.controller;

import com.inventory.gst_billing.dto.PaymentOrderRequest;
import com.inventory.gst_billing.dto.PaymentOrderResponse;
import com.inventory.gst_billing.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Only Billing Staff needs to generate payment orders!
    @PreAuthorize("hasRole('BILLING_STAFF')")
    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrderResponse> createOrder(@RequestBody PaymentOrderRequest request) {
        try {
            PaymentOrderResponse response = paymentService.createOrder(request.getAmount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay Order: " + e.getMessage());
        }
    }
}