package com.student.service.service;

import com.student.service.dto.EnrollmentRequest;
import com.student.service.dto.EnrollmentResponse;
import com.student.service.entity.Enrollment;
import com.student.service.entity.Student;
import com.student.service.exception.ResourceNotFoundException;
import com.student.service.exception.ConflictException;
import com.student.service.repository.EnrollmentRepository;
import com.student.service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    
    public EnrollmentResponse enrollInCourse(UUID studentId, EnrollmentRequest request) {
        log.info("Enrolling student {} in course {}", studentId, request.getCourseId());
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Check if already enrolled
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, request.getCourseId())) {
            throw new ConflictException("Student is already enrolled in this course");
        }
        
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourseId(request.getCourseId());
        enrollment.setStatus(Enrollment.EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgressPercentage(0.0);
        enrollment.setCompletedModules(0);
        enrollment.setCompletedLessons(0);
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Successfully enrolled student {} in course {}", studentId, request.getCourseId());
        
        return mapToResponse(savedEnrollment, studentId);
    }
    
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrolledCourses(UUID studentId) {
        log.info("Fetching enrolled courses for student {}", studentId);

        // No need to validate student exists in database since JWT already validated authentication
        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);

        return enrollments.stream()
                .map(enrollment -> mapToResponse(enrollment, studentId))
                .collect(Collectors.toList());
    }
    
    public void unenrollFromCourse(UUID studentId, Long courseId) {
        log.info("Unenrolling student {} from course {}", studentId, courseId);

        // No need to validate student exists in database since JWT already validated authentication
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        enrollment.setStatus(Enrollment.EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);

        log.info("Successfully unenrolled student {} from course {}", studentId, courseId);
    }
    
    private EnrollmentResponse mapToResponse(Enrollment enrollment, UUID studentId) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setStudentId(studentId);
        response.setCourseId(enrollment.getCourseId());
        response.setStatus(enrollment.getStatus());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setCompletedAt(enrollment.getCompletedAt());
        response.setProgressPercentage(enrollment.getProgressPercentage());
        response.setTotalModules(enrollment.getTotalModules());
        response.setCompletedModules(enrollment.getCompletedModules());
        response.setTotalLessons(enrollment.getTotalLessons());
        response.setCompletedLessons(enrollment.getCompletedLessons());
        response.setCreatedAt(enrollment.getCreatedAt());
        response.setUpdatedAt(enrollment.getUpdatedAt());
        return response;
    }
}








