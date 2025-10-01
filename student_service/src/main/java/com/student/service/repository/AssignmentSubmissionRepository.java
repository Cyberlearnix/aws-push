package com.student.service.repository;

import com.student.service.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {
    
    List<AssignmentSubmission> findByStudentId(UUID studentId);
    
    List<AssignmentSubmission> findByStudentIdAndAssignmentCourseId(UUID studentId, UUID courseId);
    
    Optional<AssignmentSubmission> findByStudentIdAndAssignmentId(UUID studentId, UUID assignmentId);
    
    @Query("SELECT as FROM AssignmentSubmission as WHERE as.student.id = :studentId AND as.assignment.courseId = :courseId")
    List<AssignmentSubmission> findAssignmentSubmissionsByStudentIdAndCourseId(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);
    
    @Query("SELECT as FROM AssignmentSubmission as WHERE as.student.id = :studentId AND as.assignment.id = :assignmentId ORDER BY as.attemptNumber DESC")
    List<AssignmentSubmission> findAssignmentSubmissionsByStudentIdAndAssignmentIdOrderByAttempt(@Param("studentId") UUID studentId, @Param("assignmentId") UUID assignmentId);
}








