package com.student.service.dto;

import com.student.service.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private Integer rating;
    private String comment;
    private String title;
    private Review.ReviewStatus status;
    private Boolean isVerified;
    private Integer helpfulVotes;
    private Integer totalVotes;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}








