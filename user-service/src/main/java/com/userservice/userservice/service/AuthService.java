package com.userservice.userservice.service;

import com.userservice.userservice.dto.PasswordLoginRequest;
import com.userservice.userservice.dto.PasswordLoginResponse;
import com.userservice.userservice.dto.RegisterRequestDTO;
import com.userservice.userservice.dto.UserPublicDTO;
import com.userservice.userservice.entity.UserEntity;
import com.userservice.userservice.enums.UserRole;
import com.userservice.userservice.repository.UserRepository;
import com.userservice.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${auth.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${auth.lockout.duration-minutes:15}")
    private long lockoutDurationMinutes;

    public boolean isRegistered(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public LoginResult loginWithPassword(PasswordLoginRequest req) {
        var userOpt = userRepository.findByEmail(req.email());
        if (userOpt.isEmpty()) {
            return LoginResult.invalid("Invalid email or password");
        }

        var user = userOpt.get();

        // lockout check
        if (user.getLockoutUntil() != null && Instant.now().isBefore(user.getLockoutUntil())) {
            long mins = Math.max(1, Duration.between(Instant.now(), user.getLockoutUntil()).toMinutes());
            return LoginResult.locked("Account locked. Try again in " + mins + " minutes.");
        }

        // password check
        boolean ok = passwordEncoder.matches(req.password(), user.getPassword());
        if (!ok) {
            int newCount = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(newCount);

            if (newCount >= maxAttempts) {
                user.setLockoutUntil(Instant.now().plus(Duration.ofMinutes(lockoutDurationMinutes)));
                userRepository.save(user);
                return LoginResult.locked("Incorrect password. Account locked for " + lockoutDurationMinutes + " minutes.");
            } else {
                userRepository.save(user);
                int left = Math.max(0, maxAttempts - newCount);
                return LoginResult.invalid("Incorrect password. Attempts left: " + left);
            }
        }

        // success → reset counters
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // tokens
        String access = jwtUtil.generateAccessToken(user);
        String refresh = jwtUtil.generateRefreshToken(user);

        // FIX: pass enum `UserRole` (not String) into UserPublicDTO
        UserPublicDTO userDto = new UserPublicDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(), // <-- enum (UserRole)
                user.getPhone(),
                user.getAlternatePhone(),
                user.getAddress(),
                user.getBiography(),
                user.getLanguage(),
                user.getPhoto(),
                user.getLinkedin(),
                user.getInstagram(),
                user.getFacebook(),
                user.getInternshala(),
                user.getCountryCode()
        );

        PasswordLoginResponse response = PasswordLoginResponse.builder()
                .userExists(true)
                .success(true)
                .message("Login successful")
                .accessToken(access)
                .refreshToken(refresh)
                .user(userDto)
                .build();

        return LoginResult.success(response);
    }

    public String registerUser(RegisterRequestDTO dto, String tempToken) {
        String email = jwtUtil.extractSubject(tempToken);

        if (isRegistered(email)) {
            throw new RuntimeException("User already registered");
        }

        if (!dto.getPassword().matches("^(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$")) {
            throw new RuntimeException("Password too weak. Must contain at least 8 characters and one special character.");
        }

        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setFullName(dto.getFullName());
        newUser.setPhone(dto.getPhone());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setRole(UserRole.STUDENT);
        newUser.setEmailVerified(true);
        userRepository.save(newUser);

        UserEntity savedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return jwtUtil.generateAccessToken(savedUser) + "::" + jwtUtil.generateRefreshToken(savedUser);
    }

    public record LoginResult(boolean ok, boolean locked, PasswordLoginResponse response, String errorMessage) {
        public static LoginResult success(PasswordLoginResponse r) { return new LoginResult(true, false, r, null); }
        public static LoginResult invalid(String msg) { return new LoginResult(false, false, null, msg); }
        public static LoginResult locked(String msg) { return new LoginResult(false, true, null, msg); }
    }
}
