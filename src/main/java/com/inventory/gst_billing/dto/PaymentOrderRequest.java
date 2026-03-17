package com.inventory.gst_billing.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentOrderRequest {
    private BigDecimal amount; // The cart total
}