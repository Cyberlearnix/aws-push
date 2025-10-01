package com.student.service.controller;

import com.student.service.dto.AnnouncementResponse;
import com.student.service.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/students/{id}")
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController {
    
    private final AnnouncementService announcementService;
    
    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(@PathVariable UUID id) {
        log.info("Fetching announcements for student {}", id);
        
        List<AnnouncementResponse> response = announcementService.getAnnouncements(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/courses/{courseId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncementsForCourse(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        
        log.info("Fetching announcements for student {} in course {}", id, courseId);
        
        List<AnnouncementResponse> response = announcementService.getAnnouncementsForCourse(id, courseId);
        return ResponseEntity.ok(response);
    }
}








