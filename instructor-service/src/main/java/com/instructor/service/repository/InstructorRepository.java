package com.instructor.service.repository;

import com.instructor.service.entity.InstructorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorRepository extends JpaRepository<InstructorEntity, Long> {

    /**
     * Find instructor by user ID
     */
    Optional<InstructorEntity> findByUserId(UUID userId);

    /**
     * Find active instructors by department
     */
    List<InstructorEntity> findByDepartmentAndIsActiveTrue(String department);

    /**
     * Find all active instructors
     */
    List<InstructorEntity> findByIsActiveTrue();

    /**
     * Check if instructor exists by user ID
     */
    boolean existsByUserId(UUID userId);

    /**
     * Find instructors by specialization
     */
    List<InstructorEntity> findBySpecializationContainingIgnoreCaseAndIsActiveTrue(String specialization);

    /**
     * Custom query to find instructors with course count
     */
    @Query("SELECT i FROM InstructorEntity i LEFT JOIN FETCH i.courses c WHERE i.isActive = true")
    List<InstructorEntity> findActiveInstructorsWithCourses();

    /**
     * Find instructors by experience years range
     */
    @Query("SELECT i FROM InstructorEntity i WHERE i.experienceYears >= :minYears AND i.experienceYears <= :maxYears AND i.isActive = true")
    List<InstructorEntity> findByExperienceYearsRange(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);
}