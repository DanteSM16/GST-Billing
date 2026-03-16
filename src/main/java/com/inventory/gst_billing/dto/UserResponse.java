package com.inventory.gst_billing.dto;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String roleName;
    private String storeName; // The name of the branch they work at
    private Boolean isActive;
}