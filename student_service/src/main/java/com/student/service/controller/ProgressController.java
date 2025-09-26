package com.student.service.controller;

import com.student.service.dto.ProgressRequest;
import com.student.service.dto.ProgressResponse;
import com.student.service.service.ProgressService;
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
public class ProgressController {
    
    private final ProgressService progressService;
    
    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<List<ProgressResponse>> getCourseProgress(
            @PathVariable Long id,
            @PathVariable Long courseId) {
        
        log.info("Fetching progress for student {} in course {}", id, courseId);
        
        List<ProgressResponse> response = progressService.getCourseProgress(id, courseId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/courses/{courseId}/progress")
    public ResponseEntity<ProgressResponse> updateProgress(
            @PathVariable Long id,
            @PathVariable Long courseId,
            @Valid @RequestBody ProgressRequest request) {
        
        log.info("Updating progress for student {} in course {}", id, courseId);
        
        // Ensure courseId in path matches request
        request.setCourseId(courseId);
        
        ProgressResponse response = progressService.updateProgress(id, courseId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/progress/overall")
    public ResponseEntity<List<ProgressResponse>> getOverallProgress(@PathVariable Long id) {
        log.info("Fetching overall progress for student {}", id);
        
        List<ProgressResponse> response = progressService.getOverallProgress(id);
        return ResponseEntity.ok(response);
    }
}








