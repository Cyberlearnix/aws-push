package com.instructor.service.service;

import com.instructor.service.client.UserServiceClient;
import com.instructor.service.dto.UserDTO;
import com.instructor.service.entity.Instructor;
import com.instructor.service.entity.Role;
import com.instructor.service.exception.ResourceNotFoundException;
import com.instructor.service.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final UserServiceClient userServiceClient;

    /**
     * Create or update an instructor profile
     */
    public Instructor createOrUpdateInstructor(UUID userId, String authToken) {
        // First, fetch user details from user service
        log.info("Fetching user details for userId: {}", userId);
        ResponseEntity<UserDTO> userResponse = userServiceClient.getUserById("Bearer " + authToken, userId.toString());
        
        if (userResponse == null || !userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            log.error("Failed to fetch user details for userId: {}", userId);
            throw new ResourceNotFoundException("User not found in user service");
        }
        
        UserDTO userDto = userResponse.getBody();
        log.info("Fetched user details: {}", userDto);

        Optional<Instructor> existingInstructor = instructorRepository.findByUserUuid(userId);
        
        if (existingInstructor.isPresent()) {
            return existingInstructor.get();
        } else {
            // Create new instructor with user details
            Instructor instructor = Instructor.builder()
                    .userId(userId)
                    .email(userDto.getEmail())
                    .name(userDto.getFullName() != null ? userDto.getFullName() : "Instructor")
                    .role(Role.INSTRUCTOR)
                    .active(true)
                    .build();
            
            return instructorRepository.save(instructor);
        }
    }

    /**
     * Get instructor by user ID
     */
    @Transactional(readOnly = true)
    public Optional<Instructor> getInstructorByUserId(UUID userId) {
        return instructorRepository.findByUserUuid(userId);
    }

    /**
     * Get all active instructors
     */
    @Transactional(readOnly = true)
    public List<Instructor> getAllInstructors() {
        return instructorRepository.findByActiveTrue();
    }

    /**
     * Deactivate instructor
     */
    public void deactivateInstructor(UUID userId) {
        log.info("Deactivating instructor with userId: {}", userId);
        Instructor instructor = instructorRepository.findByUserUuid(userId)
                .orElseThrow(() -> {
                    log.error("Instructor not found with userId: {}", userId);
                    return new IllegalArgumentException("Instructor not found");
                });
        instructor.setActive(false);
        instructorRepository.save(instructor);
        log.info("Successfully deactivated instructor with userId: {}", userId);
    }

    /**
     * Activate instructor
     */
    public void activateInstructor(UUID userId) {
        log.info("Activating instructor with userId: {}", userId);
        Instructor instructor = instructorRepository.findByUserUuid(userId)
                .orElseThrow(() -> {
                    log.error("Instructor not found with userId: {}", userId);
                    return new IllegalArgumentException("Instructor not found");
                });
        instructor.setActive(true);
        instructorRepository.save(instructor);
        log.info("Successfully activated instructor with userId: {}", userId);
    }

    /**
     * Check if instructor exists by user ID
     */
    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return instructorRepository.existsByUserId(userId);
    }
}