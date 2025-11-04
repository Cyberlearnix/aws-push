package com.userservice.userservice.service;

import lombok.extern.slf4j.Slf4j;

import com.cyberlearnix.shared.dto.UserDTO;
import com.cyberlearnix.shared.dto.event.UserCreatedEvent;
import com.cyberlearnix.shared.enums.UserRole;
import com.userservice.userservice.dto.OtpVerificationRequestDTO;
import com.userservice.userservice.dto.UpdateUserRequestDTO;
import com.userservice.userservice.dto.UserPublicDTO;
import com.userservice.userservice.entity.UserEntity;
import com.userservice.userservice.event.UserEventPublisher;
import com.userservice.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OtpCacheService otpCacheService;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;

    public boolean existsByEmail(String email) {
        log.debug("Checking if user exists by email: {}", maskEmail(email));
        boolean exists = userRepository.existsByEmail(email);
        log.debug("User exists check for {}: {}", maskEmail(email), exists);
        return exists;
    }

    public boolean isFullyRegistered(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword() != null &&
                        user.getFullName() != null &&
                        user.getPhone() != null &&
                        Boolean.TRUE.equals(user.getEmailVerified()))
                .isPresent();
    }

    public Optional<UserPublicDTO> findByEmail(String email) {
        log.debug("Looking up user by email: {}", maskEmail(email));
        return userRepository.findByEmail(email)
                .map(user -> {
                    log.debug("Found user with email: {}", maskEmail(email));
                    return UserPublicDTO.builder()
                            .id(user.getId())
                            .fullName(user.getFullName())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .alternatePhone(user.getAlternatePhone())
                            .photo(user.getPhoto())
                            .address(user.getAddress())
                            .language(user.getLanguage())
                            .biography(user.getBiography())
                            .linkedin(user.getLinkedin())
                            .instagram(user.getInstagram())
                            .facebook(user.getFacebook())
                            .internshala(user.getInternshala())
                            .role(user.getRole())
                            .build();
                });
    }

    public UserPublicDTO getPublicProfile(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));
    }

    @Transactional
    public UserEntity registerUser(String email, String fullName, String phone, String password) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] Starting user registration for email: {}", requestId, maskEmail(email));
        
        // Default role is STUDENT
        return registerUserWithRole(email, fullName, phone, password, com.cyberlearnix.shared.enums.UserRole.STUDENT);
    }
    
    @Transactional
    public UserEntity registerUserWithRole(String email, String fullName, String phone, String password, 
                                         com.cyberlearnix.shared.enums.UserRole role) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        log.info("[{}] Starting {} registration for email: {}", requestId, role, maskEmail(email));
        
        if (email == null || email.isBlank()) {
            log.error("[{}] ❌ Cannot register user: Email is null or blank", requestId);
            throw new IllegalArgumentException("Email is required");
        }
        
        log.info("[{}] 📥 Registering user - FullName: {}, Phone: {}", 
                requestId, fullName, phone);
                
        log.info("[{}] Checking if user already exists", requestId);
        if (userRepository.existsByEmail(email)) {
            log.error("[{}] ❌ User with email {} already exists", requestId, maskEmail(email));
            throw new IllegalArgumentException("Email already in use");
        }

        // Create new user
        log.info("[{}] Creating new user record for {}", requestId, maskEmail(email));
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        
        log.info("[{}] Validating password strength", requestId);
        validatePasswordStrength(password, email, fullName);
        
        log.info("[{}] Encoding password", requestId);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEmailVerified(true);
        user.setActive(true);
        
        // Set default values for instructor-specific fields
        if (role == com.cyberlearnix.shared.enums.UserRole.INSTRUCTOR) {
            user.setDepartment("");
            user.setDesignation("");
            user.setQualification("");
            user.setBio("");
            user.setSpecialization("");
            user.setExperienceYears(0);
        }

        log.info("[{}] Saving user to database", requestId);
        UserEntity savedUser = userRepository.save(user);
        log.info("[{}] ✅ User saved successfully - ID: {}, Email: {}, Role: {}", 
                requestId, savedUser.getId(), maskEmail(savedUser.getEmail()), savedUser.getRole());
        
        // Publish event if user is an INSTRUCTOR
        if (role == com.cyberlearnix.shared.enums.UserRole.INSTRUCTOR) {
            log.info("[{}] Publishing UserCreatedEvent for new instructor: {}", requestId, savedUser.getId());
            userEventPublisher.publishUserCreatedEvent(convertToUserDTO(savedUser));
        }

        return savedUser;
    }

    public void registerAdmin(String email, String rawPassword) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] Starting admin registration for email: {}", requestId, maskEmail(email));
        
        log.info("[{}] Looking for existing admin user", requestId);
        UserEntity admin = userRepository.findByEmail(email).orElse(new UserEntity());
        boolean isNewAdmin = admin.getId() == null;
        
        log.info("[{}] {} admin user", requestId, isNewAdmin ? "Creating new" : "Updating existing");
        admin.setEmail(email);
        admin.setFullName("Admin");
        admin.setPhone("9999999999");

        // Only encode and set password if not already set
        if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            log.info("[{}] Setting new password for admin", requestId);
            admin.setPassword(passwordEncoder.encode(rawPassword));
        } else {
            log.info("[{}] Admin already has password, keeping existing", requestId);
        }

        // ✅ Always enforce role ADMIN (even if user already exists)
        admin.setRole(com.cyberlearnix.shared.enums.UserRole.ADMIN);
        admin.setEmailVerified(true);

        log.info("[{}] Saving admin to database", requestId);
        userRepository.save(admin);
        log.info("[{}] ✅ Admin registered or updated - Email: {}, Role: {}", requestId, maskEmail(admin.getEmail()), admin.getRole());
    }



    public List<UserPublicDTO> getAllUsers() {
        log.info("Fetching all users from database");
        List<UserEntity> users = userRepository.findAll();
        log.info("Found {} users in database", users.size());

        return users.stream().map(user -> UserPublicDTO.builder()
                .id(user.getId() != null ? UUID.fromString(user.getId().toString()) : null)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .alternatePhone(user.getAlternatePhone())
                .address(user.getAddress())
                .photo(user.getPhoto())
                .biography(user.getBiography())
                .language(user.getLanguage())
                .linkedin(user.getLinkedin())
                .instagram(user.getInstagram())
                .facebook(user.getFacebook())
                .internshala(user.getInternshala())
                .role(user.getRole())
                .department(user.getDepartment())
                .designation(user.getDesignation())
                .qualification(user.getQualification())
                .bio(user.getBio())
                .specialization(user.getSpecialization())
                .experienceYears(user.getExperienceYears())
                .build()
        ).toList();
    }

    public UserEntity updateUser(UUID userId, UpdateUserRequestDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (dto == null) {
            return user; // Return user without changes if DTO is null
        }
        
        if (Boolean.TRUE.equals(dto.getBecomeInstructor())) {
            user.setRole(UserRole.INSTRUCTOR);
        }

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getCountryCode() != null && dto.getPhone() != null)
            user.setPhone(dto.getCountryCode() + dto.getPhone());
        if (dto.getAlternatePhone() != null) user.setAlternatePhone(dto.getAlternatePhone());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getPhoto() != null) user.setPhoto(dto.getPhoto());
        if (dto.getBiography() != null) user.setBiography(dto.getBiography());
        if (dto.getLanguage() != null) user.setLanguage(dto.getLanguage());

        if (dto.getLinkedinUrl() != null) user.setLinkedin(dto.getLinkedinUrl());
        if (dto.getInstagramUrl() != null) user.setInstagram(dto.getInstagramUrl());
        if (dto.getFacebookUrl() != null) user.setFacebook(dto.getFacebookUrl());
        if (dto.getInternshalaUrl() != null) user.setInternshala(dto.getInternshalaUrl());

        if (dto.getPassword() != null) {
            // Validate new password against current user email and name
            validatePasswordStrength(dto.getPassword(), user.getEmail(), user.getFullName());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Handle instructor fields if user is becoming an instructor
        boolean isBecomingInstructor = Boolean.TRUE.equals(dto.getBecomeInstructor());
        if (isBecomingInstructor && user.getRole() != UserRole.INSTRUCTOR) {
            user.setRole(UserRole.INSTRUCTOR);
            // Publish event to create instructor profile
            log.info("Publishing UserCreatedEvent for new instructor: {}", user.getId());
            userEventPublisher.publishUserCreatedEvent(convertToUserDTO(user));
        }

        // Set instructor-specific fields (only if user is instructor or becoming one)
        if (user.getRole() == UserRole.INSTRUCTOR || Boolean.TRUE.equals(dto.getBecomeInstructor())) {
            if (dto.getDepartment() != null) user.setDepartment(dto.getDepartment());
            if (dto.getDesignation() != null) user.setDesignation(dto.getDesignation());
            if (dto.getQualification() != null) user.setQualification(dto.getQualification());
            if (dto.getBio() != null) user.setBio(dto.getBio());
            if (dto.getSpecialization() != null) user.setSpecialization(dto.getSpecialization());
            if (dto.getExperienceYears() != null) user.setExperienceYears(dto.getExperienceYears());
        }

        userRepository.save(user);
        return user;

    }

    public void adminDeleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }


    @Transactional
    public void deleteOwnAccount(UUID userId, OtpVerificationRequestDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UUID sessionId = UUID.fromString(dto.getOtpSessionId());
        boolean valid = otpCacheService.verifyOtp(sessionId, user.getEmail(), dto.getOtp());
        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP session");
        }
        userRepository.delete(user);          // Delete account
    }

