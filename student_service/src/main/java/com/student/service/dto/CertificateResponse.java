package com.student.service.dto;

import com.student.service.entity.Certificate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    
    private UUID id;
    private UUID studentId;
    private Long courseId;
    private String certificateNumber;
    private String courseName;
    private String studentName;
    private LocalDateTime completedAt;
    private LocalDateTime issuedAt;
    private Double finalScore;
    private String grade;
    private String filePath;
    private String verificationCode;
    private Certificate.CertificateStatus status;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}








