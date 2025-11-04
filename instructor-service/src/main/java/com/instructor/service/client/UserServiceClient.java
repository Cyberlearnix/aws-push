package com.instructor.service.client;

import com.instructor.service.config.FeignClientConfig;
import com.instructor.service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "user-service",
    url = "${user-service.url}",
    configuration = FeignClientConfig.class,
    fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {
    
    @GetMapping("/api/users/email/{email}")
    ResponseEntity<UserDTO> getUserByEmail(
        @RequestHeader("Authorization") String token,
        @PathVariable("email") String email
    );
    
    @GetMapping("/api/users/{userId}")
    ResponseEntity<UserDTO> getUserById(
        @RequestHeader("Authorization") String token,
        @PathVariable("userId") String userId
    );
}
