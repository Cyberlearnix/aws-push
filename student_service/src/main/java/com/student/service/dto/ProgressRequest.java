package com.student.service.dto;

import com.student.service.entity.Progress;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressRequest {
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    private Long moduleId;
    private Long lessonId;
    
    @NotNull(message = "Progress type is required")
    private Progress.ProgressType type;
    
    @NotNull(message = "Progress status is required")
    private Progress.ProgressStatus status;
    
    @Min(value = 0, message = "Completion percentage must be between 0 and 100")
    @Max(value = 100, message = "Completion percentage must be between 0 and 100")
    private Double completionPercentage;
    
    @Min(value = 0, message = "Time spent must be non-negative")
    private Integer timeSpentMinutes;
    
    private String notes;
}








