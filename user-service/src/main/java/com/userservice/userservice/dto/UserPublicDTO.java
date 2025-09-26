package com.userservice.userservice.dto;

import com.userservice.userservice.enums.UserRole;
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
    private UserRole role;

    private String CountryCode;

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
}
