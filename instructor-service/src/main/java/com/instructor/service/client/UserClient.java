package com.instructor.service.client;

import com.instructor.service.dto.UserPublicDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserClient(RestTemplate restTemplate, @Value("${USER_SERVICE_URL:http://localhost:8081}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    public UserPublicDTO getUserById(UUID id) {
        try {
            String url = userServiceUrl + "/api/users/" + id;
            return restTemplate.getForObject(url, UserPublicDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch user from user service: " + e.getMessage());
        }
    }
}
