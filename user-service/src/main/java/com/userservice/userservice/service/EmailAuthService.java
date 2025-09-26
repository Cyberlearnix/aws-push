package com.userservice.userservice.service;

import com.userservice.userservice.dto.EmailRequestDTO;
import com.userservice.userservice.dto.OtpVerificationRequestDTO;
import com.userservice.userservice.dto.RegisterRequestDTO;
import com.userservice.userservice.entity.UserEntity;
import com.userservice.userservice.util.JwtUtil;
import com.userservice.userservice.util.OtpUtil;
import com.userservice.userservice.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final EmailService emailService;
    private final OtpUtil otpUtil;
    private final JwtUtil jwtUtil;
    private final OtpCacheService otpCacheService;
    private final UserService userService;

    private static final String ADMIN_EMAIL = "cyberlearnixprivatelimited@gmail.com";
    private static final String ADMIN_PASSWORD = "Cyberlearnix$179";

    // ✅ 1. Send OTP to email
    public Map<String, Object> sendOtpToEmail(EmailRequestDTO dto) {
        String email = dto.getEmail();
        String otp = otpUtil.generateOtp();

        emailService.sendOtpEmail(email, otp);

        // Store OTP in Redis with 5-minute TTL
        UUID sessionId = otpCacheService.createOtpSession(email, otp, 300);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent successfully");
        response.put("otpSessionId", sessionId.toString());
        response.put("expirySeconds", 300);
        response.put("deliveryMethod", "EMAIL");
        response.put("maskedContact", maskEmail(email));
        response.put("resendCooldownSeconds", 0);
        return response;
    }

    // ✅ 1b. Send Password Reset OTP to email (uses password reset template)
    public Map<String, Object> sendPasswordResetOtp(EmailRequestDTO dto) {
        String email = dto.getEmail();
        String otp = otpUtil.generateOtp();

        // Send using password reset template
        emailService.sendPasswordResetEmail(email, otp);

        // Store OTP in Redis with 5-minute TTL
        UUID sessionId = otpCacheService.createOtpSession(email, otp, 300);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset OTP sent successfully");
        response.put("otpSessionId", sessionId.toString());
        response.put("expirySeconds", 300);
        response.put("deliveryMethod", "EMAIL");
        response.put("maskedContact", maskEmail(email));
        response.put("resendCooldownSeconds", 0);
        return response;
    }

    // ✅ 2. Verify OTP and auto-login if user exists or admin
    public Map<String, Object> verifyOtp(OtpVerificationRequestDTO dto) {
        UUID sessionId = UUID.fromString(dto.getOtpSessionId());
        boolean valid = otpCacheService.verifyOtp(sessionId, dto.getEmail(), dto.getOtp());
        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        String email = dto.getEmail();
        boolean userExists = userService.existsByEmail(email);
        Map<String, Object> response = new HashMap<>();

        // ✅ Admin login
        if (email.equalsIgnoreCase(ADMIN_EMAIL)) {
            if (!userExists) {
                userService.registerAdmin(email, ADMIN_PASSWORD);
            }

            UserEntity admin = userService.getUserByEmail(email);
            response.put("success", true);
            response.put("message", "Admin login successful");
            response.put("userExists", true);
            response.put("accessToken", jwtUtil.generateAccessToken(admin));
            response.put("refreshToken", jwtUtil.generateRefreshToken(admin));
            response.put("user", userService.getPublicProfile(email));
            return response;
        }

        // ✅ Normal user login
        // ✅ Normal user login
        if (userExists) {
            UserEntity user = userService.getUserByEmail(email);

            // ✅ Fix: Change from `UserRole.USER` to a valid enum value like `UserRole.STUDENT`
            if (user != null && user.getRole() == null) {
                user.setRole(UserRole.STUDENT);
                userService.saveUser(user); // Save the updated user to the database
            }

            // Now, the JWT generation should work without error
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("userExists", true);
            response.put("accessToken", jwtUtil.generateAccessToken(user));
            response.put("refreshToken", jwtUtil.generateRefreshToken(user));
            response.put("user", userService.getPublicProfile(email));
        } else {
            // New user: return temp token for registration
            response.put("success", true);
            response.put("message", "OTP verified. Please complete your registration.");
            response.put("userExists", false);
            response.put("tempToken", jwtUtil.generateTempToken(email));
            response.put("accessToken", null);
            response.put("refreshToken", null);
            response.put("user", null);
        }

        return response;
    }

    // ✅ 3. Register new user with tempToken (only after OTP verified)
    public Map<String, Object> registerUser(RegisterRequestDTO dto, String tempToken) {
        String email = jwtUtil.extractEmail(tempToken);

        if (userService.existsByEmail(email) && userService.isFullyRegistered(email)) {
            throw new RuntimeException("User already registered");
        }

        // Save user with role USER
        userService.registerUser(email, dto.getFullName(), dto.getPhone(), dto.getPassword());

        // ✅ Fetch updated user from DB again
        UserEntity user = userService.getUserByEmail(email);

        // ✅ Build user response
        Map<String, Object> minimalUser = new HashMap<>();
        minimalUser.put("id", user.getId());
        minimalUser.put("fullName", user.getFullName());
        minimalUser.put("email", user.getEmail());
        minimalUser.put("phone", user.getPhone());
        minimalUser.put("alternatePhone", user.getAlternatePhone());
        minimalUser.put("address", user.getAddress());
        minimalUser.put("biography", user.getBiography());
        minimalUser.put("language", user.getLanguage());
        minimalUser.put("photo", user.getPhoto());
        minimalUser.put("linkedin", user.getLinkedin());
        minimalUser.put("instagram", user.getInstagram());
        minimalUser.put("facebook", user.getFacebook());
        minimalUser.put("internshala", user.getInternshala());
        minimalUser.put("role", user.getRole());

        // ✅ Final response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Registration successful");
        response.put("accessToken", jwtUtil.generateAccessToken(user));
        response.put("refreshToken", jwtUtil.generateRefreshToken(user));
        response.put("user", minimalUser);

        return response;
    }

    // ✅ Email masking (e.g. s***@domain.com)
    private String maskEmail(String email) {
        int index = email.indexOf("@");
        return index <= 1 ? "***" + email.substring(index) : email.charAt(0) + "***" + email.substring(index);
    }
}
