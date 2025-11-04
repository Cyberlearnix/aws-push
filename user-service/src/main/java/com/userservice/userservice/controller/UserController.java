package com.userservice.userservice.controller;

import com.userservice.userservice.dto.OtpVerificationRequestDTO;
import com.userservice.userservice.dto.EmailRequestDTO;
import com.userservice.userservice.dto.ForgotPasswordRequestDTO;
import com.userservice.userservice.dto.ResetPasswordRequestDTO;
import com.userservice.userservice.dto.UploadPhotoRequestDTO;
import com.userservice.userservice.dto.UpdateUserRequestDTO;
import com.userservice.userservice.dto.UserProfileUpdateResponseDTO;
import com.userservice.userservice.entity.UserEntity;
import com.cyberlearnix.shared.enums.UserRole;
import com.userservice.userservice.service.UserService;
import com.userservice.userservice.service.EmailAuthService;
import com.userservice.userservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing user profiles, authentication, and account operations")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailAuthService emailAuthService;

    // ✅ Update profile
    @PutMapping("/update")
    @Operation(
        summary = "Update user profile",
        description = "Updates the authenticated user's profile information including personal details, social media links, and biography"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @SecurityRequirement(name = "Bearer Authentication")
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
    @Operation(
        summary = "Delete own account",
        description = "Deletes the authenticated user's account after OTP verification for security"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid OTP or request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @SecurityRequirement(name = "Bearer Authentication")
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
    @Operation(
        summary = "Delete user by admin",
        description = "Allows admin users to delete any user account (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> deleteByAdmin(@Parameter(description = "User ID to delete") @PathVariable UUID id,
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
    @Operation(
        summary = "Request password reset",
        description = "Sends an OTP to the user's email for password reset. Returns success message regardless of whether email exists for security"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "OTP sent successfully (or message indicating email not found)"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
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
    @Operation(
        summary = "Reset password with OTP",
        description = "Resets user password using the OTP sent to their email"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successful"),
        @ApiResponse(responseCode = "400", description = "Invalid OTP or request data")
    })
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
    @Operation(
        summary = "Upload profile photo",
        description = "Uploads or updates the user's profile picture"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photo uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid photo data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @SecurityRequirement(name = "Bearer Authentication")
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
    @Operation(
        summary = "Get user profile",
        description = "Retrieves public profile information for a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getById(@Parameter(description = "User ID") @PathVariable UUID id) {
        try {
            return ResponseEntity.ok(userService.getPublicById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/email/{email}")
    @Operation(
        summary = "Get user by email",
        description = "Retrieves user information by email address"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getByEmail(@Parameter(description = "User's email address") @PathVariable String email) {
        try {
            return userService.findByEmail(email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
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
