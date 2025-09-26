package com.student.service.repository;

import com.student.service.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    
    List<QuizQuestion> findByQuizId(Long quizId);
    
    List<QuizQuestion> findByQuizIdOrderByOrderIndex(Long quizId);
}








