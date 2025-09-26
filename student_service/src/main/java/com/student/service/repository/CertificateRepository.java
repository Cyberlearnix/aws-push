package com.student.service.repository;

import com.student.service.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    List<Certificate> findByStudentId(Long studentId);
    
    Optional<Certificate> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
    
    Optional<Certificate> findByVerificationCode(String verificationCode);
    
    @Query("SELECT c FROM Certificate c WHERE c.student.id = :studentId AND c.status = 'ACTIVE'")
    List<Certificate> findActiveCertificatesByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT c FROM Certificate c WHERE c.courseId = :courseId AND c.status = 'ACTIVE'")
    List<Certificate> findActiveCertificatesByCourseId(@Param("courseId") Long courseId);
}








