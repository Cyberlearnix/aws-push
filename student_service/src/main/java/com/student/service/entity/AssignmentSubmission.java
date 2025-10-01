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
import java.util.UUID;

@Entity
@Table(name = "assignment_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AssignmentSubmission {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;
    
    @Column
    private Integer attemptNumber = 1;
    
    @Column(columnDefinition = "TEXT")
    private String submissionText;
    
    @Column
    private String filePath;
    
    @Column
    private String fileName;
    
    @Column
    private Long fileSize;
    
    @Column
    private LocalDateTime submittedAt;
    
    @Column
    private LocalDateTime gradedAt;
    
    @Column
    private Double score;
    
    @Column
    private Double percentage;
    
    @Column
    private String grade;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    @Column
    private Boolean isLate = false;
    
    @Column
    private Boolean isPlagiarized = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum SubmissionStatus {
        DRAFT, SUBMITTED, GRADED, RETURNED
    }
}








