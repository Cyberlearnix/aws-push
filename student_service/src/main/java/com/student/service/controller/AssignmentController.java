package com.student.service.controller;

import com.student.service.dto.AssignmentSubmissionRequest;
import com.student.service.dto.AssignmentSubmissionResponse;
import com.student.service.entity.Assignment;
import com.student.service.security.JwtAuthenticationService;
import com.student.service.service.AssignmentService;
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
@PreAuthorize("hasRole('STUDENT')")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final JwtAuthenticationService jwtAuthService;

    @GetMapping("/assignments")
    public ResponseEntity<List<Assignment>> getAssignments(@PathVariable(required = false) UUID id) {
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

        log.info("Fetching assignments for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        List<Assignment> response = assignmentService.getAssignments(studentInfo.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assignments/{assignmentId}/submit")
    public ResponseEntity<AssignmentSubmissionResponse> submitAssignment(
            @PathVariable(required = false) UUID id,
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentSubmissionRequest request) {

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

        log.info("Submitting assignment {} for student {} ({})", assignmentId, studentInfo.getId(), studentInfo.getEmail());

        AssignmentSubmissionResponse response = assignmentService.submitAssignment(studentInfo.getId(), assignmentId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/submissions")
    public ResponseEntity<List<AssignmentSubmissionResponse>> getAssignmentSubmissions(@PathVariable(required = false) UUID id) {
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

        log.info("Fetching assignment submissions for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        List<AssignmentSubmissionResponse> response = assignmentService.getAssignmentSubmissions(studentInfo.getId());
        return ResponseEntity.ok(response);
    }
}






