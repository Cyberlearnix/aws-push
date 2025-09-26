package com.student.service.dto;

import com.student.service.entity.QuizSubmission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionResponse {
    
    private Long id;
    private Long studentId;
    private Long quizId;
    private String quizTitle;
    private QuizSubmission.SubmissionStatus status;
    private Integer attemptNumber;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Integer timeSpentMinutes;
    private Double score;
    private Double percentage;
    private Boolean isPassed;
    private String feedback;
    private List<QuizAnswerResponse> answers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}








