package com.userservice.userservice.util;

import com.userservice.userservice.entity.UserEntity;
import com.userservice.userservice.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    // ✅ Initialize the signing key after loading the secret
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ✅ Expiration durations
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 1000 * 60 * 60;         // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 1000 * 60 * 60 * 24 * 7; // 7 days
    private static final long TEMP_TOKEN_EXPIRATION_MS = 1000 * 60 * 15;           // 15 minutes

    // ✅ Generate Access Token
    public String generateAccessToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public UserRole extractUserRole(String token) {
        return UserRole.valueOf(extractAllClaims(token).get("role", String.class));
    }

    // ✅ Generate Refresh Token
    public String generateRefreshToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Generate Temp Token
    public String generateTempToken(String email) {
        return Jwts.builder()
                .setSubject("temp")
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEMP_TOKEN_EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Extract Subject (userId or "temp")
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ✅ Extract Email
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    // ✅ Extract Role
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ✅ Extract UUID
    public UUID extractUserId(String token) {
        return UUID.fromString(extractSubject(token));
    }

    // ✅ Validate Temp Token
    public boolean validateTempToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ✅ Generic extractor
    public <T> T extractClaim(String token, String claimName, Class<T> clazz) {
        return extractAllClaims(token).get(claimName, clazz);
    }

    // ✅ Core claim parser
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
