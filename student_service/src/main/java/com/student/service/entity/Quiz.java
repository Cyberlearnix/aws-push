package com.student.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Quiz {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "module_id", nullable = false)
    private Long moduleId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType type;
    
    @Column
    private Integer timeLimitMinutes;
    
    @Column
    private Integer maxAttempts = 1;
    
    @Column
    private Double passingScore = 70.0;
    
    @Column
    private Double passingPercentage = 70.0;
    
    @Column
    private Integer totalQuestions;
    
    public Double getPassingPercentage() {
        return passingPercentage;
    }
    
    @Column
    private Double totalPoints;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status = QuizStatus.ACTIVE;
    
    @Column
    private LocalDateTime availableFrom;
    
    @Column
    private LocalDateTime availableUntil;
    
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizQuestion> questions;
    
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizSubmission> submissions;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum QuizType {
        PRACTICE, ASSESSMENT, FINAL_EXAM
    }
    
    public enum QuizStatus {
        DRAFT, ACTIVE, INACTIVE
    }
}









