package com.inventory.gst_billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class StockResponse {
    private String serialNo;
    private Integer productId;
}