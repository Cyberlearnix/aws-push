package com.userservice.userservice.controller;

import com.userservice.userservice.dto.EmailRequestDTO;
import com.userservice.userservice.dto.OtpVerificationRequestDTO;
import com.userservice.userservice.dto.RegisterRequestDTO;
import com.userservice.userservice.service.EmailAuthService;
import com.userservice.userservice.util.EmailValidatorUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email-auth")
@RequiredArgsConstructor
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    //  Send OTP with email format & domain validation
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody @Valid EmailRequestDTO dto) {
        try {
            String email = dto.getEmail();

            //  Block invalid or disposable emails
            if (!EmailValidatorUtil.isEmailValid(email)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid or disposable email address"
                ));
            }

            Map<String, Object> response = emailAuthService.sendOtpToEmail(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to send OTP. Please try again.",
                    "error", e.getMessage()
            ));
        }
    }

    //  Verify OTP

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequestDTO dto) {
        try {
            Map<String, Object> response = emailAuthService.verifyOtp(dto);
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Handle specific HTTP status exceptions (like 403 FORBIDDEN for deactivated accounts)
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "success", false,
                    "message", e.getReason(),
                    "error", "ACCOUNT_STATUS_ERROR"
            ));
        } catch (Exception e) {
            // Handle other unexpected errors
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "OTP verification failed",
                    "error", e.getMessage()
            ));
        }
    }
    //  Register new user (after verifying OTP)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto, HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid Authorization header");
            }

            String tempToken = authHeader.substring(7);
            Map<String, Object> response = emailAuthService.registerUser(dto, tempToken);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "Registration failed",
                    "error", e.getMessage()
            ));
        }
    }
}
