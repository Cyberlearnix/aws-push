package com.instructor.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:#{null}}")
    private String secret;

    @Value("${jwt.expiration-ms:3600000}")
    private Long expirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        try {
            System.out.println("\n🔑 INSTRUCTOR SERVICE JWT CONFIGURATION");
            System.out.println("🔑 Secret length: " + (secret != null ? secret.length() : "NULL"));
            System.out.println("🔑 Expiration: " + expirationMs + "ms");

            if (secret == null || secret.trim().isEmpty()) {
                throw new RuntimeException("JWT secret is null or empty!");
            }

            // Ensure consistent UTF-8 encoding and create a proper secret key
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            this.key = Keys.hmacShaKeyFor(keyBytes);

            System.out.println("✅ JWT Key initialized successfully");

        } catch (Exception e) {
            System.err.println("❌ JWT Initialization Error: " + e.getMessage());
            throw new RuntimeException("Failed to initialize JWT", e);
        }
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            System.err.println("❌ JWT Token validation failed: " + e.getMessage());
            System.err.println("❌ Exception type: " + e.getClass().getSimpleName());
            throw new RuntimeException("Failed to parse JWT token", e);
        }
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractSubject(token));
    }
    
    /**
     * Validates a JWT token against the given user details.
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to validate against
     * @return true if the token is valid for the given user, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username != null && 
                   username.equals(userDetails.getUsername()) && 
                   !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Checks if the token is expired.
     *
     * @param token the JWT token to check
     * @return true if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        try {
            final Date expiration = parseClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage(), e);
            return true; // If we can't parse the expiration, consider it expired
        }
    }
}
