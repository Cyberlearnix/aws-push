package com.student.service.controller;

import com.student.service.dto.MessageResponse;
import com.student.service.security.JwtAuthenticationService;
import com.student.service.service.MessageService;
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
public class MessageController {

    private final MessageService messageService;
    private final JwtAuthenticationService jwtAuthService;

    @GetMapping("/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable(required = false) UUID id) {
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

        log.info("Fetching messages for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        List<MessageResponse> response = messageService.getMessages(studentInfo.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages/courses/{courseId}")
    public ResponseEntity<List<MessageResponse>> getMessagesForCourse(
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

        log.info("Fetching messages for student {} ({}) in course {}", studentInfo.getId(), studentInfo.getEmail(), courseId);

        List<MessageResponse> response = messageService.getMessagesForCourse(studentInfo.getId(), courseId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(
            @PathVariable(required = false) UUID id,
            @PathVariable Long messageId) {

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

        log.info("Marking message {} as read for student {} ({})", messageId, studentInfo.getId(), studentInfo.getEmail());

        messageService.markMessageAsRead(studentInfo.getId(), messageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(@PathVariable(required = false) UUID id) {
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

        log.info("Getting unread message count for student {} ({})", studentInfo.getId(), studentInfo.getEmail());

        Long count = messageService.getUnreadMessageCount(studentInfo.getId());
        return ResponseEntity.ok(count);
    }
}






