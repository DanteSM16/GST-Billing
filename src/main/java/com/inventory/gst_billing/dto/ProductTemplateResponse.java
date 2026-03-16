package com.inventory.gst_billing.dto;

import lombok.Data;
import java.math.BigDecimal;

//for sending data back to postman/frontend
@Data
public class ProductTemplateResponse {
    private Integer id; // We return the ID so the frontend knows what it is
    private String hsnCode;
    private String name;
    private BigDecimal basePrice;
    private BigDecimal gstRate;
    private String imageUrl;
    private Boolean isActive;
}