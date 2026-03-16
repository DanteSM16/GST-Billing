package com.inventory.gst_billing.dto;

import lombok.Data;
import java.math.BigDecimal;
// For updating specifically just the GST RATE USING A PATCH REQUEST THAN PUT, NO NEED TO ADD OTHER INFO LIKE PUT.
@Data
public class GstUpdateRequest {
    private BigDecimal gstRate;
}