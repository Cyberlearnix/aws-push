package com.student.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionRequest {
    
    @NotNull(message = "Quiz ID is required")
    private Long quizId;
    
    @NotEmpty(message = "Answers are required")
    private Map<Long, String> answers; // questionId -> answer
    
    private String notes;
}








