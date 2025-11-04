package com.userservice.userservice.controller;

import com.userservice.userservice.dto.PasswordLoginRequest;
import com.userservice.userservice.dto.RefreshTokenRequestDTO;
import com.userservice.userservice.entity.UserEntity;
import com.userservice.userservice.service.AuthService;
import com.userservice.userservice.service.UserService;
import com.userservice.userservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import com.userservice.userservice.dto.LoginResult;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @PostMapping("/login-password")
    public ResponseEntity<?> loginWithPassword(@RequestBody PasswordLoginRequest request) {
        var result = authService.loginWithPassword(request);

        if (result.ok()) {
            return ResponseEntity.ok(result.response());
        }
        if (result.locked()) {
            return ResponseEntity.status(423)
                    .body(Map.of("error", "ACCOUNT_LOCKED", "message", result.errorMessage()));
        }
        return ResponseEntity.status(401)
                .body(Map.of("error", "INVALID_CREDENTIALS", "message", result.errorMessage()));
    }

    // ✅ Refresh access token using a valid refresh token
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDTO req) {
        try {
            String refreshToken = req.getRefreshToken();
            UUID userId = jwtUtil.extractUserId(refreshToken);
            UserEntity user = userService.getUserById(userId);

            // Check if user account is still active
            if (user != null && !user.isActive()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Your account has been deactivated. Please contact administrator for assistance.",
                        "error", "ACCOUNT_DEACTIVATED"
                ));
            }

            String newAccess = jwtUtil.generateAccessToken(user);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "accessToken", newAccess
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Invalid refresh token",
                    "error", e.getMessage()
            ));
        }
    }

    // ✅ Stateless logout (client should discard tokens)
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out"
        ));
    }
    // ✅ Helper Method to extract Bearer token
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid Authorization header");
    }

}
