package com.userservice.userservice.dto;


import lombok.Data;

@Data
public class OtpVerificationRequestDTO {
    private String email;
    private String otp;
    private String otpSessionId;


}
