package com.student.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerResponse {
    
    private Long id;
    private Long questionId;
    private String questionText;
    private String answerText;
    private String selectedOption;
    private Double pointsEarned;
    private Boolean isCorrect;
    private String feedback;
    private LocalDateTime createdAt;
}








