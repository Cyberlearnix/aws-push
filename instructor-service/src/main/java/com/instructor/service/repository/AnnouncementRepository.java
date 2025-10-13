package com.instructor.service.repository;

import com.instructor.service.entity.AnnouncementEntity;
import com.instructor.service.entity.CourseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {

    /**
     * Find announcements by course ordered by creation date desc
     */
    List<AnnouncementEntity> findByCourseAndIsActiveTrueOrderByCreatedAtDesc(CourseEntity course);

    /**
     * Find announcements by course ID ordered by creation date desc
     */
    List<AnnouncementEntity> findByCourseIdAndIsActiveTrueOrderByCreatedAtDesc(Long courseId);

    /**
     * Find announcements by course with pagination
     */
    Page<AnnouncementEntity> findByCourseAndIsActiveTrue(CourseEntity course, Pageable pageable);

    /**
     * Find announcements by priority
     */
    List<AnnouncementEntity> findByPriorityAndIsActiveTrueOrderByCreatedAtDesc(String priority);

    /**
     * Find recent announcements by course
     */
    @Query("SELECT a FROM AnnouncementEntity a WHERE a.course = :course AND a.isActive = true AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AnnouncementEntity> findRecentAnnouncementsByCourse(@Param("course") CourseEntity course, @Param("since") LocalDateTime since);

    /**
     * Find announcements by title containing keyword
     */
    List<AnnouncementEntity> findByTitleContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String keyword);
}