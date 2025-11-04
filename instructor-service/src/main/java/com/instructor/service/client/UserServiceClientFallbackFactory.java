package com.instructor.service.client;

import feign.FeignException;
import feign.codec.ErrorDecoder;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.instructor.service.dto.UserDTO;

@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    
    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public ResponseEntity<UserDTO> getUserByEmail(String token, String email) {
                log.error("Fallback: Error fetching user with email: {}. Reason: {}", 
                         email, cause != null ? cause.getMessage() : "Unknown error");
                
                if (cause instanceof FeignException) {
                    FeignException feignException = (FeignException) cause;
                    if (feignException.status() == 404) {
                        log.warn("User not found with email: {}", email);
                        return ResponseEntity.notFound().build();
                    } else if (feignException.status() == 401 || feignException.status() == 403) {
                        log.warn("Authentication/Authorization failed when fetching user with email: {}", email);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }
                }
                
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .header("X-Fallback-Reason", cause != null ? cause.getMessage() : "Service Unavailable")
                        .build();
            }
            
            @Override
            public ResponseEntity<UserDTO> getUserById(String token, String userId) {
                log.error("Fallback: Error fetching user with ID: {}. Reason: {}", 
                         userId, cause != null ? cause.getMessage() : "Unknown error");
                
                if (cause instanceof FeignException) {
                    FeignException feignException = (FeignException) cause;
                    if (feignException.status() == 404) {
                        log.warn("User not found with ID: {}", userId);
                        return ResponseEntity.notFound().build();
                    } else if (feignException.status() == 401 || feignException.status() == 403) {
                        log.warn("Authentication/Authorization failed when fetching user with ID: {}", userId);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }
                }
                
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .header("X-Fallback-Reason", cause != null ? cause.getMessage() : "Service Unavailable")
                        .build();
            }
        };
    }
}
