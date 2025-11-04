package com.instructor.service.client;

import com.instructor.service.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ResponseEntity<UserDTO> getUserByEmail(String token, String email) {
        log.error("Fallback: Failed to get user by email: {}. User service is unavailable.", email);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
    
    @Override
    public ResponseEntity<UserDTO> getUserById(String token, String userId) {
        log.error("Fallback: Failed to get user by ID: {}. User service is unavailable.", userId);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
