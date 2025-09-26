package com.student.service.controller;

import com.student.service.dto.ReviewRequest;
import com.student.service.dto.ReviewResponse;
import com.student.service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students/{id}")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping("/courses/{courseId}/reviews")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable Long id,
            @PathVariable Long courseId,
            @Valid @RequestBody ReviewRequest request) {
        
        log.info("Adding review for student {} on course {}", id, courseId);
        
        // Ensure courseId in path matches request
        request.setCourseId(courseId);
        
        ReviewResponse response = reviewService.addReview(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/courses/{courseId}/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @PathVariable Long courseId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {
        
        log.info("Updating review {} for student {} on course {}", reviewId, id, courseId);
        
        ReviewResponse response = reviewService.updateReview(id, courseId, reviewId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/courses/{courseId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @PathVariable Long courseId,
            @PathVariable Long reviewId) {
        
        log.info("Deleting review {} for student {} on course {}", reviewId, id, courseId);
        
        reviewService.deleteReview(id, courseId, reviewId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getStudentReviews(@PathVariable Long id) {
        log.info("Fetching reviews for student {}", id);
        
        List<ReviewResponse> response = reviewService.getStudentReviews(id);
        return ResponseEntity.ok(response);
    }
}








