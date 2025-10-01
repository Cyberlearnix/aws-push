package com.student.service.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
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
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    
    @Column(name = "student_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID studentId;
    
    @Column(name = "instructor_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID instructorId;
    
    @Column(name = "course_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID courseId;
    
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
    
    @Column(columnDefinition = "BINARY(16)")
    private UUID parentMessageId;
    
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








