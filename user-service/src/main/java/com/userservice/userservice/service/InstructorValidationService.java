package com.userservice.userservice.service;

import com.userservice.userservice.client.InstructorServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorValidationService {
    
    private final InstructorServiceClient instructorServiceClient;
    
    public boolean validateInstructorToken(String token) {
        try {
            ResponseEntity<Map<String, Object>> response = instructorServiceClient.validateToken("Bearer " + token);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error validating instructor token: {}", e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> getInstructorById(String instructorId, String token) {
        try {
            ResponseEntity<Map<String, Object>> response = instructorServiceClient.getInstructorById(instructorId, "Bearer " + token);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Error fetching instructor by ID {}: {}", instructorId, e.getMessage());
        }
        return null;
    }
}


