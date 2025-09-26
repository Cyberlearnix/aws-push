package com.instructor.service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AnalyticsResponse {
    private UUID instructorId;
    private Long courseId;
    private double completionPercent;
    private int dropouts;
    private int activeStudents;
}
