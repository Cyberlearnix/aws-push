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
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    
    public EnrollmentResponse getEnrollmentDetails(UUID studentId, UUID courseId) {
        log.info("Enrolling student {} in course {}", studentId, courseId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        
        return mapToResponse(enrollment);
    }
    
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(UUID studentId) {
        log.info("Fetching enrolled courses for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
        
        return enrollments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}








