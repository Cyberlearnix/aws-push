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

@RestController
@RequestMapping("/students/{id}")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {
    
    private final AssignmentService assignmentService;
    
    @GetMapping("/assignments")
    public ResponseEntity<List<Assignment>> getAssignments(@PathVariable Long id) {
        log.info("Fetching assignments for student {}", id);
        
        List<Assignment> response = assignmentService.getAssignments(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/assignments/{assignmentId}/submit")
    public ResponseEntity<AssignmentSubmissionResponse> submitAssignment(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentSubmissionRequest request) {
        
        log.info("Submitting assignment {} for student {}", assignmentId, id);
        
        AssignmentSubmissionResponse response = assignmentService.submitAssignment(id, assignmentId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/assignments/submissions")
    public ResponseEntity<List<AssignmentSubmissionResponse>> getAssignmentSubmissions(@PathVariable Long id) {
        log.info("Fetching assignment submissions for student {}", id);
        
        List<AssignmentSubmissionResponse> response = assignmentService.getAssignmentSubmissions(id);
        return ResponseEntity.ok(response);
    }
}








