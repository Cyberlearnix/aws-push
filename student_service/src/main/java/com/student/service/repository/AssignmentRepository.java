package com.student.service.repository;

import com.student.service.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    List<Assignment> findByCourseId(Long courseId);
    
    List<Assignment> findByCourseIdAndModuleId(Long courseId, Long moduleId);
    
    @Query("SELECT a FROM Assignment a WHERE a.courseId = :courseId AND a.status = 'ACTIVE'")
    List<Assignment> findActiveAssignmentsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT a FROM Assignment a WHERE a.courseId = :courseId AND a.moduleId = :moduleId AND a.status = 'ACTIVE'")
    List<Assignment> findActiveAssignmentsByCourseIdAndModuleId(@Param("courseId") Long courseId, @Param("moduleId") Long moduleId);
}








