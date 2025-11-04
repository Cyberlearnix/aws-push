package com.instructor.service.controller;

import com.instructor.service.dto.StudentGradeRequest;
import com.instructor.service.dto.StudentProgressResponse;
import com.instructor.service.dto.StudentSummaryResponse;
import com.instructor.service.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/instructors/{id}/courses/{courseId}")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // GET /instructors/{id}/courses/{courseId}/students → List students
    @GetMapping("/students")
    public ResponseEntity<List<StudentSummaryResponse>> listStudents(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(studentService.listStudents(courseId));
    }

    // GET /instructors/{id}/courses/{courseId}/students/{studentId} → Progress
    @GetMapping("/students/{studentId}")
    public ResponseEntity<StudentProgressResponse> getStudent(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @PathVariable UUID studentId
    ) {
        return ResponseEntity.ok(studentService.getStudentProgress(courseId, studentId));
    }

    // POST /instructors/{id}/courses/{courseId}/grades → Assign/update grades
    @PostMapping("/grades")
    public ResponseEntity<?> setGrade(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @Valid @RequestBody StudentGradeRequest request
    ) {
        studentService.assignOrUpdateGrade(courseId, request);
        return ResponseEntity.ok(java.util.Map.of("success", true));
    }
}
