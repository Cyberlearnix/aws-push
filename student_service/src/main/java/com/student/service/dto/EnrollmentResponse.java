package com.student.service.dto;

import com.student.service.entity.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    
    private Long id;
    private Long studentId;
    private Long courseId;
    private String courseName;
    private Enrollment.EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private Double progressPercentage;
    private Integer totalModules;
    private Integer completedModules;
    private Integer totalLessons;
    private Integer completedLessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}








