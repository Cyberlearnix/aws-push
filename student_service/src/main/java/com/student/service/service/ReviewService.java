package com.student.service.service;

import com.student.service.dto.ReviewRequest;
import com.student.service.dto.ReviewResponse;
import com.student.service.entity.Review;
import com.student.service.entity.Student;
import com.student.service.exception.ResourceNotFoundException;
import com.student.service.exception.ConflictException;
import com.student.service.repository.ReviewRepository;
import com.student.service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final StudentRepository studentRepository;
    
    public ReviewResponse addReview(Long studentId, ReviewRequest request) {
        log.info("Adding review for student {} on course {}", studentId, request.getCourseId());
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Check if student has already reviewed this course
        if (reviewRepository.findByStudentIdAndCourseId(studentId, request.getCourseId()).isPresent()) {
            throw new ConflictException("Student has already reviewed this course");
        }
        
        Review review = new Review();
        review.setStudent(student);
        review.setCourseId(request.getCourseId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setTitle(request.getTitle());
        review.setStatus(Review.ReviewStatus.ACTIVE);
        review.setIsVerified(false);
        review.setHelpfulVotes(0);
        review.setTotalVotes(0);
        review.setPublishedAt(LocalDateTime.now());
        
        Review savedReview = reviewRepository.save(review);
        log.info("Successfully added review for student {} on course {}", studentId, request.getCourseId());
        
        return mapToResponse(savedReview);
    }
    
    public ReviewResponse updateReview(Long studentId, Long courseId, Long reviewId, ReviewRequest request) {
        log.info("Updating review {} for student {} on course {}", reviewId, studentId, courseId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        // Verify that the review belongs to the student and course
        if (!review.getStudent().getId().equals(studentId) || !review.getCourseId().equals(courseId)) {
            throw new ResourceNotFoundException("Review not found");
        }
        
        // Update review fields
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setTitle(request.getTitle());
        
        Review savedReview = reviewRepository.save(review);
        log.info("Successfully updated review {} for student {} on course {}", reviewId, studentId, courseId);
        
        return mapToResponse(savedReview);
    }
    
    public void deleteReview(Long studentId, Long courseId, Long reviewId) {
        log.info("Deleting review {} for student {} on course {}", reviewId, studentId, courseId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        // Verify that the review belongs to the student and course
        if (!review.getStudent().getId().equals(studentId) || !review.getCourseId().equals(courseId)) {
            throw new ResourceNotFoundException("Review not found");
        }
        
        // Soft delete by changing status
        review.setStatus(Review.ReviewStatus.DELETED);
        reviewRepository.save(review);
        
        log.info("Successfully deleted review {} for student {} on course {}", reviewId, studentId, courseId);
    }
    
    @Transactional(readOnly = true)
    public List<ReviewResponse> getStudentReviews(Long studentId) {
        log.info("Fetching reviews for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<Review> reviews = reviewRepository.findActiveReviewsByStudentId(studentId);
        
        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setStudentId(review.getStudent().getId());
        response.setStudentName(review.getStudent().getFirstName() + " " + review.getStudent().getLastName());
        response.setCourseId(review.getCourseId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setTitle(review.getTitle());
        response.setStatus(review.getStatus());
        response.setIsVerified(review.getIsVerified());
        response.setHelpfulVotes(review.getHelpfulVotes());
        response.setTotalVotes(review.getTotalVotes());
        response.setPublishedAt(review.getPublishedAt());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }
}








