package com.instructor.service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class DashboardResponse {
    private UUID instructorId;
    private int totalCourses;
    private int totalStudents;
    private double totalEarnings;
    private double averageRating;
}
