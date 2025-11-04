package com.instructor.service.dto;

import com.instructor.service.entity.CourseCategory;
import com.instructor.service.entity.CourseLevel;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CourseRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be positive or zero")
    private Double price;
    
    @NotNull(message = "Category is required")
    private CourseCategory category;
    
    @NotNull(message = "Level is required")
    private CourseLevel level;
    
    private String prerequisites;
    private String learningOutcomes;
    private String targetAudience;
    private boolean active = true;
}
