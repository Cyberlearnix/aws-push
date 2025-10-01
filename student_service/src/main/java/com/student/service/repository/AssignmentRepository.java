package com.student.service.repository;

import com.student.service.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    
    List<Assignment> findByCourseId(UUID courseId);
    
    List<Assignment> findByCourseIdAndModuleId(UUID courseId, UUID moduleId);
    
    @Query("SELECT a FROM Assignment a WHERE a.courseId = :courseId AND a.status = 'ACTIVE'")
    List<Assignment> findActiveAssignmentsByCourseId(@Param("courseId") UUID courseId);
    
    @Query("SELECT a FROM Assignment a WHERE a.courseId = :courseId AND a.moduleId = :moduleId AND a.status = 'ACTIVE'")
    List<Assignment> findActiveAssignmentsByCourseIdAndModuleId(@Param("courseId") UUID courseId, @Param("moduleId") UUID moduleId);
}








