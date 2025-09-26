package com.instructor.service.controller;

import com.instructor.service.dto.AnnouncementRequest;
import com.instructor.service.dto.MessageRequest;
import com.instructor.service.model.Announcement;
import com.instructor.service.model.Message;
import com.instructor.service.service.CommunicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/instructors/{id}/courses/{courseId}")
@RequiredArgsConstructor
public class CommunicationController {

    private final CommunicationService communicationService;

    // POST /instructors/{id}/courses/{courseId}/announcements
    @PostMapping("/announcements")
    public ResponseEntity<Announcement> postAnnouncement(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @Valid @RequestBody AnnouncementRequest request
    ) {
        return ResponseEntity.ok(
                communicationService.postAnnouncement(courseId, request.getTitle(), request.getMessage())
        );
    }

    // POST /instructors/{id}/courses/{courseId}/messages
    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @Valid @RequestBody MessageRequest request
    ) {
        return ResponseEntity.ok(
                communicationService.sendMessage(courseId, request.getSubject(), request.getMessage())
        );
    }

    // Optional: list announcements/messages
    @GetMapping("/announcements")
    public ResponseEntity<List<Announcement>> listAnnouncements(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(communicationService.listAnnouncements(courseId));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<Message>> listMessages(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(communicationService.listMessages(courseId));
    }
}
