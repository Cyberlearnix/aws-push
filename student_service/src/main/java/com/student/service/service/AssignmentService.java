package com.student.service.service;

import com.student.service.dto.AssignmentSubmissionRequest;
import com.student.service.dto.AssignmentSubmissionResponse;
import com.student.service.entity.Assignment;
import com.student.service.entity.AssignmentSubmission;
import com.student.service.entity.Student;
import com.student.service.exception.ResourceNotFoundException;
import com.student.service.exception.ConflictException;
import com.student.service.repository.AssignmentRepository;
import com.student.service.repository.AssignmentSubmissionRepository;
import com.student.service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssignmentService {
    
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final StudentRepository studentRepository;
    
    private static final String UPLOAD_DIR = "uploads/assignments/";
    
    @Transactional(readOnly = true)
    public List<Assignment> getAssignments(UUID studentId) {
        log.info("Fetching assignments for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Get all assignments for courses the student is enrolled in
        // This would typically involve joining with enrollment data
        return assignmentRepository.findAll();
    }
    
    public AssignmentSubmissionResponse submitAssignment(UUID studentId, Long assignmentId, AssignmentSubmissionRequest request) {
        log.info("Submitting assignment {} for student {}", assignmentId, studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        
        // Check if assignment is still open
        if (assignment.getAvailableUntil() != null && LocalDateTime.now().isAfter(assignment.getAvailableUntil())) {
            throw new ConflictException("Assignment submission deadline has passed");
        }
        
        // Check max attempts
        List<AssignmentSubmission> existingSubmissions = assignmentSubmissionRepository
                .findAssignmentSubmissionsByStudentIdAndAssignmentIdOrderByAttempt(studentId, assignmentId);
        
        int nextAttempt = existingSubmissions.isEmpty() ? 1 : existingSubmissions.get(0).getAttemptNumber() + 1;
        
        if (nextAttempt > assignment.getMaxAttempts()) {
            throw new ConflictException("Maximum attempts exceeded for this assignment");
        }
        
        // Create assignment submission
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setStudent(student);
        submission.setAssignment(assignment);
        submission.setStatus(AssignmentSubmission.SubmissionStatus.SUBMITTED);
        submission.setAttemptNumber(nextAttempt);
        submission.setSubmissionText(request.getSubmissionText());
        submission.setSubmittedAt(LocalDateTime.now());
        
        // Check if submission is late
        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            submission.setIsLate(true);
        }
        
        // Handle file upload if present
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            try {
                String fileName = saveUploadedFile(request.getFile(), studentId, assignmentId);
                submission.setFileName(request.getFile().getOriginalFilename());
                submission.setFilePath(fileName);
                submission.setFileSize(request.getFile().getSize());
            } catch (IOException e) {
                log.error("Error saving uploaded file", e);
                throw new RuntimeException("Failed to save uploaded file", e);
            }
        }
        
        AssignmentSubmission savedSubmission = assignmentSubmissionRepository.save(submission);
        
        log.info("Successfully submitted assignment {} for student {} (attempt {})", 
                assignmentId, studentId, nextAttempt);
        
        return mapToResponse(savedSubmission, studentId);
    }
    
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionResponse> getAssignmentSubmissions(UUID studentId) {
        log.info("Fetching assignment submissions for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<AssignmentSubmission> submissions = assignmentSubmissionRepository.findByStudentId(studentId);
        
        return submissions.stream()
                .map(submission -> mapToResponse(submission, studentId))
                .collect(Collectors.toList());
    }
    
    private String saveUploadedFile(MultipartFile file, UUID studentId, Long assignmentId) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName != null ? 
                originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
        String fileName = "student_" + studentId.toString() + "_assignment_" + assignmentId + "_" + 
                UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        return fileName;
    }
    
    private AssignmentSubmissionResponse mapToResponse(AssignmentSubmission submission, UUID studentId) {
        AssignmentSubmissionResponse response = new AssignmentSubmissionResponse();
        response.setId(submission.getId());
        response.setStudentId(studentId);
        response.setAssignmentId(submission.getAssignment().getId());
        response.setAssignmentTitle(submission.getAssignment().getTitle());
        response.setStatus(submission.getStatus());
        response.setAttemptNumber(submission.getAttemptNumber());
        response.setSubmissionText(submission.getSubmissionText());
        response.setFileName(submission.getFileName());
        response.setFileSize(submission.getFileSize());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setGradedAt(submission.getGradedAt());
        response.setScore(submission.getScore());
        response.setPercentage(submission.getPercentage());
        response.setGrade(submission.getGrade());
        response.setFeedback(submission.getFeedback());
        response.setIsLate(submission.getIsLate());
        response.setIsPlagiarized(submission.getIsPlagiarized());
        response.setCreatedAt(submission.getCreatedAt());
        response.setUpdatedAt(submission.getUpdatedAt());
        return response;
    }
}








