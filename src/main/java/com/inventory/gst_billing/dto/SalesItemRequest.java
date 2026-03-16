package com.inventory.gst_billing.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesItemRequest {
    private String serialNo; // The specific physical item being sold
    private BigDecimal sellingPrice; // Final price after any POS discounts
}