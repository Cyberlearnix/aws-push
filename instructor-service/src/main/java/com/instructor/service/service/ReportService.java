package com.instructor.service.service;

import com.instructor.service.dto.AnalyticsResponse;
import com.instructor.service.dto.CourseResponse;
import com.instructor.service.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final CourseService courseService;

    public DashboardResponse getDashboard(UUID instructorId) {
        List<CourseResponse> courses = courseService.getAllCoursesByInstructor(instructorId);
        int totalCourses = courses.size();

        // Mock data for demo purposes
        int totalStudents = totalCourses * 15; // Assume 15 students per course
        double totalEarnings = totalStudents * 10.0; // $10 per student
        double avgRating = totalCourses > 0 ? 4.5 : 0.0; // Static rating

        return DashboardResponse.builder()
                .instructorId(instructorId)
                .totalCourses(totalCourses)
                .totalStudents(totalStudents)
                .totalEarnings(totalEarnings)
                .averageRating(avgRating)
                .build();
    }

    public AnalyticsResponse getCourseAnalytics(UUID instructorId, Long courseId) {
        // First verify the instructor has access to this course
        try {
            CourseResponse course = courseService.getCourseDetails(instructorId, courseId);
            
            // If we get here, the course exists and belongs to the instructor
            // For now, return mock data - in a real implementation, you would fetch real analytics
            int activeStudents = 15; // TODO: Replace with real data
            double completionRate = 42.0; // TODO: Replace with real data
            int dropouts = 3; // TODO: Replace with real data

            return AnalyticsResponse.builder()
                    .instructorId(instructorId)
                    .courseId(courseId)
                    .activeStudents(activeStudents)
                    .completionPercent(completionRate)
                    .dropouts(dropouts)
                    .build();
                    
        } catch (ResponseStatusException e) {
            // Re-throw 404 if course not found or access denied
            throw e;
        } catch (Exception e) {
            // Log unexpected errors
            log.error("Error getting analytics for course {}: {}", courseId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving course analytics");
        }
    }

    public double getEarnings(UUID instructorId) {
        return getDashboard(instructorId).getTotalEarnings();
    }
}
