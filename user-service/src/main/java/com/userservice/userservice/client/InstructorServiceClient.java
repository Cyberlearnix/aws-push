package com.userservice.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "instructor-service", url = "http://localhost:8081")
public interface InstructorServiceClient {
    
    @GetMapping("/instructors/{id}")
    ResponseEntity<Map<String, Object>> getInstructorById(
        @PathVariable("id") String instructorId,
        @RequestHeader("Authorization") String token
    );
    
    @GetMapping("/instructors/validate-token")
    ResponseEntity<Map<String, Object>> validateToken(
        @RequestHeader("Authorization") String token
    );
}


