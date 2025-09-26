package com.userservice.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    private String fullName;

    // 🔢 Accept country code separately (e.g. +91, +1)
    private String countryCode;

    // 📱 Local phone number (e.g. 9876543210)
    private String phone;

    private String password;
}
