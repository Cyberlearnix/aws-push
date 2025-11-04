package com.instructor.service.dto;

import com.instructor.service.entity.CourseCategory;
import com.instructor.service.entity.CourseLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long id;
    private UUID instructorId;
    private String title;
    private String description;
    private Double price;
    private CourseCategory category;
    private CourseLevel level;
    private String prerequisites;
    private String learningOutcomes;
    private String targetAudience;
    private boolean active;
    private boolean published;
}
