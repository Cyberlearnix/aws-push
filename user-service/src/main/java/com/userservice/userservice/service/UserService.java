package com.userservice.userservice.service;

import com.userservice.userservice.dto.OtpVerificationRequestDTO;
import com.userservice.userservice.dto.UpdateUserRequestDTO;
import com.userservice.userservice.dto.UserPublicDTO;
import com.userservice.userservice.entity.UserEntity;
import com.userservice.userservice.enums.UserRole;
import com.userservice.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.userservice.userservice.util.PasswordValidatorUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final OtpCacheService otpCacheService;
    private final PasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isFullyRegistered(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword() != null &&
                        user.getFullName() != null &&
                        user.getPhone() != null &&
                        Boolean.TRUE.equals(user.getEmailVerified()))
                .isPresent();
    }

    public UserPublicDTO getPublicProfile(String email) {
        return userRepository.findByEmail(email)
                .map(user -> UserPublicDTO.builder()
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
                        .role(user.getRole()) // ✅ Include role in response
                        .build())
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));
    }

    public UserEntity registerUser(String email, String fullName, String phone, String password) {
        if (email == null || email.isBlank()) {
            log.error("❌ Cannot register user: Email is null or blank");
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        log.debug("📥 Registering user with email={}, fullName={}, phone={}", email, fullName, phone);

        Optional<UserEntity> existingOpt = userRepository.findByEmail(email);
        UserEntity user = existingOpt.orElse(new UserEntity());

        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        // Validate password strength and non-equality to email/username
        PasswordValidatorUtil.validateOrThrow(password, email, fullName);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.STUDENT);

        user.setEmailVerified(true);

        UserEntity savedUser = userRepository.save(user);
        log.info("✅ User saved: id={}, email={}, role={}", savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        return savedUser;
    }

    public void registerAdmin(String email, String rawPassword) {
        UserEntity admin = userRepository.findByEmail(email).orElse(new UserEntity());

        admin.setEmail(email);
        admin.setFullName("Admin");
        admin.setPhone("9999999999");

        // Only encode and set password if not already set
        if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(rawPassword));
        }

        // ✅ Always enforce role ADMIN (even if user already exists)
        admin.setRole(UserRole.ADMIN);

        admin.setEmailVerified(true);

        userRepository.save(admin);
        log.info("✅ Admin registered or updated: email={}, role={}", admin.getEmail(), admin.getRole());
    }



    public List<UserPublicDTO> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();

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
            PasswordValidatorUtil.validateOrThrow(dto.getPassword(), user.getEmail(), user.getFullName());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
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


    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
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
        PasswordValidatorUtil.validateOrThrow(newPassword, email, user.getFullName());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // OTP already consumed in Redis by verifyOtp
    }

    public void setActive(UUID userId, boolean active) {
        UserEntity user = getUserById(userId);
        user.setIsActive(active);
        userRepository.save(user);
    }

    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }
}
