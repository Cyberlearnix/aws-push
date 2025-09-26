package com.instructor.service.service;

import com.instructor.service.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final UserClient userClient;

    public void ensureInstructor(String id) {
        Map<String, Object> user = fetchUser(id);
        Object role = user.get("role");
        if (role == null || !"INSTRUCTOR".equalsIgnoreCase(String.valueOf(role))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an INSTRUCTOR");
        }
    }

    public void ensureStudent(String id) {
        Map<String, Object> user = fetchUser(id);
        Object role = user.get("role");
        if (role == null || !"STUDENT".equalsIgnoreCase(String.valueOf(role))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a STUDENT");
        }
    }

    private Map<String, Object> fetchUser(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return userClient.getUserById(uuid);
        } catch (IllegalArgumentException iae) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id format (UUID expected)");
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to contact user-service: " + ex.getMessage());
        }
    }
}
