package com.inventory.gst_billing.security;

import com.inventory.gst_billing.entity.Role;
import com.inventory.gst_billing.entity.User;
import com.inventory.gst_billing.repository.RoleRepository;
import com.inventory.gst_billing.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // If the database has NO users, it's a brand new deployment!
        if (userRepository.count() == 0) {

            // 1. Create the Master Role
            Role ownerRole = new Role();
            ownerRole.setRoleName("OWNER");
            ownerRole = roleRepository.save(ownerRole);

            // 2. Create the Master Admin User
            User admin = new User();
            admin.setUsername("superadmin");
            // In a real app, you would pass this password via Environment Variables too!
            // But for a portfolio, hardcoding a default is fine. YOU MUST CHANGE THIS AFTER DEPLOYMENT!
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(ownerRole);
            admin.setIsActive(true);

            userRepository.save(admin);
            System.out.println("✅ SYSTEM BOOTSTRAPPED: Created default 'superadmin' account.");
        }
    }
}