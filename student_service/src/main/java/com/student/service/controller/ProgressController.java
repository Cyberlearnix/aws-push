package com.student.service.controller;

import com.student.service.dto.ProgressRequest;
import com.student.service.dto.ProgressResponse;
import com.student.service.security.JwtAuthenticationService;
import com.student.service.service.ProgressService;
import jakarta.validation.Valid;
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
public class ProgressController {

    private final ProgressService progressService;
    private final JwtAuthenticationService jwtAuthService;

    @GetMapping({"/{id}/progress/courses/{courseId}", "/progress/courses/{courseId}"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ProgressResponse>> getCourseProgress(
            @PathVariable(required = false) UUID id,
            @PathVariable Long courseId) {

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

        log.info("Fetching progress for student {} ({}) in course {}", studentInfo.getId(), studentInfo.getEmail(), courseId);

        List<ProgressResponse> response = progressService.getCourseProgress(studentInfo.getId(), courseId);
        return ResponseEntity.ok(response);
    }

    @PutMapping({"/{id}/progress/courses/{courseId}", "/progress/courses/{courseId}"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ProgressResponse> updateProgress(
            @PathVariable(required = false) UUID id,
            @PathVariable Long courseId,
            @Valid @RequestBody ProgressRequest request) {

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

        log.info("Updating progress for student {} ({}) in course {}", studentInfo.getId(), studentInfo.getEmail(), courseId);

        // Ensure courseId in path matches request
        request.setCourseId(courseId);

        ProgressResponse response = progressService.updateProgress(studentInfo.getId(), courseId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/{id}/progress/overall", "/progress/overall"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ProgressResponse>> getOverallProgress(@PathVariable(required = false) UUID id) {
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

        log.info("Fetching overall progress for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        List<ProgressResponse> response = progressService.getOverallProgress(studentInfo.getId());
        return ResponseEntity.ok(response);
    }
}






