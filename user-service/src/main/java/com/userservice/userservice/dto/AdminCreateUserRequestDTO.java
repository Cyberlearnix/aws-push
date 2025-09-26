package com.userservice.userservice.dto;

import com.userservice.userservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateUserRequestDTO {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private UserRole role; // optional, default STUDENT
}


