package com.inventory.gst_billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class B2bB2cSummaryDto {
    private String saleType; // "B2B" or "B2C"
    private BigDecimal totalTaxable;
    private BigDecimal totalTax;
}