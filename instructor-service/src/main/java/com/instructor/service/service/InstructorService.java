package com.instructor.service.service;

import com.instructor.service.entity.InstructorEntity;
import com.instructor.service.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorService {

    private final InstructorRepository instructorRepository;

    /**
     * Create or update an instructor profile
     */
    public InstructorEntity createOrUpdateInstructor(UUID userId, String department, 
                                                   String designation, String qualification, 
                                                   String bio, String specialization, 
                                                   Integer experienceYears) {
        Optional<InstructorEntity> existingInstructor = instructorRepository.findByUserId(userId);
        
        InstructorEntity instructor;
        if (existingInstructor.isPresent()) {
            instructor = existingInstructor.get();
            instructor.setDepartment(department);
            instructor.setDesignation(designation);
            instructor.setQualification(qualification);
            instructor.setBio(bio);
            instructor.setSpecialization(specialization);
            instructor.setExperienceYears(experienceYears);
        } else {
            instructor = InstructorEntity.builder()
                    .userId(userId)
                    .department(department)
                    .designation(designation)
                    .qualification(qualification)
                    .bio(bio)
                    .specialization(specialization)
                    .experienceYears(experienceYears)
                    .build();
        }
        
        return instructorRepository.save(instructor);
    }

    /**
     * Get instructor by user ID
     */
    @Transactional(readOnly = true)
    public Optional<InstructorEntity> getInstructorByUserId(UUID userId) {
        return instructorRepository.findByUserId(userId);
    }

    /**
     * Get all active instructors
     */
    @Transactional(readOnly = true)
    public List<InstructorEntity> getAllActiveInstructors() {
        return instructorRepository.findByIsActiveTrue();
    }

    /**
     * Get instructors by department
     */
    @Transactional(readOnly = true)
    public List<InstructorEntity> getInstructorsByDepartment(String department) {
        return instructorRepository.findByDepartmentAndIsActiveTrue(department);
    }

    /**
     * Search instructors by specialization
     */
    @Transactional(readOnly = true)
    public List<InstructorEntity> searchBySpecialization(String specialization) {
        return instructorRepository.findBySpecializationContainingIgnoreCaseAndIsActiveTrue(specialization);
    }

    /**
     * Deactivate instructor
     */
    public void deactivateInstructor(UUID userId) {
        InstructorEntity instructor = instructorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));
        instructor.setIsActive(false);
        instructorRepository.save(instructor);
    }

    /**
     * Activate instructor
     */
    public void activateInstructor(UUID userId) {
        InstructorEntity instructor = instructorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));
        instructor.setIsActive(true);
        instructorRepository.save(instructor);
    }

    /**
     * Check if instructor exists by user ID
     */
    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return instructorRepository.existsByUserId(userId);
    }
}