package com.student.service.controller;

import com.student.service.dto.AssignmentSubmissionRequest;
import com.student.service.dto.AssignmentSubmissionResponse;
import com.student.service.entity.Assignment;
import com.student.service.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/students/{id}")
@Slf4j
@RequiredArgsConstructor
public class AssignmentController {
    
    private final AssignmentService assignmentService;
    
    @GetMapping("/assignments")
    public ResponseEntity<List<Assignment>> getAssignments(@PathVariable UUID id) {
        log.info("Fetching assignments for student {}", id);
        
        List<Assignment> response = assignmentService.getAssignments(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/assignments/{assignmentId}/submit")
    public ResponseEntity<AssignmentSubmissionResponse> submitAssignment(
            @PathVariable("id") UUID studentId,
            @PathVariable UUID assignmentId,
            @Valid @RequestBody AssignmentSubmissionRequest request) {
        
        log.info("Submitting assignment {} for student {}", assignmentId, studentId);
        
        AssignmentSubmissionResponse response = assignmentService.submitAssignment(studentId, assignmentId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/assignments/submissions")
    public ResponseEntity<List<AssignmentSubmissionResponse>> getAssignmentSubmissions(@PathVariable("id") UUID studentId) {
        log.info("Fetching assignment submissions for student {}", studentId);
        
        List<AssignmentSubmissionResponse> response = assignmentService.getAssignmentSubmissions(studentId);
        return ResponseEntity.ok(response);
    }
}








