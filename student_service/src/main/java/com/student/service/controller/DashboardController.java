package com.student.service.controller;

import com.student.service.dto.DashboardResponse;
import com.student.service.dto.StatsResponse;
import com.student.service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/students/{id}")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable("id") UUID studentId) {
        log.info("Fetching dashboard for student {}", studentId);
        
        DashboardResponse response = dashboardService.getDashboard(studentId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(@PathVariable("id") UUID studentId) {
        log.info("Fetching stats for student {}", studentId);
        
        StatsResponse response = dashboardService.getStats(studentId);
        return ResponseEntity.ok(response);
    }
}








