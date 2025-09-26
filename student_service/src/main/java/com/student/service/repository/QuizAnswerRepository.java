package com.student.service.repository;

import com.student.service.entity.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    
    List<QuizAnswer> findBySubmissionId(Long submissionId);
    
    List<QuizAnswer> findByQuestionId(Long questionId);
}








