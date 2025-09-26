package com.student.service.service;

import com.student.service.dto.ProgressRequest;
import com.student.service.dto.ProgressResponse;
import com.student.service.entity.Progress;
import com.student.service.entity.Student;
import com.student.service.exception.ResourceNotFoundException;
import com.student.service.repository.ProgressRepository;
import com.student.service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProgressService {
    
    private final ProgressRepository progressRepository;
    private final StudentRepository studentRepository;
    
    @Transactional(readOnly = true)
    public List<ProgressResponse> getCourseProgress(Long studentId, Long courseId) {
        log.info("Fetching progress for student {} in course {}", studentId, courseId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<Progress> progressList = progressRepository.findByStudentIdAndCourseId(studentId, courseId);
        
        return progressList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public ProgressResponse updateProgress(Long studentId, Long courseId, ProgressRequest request) {
        log.info("Updating progress for student {} in course {}", studentId, courseId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        Progress progress;
        
        if (request.getModuleId() != null && request.getLessonId() != null) {
            // Lesson progress
            progress = progressRepository.findByStudentIdAndCourseIdAndModuleIdAndLessonId(
                    studentId, courseId, request.getModuleId(), request.getLessonId())
                    .orElse(createNewProgress(studentId, courseId, request));
        } else if (request.getModuleId() != null) {
            // Module progress
            progress = progressRepository.findByStudentIdAndCourseIdAndModuleId(
                    studentId, courseId, request.getModuleId())
                    .orElse(createNewProgress(studentId, courseId, request));
        } else {
            // Course progress
            progress = progressRepository.findByStudentIdAndCourseIdAndModuleIdAndLessonId(
                    studentId, courseId, null, null)
                    .orElse(createNewProgress(studentId, courseId, request));
        }
        
        // Update progress fields
        progress.setStatus(request.getStatus());
        progress.setCompletionPercentage(request.getCompletionPercentage());
        progress.setTimeSpentMinutes(request.getTimeSpentMinutes());
        progress.setNotes(request.getNotes());
        progress.setLastAccessedAt(LocalDateTime.now());
        
        if (request.getStatus() == Progress.ProgressStatus.IN_PROGRESS && progress.getStartedAt() == null) {
            progress.setStartedAt(LocalDateTime.now());
        }
        
        if (request.getStatus() == Progress.ProgressStatus.COMPLETED) {
            progress.setCompletedAt(LocalDateTime.now());
        }
        
        Progress savedProgress = progressRepository.save(progress);
        log.info("Successfully updated progress for student {} in course {}", studentId, courseId);
        
        return mapToResponse(savedProgress);
    }
    
    @Transactional(readOnly = true)
    public List<ProgressResponse> getOverallProgress(Long studentId) {
        log.info("Fetching overall progress for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<Progress> progressList = progressRepository.findCourseProgressByStudentId(studentId);
        
        return progressList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private Progress createNewProgress(Long studentId, Long courseId, ProgressRequest request) {
        Progress progress = new Progress();
        progress.setStudent(studentRepository.findById(studentId).orElseThrow());
        progress.setCourseId(courseId);
        progress.setModuleId(request.getModuleId());
        progress.setLessonId(request.getLessonId());
        progress.setType(request.getType());
        progress.setStatus(request.getStatus());
        progress.setCompletionPercentage(request.getCompletionPercentage() != null ? request.getCompletionPercentage() : 0.0);
        progress.setTimeSpentMinutes(request.getTimeSpentMinutes() != null ? request.getTimeSpentMinutes() : 0);
        progress.setNotes(request.getNotes());
        progress.setLastAccessedAt(LocalDateTime.now());
        
        if (request.getStatus() == Progress.ProgressStatus.IN_PROGRESS) {
            progress.setStartedAt(LocalDateTime.now());
        }
        
        if (request.getStatus() == Progress.ProgressStatus.COMPLETED) {
            progress.setCompletedAt(LocalDateTime.now());
        }
        
        return progress;
    }
    
    private ProgressResponse mapToResponse(Progress progress) {
        ProgressResponse response = new ProgressResponse();
        response.setId(progress.getId());
        response.setStudentId(progress.getStudent().getId());
        response.setCourseId(progress.getCourseId());
        response.setModuleId(progress.getModuleId());
        response.setLessonId(progress.getLessonId());
        response.setType(progress.getType());
        response.setStatus(progress.getStatus());
        response.setCompletionPercentage(progress.getCompletionPercentage());
        response.setTimeSpentMinutes(progress.getTimeSpentMinutes());
        response.setStartedAt(progress.getStartedAt());
        response.setCompletedAt(progress.getCompletedAt());
        response.setLastAccessedAt(progress.getLastAccessedAt());
        response.setNotes(progress.getNotes());
        response.setCreatedAt(progress.getCreatedAt());
        response.setUpdatedAt(progress.getUpdatedAt());
        return response;
    }
}








