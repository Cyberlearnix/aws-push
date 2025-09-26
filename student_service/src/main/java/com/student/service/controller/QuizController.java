package com.student.service.controller;

import com.student.service.dto.QuizSubmissionRequest;
import com.student.service.dto.QuizSubmissionResponse;
import com.student.service.entity.Quiz;
import com.student.service.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students/{id}")
@RequiredArgsConstructor
@Slf4j
public class QuizController {
    
    private final QuizService quizService;
    
    @GetMapping("/courses/{courseId}/modules/{moduleId}/quiz")
    public ResponseEntity<List<Quiz>> getQuizzesForModule(
            @PathVariable Long id,
            @PathVariable Long courseId,
            @PathVariable Long moduleId) {
        
        log.info("Fetching quizzes for student {} in course {} module {}", id, courseId, moduleId);
        
        List<Quiz> response = quizService.getQuizzesForModule(id, courseId, moduleId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/courses/{courseId}/modules/{moduleId}/quiz/{quizId}/submit")
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable Long id,
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmissionRequest request) {
        
        log.info("Submitting quiz {} for student {} in course {} module {}", quizId, id, courseId, moduleId);
        
        QuizSubmissionResponse response = quizService.submitQuiz(id, courseId, moduleId, quizId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/courses/{courseId}/quizzes/results")
    public ResponseEntity<List<QuizSubmissionResponse>> getQuizResults(
            @PathVariable Long id,
            @PathVariable Long courseId) {
        
        log.info("Fetching quiz results for student {} in course {}", id, courseId);
        
        List<QuizSubmissionResponse> response = quizService.getQuizResults(id, courseId);
        return ResponseEntity.ok(response);
    }
}








