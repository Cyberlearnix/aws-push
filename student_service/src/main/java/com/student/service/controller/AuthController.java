package com.student.service.controller;

import com.student.service.entity.Student;
import com.student.service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/validate-credentials")
    public ResponseEntity<Map<String, Object>> validateCredentials(@Valid @RequestBody LoginRequest request) {
        log.info("Credential validation attempt for email: {}", request.getEmail());

        Student student = studentRepository.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        Map<String, Object> response = Map.of(
            "valid", true,
            "userId", student.getId(),
            "email", student.getEmail(),
            "role", student.getRole().name(),
            "firstName", student.getFirstName(),
            "lastName", student.getLastName()
        );

        log.info("Successful credential validation for student: {}", student.getEmail());
        return ResponseEntity.ok(response);
    }

    public static class LoginRequest {
        private String email;
        private String password;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}






