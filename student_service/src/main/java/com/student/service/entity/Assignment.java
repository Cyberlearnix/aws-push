package com.student.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Assignment {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "course_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID courseId;
    
    @Column(name = "module_id", columnDefinition = "BINARY(16)")
    private UUID moduleId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String instructions;
    
    @Column
    private Double totalPoints;
    
    @Column
    private LocalDateTime dueDate;
    
    @Column
    private LocalDateTime availableFrom;
    
    @Column
    private LocalDateTime availableUntil;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;
    
    @Column
    private Boolean allowLateSubmission = false;
    
    @Column
    private Integer maxAttempts = 1;
    
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AssignmentSubmission> submissions;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum AssignmentType {
        HOMEWORK, PROJECT, ESSAY, PRESENTATION
    }
    
    public enum AssignmentStatus {
        DRAFT, ACTIVE, CLOSED
    }
}








