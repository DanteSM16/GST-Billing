package com.inventory.gst_billing.service;

import com.inventory.gst_billing.dto.AuthRequest;
import com.inventory.gst_billing.entity.Store;
import com.inventory.gst_billing.repository.StoreRepository;
import com.inventory.gst_billing.util.JwtUtil;
import com.inventory.gst_billing.dto.RegisterRequest;
import com.inventory.gst_billing.entity.Role;
import com.inventory.gst_billing.entity.User;
import com.inventory.gst_billing.repository.RoleRepository;
import com.inventory.gst_billing.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service // Tells Spring this is our brain/logic layer, and to create it as bean and inject into controller
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;   // leter added for dependency injection for login endpoint
    // add here, add in consutructor parameter and add in ddeclaration.

    private final StoreRepository storeRepository; // ALSO LATER ADDED FOR MULTI STORE STATE LOGIC.

    // Constructer based Dependency Injection. No need for autowired as single constructor
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, StoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.storeRepository = storeRepository;
    }

    public String registerUser(RegisterRequest request) {
        // 1. Check if user exists, validation. If exists exception throw so no db change.
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        // 2. Fetch the role from DB. If it doesn't exist, create it Just to make our testing easier right now
        Role role = roleRepository.findByRoleName(request.getRoleName()) // This gives us Optional <Role> object
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName(request.getRoleName());
                    return roleRepository.save(newRole);
                }); //orElseGet is Optional method it executes if reutrns empty , it will execute the func inside

        // 3. Create user and encrypt password
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Bcrypt encryption
        user.setRole(role);
        if (request.getStoreId() != null) { // ALSO ADDED LATER AFTER STORE LOGIC ADDED.
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Store not found!"));
            user.setStore(store);
        }

        userRepository.save(user); // Saves to Postgres DB
        return "User registered successfully!";
    }




    public String login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password!");
        }

        //  Explicitly throw the error here so the GlobalExceptionHandler catches it
        if (!user.getIsActive()) {
            throw new RuntimeException("Your account has been deactivated. Contact the Administrator.");
        }

        return jwtUtil.generateToken(user.getUsername(), user.getRole().getRoleName());
    }
}