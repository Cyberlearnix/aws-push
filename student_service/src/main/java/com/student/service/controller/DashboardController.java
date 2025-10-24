package com.student.service.controller;

import com.student.service.dto.DashboardResponse;
import com.student.service.dto.StatsResponse;
import com.student.service.security.JwtAuthenticationService;
import com.student.service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;
    private final JwtAuthenticationService jwtAuthService;

    @GetMapping({"/{id}/dashboard", "/dashboard"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable(required = false) UUID id) {
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

        log.info("Fetching dashboard for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        DashboardResponse response = dashboardService.getDashboard(studentInfo.getId());
        response.setStudentName(studentInfo.getEmail()); // Set from JWT data
        response.setStudentEmail(studentInfo.getEmail()); // Set from JWT data
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/{id}/stats", "/stats"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StatsResponse> getStats(@PathVariable(required = false) UUID id) {
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

        log.info("Fetching stats for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        StatsResponse response = dashboardService.getStats(studentInfo.getId());
        response.setStudentName(studentInfo.getEmail()); // Set from JWT data
        return ResponseEntity.ok(response);
    }
}






