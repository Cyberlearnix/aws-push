package com.student.service.repository;

import com.student.service.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    
    List<AssignmentSubmission> findByStudentId(Long studentId);
    
    List<AssignmentSubmission> findByStudentIdAndAssignmentCourseId(Long studentId, Long courseId);
    
    Optional<AssignmentSubmission> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);
    
    @Query("SELECT as FROM AssignmentSubmission as WHERE as.student.id = :studentId AND as.assignment.courseId = :courseId")
    List<AssignmentSubmission> findAssignmentSubmissionsByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT as FROM AssignmentSubmission as WHERE as.student.id = :studentId AND as.assignment.id = :assignmentId ORDER BY as.attemptNumber DESC")
    List<AssignmentSubmission> findAssignmentSubmissionsByStudentIdAndAssignmentIdOrderByAttempt(@Param("studentId") Long studentId, @Param("assignmentId") Long assignmentId);
}