// ... (rest of the code remains the same)

    public UserEntity getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", maskEmail(email));
        return userRepository.findByEmail(email)
                .map(user -> {
                    log.debug("User found - ID: {}, Role: {}, Active: {}", user.getId(), user.getRole(), user.isActive());
                    return user;
                })
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", maskEmail(email));
                    return new RuntimeException("User not found with email: " + email);
                });
    }

    public UserEntity getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserPublicDTO getPublicById(UUID id) {
        UserEntity u = getUserById(id);
        return UserPublicDTO.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .alternatePhone(u.getAlternatePhone())
                .address(u.getAddress())
                .photo(u.getPhoto())
                .biography(u.getBiography())
                .language(u.getLanguage())
                .linkedin(u.getLinkedin())
                .instagram(u.getInstagram())
                .facebook(u.getFacebook())
                .internshala(u.getInternshala())
                .role(u.getRole())
                .department(u.getDepartment())
                .designation(u.getDesignation())
                .qualification(u.getQualification())
                .bio(u.getBio())
                .specialization(u.getSpecialization())
                .experienceYears(u.getExperienceYears())
                .build();
    }

    public UserEntity setPhoto(UUID userId, String photo) {
        UserEntity user = getUserById(userId);
        user.setPhoto(photo);
        return userRepository.save(user);
    }

    public void resetPassword(String email, String otpSessionId, String otp, String newPassword) {
        // verify OTP session
        UUID sessionUuid = UUID.fromString(otpSessionId);
        boolean valid = otpCacheService.verifyOtp(sessionUuid, email, otp);
        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP session");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate new password against email and user's full name
        validatePasswordStrength(newPassword, email, user.getFullName());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // OTP already consumed in Redis by verifyOtp
    }

    public void setActive(UUID userId, boolean active) {
        UserEntity user = getUserById(userId);
        user.setActive(active);
        userRepository.save(user);
    }

    public UserEntity saveUser(UserEntity user) {
        log.debug("Saving user - ID: {}, Email: {}, Role: {}", user.getId(), maskEmail(user.getEmail()), user.getRole());
        UserEntity savedUser = userRepository.save(user);
        log.debug("User saved successfully - ID: {}", savedUser.getId());
        return savedUser;
    }
    
    private void validatePasswordStrength(String password, String email, String fullName) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required and cannot be empty");
        }

        String pwd = password.trim();

        if (pwd.length() < 8) {
            throw new IllegalArgumentException("Password should be minimum 8 characters long (current: " + pwd.length() + " characters)");
        }

        // Check for special characters
        boolean hasSpecialChar = false;
        for (int i = 0; i < pwd.length(); i++) {
            char c = pwd.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
                break;
            }
        }
        if (!hasSpecialChar) {
            throw new IllegalArgumentException("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)");
        }

        // Compare ignoring case and whitespace
        if (fullName != null && !fullName.trim().isEmpty()) {
            String name = fullName.trim();
            if (pwd.equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("Password cannot be the same as your full name for security reasons");
            }
        }

        if (email != null && !email.trim().isEmpty()) {
            String e = email.trim();
            String local = e;
            int at = e.indexOf('@');
            if (at > 0) {
                local = e.substring(0, at);
            }
            if (pwd.equalsIgnoreCase(e) || pwd.equalsIgnoreCase(local)) {
                throw new IllegalArgumentException("Password cannot be the same as your email address for security reasons");
            }
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.length() <= 3) return email;
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return email;
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
    
    private com.cyberlearnix.shared.dto.UserDTO convertToUserDTO(UserEntity user) {
        return com.cyberlearnix.shared.dto.UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(com.cyberlearnix.shared.enums.UserRole.valueOf(user.getRole().name()))
                .department(user.getDepartment())
                .designation(user.getDesignation())
                .qualification(user.getQualification())
                .bio(user.getBio())
                .specialization(user.getSpecialization())
                .experienceYears(user.getExperienceYears())
                .isActive(user.isActive())
                .build();
    }
}
