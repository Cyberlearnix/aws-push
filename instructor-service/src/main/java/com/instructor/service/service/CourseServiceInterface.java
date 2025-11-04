package com.instructor.service.service;

import com.instructor.service.dto.CourseRequest;
import com.instructor.service.dto.CourseResponse;

import java.util.List;
import java.util.UUID;

public interface CourseServiceInterface {
    List<CourseResponse> getAllCoursesByInstructor(UUID instructorId);
    CourseResponse createCourse(CourseRequest request, UUID instructorId, String authToken);
    // Add other methods that are used by ReportService if needed
}
