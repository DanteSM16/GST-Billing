package com.inventory.gst_billing.dto;

import lombok.Data;
import java.util.List;

@Data
public class SalesInvoiceRequest {
    private String customerName;
    private String customerContact;
    private String billingAddress;
    private String stateCode; // Determines IGST vs CGST/SGST
    private String customerGstin; //send if b2b mandatory for b2b b2c segregation,

    private List<SalesItemRequest> items; // Scanned items
}