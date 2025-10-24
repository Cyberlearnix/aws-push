package com.student.service.service;

import com.student.service.dto.QuizSubmissionRequest;
import com.student.service.dto.QuizSubmissionResponse;
import com.student.service.dto.QuizAnswerResponse;
import com.student.service.entity.*;
import com.student.service.exception.ResourceNotFoundException;
import com.student.service.exception.ConflictException;
import com.student.service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizService {
    
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final StudentRepository studentRepository;
    
    @Transactional(readOnly = true)
    public List<Quiz> getQuizzesForModule(UUID studentId, Long courseId, Long moduleId) {
        log.info("Fetching quizzes for student {} in course {} module {}", studentId, courseId, moduleId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        return quizRepository.findActiveQuizzesByCourseIdAndModuleId(courseId, moduleId);
    }
    
    public QuizSubmissionResponse submitQuiz(UUID studentId, Long courseId, Long moduleId, Long quizId, QuizSubmissionRequest request) {
        log.info("Submitting quiz {} for student {} in course {} module {}", quizId, studentId, courseId, moduleId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        
        // Check if quiz belongs to the specified course and module
        if (!quiz.getCourseId().equals(courseId) || !quiz.getModuleId().equals(moduleId)) {
            throw new ResourceNotFoundException("Quiz not found in specified course/module");
        }
        
        // Check max attempts
        Integer maxAttempts = quizSubmissionRepository.findMaxAttemptNumberByStudentIdAndQuizId(studentId, quizId);
        int nextAttempt = (maxAttempts != null ? maxAttempts : 0) + 1;
        
        if (nextAttempt > quiz.getMaxAttempts()) {
            throw new ConflictException("Maximum attempts exceeded for this quiz");
        }
        
        // Create quiz submission
        QuizSubmission submission = new QuizSubmission();
        submission.setStudent(student);
        submission.setQuiz(quiz);
        submission.setStatus(QuizSubmission.SubmissionStatus.SUBMITTED);
        submission.setAttemptNumber(nextAttempt);
        submission.setStartedAt(LocalDateTime.now().minusMinutes(quiz.getTimeLimitMinutes() != null ? quiz.getTimeLimitMinutes() : 60));
        submission.setSubmittedAt(LocalDateTime.now());
        
        QuizSubmission savedSubmission = quizSubmissionRepository.save(submission);
        
        // Process answers and calculate score
        double totalScore = 0.0;
        double totalPoints = 0.0;
        
        for (Map.Entry<Long, String> entry : request.getAnswers().entrySet()) {
            Long questionId = entry.getKey();
            String answer = entry.getValue();
            
            QuizQuestion question = quizQuestionRepository.findById(questionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
            
            QuizAnswer quizAnswer = new QuizAnswer();
            quizAnswer.setSubmission(savedSubmission);
            quizAnswer.setQuestion(question);
            quizAnswer.setAnswerText(answer);
            
            // Calculate score for this answer
            double pointsEarned = calculateAnswerScore(question, answer);
            quizAnswer.setPointsEarned(pointsEarned);
            quizAnswer.setIsCorrect(pointsEarned > 0);
            
            totalScore += pointsEarned;
            totalPoints += question.getPoints();
            
            quizAnswerRepository.save(quizAnswer);
        }
        
        // Update submission with final score
        double percentage = totalPoints > 0 ? (totalScore / totalPoints) * 100 : 0.0;
        savedSubmission.setScore(totalScore);
        savedSubmission.setPercentage(percentage);
        savedSubmission.setIsPassed(percentage >= quiz.getPassingScore());
        
        quizSubmissionRepository.save(savedSubmission);
        
        log.info("Successfully submitted quiz {} for student {} (attempt {})", quizId, studentId, nextAttempt);
        
        return mapToResponse(savedSubmission, studentId);
    }
    
    @Transactional(readOnly = true)
    public List<QuizSubmissionResponse> getQuizResults(UUID studentId, Long courseId) {
        log.info("Fetching quiz results for student {} in course {}", studentId, courseId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<QuizSubmission> submissions = quizSubmissionRepository.findQuizSubmissionsByStudentIdAndCourseId(studentId, courseId);
        
        return submissions.stream()
                .map(submission -> mapToResponse(submission, studentId))
                .collect(Collectors.toList());
    }
    
    private double calculateAnswerScore(QuizQuestion question, String answer) {
        if (question.getType() == QuizQuestion.QuestionType.MULTIPLE_CHOICE) {
            // Check if selected option is correct
            return question.getOptions().stream()
                    .filter(option -> option.getOptionValue().equals(answer) && option.getIsCorrect())
                    .findFirst()
                    .map(option -> question.getPoints())
                    .orElse(0.0);
        } else if (question.getType() == QuizQuestion.QuestionType.TRUE_FALSE) {
            return answer.equalsIgnoreCase(question.getCorrectAnswer()) ? question.getPoints() : 0.0;
        } else if (question.getType() == QuizQuestion.QuestionType.FILL_IN_BLANK) {
            return answer.equalsIgnoreCase(question.getCorrectAnswer()) ? question.getPoints() : 0.0;
        }
        // For essay questions, manual grading is required
        return 0.0;
    }
    
    private QuizSubmissionResponse mapToResponse(QuizSubmission submission, UUID studentId) {
        QuizSubmissionResponse response = new QuizSubmissionResponse();
        response.setId(submission.getId());
        response.setStudentId(studentId);
        response.setQuizId(submission.getQuiz().getId());
        response.setQuizTitle(submission.getQuiz().getTitle());
        response.setStatus(submission.getStatus());
        response.setAttemptNumber(submission.getAttemptNumber());
        response.setStartedAt(submission.getStartedAt());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setTimeSpentMinutes(submission.getTimeSpentMinutes());
        response.setScore(submission.getScore());
        response.setPercentage(submission.getPercentage());
        response.setIsPassed(submission.getIsPassed());
        response.setFeedback(submission.getFeedback());
        response.setCreatedAt(submission.getCreatedAt());
        response.setUpdatedAt(submission.getUpdatedAt());
        
        // Map answers
        List<QuizAnswerResponse> answerResponses = submission.getAnswers().stream()
                .map(this::mapAnswerToResponse)
                .collect(Collectors.toList());
        response.setAnswers(answerResponses);
        
        return response;
    }
    
    private QuizAnswerResponse mapAnswerToResponse(QuizAnswer answer) {
        QuizAnswerResponse response = new QuizAnswerResponse();
        response.setId(answer.getId());
        response.setQuestionId(answer.getQuestion().getId());
        response.setQuestionText(answer.getQuestion().getQuestionText());
        response.setAnswerText(answer.getAnswerText());
        response.setSelectedOption(answer.getSelectedOption());
        response.setPointsEarned(answer.getPointsEarned());
        response.setIsCorrect(answer.getIsCorrect());
        response.setFeedback(answer.getFeedback());
        response.setCreatedAt(answer.getCreatedAt());
        return response;
    }
}








