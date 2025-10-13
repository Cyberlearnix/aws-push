package com.instructor.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "modules",
        indexes = {
                @Index(name = "idx_module_course", columnList = "course_id"),
                @Index(name = "idx_module_order", columnList = "course_id, orderIndex")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content; // Could be markdown, HTML, or reference to external content

    @Column(length = 20)
    private String contentType; // TEXT, VIDEO, DOCUMENT, QUIZ, etc.

    @Column
    private String contentUrl; // URL for videos, documents, etc.

    @Builder.Default
    @Column(nullable = false)
    private Integer orderIndex = 0; // Order within the course

    private Integer duration; // Duration in minutes

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPublished = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Many-to-One relationship with course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;
}