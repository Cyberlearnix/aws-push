package com.instructor.service.controller;

import com.instructor.service.dto.AnalyticsResponse;
import com.instructor.service.dto.DashboardResponse;
import com.instructor.service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/instructors/{id}")
@RequiredArgsConstructor
public class DashboardController {

    private final ReportService reportService;

    // GET /instructors/{id}/dashboard → Instructor dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard(@PathVariable("id") UUID instructorId) {
        return ResponseEntity.ok(reportService.getDashboard(instructorId));
    }

    // GET /instructors/{id}/courses/{courseId}/analytics → Course analytics
    @GetMapping("/courses/{courseId}/analytics")
    public ResponseEntity<AnalyticsResponse> analytics(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(reportService.getCourseAnalytics(instructorId, courseId));
    }

    // GET /instructors/{id}/earnings → Instructor earnings report
    @GetMapping("/earnings")
    public ResponseEntity<?> earnings(@PathVariable("id") UUID instructorId) {
        return ResponseEntity.ok(java.util.Map.of("instructorId", instructorId, "totalEarnings", reportService.getEarnings(instructorId)));
    }
}
