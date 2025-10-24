package com.student.service.controller;

import com.student.service.dto.EnrollmentRequest;
import com.student.service.dto.EnrollmentResponse;
import com.student.service.security.JwtAuthenticationService;
import com.student.service.service.EnrollmentService;
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
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final JwtAuthenticationService jwtAuthService;

    // Handle both patterns: /api/students/{id}/courses/... and /api/students/courses/...
    @PostMapping({"/{id}/courses/{courseId}/enroll", "/courses/{courseId}/enroll"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponse> enrollInCourse(
            @PathVariable(required = false) UUID id,
            @PathVariable Long courseId,
            @Valid @RequestBody EnrollmentRequest request) {

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

        log.info("Enrolling student {} ({}) in course {}", studentInfo.getId(), studentInfo.getEmail(), courseId);

        // Ensure courseId in path matches request
        request.setCourseId(courseId);

        EnrollmentResponse response = enrollmentService.enrollInCourse(studentInfo.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping({"/{id}/courses", "/courses"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrolledCoursesHandler(@PathVariable(required = false) UUID id) {
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

        log.info("Fetching enrolled courses for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        List<EnrollmentResponse> response = enrollmentService.getEnrolledCourses(studentInfo.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping({"/{id}/courses/{courseId}/enroll", "/courses/{courseId}/enroll"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> unenrollFromCourse(
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

        log.info("Unenrolling student {} ({}) from course {}", studentInfo.getId(), studentInfo.getEmail(), courseId);

        enrollmentService.unenrollFromCourse(studentInfo.getId(), courseId);
        return ResponseEntity.noContent().build();
    }
}








