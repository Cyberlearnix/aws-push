package com.instructor.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "messages",
        indexes = {
                @Index(name = "idx_message_course", columnList = "course_id"),
                @Index(name = "idx_message_recipient", columnList = "recipientUserId"),
                @Index(name = "idx_message_created", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column
    private UUID recipientUserId; // Specific student or null for all students
    
    @Column(nullable = false)
    private UUID senderId;
    
    @Column(nullable = false, length = 100)
    private String senderName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(length = 20)
    private String messageType; // INDIVIDUAL, BROADCAST, REPLY

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}