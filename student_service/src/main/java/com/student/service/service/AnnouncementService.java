package com.student.service.service;

import com.student.service.dto.AnnouncementResponse;
import com.student.service.entity.Announcement;
import com.student.service.entity.Student;
import com.student.service.exception.ResourceNotFoundException;
import com.student.service.repository.AnnouncementRepository;
import com.student.service.repository.StudentRepository;
import com.student.service.repository.EnrollmentRepository;
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
@Transactional(readOnly = true)
public class AnnouncementService {
    
    private final AnnouncementRepository announcementRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    
    public List<AnnouncementResponse> getAnnouncements(UUID studentId) {
        log.info("Fetching announcements for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Get all course IDs the student is enrolled in
        List<UUID> enrolledCourseIds = enrollmentRepository.findActiveEnrollmentsByStudentId(studentId)
                .stream()
                .map(enrollment -> enrollment.getCourseId())
                .collect(Collectors.toList());
        
        if (enrolledCourseIds.isEmpty()) {
            return List.of();
        }
        
        // Get active announcements for enrolled courses
        List<Announcement> announcements = announcementRepository
                .findActiveAnnouncementsByCourseIds(enrolledCourseIds, LocalDateTime.now());
        
        return announcements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<AnnouncementResponse> getAnnouncementsForCourse(UUID studentId, UUID courseId) {
        log.info("Fetching announcements for student {} in course {}", studentId, courseId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Verify student is enrolled in the course
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new ResourceNotFoundException("Student is not enrolled in this course");
        }
        
        List<Announcement> announcements = announcementRepository
                .findActiveAnnouncementsByCourseId(courseId, LocalDateTime.now());
        
        return announcements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private AnnouncementResponse mapToResponse(Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId().toString());
        response.setCourseId(announcement.getCourseId().toString());
        response.setInstructorId(announcement.getInstructorId().toString());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setType(announcement.getType().name());
        response.setStatus(announcement.getStatus().name());
        response.setPublishedAt(announcement.getPublishedAt());
        response.setExpiresAt(announcement.getExpiresAt());
        response.setIsImportant(announcement.getIsImportant());
        response.setIsPinned(announcement.getIsPinned());
        response.setCreatedAt(announcement.getCreatedAt());
        response.setUpdatedAt(announcement.getUpdatedAt());
        return response;
    }
}








