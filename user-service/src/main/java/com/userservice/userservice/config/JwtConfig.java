package com.userservice.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Ensure the secret is not null or empty
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET is not configured. Please set the JWT_SECRET environment variable.");
        }
        
        // Log the first few characters of the secret for debugging (don't log the whole secret)
        String secretPreview = jwtSecret.length() > 5 
            ? jwtSecret.substring(0, 5) + "..." 
            : "[too short]";
        System.out.println("🔑 JWT Secret configured (first 5 chars): " + secretPreview);
        
        // Create the key spec with the correct algorithm
        SecretKeySpec key = new SecretKeySpec(
            jwtSecret.getBytes(StandardCharsets.UTF_8), 
            "HmacSHA512");
            
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
