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
public class AnalyticsResponse {
    private UUID instructorId;
    private Long courseId;
    private double completionPercent;
    private int dropouts;
    private int activeStudents;
}
