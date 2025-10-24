package com.student.service.controller;

import com.student.service.dto.QuizSubmissionRequest;
import com.student.service.dto.QuizSubmissionResponse;
import com.student.service.entity.Quiz;
import com.student.service.security.JwtAuthenticationService;
import com.student.service.service.QuizService;
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
public class QuizController {

    private final QuizService quizService;
    private final JwtAuthenticationService jwtAuthService;

    @GetMapping("/quiz/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<List<Quiz>> getQuizzesForModule(
            @PathVariable(required = false) UUID id,
            @PathVariable Long courseId,
            @PathVariable Long moduleId) {

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

        log.info("Fetching quizzes for student {} ({}) in course {} module {}", studentInfo.getId(), studentInfo.getEmail(), courseId, moduleId);

        List<Quiz> response = quizService.getQuizzesForModule(studentInfo.getId(), courseId, moduleId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/quiz/courses/{courseId}/modules/{moduleId}/submit/{quizId}")
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable(required = false) UUID id,
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmissionRequest request) {

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

        log.info("Submitting quiz {} for student {} ({}) in course {} module {}", quizId, studentInfo.getId(), studentInfo.getEmail(), courseId, moduleId);

        QuizSubmissionResponse response = quizService.submitQuiz(studentInfo.getId(), courseId, moduleId, quizId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/quiz/courses/{courseId}/results")
    public ResponseEntity<List<QuizSubmissionResponse>> getQuizResults(
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

        log.info("Fetching quiz results for student {} ({}) in course {}", studentInfo.getId(), studentInfo.getEmail(), courseId);

        List<QuizSubmissionResponse> response = quizService.getQuizResults(studentInfo.getId(), courseId);
        return ResponseEntity.ok(response);
    }
}






