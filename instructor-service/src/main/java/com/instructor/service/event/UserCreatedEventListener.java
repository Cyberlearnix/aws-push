package com.instructor.service.event;

import com.cyberlearnix.shared.dto.UserDTO;
import com.cyberlearnix.shared.dto.event.UserCreatedEvent;
import com.cyberlearnix.shared.enums.UserRole;
import com.instructor.service.entity.Role;
import com.instructor.service.entity.Instructor;
import com.instructor.service.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final InstructorRepository instructorRepository;

    @Async
    @EventListener
    @Transactional
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        UserDTO user = event.getUser();
        
        try {
            // Only process INSTRUCTOR users
            if (user.getRole() != UserRole.INSTRUCTOR) {
                log.debug("Skipping non-instructor user: {}", user.getEmail());
                return;
            }
            
            // Check if instructor already exists
            if (user.getId() != null && instructorRepository.existsByUserId(user.getId())) {
                log.info("Instructor profile already exists for user: {}", user.getEmail());
                return;
            }

            log.info("Creating instructor profile for user: {}", user.getEmail());
            
            // Create a new instructor profile with the user's details
            Instructor instructor = Instructor.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getFullName() != null ? user.getFullName() : "Instructor")
                    .active(true)
                    .role(Role.INSTRUCTOR)
                    .build();
            
            instructorRepository.save(instructor);
            log.info("Successfully created instructor profile for user: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Error creating instructor profile for user: " + (user != null ? user.getEmail() : "unknown"), e);
            // You might want to implement a retry mechanism here
        }
    }
}
