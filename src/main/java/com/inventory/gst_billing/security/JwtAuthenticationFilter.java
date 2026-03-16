package com.inventory.gst_billing.security;

import com.inventory.gst_billing.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Get the Authorization header from the incoming request. Industory standard method to send authorized request.
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // 2. Check if the header exists and starts with "Bearer ", remove it to get the just the token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Remove "Bearer " to get just the token
            username = jwtUtil.extractUsername(jwt);
        }

        // 3. If we found a username, and the user is not already logged in to this request from prev filter/session.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 4. Mathematically validate the token using the util method, check if username match and expiry.
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // 5. Create an Authentication token for Spring Security, Spring doesnt know JWT it only knows authentication
                // UsernamePasswordAuthenticationToken IS ONE IMPLEMENATION OF AUTHENTICATION, PASS IT USER DETAIL.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Passing extra metadata like IP address, session ID if exists.
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Put it in the Security Context, now it wont be null, the user will be consdiered logged in for this requst
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 7. Pass the request to the next filter in the chain (or to the Controller)
        // Mandatory to do this, or request stops here and never reached the Controller.
        filterChain.doFilter(request, response);
    }
}