package com.student.service.service;

import com.student.service.dto.CertificateResponse;
import com.student.service.entity.Certificate;
import com.student.service.entity.Student;
import com.student.service.exception.ResourceNotFoundException;
import com.student.service.repository.CertificateRepository;
import com.student.service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CertificateService {
    
    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    
    @Transactional(readOnly = true)
    public List<CertificateResponse> getCertificates(UUID studentId) {
        log.info("Fetching certificates for student {}", studentId);
        
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        
        return certificateRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CertificateResponse getCertificate(UUID studentId, UUID certificateId) {
        log.info("Fetching certificate {} for student {}", certificateId, studentId);
        
        // Verify student exists
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        
        return certificateRepository.findById(certificateId)
                .map(certificate -> {
                    // Verify certificate belongs to the student
                    if (!certificate.getStudent().getId().equals(studentId)) {
                        throw new ResourceNotFoundException("Certificate not found for the given student");
                    }
                    return mapToResponse(certificate);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));
    }
    
    @Transactional
    public CertificateResponse generateCertificate(UUID studentId, UUID courseId, String courseName) {
        log.info("Generating certificate for student {} and course {}", studentId, courseId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Check if certificate already exists
        Optional<Certificate> existingCertificate = certificateRepository
                .findByStudentIdAndCourseId(studentId, courseId);
                
        if (existingCertificate.isPresent()) {
            return mapToResponse(existingCertificate.get());
        }
        
        // Create new certificate
        Certificate certificate = new Certificate();
        certificate.setStudent(student);
        certificate.setCourseId(courseId);
        certificate.setCourseName(courseName);
        certificate.setStudentName(student.getFirstName() + " " + student.getLastName());
        certificate.setStatus(Certificate.CertificateStatus.ACTIVE);
        certificate.setIssuedAt(LocalDateTime.now());
        certificate.setCertificateNumber("CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        certificate.setVerificationCode(UUID.randomUUID().toString());
        
        Certificate savedCertificate = certificateRepository.save(certificate);
        return mapToResponse(savedCertificate);
    }
    
    public CertificateResponse verifyCertificate(String verificationCode) {
        log.info("Verifying certificate with code {}", verificationCode);
        
        Certificate certificate = certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found or invalid verification code"));
        
        if (certificate.getStatus() != Certificate.CertificateStatus.ACTIVE) {
            throw new ResourceNotFoundException("Certificate is not active");
        }
        
        return mapToResponse(certificate);
    }
    
    private CertificateResponse mapToResponse(Certificate certificate) {
        CertificateResponse response = new CertificateResponse();
        response.setId(certificate.getId() != null ? certificate.getId().toString() : null);
        response.setStudentId(certificate.getStudent() != null && certificate.getStudent().getId() != null ? 
            certificate.getStudent().getId().toString() : null);
        response.setCourseId(certificate.getCourseId() != null ? certificate.getCourseId().toString() : null);
        response.setCertificateNumber(certificate.getCertificateNumber());
        response.setCourseName(certificate.getCourseName());
        response.setStudentName(certificate.getStudentName());
        response.setCompletedAt(certificate.getCompletedAt());
        response.setIssuedAt(certificate.getIssuedAt());
        response.setFinalScore(certificate.getFinalScore());
        response.setGrade(certificate.getGrade());
        response.setFilePath(certificate.getFilePath());
        response.setVerificationCode(certificate.getVerificationCode());
        response.setStatus(certificate.getStatus() != null ? certificate.getStatus().name() : null);
        response.setMetadata(certificate.getMetadata());
        response.setCreatedAt(certificate.getCreatedAt());
        response.setUpdatedAt(certificate.getUpdatedAt());
        return response;
    }
}








