package com.inventory.gst_billing.security;


import com.inventory.gst_billing.entity.User;
import com.inventory.gst_billing.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        //  DELETE SECURITY LOCK
        if (!user.getIsActive()) {
            // Throwing this specific exception tells Spring to deny the login attempt
            throw new org.springframework.security.authentication.DisabledException("Your account has been deactivated.");
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName());

        // Send the data using default user objefct of spring
        // The standard constructor is (username, password, authorities).
        // The advanced constructor is (username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities)
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getIsActive(), // Enabled
                true,               // Account non-expired
                true,               // Credentials non-expired
                true,               // Account non-locked
                java.util.Collections.singletonList(authority)
        );
    }
}