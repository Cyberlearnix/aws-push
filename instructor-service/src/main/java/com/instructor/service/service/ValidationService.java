package com.instructor.service.service;

import com.instructor.service.client.UserClient;
import com.instructor.service.dto.UserPublicDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final UserClient userClient;

    public void ensureInstructor(String id) {
        UserPublicDTO user = fetchUser(id);
        if (user.getRole() == null || !"INSTRUCTOR".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an INSTRUCTOR");
        }
    }

    public void ensureStudent(String id) {
        UserPublicDTO user = fetchUser(id);
        if (user.getRole() == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a STUDENT");
        }
    }

    private UserPublicDTO fetchUser(String id) {
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
