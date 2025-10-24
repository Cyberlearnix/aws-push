package com.student.service.controller;

import com.student.service.dto.AnnouncementResponse;
import com.student.service.security.JwtAuthenticationService;
import com.student.service.service.AnnouncementService;
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
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final JwtAuthenticationService jwtAuthService;

    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(@PathVariable(required = false) UUID id) {
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

        log.info("Fetching announcements for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        List<AnnouncementResponse> response = announcementService.getAnnouncements(studentInfo.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/announcements/courses/{courseId}")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncementsForCourse(
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

        log.info("Fetching announcements for student {} ({}) in course {}", studentInfo.getId(), studentInfo.getEmail(), courseId);

        List<AnnouncementResponse> response = announcementService.getAnnouncementsForCourse(studentInfo.getId(), courseId);
        return ResponseEntity.ok(response);
    }
}






