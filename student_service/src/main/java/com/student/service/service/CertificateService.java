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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CertificateService {
    
    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    
    public List<CertificateResponse> getCertificates(UUID studentId) {
        log.info("Fetching certificates for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<Certificate> certificates = certificateRepository.findActiveCertificatesByStudentId(studentId);
        
        return certificates.stream()
                .map(certificate -> mapToResponse(certificate, studentId))
                .collect(Collectors.toList());
    }
    
    public CertificateResponse getCertificate(UUID studentId, UUID certificateId) {
        log.info("Fetching certificate {} for student {}", certificateId, studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));
        
        // Verify that the certificate belongs to the student
        if (!certificate.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Certificate not found");
        }
        
        return mapToResponse(certificate, studentId);
    }
    
    public CertificateResponse verifyCertificate(String verificationCode) {
        log.info("Verifying certificate with code {}", verificationCode);
        
        Certificate certificate = certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found or invalid verification code"));
        
        if (certificate.getStatus() != Certificate.CertificateStatus.ACTIVE) {
            throw new ResourceNotFoundException("Certificate is not active");
        }
        
        return mapToResponse(certificate, certificate.getStudent().getId());
    }
    
    private CertificateResponse mapToResponse(Certificate certificate, UUID studentId) {
        CertificateResponse response = new CertificateResponse();
        response.setId(certificate.getId());
        response.setStudentId(studentId);
        response.setCourseId(certificate.getCourseId());
        response.setCertificateNumber(certificate.getCertificateNumber());
        response.setCourseName(certificate.getCourseName());
        response.setStudentName(certificate.getStudentName());
        response.setCompletedAt(certificate.getCompletedAt());
        response.setIssuedAt(certificate.getIssuedAt());
        response.setFinalScore(certificate.getFinalScore());
        response.setGrade(certificate.getGrade());
        response.setFilePath(certificate.getFilePath());
        response.setVerificationCode(certificate.getVerificationCode());
        response.setStatus(certificate.getStatus());
        response.setMetadata(certificate.getMetadata());
        response.setCreatedAt(certificate.getCreatedAt());
        response.setUpdatedAt(certificate.getUpdatedAt());
        return response;
    }
}








