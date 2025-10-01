package com.student.service.dto;

import com.student.service.entity.Announcement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {
    
    private String id;
    private String courseId;
    private String courseName;
    private String instructorId;
    private String instructorName;
    private String title;
    private String content;
    private String type;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private Boolean isImportant;
    private Boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}








