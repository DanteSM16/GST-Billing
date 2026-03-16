package com.inventory.gst_billing.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data


// This one is baically equivaletn to each line an invoice
public class PurchaseItemRequest {
    private Integer productId;
    private Integer quantity; // E.g., 5
    private BigDecimal purchasePrice; // Negotiated price per unit
    private List<String> serialNumbers; // Must contain exactly the same amount of entries as quanity
}