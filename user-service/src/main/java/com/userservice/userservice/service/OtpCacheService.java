package com.userservice.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpCacheService {

    private final StringRedisTemplate redisTemplate;

    private String keyFor(UUID sessionId) {
        return "otp:session:" + sessionId;
    }

    public UUID createOtpSession(String email, String otp, int ttlSeconds) {
        UUID sessionId = UUID.randomUUID();
        String key = keyFor(sessionId);
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        ops.put(key, "email", email);
        ops.put(key, "otp", otp);
        ops.put(key, "verified", "false");
        // Set TTL
        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        return sessionId;
    }

    public boolean verifyOtp(UUID sessionId, String email, String otp) {
        String key = keyFor(sessionId);
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        Map<String, String> all = ops.entries(key);
        if (all == null || all.isEmpty()) {
            return false; // expired or not found
        }
        String storedEmail = all.get("email");
        String storedOtp = all.get("otp");
        if (storedEmail == null || storedOtp == null) return false;
        if (!storedEmail.equalsIgnoreCase(email)) return false;
        if (!storedOtp.equals(otp)) return false;
        // Mark verified and consume OTP by deleting the key
        redisTemplate.delete(key);
        return true;
    }
}
