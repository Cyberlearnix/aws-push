package com.student.service.repository;

import com.student.service.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    
    List<Enrollment> findByStudentId(UUID studentId);
    
    Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
    
    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = com.student.service.entity.Enrollment$EnrollmentStatus.ENROLLED")
    List<Enrollment> findActiveEnrollmentsByStudentId(@Param("studentId") UUID studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.courseId = :courseId AND e.status = com.student.service.entity.Enrollment$EnrollmentStatus.ENROLLED")
    List<Enrollment> findActiveEnrollmentsByCourseId(@Param("courseId") UUID courseId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId AND e.status = com.student.service.entity.Enrollment$EnrollmentStatus.ENROLLED")
    Long countActiveEnrollmentsByCourseId(@Param("courseId") UUID courseId);
}








