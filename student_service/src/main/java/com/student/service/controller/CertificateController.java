package com.student.service.controller;

import com.student.service.dto.CertificateResponse;
import com.student.service.security.JwtAuthenticationService;
import com.student.service.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
public class CertificateController {

    private final CertificateService certificateService;
    private final JwtAuthenticationService jwtAuthService;

    @GetMapping("/certificates")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CertificateResponse>> getCertificates(@PathVariable(required = false) UUID id) {
        // Get student information from JWT
        JwtAuthenticationService.StudentInfo studentInfo = jwtAuthService.getCurrentStudentInfo();
        if (studentInfo == null) {
            log.warn("No student information found in JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate role
        if (!"STUDENT".equals(studentInfo.getRole())) {
            log.warn("Access denied: User role is {}, expected STUDENT", studentInfo.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // If ID is provided in URL, validate it matches JWT student ID
        if (id != null && !studentInfo.getId().equals(id)) {
            log.warn("Student ID mismatch: JWT={}, URL={}", studentInfo.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Fetching certificates for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        List<CertificateResponse> response = certificateService.getCertificates(studentInfo.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/certificates/{certificateId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CertificateResponse> getCertificate(
            @PathVariable(required = false) UUID id,
            @PathVariable UUID certificateId) {

        // Get student information from JWT
        JwtAuthenticationService.StudentInfo studentInfo = jwtAuthService.getCurrentStudentInfo();
        if (studentInfo == null) {
            log.warn("No student information found in JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate role
        if (!"STUDENT".equals(studentInfo.getRole())) {
            log.warn("Access denied: User role is {}, expected STUDENT", studentInfo.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // If ID is provided in URL, validate it matches JWT student ID
        if (id != null && !studentInfo.getId().equals(id)) {
            log.warn("Student ID mismatch: JWT={}, URL={}", studentInfo.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Fetching certificate {} for student {} ({})", certificateId, studentInfo.getId(), studentInfo.getEmail());

        CertificateResponse response = certificateService.getCertificate(studentInfo.getId(), certificateId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/certificates/verify/{verificationCode}")
    @PreAuthorize("permitAll()") // Allow public access for certificate verification
    public ResponseEntity<CertificateResponse> verifyCertificate(
            @PathVariable String verificationCode) {

        log.info("Verifying certificate with code {}", verificationCode);

        // This endpoint doesn't require authentication as it's for public verification
        CertificateResponse response = certificateService.verifyCertificate(verificationCode);
        return ResponseEntity.ok(response);
    }
}






