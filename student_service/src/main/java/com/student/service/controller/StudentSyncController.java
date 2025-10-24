package com.student.service.controller;

import com.student.service.dto.StudentResponse;
import com.student.service.dto.StudentSyncRequest;
import com.student.service.entity.Student;
import com.student.service.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/students/sync")
@RequiredArgsConstructor
@Slf4j
public class StudentSyncController {
    
    private final StudentRepository studentRepository;
    
    @PostMapping
    public ResponseEntity<StudentResponse> syncStudent(@Valid @RequestBody StudentSyncRequest request) {
        log.info("Syncing student with id: {}", request.getId());
        
        // Check if student already exists
        Student student = studentRepository.findById(request.getId()).orElse(null);
        
        if (student != null) {
            log.info("Student already exists, updating details");
            // Update existing student
            student.setEmail(request.getEmail());
            student.setFirstName(request.getFirstName());
            student.setLastName(request.getLastName());
            student.setPhoneNumber(request.getPhoneNumber());
            student.setProfilePicture(request.getProfilePicture());
            student.setUpdatedAt(LocalDateTime.now());
        } else {
            log.info("Creating new student record");
            // Create new student
            student = new Student();
            student.setId(request.getId());
            student.setEmail(request.getEmail());
            student.setFirstName(request.getFirstName());
            student.setLastName(request.getLastName());
            student.setPhoneNumber(request.getPhoneNumber());
            student.setProfilePicture(request.getProfilePicture());
            student.setPassword("synced_from_user_service"); // Placeholder, not used for auth
            student.setRole(Student.Role.STUDENT);
            student.setStatus(Student.AccountStatus.ACTIVE);
            // isActive defaults to true in entity
            student.setCreatedAt(LocalDateTime.now());
            student.setUpdatedAt(LocalDateTime.now());
        }
        
        Student savedStudent = studentRepository.save(student);
        log.info("Student synced successfully: {}", savedStudent.getId());
        
        StudentResponse response = mapToResponse(savedStudent);
        return ResponseEntity.status(student.getId().equals(request.getId()) ? HttpStatus.OK : HttpStatus.CREATED)
                .body(response);
    }
    
    private StudentResponse mapToResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setEmail(student.getEmail());
        response.setFirstName(student.getFirstName());
        response.setLastName(student.getLastName());
        response.setPhoneNumber(student.getPhoneNumber());
        response.setProfilePicture(student.getProfilePicture());
        response.setRole(student.getRole());
        response.setStatus(student.getStatus());
        response.setLastLogin(student.getLastLogin());
        response.setCreatedAt(student.getCreatedAt());
        response.setUpdatedAt(student.getUpdatedAt());
        return response;
    }
}
