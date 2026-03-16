package com.inventory.gst_billing.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesInvoiceResponse {
    private Integer invoiceId;
    private String message;
    private BigDecimal totalTaxable;
    private BigDecimal cgstTotal;
    private BigDecimal sgstTotal;
    private BigDecimal igstTotal;
    private String storeName;
    private String storeGstin;
    private String storeAddress;
    private String storeStateCode;
}