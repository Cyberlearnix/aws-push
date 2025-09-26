package com.student.service.controller;

import com.student.service.dto.MessageResponse;
import com.student.service.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students/{id}")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    
    private final MessageService messageService;
    
    @GetMapping("/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long id) {
        log.info("Fetching messages for student {}", id);
        
        List<MessageResponse> response = messageService.getMessages(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/courses/{courseId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessagesForCourse(
            @PathVariable Long id,
            @PathVariable Long courseId) {
        
        log.info("Fetching messages for student {} in course {}", id, courseId);
        
        List<MessageResponse> response = messageService.getMessagesForCourse(id, courseId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(
            @PathVariable Long id,
            @PathVariable Long messageId) {
        
        log.info("Marking message {} as read for student {}", messageId, id);
        
        messageService.markMessageAsRead(id, messageId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/messages/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(@PathVariable Long id) {
        log.info("Getting unread message count for student {}", id);
        
        Long count = messageService.getUnreadMessageCount(id);
        return ResponseEntity.ok(count);
    }
}








