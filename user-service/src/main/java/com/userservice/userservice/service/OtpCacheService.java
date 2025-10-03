package com.userservice.userservice.service;

import com.userservice.userservice.exception.OtpVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpCacheService {

    private final StringRedisTemplate redisTemplate;

    private String keyFor(UUID sessionId) {
        return "otp:session:" + sessionId;
    }

    public UUID createOtpSession(String email, String otp, int ttlSeconds) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] Creating OTP session for email: {} with TTL: {}s", requestId, maskEmail(email), ttlSeconds);
        
        UUID sessionId = UUID.randomUUID();
        String key = keyFor(sessionId);
        log.debug("[{}] Generated session ID: {}, Redis key: {}", requestId, sessionId, key);
        
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        ops.put(key, "email", email);
        ops.put(key, "otp", otp);
        ops.put(key, "verified", "false");
        log.debug("[{}] OTP data stored in Redis", requestId);
        
        // Set TTL
        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        log.info("[{}] OTP session created successfully - SessionID: {}, Expires in: {}s", 
            requestId, sessionId, ttlSeconds);
        return sessionId;
    }

    /**
     * Verifies the OTP and returns true if valid
     * @throws OtpVerificationException with specific error type if verification fails
     */
    public boolean verifyOtp(UUID sessionId, String email, String otp) throws OtpVerificationException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] Starting OTP verification - SessionID: {}, Email: {}, OTP: ***{}", 
            requestId, sessionId, maskEmail(email), otp.substring(Math.max(0, otp.length() - 2)));
        
        String key = keyFor(sessionId);
        log.debug("[{}] Looking up Redis key: {}", requestId, key);
        
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        Map<String, String> all = ops.entries(key);
        
        log.debug("[{}] Retrieved session data from Redis (keys: {})", requestId, 
            all != null ? all.keySet() : "null");
        
        // Check if session exists
        if (all == null || all.isEmpty()) {
            log.warn("[{}] OTP verification failed: Session not found or expired - SessionID: {}", 
                requestId, sessionId);
            throw new OtpVerificationException(
                OtpVerificationException.OtpErrorType.EXPIRED_OTP,
                "OTP session expired or invalid. Please request a new OTP."
            );
        }
        
        String storedEmail = all.get("email");
        String storedOtp = all.get("otp");
        
        log.debug("[{}] Session data validation - Stored email: {}, Stored OTP: ***{}", 
            requestId, maskEmail(storedEmail), 
            storedOtp != null ? storedOtp.substring(Math.max(0, storedOtp.length() - 2)) : "null");
        
        // Validate email and OTP
        if (storedEmail == null || storedOtp == null) {
            log.warn("[{}] Invalid OTP session data - SessionID: {} (storedEmail: {}, storedOtp: {})", 
                requestId, sessionId, storedEmail != null ? "present" : "null", 
                storedOtp != null ? "present" : "null");
            throw new OtpVerificationException(
                OtpVerificationException.OtpErrorType.INVALID_SESSION,
                "Invalid OTP session. Please request a new OTP."
            );
        }
        
        if (!storedEmail.equalsIgnoreCase(email)) {
            log.warn("[{}] Email mismatch for OTP verification - Expected: {}, Got: {}", 
                requestId, maskEmail(storedEmail), maskEmail(email));
            throw new OtpVerificationException(
                OtpVerificationException.OtpErrorType.EMAIL_MISMATCH,
                "Email does not match the one the OTP was sent to."
            );
        }
        
        if (!storedOtp.equals(otp)) {
            log.warn("[{}] Invalid OTP provided - Expected: ***{}, Got: ***{} for email: {}", 
                requestId, 
                storedOtp.substring(Math.max(0, storedOtp.length() - 2)),
                otp.substring(Math.max(0, otp.length() - 2)),
                maskEmail(email));
            throw new OtpVerificationException(
                OtpVerificationException.OtpErrorType.INVALID_OTP,
                "Invalid OTP. Please check and try again."
            );
        }
        
        // If we get here, OTP is valid. Consume it by deleting the key
        log.info("[{}] OTP validation successful, consuming session", requestId);
        redisTemplate.delete(key);
        log.info("[{}] OTP verified and consumed successfully for email: {}", requestId, maskEmail(email));
        return true;
    }
    
    private String maskEmail(String email) {
        if (email == null || email.length() <= 3) return email;
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return email;
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
