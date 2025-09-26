package com.student.service.controller;

import com.student.service.dto.DashboardResponse;
import com.student.service.dto.StatsResponse;
import com.student.service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students/{id}")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable Long id) {
        log.info("Fetching dashboard for student {}", id);
        
        DashboardResponse response = dashboardService.getDashboard(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(@PathVariable Long id) {
        log.info("Fetching stats for student {}", id);
        
        StatsResponse response = dashboardService.getStats(id);
        return ResponseEntity.ok(response);
    }
}








