package com.student.service.dto;

import com.student.service.entity.Progress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    
    private Long id;
    private Long studentId;
    private Long courseId;
    private Long moduleId;
    private Long lessonId;
    private Progress.ProgressType type;
    private Progress.ProgressStatus status;
    private Double completionPercentage;
    private Integer timeSpentMinutes;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}








