package com.userservice.userservice.service;

import com.userservice.userservice.dto.EmailRequestDTO;
import com.userservice.userservice.dto.OtpVerificationRequestDTO;
import com.userservice.userservice.dto.RegisterRequestDTO;
import com.userservice.userservice.entity.UserEntity;
import com.userservice.userservice.enums.UserRole;
import com.userservice.userservice.exception.OtpVerificationException;
import com.userservice.userservice.util.JwtUtil;
import com.userservice.userservice.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
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
        String requestId = UUID.randomUUID().toString();
        String email = dto.getEmail();
        log.info("[{}] Starting OTP send process for email: {}", requestId, maskEmail(email));
        
        String otp = otpUtil.generateOtp();
        log.info("[{}] Generated OTP for email: {} (length: {})", requestId, maskEmail(email), otp.length());

        try {
            emailService.sendOtpEmail(email, otp);
            log.info("[{}] OTP email sent successfully to: {}", requestId, maskEmail(email));
        } catch (Exception e) {
            log.error("[{}] Failed to send OTP email to: {} - Error: {}", requestId, maskEmail(email), e.getMessage(), e);
            throw e;
        }

        // Store OTP in Redis with 5-minute TTL
        UUID sessionId = otpCacheService.createOtpSession(email, otp, 300);
        log.info("[{}] Created OTP session: {} for email: {}", requestId, sessionId, maskEmail(email));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent successfully");
        response.put("otpSessionId", sessionId.toString());
        response.put("expirySeconds", 300);
        response.put("deliveryMethod", "EMAIL");
        response.put("maskedContact", maskEmail(email));
        response.put("resendCooldownSeconds", 0);
        
        log.info("[{}] OTP send process completed successfully for email: {}", requestId, maskEmail(email));
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
        String requestId = UUID.randomUUID().toString();
        String email = dto.getEmail();
        log.info("[{}] Starting OTP verification for email: {}, session: {}, otp: ***{}", 
            requestId, maskEmail(email), dto.getOtpSessionId(), dto.getOtp().substring(Math.max(0, dto.getOtp().length() - 2)));
            
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate session ID and verify OTP
            log.info("[{}] Parsing session ID: {}", requestId, dto.getOtpSessionId());
            UUID sessionId = UUID.fromString(dto.getOtpSessionId());
            
            log.info("[{}] Verifying OTP with cache service for email: {}", requestId, maskEmail(email));
            otpCacheService.verifyOtp(sessionId, email, dto.getOtp());
            log.info("[{}] OTP verified successfully for email: {}", requestId, maskEmail(email));

            log.info("[{}] Checking if user exists in database: {}", requestId, maskEmail(email));
            boolean userExists = userService.existsByEmail(email);
            log.info("[{}] User exists check result for {}: {}", requestId, maskEmail(email), userExists);

            // ✅ Admin login
            if (email.equalsIgnoreCase(ADMIN_EMAIL)) {
                log.info("[{}] Admin login detected for email: {}", requestId, maskEmail(email));
                if (!userExists) {
                    log.info("[{}] Admin user does not exist, creating admin account", requestId);
                    userService.registerAdmin(email, ADMIN_PASSWORD);
                    log.info("[{}] Admin account created successfully", requestId);
                }

                log.info("[{}] Fetching admin user details", requestId);
                UserEntity admin = userService.getUserByEmail(email);
                log.info("[{}] Admin user fetched - Role: {}, Active: {}", requestId, admin.getRole(), admin.getIsActive());
                
                // Check if admin account is active
                if (!Boolean.TRUE.equals(admin.getIsActive())) {
                    log.warn("[{}] Admin account is deactivated: {}", requestId, maskEmail(email));
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account has been deactivated. Please contact system administrator for assistance.");
                }
                
                log.info("[{}] Generating JWT tokens for admin", requestId);
                String accessToken = jwtUtil.generateAccessToken(admin);
                String refreshToken = jwtUtil.generateRefreshToken(admin);
                log.info("[{}] JWT tokens generated for admin (access token length: {})", requestId, accessToken.length());
                
                response.put("success", true);
                response.put("message", "Admin login successful");
                response.put("userExists", true);
                response.put("accessToken", accessToken);
                response.put("refreshToken", refreshToken);
                response.put("user", userService.getPublicProfile(email));
                
                log.info("[{}] Admin login completed successfully", requestId);
                return response;
            }

            // ✅ Normal user login
            if (userExists) {
                log.info("[{}] Existing user login for email: {}", requestId, maskEmail(email));
                UserEntity user = userService.getUserByEmail(email);
                log.info("[{}] User fetched - Role: {}, Active: {}", requestId, user.getRole(), user.getIsActive());
                
                // Check if user account is active
                if (!Boolean.TRUE.equals(user.getIsActive())) {
                    log.warn("[{}] User account is deactivated: {}", requestId, maskEmail(email));
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account has been deactivated. Please contact administrator for assistance.");
                }

                // Set default role if not set
                if (user != null && user.getRole() == null) {
                    log.info("[{}] User has no role, setting default STUDENT role", requestId);
                    user.setRole(UserRole.STUDENT);
                    userService.saveUser(user);
                    log.info("[{}] Default role STUDENT assigned to user", requestId);
                }

                log.info("[{}] Generating JWT tokens for existing user", requestId);
                String accessToken = jwtUtil.generateAccessToken(user);
                String refreshToken = jwtUtil.generateRefreshToken(user);
                log.info("[{}] JWT tokens generated for user (access token length: {})", requestId, accessToken.length());
                
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("userExists", true);
                response.put("accessToken", accessToken);
                response.put("refreshToken", refreshToken);
                response.put("user", userService.getPublicProfile(email));
                
                log.info("[{}] Existing user login completed successfully", requestId);
            } else {
                // New user: return temp token for registration
                log.info("[{}] New user detected, generating temp token for registration", requestId);
                String tempToken = jwtUtil.generateTempToken(email);
                log.info("[{}] Temp token generated for new user (length: {})", requestId, tempToken.length());
                
                response.put("success", true);
                response.put("message", "OTP verified. Please complete your registration.");
                response.put("userExists", false);
                response.put("tempToken", tempToken);
                response.put("accessToken", null);
                response.put("refreshToken", null);
                response.put("user", null);
                
                log.info("[{}] New user OTP verification completed, awaiting registration", requestId);
            }

            log.info("[{}] OTP verification process completed successfully for email: {}", requestId, maskEmail(email));
            return response;
            
        } catch (ResponseStatusException e) {
            // Re-throw ResponseStatusExceptions (like account deactivated) as-is
            log.info("[{}] OTP verification failed for email: {} - Status: {}, Reason: {}", 
                requestId, maskEmail(email), e.getStatusCode(), e.getReason());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("[{}] Invalid session ID format: {} for email: {}", requestId, dto.getOtpSessionId(), maskEmail(email), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid session ID format");
        } catch (OtpVerificationException e) {
            log.warn("[{}] OTP verification failed for email: {} - Error: {}", requestId, maskEmail(email), e.getMessage());
            throw new ResponseStatusException(
                e.getErrorType() == OtpVerificationException.OtpErrorType.EXPIRED_OTP ? 
                    HttpStatus.GONE : HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        } catch (Exception e) {
            log.error("[{}] Unexpected error during OTP verification for email: {}", requestId, maskEmail(email), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "An error occurred during OTP verification"
            );
        }
    }

    // ✅ 3. Register new user with tempToken (only after OTP verified)
    public Map<String, Object> registerUser(RegisterRequestDTO dto, String tempToken) {
        String requestId = UUID.randomUUID().toString();
        log.info("[{}] Starting user registration with temp token", requestId);
        
        String email = jwtUtil.extractEmail(tempToken);
        log.info("[{}] Extracted email from temp token: {}", requestId, maskEmail(email));

        if (userService.existsByEmail(email) && userService.isFullyRegistered(email)) {
            log.warn("[{}] User already registered: {}", requestId, maskEmail(email));
            throw new RuntimeException("User already registered");
        }
        
        log.info("[{}] Registering new user - FullName: {}, Phone: {}", 
            requestId, dto.getFullName(), dto.getPhone().replaceAll(".(?=.{2})", "*"));

        // Save user with role USER
        userService.registerUser(email, dto.getFullName(), dto.getPhone(), dto.getPassword());
        log.info("[{}] User registered successfully in database", requestId);

        // ✅ Fetch updated user from DB again
        log.info("[{}] Fetching registered user details from database", requestId);
        UserEntity user = userService.getUserByEmail(email);
        log.info("[{}] User fetched - ID: {}, Role: {}", requestId, user.getId(), user.getRole());

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
