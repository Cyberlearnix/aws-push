package com.userservice.userservice.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpUtil {
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOtp() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }
}
