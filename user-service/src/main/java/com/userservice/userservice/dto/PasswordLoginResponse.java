package com.userservice.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordLoginResponse {
    private boolean userExists;
    private boolean success;
    private String message;
    private String accessToken;
    private UserPublicDTO user;  // use your existing DTO
    private String refreshToken;
}
