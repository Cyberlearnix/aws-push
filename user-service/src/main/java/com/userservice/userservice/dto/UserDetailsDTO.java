package com.userservice.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class UserDetailsDTO {
    private UUID id;
    private String fullName;
    private String email;
    private String CountryCode;
    private String phone;
    private String alternatePhone;
    private String address;
    private String biography;
    private String language;
    private String photoUrl;
    private String linkedin;
    private String instagram;
    private String facebook;
    private String internshala;
    private String role;
    private boolean emailVerified;
}
