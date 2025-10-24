package com.student.service.repository;

import com.student.service.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    List<Progress> findByStudentId(UUID studentId);

    List<Progress> findByStudentIdAndCourseId(UUID studentId, Long courseId);

    Optional<Progress> findByStudentIdAndCourseIdAndModuleId(UUID studentId, Long courseId, Long moduleId);

    Optional<Progress> findByStudentIdAndCourseIdAndModuleIdAndLessonId(UUID studentId, Long courseId, Long moduleId, Long lessonId);

    @Query("SELECT p FROM Progress p WHERE p.student.id = :studentId AND p.type = 'COURSE'")
    List<Progress> findCourseProgressByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT p FROM Progress p WHERE p.student.id = :studentId AND p.courseId = :courseId AND p.type = 'MODULE'")
    List<Progress> findModuleProgressByStudentIdAndCourseId(@Param("studentId") UUID studentId, @Param("courseId") Long courseId);

    @Query("SELECT AVG(p.completionPercentage) FROM Progress p WHERE p.student.id = :studentId AND p.type = 'COURSE'")
    Double getAverageCourseProgressByStudentId(@Param("studentId") UUID studentId);
}



