package com.inventory.gst_billing.dto;

import lombok.Data;
import java.math.BigDecimal;
// For updating specifically just the PRICE USING A PATCH REQUEST THAN PUT, NO NEED TO ADD OTHER INFO LIKE PUT.
@Data
public class PriceUpdateRequest {
    private BigDecimal basePrice;
}