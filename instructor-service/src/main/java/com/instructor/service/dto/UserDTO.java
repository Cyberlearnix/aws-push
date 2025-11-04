package com.instructor.service.dto;

import com.instructor.service.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String email;
    private String fullName;
    private UserRole role;
    private Boolean isActive;
    private String password; // Note: In a real application, you might want to avoid sending passwords around
}
