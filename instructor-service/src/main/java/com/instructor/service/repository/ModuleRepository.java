package com.instructor.service.repository;

import com.instructor.service.entity.CourseEntity;
import com.instructor.service.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<ModuleEntity, Long> {

    /**
     * Find modules by course ordered by orderIndex
     */
    List<ModuleEntity> findByCourseOrderByOrderIndexAsc(CourseEntity course);

    /**
     * Find modules by course ID ordered by orderIndex
     */
    List<ModuleEntity> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    /**
     * Find published modules by course
     */
    List<ModuleEntity> findByCourseAndPublishedTrueOrderByOrderIndexAsc(CourseEntity course);

    /**
     * Find modules by content type
     */
    List<ModuleEntity> findByContentTypeAndPublishedTrue(String contentType);

    /**
     * Find module by course and order index
     */
    Optional<ModuleEntity> findByCourseAndOrderIndex(CourseEntity course, Integer orderIndex);

    /**
     * Count modules by course
     */
    long countByCourse(CourseEntity course);

    /**
     * Count published modules by course
     */
    long countByCourseAndPublishedTrue(CourseEntity course);

    /**
     * Get max order index for a course
     */
    @Query("SELECT COALESCE(MAX(m.orderIndex), 0) FROM ModuleEntity m WHERE m.course = :course")
    Integer findMaxOrderIndexByCourse(@Param("course") CourseEntity course);

    /**
     * Find modules with specific duration range
     */
    @Query("SELECT m FROM ModuleEntity m WHERE m.duration >= :minDuration AND m.duration <= :maxDuration AND m.published = true")
    List<ModuleEntity> findByDurationRange(@Param("minDuration") Integer minDuration, @Param("maxDuration") Integer maxDuration);

    /**
     * Search modules by title containing keyword
     */
    List<ModuleEntity> findByTitleContainingIgnoreCaseAndPublishedTrue(String keyword);
}