package com.student.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationService {

    private final JwtUtil jwtUtil;

    /**
     * Extract student ID from JWT token
     */
    public UUID getCurrentStudentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found");
            return null;
        }

        String username = authentication.getName();
        if (username == null) {
            log.warn("No username in authentication");
            return null;
        }

        try {
            // Get token from the authentication details (assuming it's stored there)
            Object credentials = authentication.getCredentials();
            if (credentials instanceof String) {
                String token = (String) credentials;
                return jwtUtil.extractUserId(token);
            }

            // Fallback: try to get from request header
            return getStudentIdFromCurrentRequest();
        } catch (Exception e) {
            log.error("Error extracting student ID from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract student ID from current request's Authorization header
     */
    private UUID getStudentIdFromCurrentRequest() {
        try {
            org.springframework.web.context.request.RequestAttributes requestAttributes =
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
            jakarta.servlet.http.HttpServletRequest request =
                ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtUtil.extractUserId(token);
            }
        } catch (Exception e) {
            log.debug("Could not extract token from request context: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get current student email from JWT
     */
    public String getCurrentStudentEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) return null;

            Object credentials = authentication.getCredentials();
            if (credentials instanceof String) {
                return jwtUtil.extractEmail((String) credentials);
            }

            // Fallback to request header
            return getEmailFromCurrentRequest();
        } catch (Exception e) {
            log.error("Error extracting student email from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get current student email from request header
     */
    private String getEmailFromCurrentRequest() {
        try {
            org.springframework.web.context.request.RequestAttributes requestAttributes =
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
            jakarta.servlet.http.HttpServletRequest request =
                ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtUtil.extractEmail(token);
            }
        } catch (Exception e) {
            log.debug("Could not extract email from request context: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get current student role from JWT
     */
    public String getCurrentStudentRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) return null;

            Object credentials = authentication.getCredentials();
            if (credentials instanceof String) {
                return jwtUtil.extractRole((String) credentials);
            }

            // Fallback to request header
            return getRoleFromCurrentRequest();
        } catch (Exception e) {
            log.error("Error extracting student role from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get current student role from request header
     */
    private String getRoleFromCurrentRequest() {
        try {
            org.springframework.web.context.request.RequestAttributes requestAttributes =
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
            jakarta.servlet.http.HttpServletRequest request =
                ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtUtil.extractRole(token);
            }
        } catch (Exception e) {
            log.debug("Could not extract role from request context: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Check if current user is a student
     */
    public boolean isCurrentUserStudent() {
        String role = getCurrentStudentRole();
        return "STUDENT".equals(role);
    }

    /**
     * Validate that the provided student ID matches the JWT student ID
     */
    public boolean validateStudentAccess(UUID requestedStudentId) {
        UUID currentStudentId = getCurrentStudentId();
        if (currentStudentId == null) {
            log.warn("No current student found in JWT");
            return false;
        }

        boolean matches = currentStudentId.equals(requestedStudentId);
        if (!matches) {
            log.warn("Student ID mismatch: JWT={}, Requested={}", currentStudentId, requestedStudentId);
        }

        return matches;
    }

    /**
     * Get authenticated student information
     */
    public StudentInfo getCurrentStudentInfo() {
        UUID studentId = getCurrentStudentId();
        String email = getCurrentStudentEmail();
        String role = getCurrentStudentRole();

        if (studentId == null || email == null || role == null) {
            log.warn("Incomplete student information in JWT: id={}, email={}, role={}", studentId, email, role);
            return null;
        }

        return new StudentInfo(studentId, email, role);
    }

    /**
     * Simple DTO to hold student information from JWT
     */
    public static class StudentInfo {
        private final UUID id;
        private final String email;
        private final String role;

        public StudentInfo(UUID id, String email, String role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }

        public UUID getId() { return id; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}
