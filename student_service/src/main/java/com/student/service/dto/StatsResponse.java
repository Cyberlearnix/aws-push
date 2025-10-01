package com.student.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {
    
    private String studentId;
    private String studentName;
    private Integer totalCoursesEnrolled;
    private Integer totalCoursesCompleted;
    private Integer totalCertificatesEarned;
    private Integer totalQuizzesTaken;
    private Integer totalAssignmentsSubmitted;
    private Integer totalReviewsPosted;
    private Double averageQuizScore;
    private Double averageAssignmentScore;
    private Integer totalTimeSpentMinutes;
    private Double totalTimeSpentHours;
    private LocalDateTime firstEnrollmentDate;
    private LocalDateTime lastActivityDate;
    private Map<String, Integer> coursesByCategory;
    private Map<String, Double> monthlyProgress;
    private Map<String, Integer> activityByMonth;
}








