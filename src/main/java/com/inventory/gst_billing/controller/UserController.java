package com.inventory.gst_billing.controller;

import com.inventory.gst_billing.dto.UserResponse;
import com.inventory.gst_billing.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('OWNER', 'DEVELOPER')") // Strict Security!
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{username}/toggle-status")
    public ResponseEntity<String> toggleUserStatus(@PathVariable String username) {
        return ResponseEntity.ok(userService.toggleUserStatus(username));
    }
}