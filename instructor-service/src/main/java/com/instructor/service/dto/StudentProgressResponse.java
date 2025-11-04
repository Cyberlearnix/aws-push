package com.instructor.service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProgressResponse {
    private UUID studentId;
    private Long courseId;
    private double completionPercent;
    private String status; // e.g., IN_PROGRESS, COMPLETED
}
