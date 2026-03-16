package com.inventory.gst_billing.service;

import com.inventory.gst_billing.dto.UserResponse;
import com.inventory.gst_billing.entity.User;
import com.inventory.gst_billing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // Fetch all employees for the Admin Table, except owner
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream()
                // do not send owner in the list of users so cannot remove owner.
                .filter(user -> !user.getRole().getRoleName().equals("OWNER"))
                .map(user -> {
                    UserResponse dto = new UserResponse();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setRoleName(user.getRole().getRoleName());
                    dto.setIsActive(user.getIsActive());
                    dto.setStoreName(user.getStore() != null ? user.getStore().getStoreName() : "System Wide");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Toggle Active Status (Fire / Rehire)
    @Transactional
    public String toggleUserStatus(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Flip the boolean to make it active/inactive
        user.setIsActive(!user.getIsActive());
        userRepo.save(user);

        return user.getIsActive() ? "User Activated" : "User Deactivated (Fired)";
    }
}