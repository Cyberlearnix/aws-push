package com.student.service.dto;

import com.student.service.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    
    private Long id;
    private UUID studentId;
    private String studentName;
    private Long instructorId;
    private String instructorName;
    private Long courseId;
    private String courseName;
    private String subject;
    private String content;
    private Message.MessageType type;
    private Message.MessageStatus status;
    private LocalDateTime readAt;
    private LocalDateTime repliedAt;
    private Long parentMessageId;
    private String attachmentPath;
    private String attachmentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}








