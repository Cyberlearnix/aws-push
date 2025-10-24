package com.student.service.controller;

import com.student.service.dto.ReviewRequest;
import com.student.service.dto.ReviewResponse;
import com.student.service.security.JwtAuthenticationService;
import com.student.service.service.ReviewService;
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
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtAuthenticationService jwtAuthService;

    @PostMapping("/reviews/courses/{courseId}")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable(required = false) UUID id,
            @PathVariable Long courseId,
            @Valid @RequestBody ReviewRequest request) {

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

        log.info("Adding review for student {} ({}) on course {}", studentInfo.getId(), studentInfo.getEmail(), courseId);

        // Ensure courseId in path matches request
        request.setCourseId(courseId);

        ReviewResponse response = reviewService.addReview(studentInfo.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/reviews/courses/{courseId}/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable(required = false) UUID id,
            @PathVariable Long courseId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {

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

        log.info("Updating review {} for student {} ({}) on course {}", reviewId, studentInfo.getId(), studentInfo.getEmail(), courseId);

        ReviewResponse response = reviewService.updateReview(studentInfo.getId(), courseId, reviewId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reviews/courses/{courseId}/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable(required = false) UUID id,
            @PathVariable Long courseId,
            @PathVariable Long reviewId) {

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

        log.info("Deleting review {} for student {} ({}) on course {}", reviewId, studentInfo.getId(), studentInfo.getEmail(), courseId);

        reviewService.deleteReview(studentInfo.getId(), courseId, reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getStudentReviews(@PathVariable(required = false) UUID id) {
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

        log.info("Fetching reviews for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        List<ReviewResponse> response = reviewService.getStudentReviews(studentInfo.getId());
        return ResponseEntity.ok(response);
    }
}






