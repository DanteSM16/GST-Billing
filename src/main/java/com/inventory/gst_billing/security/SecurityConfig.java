package com.inventory.gst_billing.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// IMPORTING SECURITY CLASSES
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableMethodSecurity //added later, annotation used to activate method-level authorization,
                      // allowing security checks directly on service or controller methods using @PreAuthorize

public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; //to inject the new filter

    // Inject our new filter
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // The standard hashing algorithm
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults()) //added later to allow to interact with react frontend
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .anyRequest().authenticated() // IMPLIED NEED TO BE LOGGED IN FOR ALL OTHER ENDPOINTS.
                )
                //  Tell Spring NOT to use RAM to remember users (Stateless REST API)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Put our custom JWT filter BEFORE the standard Spring Security filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}