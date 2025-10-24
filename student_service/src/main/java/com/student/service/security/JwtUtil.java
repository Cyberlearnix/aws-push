package com.student.service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:#{null}}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private SecretKey key;

    // For testing purposes
    public String getCurrentSecret() {
        return this.secret;
    }

    // Initialize the signing key after loading the secret
    @PostConstruct
    public void init() {
        try {
            logger.info("\n🔑 STUDENT SERVICE JWT CONFIGURATION");
            logger.info("🔑 Secret length: {}", (secret != null ? secret.length() : "NULL"));
            logger.info("🔑 First 20 chars: {}", (secret != null && secret.length() > 20 ?
                secret.substring(0, 20) + "..." : "NULL"));
            logger.info("🔑 Expiration: {} ms", expiration);

            if (secret == null || secret.trim().isEmpty()) {
                logger.error("JWT secret is null or empty!");
                throw new RuntimeException("JWT secret is null or empty!");
            }

            // Log environment variables for debugging
            String envSecret = System.getenv("JWT_SECRET");
            logger.info("🔑 JWT_SECRET from env: {}",
                envSecret != null ? "[SET]" + (envSecret.length() > 5 ?
                    envSecret.substring(0, 5) + "..." : "[TOO_SHORT]") : "NULL");

            // Ensure consistent UTF-8 encoding
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            logger.debug("Key bytes length: {}", keyBytes.length);

            this.key = Keys.hmacShaKeyFor(keyBytes);
            logger.debug("Signing key algorithm: {}", key.getAlgorithm());

            // Test the key by generating a sample token
            String testToken = Jwts.builder()
                .setSubject("test")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

            logger.info("✅ JWT Key initialized successfully");
            logger.debug("🔑 Test token: {}", testToken);

        } catch (Exception e) {
            logger.error("❌ JWT Initialization Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize JWT", e);
        }
    }

    public Claims parseClaims(String token) {
        logger.debug("Parsing JWT token. Token length: {}", token != null ? token.length() : 0);

        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            logger.debug("Successfully parsed JWT claims for subject: {}", claims.getSubject());
            return claims;

        } catch (ExpiredJwtException e) {
            logger.error("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("Malformed JWT token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed. Expected key: {}",
                key != null ? key.getAlgorithm() : "null");
            logger.error("This usually means the JWT secret doesn't match between services");
            logger.error("Current secret length: {}", secret != null ? secret.length() : 0);
            logger.error("First 10 chars of secret: {}",
                secret != null && secret.length() > 10 ? secret.substring(0, 10) : "N/A");
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String extractUsername(String token) {
        return extractSubject(token);
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

    public boolean validateToken(String token) {
        logger.debug("Validating JWT token");
        try {
            Claims claims = parseClaims(token);
            boolean isValid = claims != null && claims.getExpiration().after(new Date());

            if (isValid) {
                logger.debug("Token is valid for subject: {}", claims.getSubject());
            } else {
                logger.warn("Token validation failed - expired: {}",
                    claims != null ? claims.getExpiration() : "invalid token");
            }

            return isValid;
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}


