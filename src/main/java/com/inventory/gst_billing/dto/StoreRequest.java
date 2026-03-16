package com.inventory.gst_billing.dto;

import lombok.Data;

@Data
public class StoreRequest {
    private String storeName;
    private String gstin;
    private String stateCode;
    private String address;
}