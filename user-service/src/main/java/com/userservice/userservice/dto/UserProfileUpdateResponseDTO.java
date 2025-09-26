package com.userservice.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileUpdateResponseDTO {

    private String userId;
    private String fullName;
    private String phone;
    private String alternatePhone;
    private String address;

    private String photo; // Photo URL
    private String biography;
    private String language;

    private String linkedinUrl;
    private String instagramUrl;
    private String facebookUrl;
    private String internshalaUrl;

    private String countryCode;

    private Boolean isInstructor;

    private String message; // e.g., "Profile updated successfully"
}
