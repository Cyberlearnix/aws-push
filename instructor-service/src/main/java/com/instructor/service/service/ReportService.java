package com.instructor.service.service;

import com.instructor.service.dto.AnalyticsResponse;
import com.instructor.service.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final CourseService courseService;
    private final StudentService studentService;

    public DashboardResponse getDashboard(UUID instructorId) {
        int totalCourses = courseService.getAllCoursesByInstructor(instructorId).size();
        // naive aggregates for demo purposes
        int totalStudents = 0;
        for (var c : courseService.getAllCoursesByInstructor(instructorId)) {
            totalStudents += studentService.listStudents(c.getId()).size();
        }
        double totalEarnings = totalStudents * 10.0; // demo: $10 per student
        double avgRating = 4.5; // static for demo
        return DashboardResponse.builder()
                .instructorId(instructorId)
                .totalCourses(totalCourses)
                .totalStudents(totalStudents)
                .totalEarnings(totalEarnings)
                .averageRating(avgRating)
                .build();
    }

    public AnalyticsResponse getCourseAnalytics(UUID instructorId, Long courseId) {
        // demo analytics values
        int active = studentService.listStudents(courseId).size();
        double completion = active == 0 ? 0.0 : 42.0; // demo
        int dropouts = Math.max(0, active / 10);
        return AnalyticsResponse.builder()
                .instructorId(instructorId)
                .courseId(courseId)
                .activeStudents(active)
                .completionPercent(completion)
                .dropouts(dropouts)
                .build();
    }

    public double getEarnings(UUID instructorId) {
        return getDashboard(instructorId).getTotalEarnings();
    }
}
