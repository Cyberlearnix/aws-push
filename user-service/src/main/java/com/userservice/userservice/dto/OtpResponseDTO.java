package com.userservice.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpResponseDTO {
    private boolean success;
    private String message;
    private String otpSessionId;
    private int expirySeconds;
    private String deliveryMethod;
    private String maskedContact;
    private int resendCooldownSeconds;
}
