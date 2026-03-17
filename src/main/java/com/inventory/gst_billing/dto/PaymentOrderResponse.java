package com.inventory.gst_billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentOrderResponse {
    private String orderId;       // The ID Razorpay generates
    private Integer amount;       // Amount in paise (Razorpay standard)
    private String currency;      // "INR"
    private String keyId;         // We send the public key to React so it can open the modal
}