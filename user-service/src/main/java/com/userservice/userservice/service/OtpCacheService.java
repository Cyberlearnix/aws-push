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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpCacheService {

    private final StringRedisTemplate redisTemplate;
    private final Map<String, OtpSession> inMemoryCache = new ConcurrentHashMap<>();

    private String keyFor(UUID sessionId) {
        return "otp:session:" + sessionId;
    }

    public UUID createOtpSession(String email, String otp, int ttlSeconds) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] Creating OTP session for email: {} with TTL: {}s", requestId, maskEmail(email), ttlSeconds);

        UUID sessionId = UUID.randomUUID();
        String key = keyFor(sessionId);
        log.debug("[{}] Generated session ID: {}, Redis key: {}", requestId, sessionId, key);

        OtpSession session = new OtpSession(email, otp, ttlSeconds);
        inMemoryCache.put(key, session);

        // Also try Redis if available
        try {
            HashOperations<String, String, String> ops = redisTemplate.opsForHash();
            ops.put(key, "email", email);
            ops.put(key, "otp", otp);
            ops.put(key, "verified", "false");
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
            log.debug("[{}] OTP data stored in Redis", requestId);
        } catch (Exception e) {
            log.warn("[{}] Redis not available, using in-memory cache only: {}", requestId, e.getMessage());
        }

        log.info("[{}] OTP session created successfully - SessionID: {}, Expires in: {}s",
            requestId, sessionId, ttlSeconds);
        return sessionId;
    }

    public boolean verifyOtp(UUID sessionId, String email, String otp) throws OtpVerificationException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] Starting OTP verification - SessionID: {}, Email: {}, OTP: ***{}",
            requestId, sessionId, maskEmail(email), otp.substring(Math.max(0, otp.length() - 2)));

        String key = keyFor(sessionId);

        // Try Redis first, fallback to in-memory cache
        OtpSession session = null;
        try {
            HashOperations<String, String, String> ops = redisTemplate.opsForHash();
            Map<String, String> redisData = ops.entries(key);

            if (redisData != null && !redisData.isEmpty()) {
                String storedEmail = redisData.get("email");
                String storedOtp = redisData.get("otp");

                if (storedEmail != null && storedOtp != null) {
                    session = new OtpSession(storedEmail, storedOtp, 300);
                }
            }
        } catch (Exception e) {
            log.debug("[{}] Redis not available, checking in-memory cache: {}", requestId, e.getMessage());
        }

        // Fallback to in-memory cache if Redis failed
        if (session == null) {
            session = inMemoryCache.get(key);
        }

        if (session == null || session.isExpired()) {
            log.warn("[{}] OTP verification failed: Session not found or expired - SessionID: {}",
                requestId, sessionId);
            throw new OtpVerificationException(
                OtpVerificationException.OtpErrorType.EXPIRED_OTP,
                "OTP session expired or invalid. Please request a new OTP."
            );
        }

        // Validate email and OTP
        if (!session.getEmail().equalsIgnoreCase(email)) {
            log.warn("[{}] Email mismatch for OTP verification - Expected: {}, Got: {}",
                requestId, maskEmail(session.getEmail()), maskEmail(email));
            throw new OtpVerificationException(
                OtpVerificationException.OtpErrorType.EMAIL_MISMATCH,
                "Email does not match the one the OTP was sent to."
            );
        }

        if (!session.getOtp().equals(otp)) {
            log.warn("[{}] Invalid OTP provided - Expected: ***{}, Got: ***{} for email: {}",
                requestId,
                session.getOtp().substring(Math.max(0, session.getOtp().length() - 2)),
                otp.substring(Math.max(0, otp.length() - 2)),
                maskEmail(email));
            throw new OtpVerificationException(
                OtpVerificationException.OtpErrorType.INVALID_OTP,
                "Invalid OTP. Please check and try again."
            );
        }

        // If we get here, OTP is valid. Clean up both caches
        log.info("[{}] OTP validation successful, cleaning up session", requestId);
        inMemoryCache.remove(key);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.debug("[{}] Failed to delete Redis key: {}", requestId, e.getMessage());
        }

        log.info("[{}] OTP verified and consumed successfully for email: {}", requestId, maskEmail(email));
        return true;
    }

    private String maskEmail(String email) {
        if (email == null || email.length() <= 3) return email;
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return email;
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    // Simple in-memory session data structure
    private static class OtpSession {
        private final String email;
        private final String otp;
        private final long expiryTime;

        public OtpSession(String email, String otp, int ttlSeconds) {
            this.email = email;
            this.otp = otp;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000L);
        }

        public String getEmail() { return email; }
        public String getOtp() { return otp; }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}
