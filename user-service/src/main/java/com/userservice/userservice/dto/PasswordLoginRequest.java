package com.userservice.userservice.dto;

public record PasswordLoginRequest(
        String email,
        String password
) {}
