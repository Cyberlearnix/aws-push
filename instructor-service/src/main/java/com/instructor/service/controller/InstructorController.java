package com.instructor.service.controller;

import com.instructor.service.dto.InstructorResponse;
import com.instructor.service.entity.Instructor;
import com.instructor.service.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorService instructorService;

    // POST /instructors/profile → Create or update instructor profile
    @PostMapping("/profile")
    public ResponseEntity<InstructorResponse> createOrUpdateInstructorProfile(
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract the token from the Authorization header (remove 'Bearer ' prefix if present)
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        
        UUID userId = getCurrentUserId();
        Instructor instructor = instructorService.createOrUpdateInstructor(userId, token);

        InstructorResponse response = new InstructorResponse();
        response.setId(instructor.getId());
        response.setUserId(instructor.getUserId());
        response.setEmail(instructor.getEmail());
        response.setName(instructor.getName());
        response.setActive(instructor.getActive());

        return ResponseEntity.ok(response);
    }

    // GET /instructors/profile → Get current instructor profile
    @GetMapping("/profile")
    public ResponseEntity<InstructorResponse> getInstructorProfile() {
        UUID userId = getCurrentUserId();
        Instructor instructor = instructorService.getInstructorByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Instructor profile not found"));

        InstructorResponse response = new InstructorResponse();
        response.setId(instructor.getId());
        response.setUserId(instructor.getUserId());
        response.setEmail(instructor.getEmail());
        response.setName(instructor.getName());
        response.setActive(instructor.getActive());
                
        return ResponseEntity.ok(response);
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return UUID.fromString(authentication.getName());
    }

    public static class CreateInstructorRequest {
        private String department;
        private String designation;
        private String qualification;
        private String bio;
        private String specialization;
        private Integer experienceYears;

        // Getters and setters
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getQualification() { return qualification; }
        public void setQualification(String qualification) { this.qualification = qualification; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }

        public Integer getExperienceYears() { return experienceYears; }
        public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    }
}
