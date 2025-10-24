package com.student.service.service;

import com.student.service.dto.*;
import com.student.service.entity.Student;
import com.student.service.exception.ResourceNotFoundException;
import com.student.service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {
    
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CertificateRepository certificateRepository;
    private final MessageRepository messageRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuizRepository quizRepository;
    private final AnnouncementRepository announcementRepository;
    
    public DashboardResponse getDashboard(UUID studentId) {
        log.info("Fetching dashboard for student {}", studentId);

        // No need to validate student exists in database since JWT already validated authentication
        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setStudentId(studentId);
        // Student name and email will be set by the controller from JWT data

        // Get enrollment statistics
        List<com.student.service.entity.Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
        dashboard.setTotalEnrolledCourses(enrollments.size());
        dashboard.setCompletedCourses((int) enrollments.stream()
                .filter(e -> e.getStatus() == com.student.service.entity.Enrollment.EnrollmentStatus.COMPLETED)
                .count());
        dashboard.setActiveCourses((int) enrollments.stream()
                .filter(e -> e.getStatus() == com.student.service.entity.Enrollment.EnrollmentStatus.ENROLLED)
                .count());

        // Calculate overall progress
        double overallProgress = enrollments.stream()
                .mapToDouble(e -> e.getProgressPercentage() != null ? e.getProgressPercentage() : 0.0)
                .average()
                .orElse(0.0);
        dashboard.setOverallProgress(overallProgress);

        // Get certificate count
        dashboard.setTotalCertificates(certificateRepository.findActiveCertificatesByStudentId(studentId).size());

        // Get unread messages count
        dashboard.setUnreadMessages(messageRepository.countUnreadMessagesByStudentId(studentId).intValue());

        // Get pending assignments (this would need more complex logic based on due dates)
        dashboard.setPendingAssignments(0); // Placeholder

        // Get upcoming quizzes (this would need more complex logic based on availability dates)
        dashboard.setUpcomingQuizzes(0); // Placeholder

        // Get recent enrollments (last 5)
        List<EnrollmentResponse> recentEnrollments = enrollments.stream()
                .limit(5)
                .map(enrollment -> mapEnrollmentToResponse(enrollment, studentId))
                .collect(Collectors.toList());
        dashboard.setRecentEnrollments(recentEnrollments);

        // Get recent certificates (last 5)
        List<CertificateResponse> recentCertificates = certificateRepository.findActiveCertificatesByStudentId(studentId)
                .stream()
                .limit(5)
                .map(certificate -> mapCertificateToResponse(certificate, studentId))
                .collect(Collectors.toList());
        dashboard.setRecentCertificates(recentCertificates);

        // Get recent announcements (last 5)
        List<Long> enrolledCourseIds = enrollments.stream()
                .map(e -> e.getCourseId())
                .collect(Collectors.toList());

        List<AnnouncementResponse> recentAnnouncements = announcementRepository
                .findActiveAnnouncementsByCourseIds(enrolledCourseIds, java.time.LocalDateTime.now())
                .stream()
                .limit(5)
                .map(this::mapAnnouncementToResponse)
                .collect(Collectors.toList());
        dashboard.setRecentAnnouncements(recentAnnouncements);

        // Get recent messages (last 5)
        List<MessageResponse> recentMessages = messageRepository.findByStudentId(studentId)
                .stream()
                .limit(5)
                .map(message -> mapMessageToResponse(message, studentId))
                .collect(Collectors.toList());
        dashboard.setRecentMessages(recentMessages);

        log.info("Successfully fetched dashboard for student {}", studentId);
        return dashboard;
    }
    
    public StatsResponse getStats(UUID studentId) {
        log.info("Fetching stats for student {}", studentId);

        // No need to validate student exists in database since JWT already validated authentication
        StatsResponse stats = new StatsResponse();
        stats.setStudentId(studentId);
        // Student name will be set by the controller from JWT data

        // Get enrollment statistics
        List<com.student.service.entity.Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
        stats.setTotalCoursesEnrolled(enrollments.size());
        stats.setTotalCoursesCompleted((int) enrollments.stream()
                .filter(e -> e.getStatus() == com.student.service.entity.Enrollment.EnrollmentStatus.COMPLETED)
                .count());

        // Get certificate count
        stats.setTotalCertificatesEarned(certificateRepository.findActiveCertificatesByStudentId(studentId).size());

        // Get quiz and assignment statistics (these would need more complex queries)
        stats.setTotalQuizzesTaken(0); // Placeholder
        stats.setTotalAssignmentsSubmitted(0); // Placeholder
        stats.setAverageQuizScore(0.0); // Placeholder
        stats.setAverageAssignmentScore(0.0); // Placeholder

        // Get time spent statistics
        stats.setTotalTimeSpentMinutes(0); // Placeholder
        stats.setTotalTimeSpentHours(0); // Placeholder

        // Get dates
        if (!enrollments.isEmpty()) {
            stats.setFirstEnrollmentDate(enrollments.stream()
                    .map(e -> e.getEnrolledAt())
                    .min(java.time.LocalDateTime::compareTo)
                    .orElse(null));
            stats.setLastActivityDate(enrollments.stream()
                    .map(e -> e.getUpdatedAt())
                    .max(java.time.LocalDateTime::compareTo)
                    .orElse(null));
        }

        log.info("Successfully fetched stats for student {}", studentId);
        return stats;
    }
    
    private EnrollmentResponse mapEnrollmentToResponse(com.student.service.entity.Enrollment enrollment, UUID studentId) {
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
    
    private CertificateResponse mapCertificateToResponse(com.student.service.entity.Certificate certificate, UUID studentId) {
        CertificateResponse response = new CertificateResponse();
        response.setId(certificate.getId());
        response.setStudentId(studentId);
        response.setCourseId(certificate.getCourseId());
        response.setCertificateNumber(certificate.getCertificateNumber());
        response.setCourseName(certificate.getCourseName());
        response.setStudentName(certificate.getStudentName());
        response.setCompletedAt(certificate.getCompletedAt());
        response.setIssuedAt(certificate.getIssuedAt());
        response.setFinalScore(certificate.getFinalScore());
        response.setGrade(certificate.getGrade());
        response.setFilePath(certificate.getFilePath());
        response.setVerificationCode(certificate.getVerificationCode());
        response.setStatus(certificate.getStatus());
        response.setMetadata(certificate.getMetadata());
        response.setCreatedAt(certificate.getCreatedAt());
        response.setUpdatedAt(certificate.getUpdatedAt());
        return response;
    }
    
    private AnnouncementResponse mapAnnouncementToResponse(com.student.service.entity.Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId());
        response.setCourseId(announcement.getCourseId());
        response.setInstructorId(announcement.getInstructorId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setType(announcement.getType());
        response.setStatus(announcement.getStatus());
        response.setPublishedAt(announcement.getPublishedAt());
        response.setExpiresAt(announcement.getExpiresAt());
        response.setIsImportant(announcement.getIsImportant());
        response.setIsPinned(announcement.getIsPinned());
        response.setCreatedAt(announcement.getCreatedAt());
        response.setUpdatedAt(announcement.getUpdatedAt());
        return response;
    }
    
    private MessageResponse mapMessageToResponse(com.student.service.entity.Message message, UUID studentId) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setStudentId(studentId);
        response.setInstructorId(message.getInstructorId());
        response.setCourseId(message.getCourseId());
        response.setSubject(message.getSubject());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setStatus(message.getStatus());
        response.setReadAt(message.getReadAt());
        response.setRepliedAt(message.getRepliedAt());
        response.setParentMessageId(message.getParentMessageId());
        response.setAttachmentPath(message.getAttachmentPath());
        response.setAttachmentName(message.getAttachmentName());
        response.setCreatedAt(message.getCreatedAt());
        response.setUpdatedAt(message.getUpdatedAt());
        return response;
    }
}








