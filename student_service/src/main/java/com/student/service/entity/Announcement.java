package com.student.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "announcements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Announcement {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    
    @Column(name = "course_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID courseId;
    
    @Column(name = "instructor_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID instructorId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementStatus status = AnnouncementStatus.ACTIVE;
    
    @Column
    private LocalDateTime publishedAt;
    
    @Column
    private LocalDateTime expiresAt;
    
    @Column
    private Boolean isImportant = false;
    
    @Column
    private Boolean isPinned = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum AnnouncementType {
        GENERAL, ASSIGNMENT, QUIZ, COURSE_UPDATE, SYSTEM
    }
    
    public enum AnnouncementStatus {
        DRAFT, ACTIVE, EXPIRED, ARCHIVED
    }
}








