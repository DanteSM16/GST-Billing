package com.inventory.gst_billing.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component // Tells Spring to manage this class
public class JwtUtil {

    // A secure 256-bit secret key used to sign the token
    // hardcoded here, in prod this goes in application.properties and inject with @Value annotation for spring
    //private final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    // ADD THIS LINE:
    @Value("${jwt.secret}")
    private String secretKey;

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Method to generate the token
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // We hide the user's role inside the token!
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Token valid for 10 hours
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    // Generic method to extract any claim, use it for UserNAME AND EXPIRY BELOW. uses a function interface.
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
    // Helper method to extract the username from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); //Claims::getSubject is same as claims -> claims.getSubject() lambda
    }
    // Helper method to extract expiration date to check if token is expired
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    // Validate if the token is valid for this specific user and check expiry
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !extractExpiration(token).before(new Date()));
    }
}