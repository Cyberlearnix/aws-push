package com.instructor.service.repository;

import com.instructor.service.entity.CourseEntity;
import com.instructor.service.entity.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    /**
     * Find messages by course ID ordered by creation date desc
     */
    List<MessageEntity> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    /**
     * Find messages by course ordered by creation date desc
     */
    List<MessageEntity> findByCourseAndActiveTrueOrderByCreatedAtDesc(CourseEntity course);

    /**
     * Find messages by course ID ordered by creation date desc
     */
    List<MessageEntity> findByCourseIdAndActiveTrueOrderByCreatedAtDesc(Long courseId);

    /**
     * Find messages by course with pagination
     */
    Page<MessageEntity> findByCourseAndActiveTrue(CourseEntity course, Pageable pageable);

    /**
     * Find messages for a specific recipient
     */
    List<MessageEntity> findByCourseAndRecipientUserIdAndActiveTrueOrderByCreatedAtDesc(CourseEntity course, UUID recipientUserId);

    /**
     * Find broadcast messages (no specific recipient)
     */
    List<MessageEntity> findByCourseAndRecipientUserIdIsNullAndActiveTrueOrderByCreatedAtDesc(CourseEntity course);

    /**
     * Find messages by type
     */
    List<MessageEntity> findByMessageTypeAndActiveTrueOrderByCreatedAtDesc(String messageType);

    /**
     * Find unread messages by recipient
     */
    List<MessageEntity> findByRecipientUserIdAndIsReadFalseAndActiveTrueOrderByCreatedAtDesc(UUID recipientUserId);

    /**
     * Find recent messages by course
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.course = :course AND m.active = true AND m.createdAt >= :since ORDER BY m.createdAt DESC")
    List<MessageEntity> findRecentMessagesByCourse(@Param("course") CourseEntity course, @Param("since") LocalDateTime since);

    /**
     * Count unread messages by recipient
     */
    long countByRecipientUserIdAndIsReadFalseAndActiveTrue(UUID recipientUserId);

    /**
     * Count messages by course
     */
    long countByCourseAndActiveTrue(CourseEntity course);

    /**
     * Find messages by subject containing keyword
     */
    List<MessageEntity> findBySubjectContainingIgnoreCaseAndActiveTrueOrderByCreatedAtDesc(String keyword);

    /**
     * Find messages within a date range
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.createdAt >= :startDate AND m.createdAt <= :endDate AND m.active = true ORDER BY m.createdAt DESC")
    List<MessageEntity> findMessagesInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Mark message as read
     */
    @Query("UPDATE MessageEntity m SET m.isRead = true WHERE m.id = :messageId")
    void markAsRead(@Param("messageId") Long messageId);
}