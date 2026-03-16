package com.inventory.gst_billing.dto;

import lombok.Data;
import java.math.BigDecimal;

// for the data received from frontend/postman
@Data
public class ProductTemplateRequest {
    private String hsnCode;
    private String name;
    private BigDecimal basePrice;
    private BigDecimal gstRate;
    private String imageUrl;
}