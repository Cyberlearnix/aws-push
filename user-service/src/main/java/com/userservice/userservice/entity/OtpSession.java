package com.userservice.userservice.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated // Deprecated: OTP/session storage moved to Redis (see OtpCacheService)
public class OtpSession {
    // Plain POJO fields (no JPA annotations). This class is left only for backward compatibility in code references.
    private UUID sessionId;

    private String email;
    private String otp;
    private LocalDateTime expiry;
    private boolean verified;
}
