package com.inventory.gst_billing.dto;

import lombok.Data;
import java.math.BigDecimal;

// for the response we want to send, the message is fail or sucess, we want to give invoice id and breakup n net apyable
@Data
public class PurchaseInvoiceResponse {
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