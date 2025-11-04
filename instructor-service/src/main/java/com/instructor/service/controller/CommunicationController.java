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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/instructors/{id}/courses/{courseId}")
@RequiredArgsConstructor
public class CommunicationController {

    private final CommunicationService communicationService;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return UUID.fromString(user.getUsername());
        }
        throw new SecurityException("User not authenticated");
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return user.getUsername();
        }
        return "System";
    }

    // POST /instructors/{id}/courses/{courseId}/announcements
    @PostMapping("/announcements")
    public ResponseEntity<Announcement> postAnnouncement(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @Valid @RequestBody AnnouncementRequest request
    ) {
        return ResponseEntity.ok(
                communicationService.createAnnouncement(courseId, request.getTitle(), request.getMessage(), getCurrentUserId())
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
                communicationService.sendMessage(courseId, request.getSubject(), request.getMessage(), getCurrentUserId(), getCurrentUsername())
        );
    }

    // Optional: list announcements/messages
    @GetMapping("/announcements")
    public ResponseEntity<List<Announcement>> listAnnouncements(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(communicationService.getAnnouncements(courseId));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<Message>> listMessages(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(communicationService.getMessages(courseId));
    }
}
