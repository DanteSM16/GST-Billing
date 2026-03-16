package com.inventory.gst_billing.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String roleName;
    private Integer storeId;
}