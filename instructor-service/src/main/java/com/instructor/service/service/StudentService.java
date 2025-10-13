package com.instructor.service.service;

import com.instructor.service.dto.StudentGradeRequest;
import com.instructor.service.dto.StudentProgressResponse;
import com.instructor.service.dto.StudentSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StudentService {

    // courseId -> set of studentIds
    private final Map<Long, Set<Long>> courseEnrollments = new ConcurrentHashMap<>();
    // key: courseId:studentId -> progress percent
    private final Map<String, Double> progress = new ConcurrentHashMap<>();
    // key: courseId:studentId -> grade
    private final Map<String, Double> grades = new ConcurrentHashMap<>();

    public List<StudentSummaryResponse> listStudents(Long courseId) {
        return courseEnrollments.getOrDefault(courseId, Collections.emptySet())
                .stream()
                .map(sid -> StudentSummaryResponse.builder()
                        .studentId(sid)
                        .name("Student-" + sid)
                        .email("student" + sid + "@example.com")
                        .progressPercent(progress.getOrDefault(key(courseId, sid), 0.0))
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public StudentProgressResponse getStudentProgress(Long courseId, Long studentId) {
        double pct = progress.getOrDefault(key(courseId, studentId), 0.0);
        return StudentProgressResponse.builder()
                .studentId(studentId)
                .courseId(courseId)
                .completionPercent(pct)
                .status(pct >= 100.0 ? "COMPLETED" : "IN_PROGRESS")
                .build();
    }

    public void assignOrUpdateGrade(Long courseId, StudentGradeRequest req) {
        grades.put(key(courseId, req.getStudentId()), req.getGrade());
        // ensure enrollment exists so list endpoints reflect the student
        enroll(courseId, req.getStudentId());
    }

    // utility for tests/demos
    public void enroll(Long courseId, Long studentId) {
        courseEnrollments.computeIfAbsent(courseId, k -> ConcurrentHashMap.newKeySet()).add(studentId);
    }

    private String key(Long courseId, Long studentId) {
        return courseId + ":" + studentId;
    }
}
