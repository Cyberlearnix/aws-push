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
    
    private Long id;
    private Long courseId;
    private String courseName;
    private Long instructorId;
    private String instructorName;
    private String title;
    private String content;
    private Announcement.AnnouncementType type;
    private Announcement.AnnouncementStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private Boolean isImportant;
    private Boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}








