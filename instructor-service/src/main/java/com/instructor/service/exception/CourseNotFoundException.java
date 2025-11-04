package com.instructor.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CourseNotFoundException extends ResponseStatusException {
    public CourseNotFoundException(Long courseId) {
        super(HttpStatus.NOT_FOUND, "Course not found with ID: " + courseId);
    }

    public CourseNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
