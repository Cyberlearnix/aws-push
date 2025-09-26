package com.student.service.repository;

import com.student.service.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    List<Quiz> findByCourseId(Long courseId);
    
    List<Quiz> findByCourseIdAndModuleId(Long courseId, Long moduleId);
    
    @Query("SELECT q FROM Quiz q WHERE q.courseId = :courseId AND q.moduleId = :moduleId AND q.status = 'ACTIVE'")
    List<Quiz> findActiveQuizzesByCourseIdAndModuleId(@Param("courseId") Long courseId, @Param("moduleId") Long moduleId);
    
    @Query("SELECT q FROM Quiz q WHERE q.courseId = :courseId AND q.status = 'ACTIVE'")
    List<Quiz> findActiveQuizzesByCourseId(@Param("courseId") Long courseId);
}








