package com.student.service.repository;

import com.student.service.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    
    List<Progress> findByStudentId(Long studentId);
    
    List<Progress> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    Optional<Progress> findByStudentIdAndCourseIdAndModuleId(Long studentId, Long courseId, Long moduleId);
    
    Optional<Progress> findByStudentIdAndCourseIdAndModuleIdAndLessonId(Long studentId, Long courseId, Long moduleId, Long lessonId);
    
    @Query("SELECT p FROM Progress p WHERE p.student.id = :studentId AND p.type = 'COURSE'")
    List<Progress> findCourseProgressByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT p FROM Progress p WHERE p.student.id = :studentId AND p.courseId = :courseId AND p.type = 'MODULE'")
    List<Progress> findModuleProgressByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT AVG(p.completionPercentage) FROM Progress p WHERE p.student.id = :studentId AND p.type = 'COURSE'")
    Double getAverageCourseProgressByStudentId(@Param("studentId") Long studentId);
}








