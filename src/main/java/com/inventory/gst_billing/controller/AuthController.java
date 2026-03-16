package com.inventory.gst_billing.controller;


import com.inventory.gst_billing.dto.AuthRequest;
import com.inventory.gst_billing.dto.RegisterRequest;
import com.inventory.gst_billing.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Tells Spring this class handles HTTP requests, accept JSON, CALL SERVICE, SEND BACK STATUS N JSON
@RequestMapping("/api/auth") // Base URL for this controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 1st Endpoint: POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String response = authService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    // 2nd ENDPOINT: POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(token);
    }
}