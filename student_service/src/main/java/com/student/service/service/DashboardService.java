package com.student.service.service;

import com.student.service.dto.*;
import com.student.service.entity.*;
import com.student.service.exception.ResourceNotFoundException;
import com.student.service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {
    
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MessageRepository messageRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuizRepository quizRepository;
    private final AnnouncementRepository announcementRepository;
    
    public DashboardResponse getDashboard(UUID studentId) {
        log.info("Fetching dashboard for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setStudentId(student.getId().toString());
        dashboard.setStudentName(student.getFirstName() + " " + student.getLastName());
        dashboard.setStudentEmail(student.getEmail());
        
        // Get enrollment statistics
        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
        dashboard.setTotalEnrolledCourses(enrollments.size());
        dashboard.setCompletedCourses((int) enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .count());
        dashboard.setActiveCourses((int) enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .count());
        
        // Calculate overall progress
        double totalProgress = enrollments.stream()
                .map(Enrollment::getProgressPercentage)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        dashboard.setOverallProgress(totalProgress);
        
        // Get pending assignments - placeholder implementation
        // List<Assignment> pendingAssignments = assignmentRepository.findByStudentIdAndStatus(
        //         studentId, Assignment.AssignmentStatus.PENDING);
        dashboard.setPendingAssignments(0); // Placeholder value
        
        // Get upcoming quizzes (next 7 days)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekFromNow = now.plusDays(7);
        // Placeholder implementation
        dashboard.setUpcomingQuizzes(0); // Placeholder value
        
        // Get recent enrollments (last 5)
        List<EnrollmentResponse> recentEnrollments = enrollments.stream()
                .sorted(Comparator.comparing(Enrollment::getEnrolledAt).reversed())
                .limit(5)
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
        dashboard.setRecentEnrollments(recentEnrollments);
        
        // Get recent certificates (last 5) - placeholder implementation
        dashboard.setRecentCertificates(new ArrayList<>()); // Empty list as placeholder
        
        Set<UUID> enrolledCourseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toSet());
                
        List<AnnouncementResponse> recentAnnouncements = !enrolledCourseIds.isEmpty() ?
                announcementRepository.findActiveAnnouncementsByCourseIds(
                        new ArrayList<>(enrolledCourseIds),
                        LocalDateTime.now()
                ).stream()
                .limit(5)
                .map(this::mapToAnnouncementResponse)
                .collect(Collectors.toList()) :
                Collections.emptyList();
                
        dashboard.setRecentAnnouncements(recentAnnouncements);
        
        // Get recent messages (last 5)
        List<MessageResponse> recentMessages = messageRepository
                .findByStudentId(studentId)
                .stream()
                .sorted(Comparator.comparing(Message::getCreatedAt).reversed())
                .limit(5)
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
                
        dashboard.setRecentMessages(recentMessages);
        
        log.info("Successfully fetched dashboard for student {}", studentId);
        return dashboard;
    }
    
    public StatsResponse getStats(UUID studentId) {
        log.info("Fetching stats for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        StatsResponse stats = new StatsResponse();
        stats.setStudentId(student.getId().toString());
        stats.setStudentName(student.getFirstName() + " " + student.getLastName());
        
        // Get enrollment statistics
        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
        stats.setTotalCoursesEnrolled(enrollments.size());
        stats.setTotalCoursesCompleted((int) enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .count());
        
        // Get certificate count - placeholder implementation
        stats.setTotalCertificatesEarned(0);
        
        // Get quiz statistics - placeholder implementation
        List<QuizSubmission> quizSubmissions = new ArrayList<>();
        stats.setTotalQuizzesTaken(quizSubmissions.size());
        
        // Calculate average quiz score
        double avgQuizScore = quizSubmissions.stream()
                .map(QuizSubmission::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        stats.setAverageQuizScore(Math.round(avgQuizScore * 10.0) / 10.0);
        
        // Get assignment statistics - placeholder implementation
        List<AssignmentSubmission> assignmentSubmissions = new ArrayList<>();
        stats.setTotalAssignmentsSubmitted(assignmentSubmissions.size());
        
        // Calculate average assignment score
        double avgAssignmentScore = assignmentSubmissions.stream()
                .map(AssignmentSubmission::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        stats.setAverageAssignmentScore(Math.round(avgAssignmentScore * 10.0) / 10.0);
        
        // Get time spent statistics (in minutes) - placeholder implementation
        int totalMinutesSpent = 0; // Placeholder value
        stats.setTotalTimeSpentMinutes(totalMinutesSpent);
        stats.setTotalTimeSpentHours(totalMinutesSpent / 60.0);
        
        // Get dates
        if (!enrollments.isEmpty()) {
            stats.setFirstEnrollmentDate(enrollments.stream()
                    .map(Enrollment::getEnrolledAt)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null));
                    
            // Get most recent activity from enrollments
            LocalDateTime lastEnrollmentUpdate = enrollments.stream()
                    .map(Enrollment::getUpdatedAt)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
                    
            LocalDateTime lastAssignmentUpdate = assignmentSubmissions.stream()
                    .map(AssignmentSubmission::getSubmittedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
                    
            LocalDateTime lastQuizUpdate = quizSubmissions.stream()
                    .map(QuizSubmission::getSubmittedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
                    
            // Find the most recent date
            stats.setLastActivityDate(Stream.of(lastEnrollmentUpdate, lastAssignmentUpdate, lastQuizUpdate)
                    .filter(Objects::nonNull)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null));
        }
        
        log.info("Successfully fetched stats for student {}", studentId);
        return stats;
    }
    
    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId() != null ? enrollment.getId().toString() : null);
        response.setStudentId(enrollment.getStudent() != null && enrollment.getStudent().getId() != null ? 
                           enrollment.getStudent().getId().toString() : null);
        response.setCourseId(enrollment.getCourseId() != null ? enrollment.getCourseId().toString() : null);
        response.setStatus(enrollment.getStatus() != null ? enrollment.getStatus().name() : null);
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setCompletedAt(enrollment.getCompletedAt());
        response.setProgressPercentage(enrollment.getProgressPercentage());
        response.setTotalModules(enrollment.getTotalModules() != null ? enrollment.getTotalModules() : 0);
        response.setCompletedModules(enrollment.getCompletedModules() != null ? enrollment.getCompletedModules() : 0);
        response.setTotalLessons(enrollment.getTotalLessons() != null ? enrollment.getTotalLessons() : 0);
        response.setCompletedLessons(enrollment.getCompletedLessons() != null ? enrollment.getCompletedLessons() : 0);
        response.setCreatedAt(enrollment.getCreatedAt());
        response.setUpdatedAt(enrollment.getUpdatedAt());
        return response;
    }
    
    private CertificateResponse mapToCertificateResponse(Certificate certificate) {
        CertificateResponse response = new CertificateResponse();
        response.setId(certificate.getId().toString());
        response.setStudentId(certificate.getStudent().getId().toString());
        response.setCourseId(certificate.getCourseId().toString());
        response.setCertificateNumber(certificate.getCertificateNumber());
        response.setCourseName(certificate.getCourseName());
        response.setStudentName(certificate.getStudentName());
        response.setCompletedAt(certificate.getCompletedAt());
        response.setIssuedAt(certificate.getIssuedAt());
        response.setFinalScore(certificate.getFinalScore());
        response.setGrade(certificate.getGrade());
        response.setFilePath(certificate.getFilePath());
        response.setVerificationCode(certificate.getVerificationCode());
        response.setStatus(certificate.getStatus().name());
        response.setCreatedAt(certificate.getCreatedAt());
        response.setUpdatedAt(certificate.getUpdatedAt());
        return response;
    }
    
    private AnnouncementResponse mapToAnnouncementResponse(Announcement announcement) {
        if (announcement == null) {
            return null;
        }
        
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId() != null ? announcement.getId().toString() : null);
        response.setCourseId(announcement.getCourseId() != null ? announcement.getCourseId().toString() : null);
        response.setInstructorId(announcement.getInstructorId() != null ? announcement.getInstructorId().toString() : null);
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setType(announcement.getType() != null ? announcement.getType().name() : null);
        response.setStatus(announcement.getStatus() != null ? announcement.getStatus().name() : null);
        response.setPublishedAt(announcement.getPublishedAt());
        return response;
    }
    
    private MessageResponse mapToMessageResponse(Message message) {
        if (message == null) {
            return null;
        }
        
        MessageResponse response = new MessageResponse();
        response.setId(message.getId() != null ? message.getId().toString() : null);
        response.setStudentId(message.getStudentId() != null ? message.getStudentId().toString() : null);
        response.setInstructorId(message.getInstructorId() != null ? message.getInstructorId().toString() : null);
        response.setCourseId(message.getCourseId() != null ? message.getCourseId().toString() : null);
        response.setSubject(message.getSubject());
        response.setContent(message.getContent());
        
        // Handle enum to string conversion
        if (message.getType() != null) {
            response.setType(message.getType().name());
        }
        
        if (message.getStatus() != null) {
            response.setStatus(message.getStatus().name());
        }
        
        response.setReadAt(message.getReadAt());
        response.setRepliedAt(message.getRepliedAt());
        response.setParentMessageId(message.getParentMessageId() != null ? 
                                  message.getParentMessageId().toString() : null);
        response.setAttachmentPath(message.getAttachmentPath());
        response.setAttachmentName(message.getAttachmentName());
        response.setCreatedAt(message.getCreatedAt());
        response.setUpdatedAt(message.getUpdatedAt());
        return response;
    }
}








