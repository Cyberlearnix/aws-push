package com.instructor.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPublicDTO {

    private UUID id;
    private String fullName;
    private String email;
    private String role;

    private String countryCode;

    private String phone;
    private String alternatePhone;
    private String address;
    private String biography;
    private String language;
    private String photo;

    private String linkedin;
    private String instagram;
    private String facebook;
    private String internshala;

    // --- Instructor-specific fields (only populated if user role is INSTRUCTOR) ---
    private String department;
    private String designation;
    private String qualification;
    private String bio;
    private String specialization;
    private Integer experienceYears;
}
