package com.student.service.controller;

import com.student.service.dto.EnrollmentRequest;
import com.student.service.dto.EnrollmentResponse;
import com.student.service.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students/{id}/courses")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {
    
    private final EnrollmentService enrollmentService;
    
    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<EnrollmentResponse> enrollInCourse(
            @PathVariable Long id,
            @PathVariable Long courseId,
            @Valid @RequestBody EnrollmentRequest request) {
        
        log.info("Enrolling student {} in course {}", id, courseId);
        
        // Ensure courseId in path matches request
        request.setCourseId(courseId);
        
        EnrollmentResponse response = enrollmentService.enrollInCourse(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> getEnrolledCourses(@PathVariable Long id) {
        log.info("Fetching enrolled courses for student {}", id);
        
        List<EnrollmentResponse> response = enrollmentService.getEnrolledCourses(id);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> unenrollFromCourse(
            @PathVariable Long id,
            @PathVariable Long courseId) {
        
        log.info("Unenrolling student {} from course {}", id, courseId);
        
        enrollmentService.unenrollFromCourse(id, courseId);
        return ResponseEntity.noContent().build();
    }
}








