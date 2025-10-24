package com.student.service.controller;

import com.student.service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class JwtTestController {

    private final JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @GetMapping("/jwt-secret-info")
    public Map<String, Object> getJwtSecretInfo() {
        return Map.of(
            "secretLength", jwtSecret.length(),
            "secretPreview", jwtSecret.substring(0, Math.min(50, jwtSecret.length())) + "...",
            "service", "student-service"
        );
    }
}
