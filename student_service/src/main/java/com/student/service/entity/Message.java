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

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private UUID studentId;
    
    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.UNREAD;
    
    @Column
    private LocalDateTime readAt;
    
    @Column
    private LocalDateTime repliedAt;
    
    @Column
    private Long parentMessageId;
    
    @Column
    private String attachmentPath;
    
    @Column
    private String attachmentName;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum MessageType {
        QUESTION, FEEDBACK, CLARIFICATION, GENERAL
    }
    
    public enum MessageStatus {
        UNREAD, READ, REPLIED, ARCHIVED
    }
}






