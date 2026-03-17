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

    //  PAYMENT DATA
    private String paymentMethod; // "RAZORPAY_ONLINE" or "CASH"
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature; // The cryptographic proof!

    private List<SalesItemRequest> items; // Scanned items
}
