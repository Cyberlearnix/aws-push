package com.student.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Certificate {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Column(name = "course_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID courseId;
    
    @Column(nullable = false, unique = true)
    private String certificateNumber;
    
    @Column(nullable = false)
    private String courseName;
    
    @Column
    private String studentName;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column
    private LocalDateTime issuedAt;
    
    @Column
    private Double finalScore;
    
    @Column
    private String grade;
    
    @Column
    private String filePath;
    
    @Column
    private String verificationCode;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateStatus status = CertificateStatus.ACTIVE;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum CertificateStatus {
        ACTIVE, REVOKED, EXPIRED
    }
}








