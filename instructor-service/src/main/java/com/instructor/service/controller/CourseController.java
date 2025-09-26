package com.instructor.service.controller;

import com.instructor.service.dto.CourseRequest;
import com.instructor.service.dto.CourseResponse;
import com.instructor.service.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/instructors/{id}/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // POST /instructors/{id}/courses → Create a new course
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @PathVariable("id") UUID instructorId,
            @Valid @RequestBody CourseRequest request
    ) {
        CourseResponse response = courseService.createCourse(instructorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /instructors/{id}/courses → Get all courses created by instructor
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses(
            @PathVariable("id") UUID instructorId
    ) {
        return ResponseEntity.ok(courseService.getAllCoursesByInstructor(instructorId));
    }

    // GET /instructors/{id}/courses/{courseId} → Get details of a specific course
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(courseService.getCourseDetails(instructorId, courseId));
    }

    // PUT /instructors/{id}/courses/{courseId} → Update course info
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequest request
    ) {
        return ResponseEntity.ok(courseService.updateCourse(instructorId, courseId, request));
    }

    // DELETE /instructors/{id}/courses/{courseId} → Delete/unpublish a course
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        courseService.deleteCourse(instructorId, courseId);
        return ResponseEntity.noContent().build();
    }
}
