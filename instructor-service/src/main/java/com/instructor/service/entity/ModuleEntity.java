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

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ContentType contentType = ContentType.TEXT;

    @Column(name = "video_url")
    private String videoUrl; // URL for video content
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl; // URL for thumbnail image

    @Builder.Default
    @Column(nullable = false)
    private Integer orderIndex = 0; // Order within the course

    private Integer duration; // Duration in minutes

    @Builder.Default
    @Column(name = "is_published", nullable = false)
    private Boolean published = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Many-to-One relationship with course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    // Many-to-One relationship with instructor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;
}