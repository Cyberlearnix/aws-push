package com.student.service.repository;

import com.student.service.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    
    List<Announcement> findByCourseId(UUID courseId);
    
    List<Announcement> findByInstructorId(UUID instructorId);
    
    @Query("SELECT a FROM Announcement a WHERE a.courseId = :courseId AND a.status = 'ACTIVE' AND (a.expiresAt IS NULL OR a.expiresAt > :now)")
    List<Announcement> findActiveAnnouncementsByCourseId(@Param("courseId") UUID courseId, @Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM Announcement a WHERE a.courseId IN :courseIds AND a.status = 'ACTIVE' AND (a.expiresAt IS NULL OR a.expiresAt > :now) ORDER BY a.isPinned DESC, a.publishedAt DESC")
    List<Announcement> findActiveAnnouncementsByCourseIds(
            @Param("courseIds") List<UUID> courseIds, @Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM Announcement a WHERE a.courseId = :courseId AND a.isImportant = true AND a.status = 'ACTIVE' AND (a.expiresAt IS NULL OR a.expiresAt > :now)")
    List<Announcement> findImportantAnnouncementsByCourseId(@Param("courseId") UUID courseId, @Param("now") LocalDateTime now);
}






