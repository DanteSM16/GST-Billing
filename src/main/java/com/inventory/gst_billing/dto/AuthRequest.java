package com.inventory.gst_billing.dto;

import lombok.Data;

@Data // Lombok ANNOTATION TO Add Getters, Setters, toString
public class AuthRequest {
    private String username;
    private String password;
}