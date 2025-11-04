package com.userservice.userservice.dto;

import lombok.Data;

@Data
public class UpdateUserRequestDTO {
    private String fullName;
    private String phone;
    private String alternatePhone;
    private String address;
    private String password;

    private String photo; // Photo URL
    private String biography;
    private String language;

    private String linkedinUrl;
    private String instagramUrl;
    private String facebookUrl;
    private String internshalaUrl;

    private String countryCode;

    // ✅ New field: Optional flag for instructor request
    private Boolean becomeInstructor;

    // --- Instructor-specific fields (only used if becomeInstructor is true) ---
    private String department;
    private String designation;
    private String qualification;
    private String bio;
    private String specialization;
    private Integer experienceYears;
}
