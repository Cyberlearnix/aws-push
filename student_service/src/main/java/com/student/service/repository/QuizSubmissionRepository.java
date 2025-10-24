package com.student.service.repository;

import com.student.service.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    List<QuizSubmission> findByStudentId(UUID studentId);

    List<QuizSubmission> findByStudentIdAndQuizCourseId(UUID studentId, Long courseId);

    Optional<QuizSubmission> findByStudentIdAndQuizId(UUID studentId, Long quizId);

    @Query("SELECT qs FROM QuizSubmission qs WHERE qs.student.id = :studentId AND qs.quiz.courseId = :courseId")
    List<QuizSubmission> findQuizSubmissionsByStudentIdAndCourseId(@Param("studentId") UUID studentId, @Param("courseId") Long courseId);

    @Query("SELECT qs FROM QuizSubmission qs WHERE qs.student.id = :studentId AND qs.quiz.id = :quizId ORDER BY qs.attemptNumber DESC")
    List<QuizSubmission> findQuizSubmissionsByStudentIdAndQuizIdOrderByAttempt(@Param("studentId") UUID studentId, @Param("quizId") Long quizId);

    @Query("SELECT MAX(qs.attemptNumber) FROM QuizSubmission qs WHERE qs.student.id = :studentId AND qs.quiz.id = :quizId")
    Integer findMaxAttemptNumberByStudentIdAndQuizId(@Param("studentId") UUID studentId, @Param("quizId") Long quizId);
}



