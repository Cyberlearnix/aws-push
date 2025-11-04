package com.instructor.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// This class is no longer an entity as it's a duplicate of Instructor class
// Kept for backward compatibility or DTO purposes
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID userId; // References user from user-service

    @Column(length = 100)
    private String department;

    @Column(length = 50)
    private String designation;

    @Column(length = 20)
    private String qualification;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 100)
    private String specialization;

    private Integer experienceYears;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // One-to-Many relationship with courses
    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourseEntity> courses = new ArrayList<>();
}