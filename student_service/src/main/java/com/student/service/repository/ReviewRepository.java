package com.student.service.repository;

import com.student.service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByStudentId(Long studentId);
    
    List<Review> findByCourseId(Long courseId);
    
    Optional<Review> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    @Query("SELECT r FROM Review r WHERE r.courseId = :courseId AND r.status = 'ACTIVE'")
    List<Review> findActiveReviewsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT r FROM Review r WHERE r.student.id = :studentId AND r.status = 'ACTIVE'")
    List<Review> findActiveReviewsByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId AND r.status = 'ACTIVE'")
    Double getAverageRatingByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.courseId = :courseId AND r.status = 'ACTIVE'")
    Long getReviewCountByCourseId(@Param("courseId") Long courseId);
}








