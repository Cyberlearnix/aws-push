package com.userservice.userservice.dto;

import com.cyberlearnix.shared.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequestDTO {
    private String fullName;
    private String phone;
    private String address;
    private String photo;
    private UserRole role; // optional
    private Boolean isActive; // optional
}


