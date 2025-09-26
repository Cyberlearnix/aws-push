package com.student.service.dto;

import com.student.service.entity.AssignmentSubmission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionResponse {
    
    private Long id;
    private Long studentId;
    private Long assignmentId;
    private String assignmentTitle;
    private AssignmentSubmission.SubmissionStatus status;
    private Integer attemptNumber;
    private String submissionText;
    private String fileName;
    private Long fileSize;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private Double score;
    private Double percentage;
    private String grade;
    private String feedback;
    private Boolean isLate;
    private Boolean isPlagiarized;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}








