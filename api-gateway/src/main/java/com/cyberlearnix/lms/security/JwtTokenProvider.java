package com.cyberlearnix.lms.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:your-256-bit-secret-make-sure-this-is-secure-and-kept-secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-in-ms:86400000}") // 24 hours
    private long jwtExpirationInMs;

    private Key key;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 256 bits (32 characters) long");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            System.out.println("Validating JWT token...");
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
            System.out.println("JWT Token is valid. Claims: " + claims);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT Token validation error: " + e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Try to get roles as a list first
            List<String> roles = (List<String>) claims.get("roles", List.class);
            
            // If roles is null or empty, try to get the role as a single string
            if ((roles == null || roles.isEmpty()) && claims.get("role") != null) {
                String role = claims.get("role", String.class);
                if (role != null && !role.isEmpty()) {
                    roles = Collections.singletonList(role);
                }
            }
            
            System.out.println("Roles from token: " + roles);
            return roles != null ? roles : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error extracting roles from token: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
