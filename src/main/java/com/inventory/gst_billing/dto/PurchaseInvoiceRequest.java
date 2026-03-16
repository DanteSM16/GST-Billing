package com.inventory.gst_billing.dto;

import lombok.Data;
import java.util.List;

// this one is basically
@Data
public class PurchaseInvoiceRequest {
    private String supplierName;
    private String supplierGstin;
    private String billingAddress;
    private String contactNumber;
    private String stateCode; // "MH", "KA",etc.
    private Integer storeId;

    // The list of items being purchased
    // in this single transaction
    // nesting it here makes it so we need 1 api call only even for bulk purchase of diff items.
    private List<PurchaseItemRequest> items;
}