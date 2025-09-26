package com.userservice.userservice.controller;

import com.userservice.userservice.dto.OtpVerificationRequestDTO;
import com.userservice.userservice.dto.EmailRequestDTO;
import com.userservice.userservice.dto.ForgotPasswordRequestDTO;
import com.userservice.userservice.dto.ResetPasswordRequestDTO;
import com.userservice.userservice.dto.UploadPhotoRequestDTO;
import com.userservice.userservice.dto.UpdateUserRequestDTO;
import com.userservice.userservice.dto.UserProfileUpdateResponseDTO;
import com.userservice.userservice.entity.UserEntity;
import com.userservice.userservice.enums.UserRole;
import com.userservice.userservice.service.UserService;
import com.userservice.userservice.service.EmailAuthService;
import com.userservice.userservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailAuthService emailAuthService;

    // ✅ Update profile
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateUserRequestDTO dto,
                                           HttpServletRequest request) {
        try {
            String token = extractToken(request);
            UUID userId = jwtUtil.extractUserId(token);

            UserEntity updatedUser = userService.updateUser(userId, dto);

            UserProfileUpdateResponseDTO response = UserProfileUpdateResponseDTO.builder()
                    .userId(updatedUser.getId().toString())
                    .fullName(updatedUser.getFullName())
                    .phone(updatedUser.getPhone())
                    .alternatePhone(updatedUser.getAlternatePhone())
                    .address(updatedUser.getAddress())
                    .photo(updatedUser.getPhoto())
                    .biography(updatedUser.getBiography())
                    .language(updatedUser.getLanguage())
                    .linkedinUrl(updatedUser.getLinkedin())
                    .instagramUrl(updatedUser.getInstagram())
                    .facebookUrl(updatedUser.getFacebook())
                    .internshalaUrl(updatedUser.getInternshala())
                    .countryCode(updatedUser.getCountryCode())
                    .isInstructor(updatedUser.getRole() == UserRole.INSTRUCTOR)
                    .message("Profile updated successfully")
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ✅ Delete own account (with OTP verification)
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteSelf(@RequestBody OtpVerificationRequestDTO dto,
                                        HttpServletRequest request) {
        try {
            String token = extractToken(request);
            UUID userId = jwtUtil.extractUserId(token);
            userService.deleteOwnAccount(userId, dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Account deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ✅ Admin delete any user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteByAdmin(@PathVariable UUID id,
                                           HttpServletRequest request) {
        try {
            String token = extractToken(request);
            String role = jwtUtil.extractClaim(token, "role", String.class);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            userService.adminDeleteUser(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "User deleted by admin"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ✅ Forgot password → send OTP (reuse email-auth)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
        try {
            // Check if user exists
            if (!userService.existsByEmail(dto.getEmail())) {
                return ResponseEntity.accepted().body(Map.of(
                        "message", "If this email is registered, you will receive password reset instructions",
                        "email", dto.getEmail()
                ));
            }
            
            // Send OTP for password reset using dedicated email auth service
            EmailRequestDTO emailReq = new EmailRequestDTO();
            emailReq.setEmail(dto.getEmail());
            Map<String, Object> response = emailAuthService.sendPasswordResetOtp(emailReq);
            return ResponseEntity.accepted().body(response);
        } catch (Exception e) {
            return ResponseEntity.accepted().body(Map.of(
                    "message", "If this email is registered, you will receive password reset instructions",
                    "email", dto.getEmail()
            ));
        }
    }

    // ✅ Reset password using OTP
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO dto) {
        try {
            userService.resetPassword(dto.getEmail(), dto.getOtpSessionId(), dto.getOtp(), dto.getNewPassword());
            return ResponseEntity.ok(Map.of("success", true, "message", "Password reset successful"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ✅ Upload/change profile picture
    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadPhoto(@RequestBody UploadPhotoRequestDTO dto,
                                         HttpServletRequest request) {
        try {
            String token = extractToken(request);
            UUID userId = jwtUtil.extractUserId(token);
            UserEntity user = userService.setPhoto(userId, dto.getPhoto());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "photo", user.getPhoto()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ✅ Get user profile by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(userService.getPublicById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return header.substring(7);
    }
}
