package com.student.service.repository;

import com.student.service.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByStudentId(Long studentId);
    
    List<Message> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    @Query("SELECT m FROM Message m WHERE m.studentId = :studentId AND m.status = 'UNREAD'")
    List<Message> findUnreadMessagesByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT m FROM Message m WHERE m.studentId = :studentId AND m.courseId = :courseId ORDER BY m.createdAt DESC")
    List<Message> findMessagesByStudentIdAndCourseIdOrderByCreatedAt(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.studentId = :studentId AND m.status = 'UNREAD'")
    Long countUnreadMessagesByStudentId(@Param("studentId") Long studentId);
}








