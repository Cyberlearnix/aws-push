package com.student.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private Integer totalEnrolledCourses;
    private Integer completedCourses;
    private Integer activeCourses;
    private Double overallProgress;
    private Integer totalCertificates;
    private Integer unreadMessages;
    private Integer pendingAssignments;
    private Integer upcomingQuizzes;
    private List<EnrollmentResponse> recentEnrollments;
    private List<CertificateResponse> recentCertificates;
    private List<AnnouncementResponse> recentAnnouncements;
    private List<MessageResponse> recentMessages;
}








