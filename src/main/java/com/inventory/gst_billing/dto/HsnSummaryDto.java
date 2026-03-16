package com.inventory.gst_billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class HsnSummaryDto {
    private String hsnCode;
    private BigDecimal totalTaxableValue;
    private BigDecimal totalTaxAmount;
}