package com.userservice.userservice.controller;

import com.cyberlearnix.shared.enums.UserRole;
import com.userservice.userservice.dto.AdminCreateUserRequestDTO;
import com.userservice.userservice.dto.AdminUpdateUserRequestDTO;
import com.userservice.userservice.dto.UserPublicDTO;
import com.userservice.userservice.entity.UserEntity;
import com.userservice.userservice.service.UserService;
import com.userservice.userservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 1. GET /admin/all-users-details → Get all users
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all-users-details")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        String authHeader = request.getHeader("Authorization");
        String userAgent = request.getHeader("User-Agent");
        
        logger.info("[{}] 🔐 Admin endpoint accessed - GET /all-users-details", requestId);
        logger.info("[{}] Request details - IP: {}, User-Agent: {}, Auth Header: {}", 
            requestId, request.getRemoteAddr(), userAgent, 
            authHeader != null ? "Bearer ***" + authHeader.substring(Math.max(0, authHeader.length() - 10)) : "null");
        
        // Log current authentication context
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.info("[{}] 🔐 Authentication context - Principal: {}, Authorities: {}, Authenticated: {}", 
                requestId, auth.getName(), auth.getAuthorities(), auth.isAuthenticated());
        } else {
            logger.warn("[{}] ⚠️ No authentication context found!", requestId);
        }
        
        try {
            List<UserPublicDTO> users = userService.getAllUsers();
            logger.info("[{}] Successfully retrieved {} users", requestId, users.size());
            return ResponseEntity.ok(
                Map.of("success", true, "count", users.size(), "users", users, "requestId", requestId)
            );
        } catch (Exception e) {
            logger.error("[{}] Error retrieving users: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Error retrieving users", "requestId", requestId)
            );
        }
    }

    // 2. GET /admin/users/{id} → Get specific user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable UUID id, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.info("[{}] Fetching user with ID: {} - IP: {}", requestId, id, request.getRemoteAddr());
        
        try {
            UserPublicDTO user = userService.getPublicById(id);
            if (user != null) {
                logger.info("[{}] Successfully fetched user: {}", requestId, user.getEmail());
                return ResponseEntity.ok(Map.of("success", true, "user", user, "requestId", requestId));
            } else {
                logger.warn("[{}] User not found with ID: {}", requestId, id);
                return ResponseEntity.status(404).body(
                    Map.of("success", false, "message", "User not found", "requestId", requestId)
                );
            }
        } catch (Exception e) {
            logger.error("[{}] Error fetching user {}: {}", requestId, id, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Error fetching user", "requestId", requestId)
            );
        }
    }

    // 2. POST /admin/add-users → Create user (admin only, no OTP required)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/add-users")
    public ResponseEntity<?> addUser(@RequestBody AdminCreateUserRequestDTO dto, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.info("[{}] Creating new user with email: {} - IP: {}", requestId, dto.getEmail(), request.getRemoteAddr());
        
        // Use the role directly from DTO, default to STUDENT if not specified
        UserRole role = dto.getRole() != null ? dto.getRole() : UserRole.STUDENT;
        return createUserWithRole(dto, role, requestId, request);
    }
    
    // 3.1 POST /admin/add-instructor → Create instructor manually
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/add-instructor")
    public ResponseEntity<?> addInstructor(@RequestBody AdminCreateUserRequestDTO dto, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.info("[{}] Creating new instructor with email: {} - IP: {}", requestId, dto.getEmail(), request.getRemoteAddr());
        
        // Force role to be INSTRUCTOR
        return createUserWithRole(dto, UserRole.INSTRUCTOR, requestId, request);
    }
    
    private ResponseEntity<?> createUserWithRole(AdminCreateUserRequestDTO dto, UserRole role, String requestId, HttpServletRequest request) {
        
        try {
            logger.info("[{}] Creating new {} with email: {}", requestId, role, dto.getEmail());
            
            // Create user with the specified role
            UserEntity savedUser = userService.registerUserWithRole(
                dto.getEmail(),
                dto.getFullName(),
                dto.getPhone(),
                dto.getPassword(),
                role
            );

            logger.info("[{}] Successfully created {} with ID: {}", requestId, role, savedUser.getId());
            
            // Return appropriate response based on role
            if (role == UserRole.INSTRUCTOR) {
                return ResponseEntity.status(201).body(Map.of(
                    "success", true,
                    "message", "Instructor created successfully",
                    "userId", savedUser.getId(),
                    "email", savedUser.getEmail(),
                    "role", role.name(),
                    "requestId", requestId
                ));
            } else {
                return ResponseEntity.status(201).body(Map.of(
                    "success", true,
                    "message", "User created successfully",
                    "userId", savedUser.getId(),
                    "email", savedUser.getEmail(),
                    "role", role.name(),
                    "requestId", requestId
                ));
            }
            
        } catch (IllegalArgumentException e) {
            // Handle password validation and other business logic errors
            logger.warn("[{}] Validation error creating user: {}", requestId, e.getMessage());
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage(), "requestId", requestId, "errorType", "VALIDATION_ERROR")
            );
        } catch (Exception e) {
            logger.error("[{}] Unexpected error creating user: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "An unexpected error occurred while creating the user", "requestId", requestId, "errorType", "SERVER_ERROR")
            );
        }
    }

    // 4. PUT /admin/users/{id} → Update user details
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable UUID id, 
            @RequestBody AdminUpdateUserRequestDTO dto,
            HttpServletRequest request) {
                
        String requestId = UUID.randomUUID().toString();
        logger.info("[{}] Updating user with ID: {} - IP: {}", requestId, id, request.getRemoteAddr());
        
        try {
            UserEntity user = userService.getUserById(id);
            if (user == null) {
                logger.warn("[{}] User not found with ID: {}", requestId, id);
                return ResponseEntity.status(404).body(
                    Map.of("success", false, "message", "User not found", "requestId", requestId)
                );
            }
            
            // Log changes
            if (dto.getFullName() != null && !dto.getFullName().equals(user.getFullName())) {
                logger.info("[{}] Updating full name from '{}' to '{}' for user {}", 
                    requestId, user.getFullName(), dto.getFullName(), user.getEmail());
                user.setFullName(dto.getFullName());
            }
            
            if (dto.getPhone() != null && !dto.getPhone().equals(user.getPhone())) {
                logger.info("[{}] Updating phone for user {}: {} -> {}", 
                    requestId, user.getEmail(), user.getPhone(), dto.getPhone());
                user.setPhone(dto.getPhone());
            }
            
            if (dto.getAddress() != null) user.setAddress(dto.getAddress());
            if (dto.getPhoto() != null) user.setPhoto(dto.getPhoto());
            
            if (dto.getRole() != null && dto.getRole() != user.getRole()) {
                logger.info("[{}] Updating role for user {} from {} to {}", 
                    requestId, user.getEmail(), user.getRole(), dto.getRole());
                user.setRole(dto.getRole());
            }
            
            if (dto.getIsActive() != null && dto.getIsActive() != user.isActive()) {
                logger.info("[{}] Updating active status for user: {} to {}", requestId, user.getEmail(), dto.getIsActive() ? "ACTIVE" : "INACTIVE");
                user.setActive(dto.getIsActive());
            }
            
            // Save the updated user
            user = userService.saveUser(user);
            logger.info("[{}] Successfully updated user: {}", requestId, user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "User updated successfully",
                "user", userService.getPublicById(user.getId()),
                "requestId", requestId
            ));
            
        } catch (Exception e) {
            logger.error("[{}] Error updating user {}: {}", requestId, id, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Error updating user", "requestId", requestId)
            );
        }
    }

    // 5. DELETE /admin/users/{id} → Delete user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.info("[{}] Received request to delete user with ID: {} - IP: {}", 
            requestId, id, request.getRemoteAddr());
            
        try {
            UserEntity user = userService.getUserById(id);
            if (user == null) {
                logger.warn("[{}] User not found for deletion: {}", requestId, id);
                return ResponseEntity.status(404).body(
                    Map.of("success", false, "message", "User not found", "requestId", requestId)
                );
            }
            
            logger.info("[{}] Deleting user: {} (ID: {})", requestId, user.getEmail(), user.getId());
            userService.adminDeleteUser(id);
            
            logger.info("[{}] Successfully deleted user: {} (ID: {})", requestId, user.getEmail(), id);
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "User deleted successfully",
                "deletedUserId", id,
                "requestId", requestId
            ));
            
        } catch (Exception e) {
            logger.error("[{}] Error deleting user {}: {}", requestId, id, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Error deleting user", "requestId", requestId)
            );
        }
    }

    // 6. POST /admin/users/{id}/deactivate → Suspend user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable UUID id, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.info("[{}] Deactivating user with ID: {} - IP: {}", 
            requestId, id, request.getRemoteAddr());
            
        try {
            UserEntity user = userService.getUserById(id);
            if (user == null) {
                logger.warn("[{}] User not found for deactivation: {}", requestId, id);
                return ResponseEntity.status(404).body(
                    Map.of("success", false, "message", "User not found", "requestId", requestId)
                );
            }
            
            if (!user.isActive()) {
                logger.info("[{}] User {} is already deactivated", requestId, user.getEmail());
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "User is already deactivated",
                    "userId", id,
                    "requestId", requestId
                ));
            }
            
            logger.info("[{}] Deactivating user: {} (ID: {})", requestId, user.getEmail(), id);
            userService.setActive(id, false);
            
            logger.info("[{}] Successfully deactivated user: {}", requestId, user.getEmail());
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "User deactivated successfully",
                "userId", id,
                "requestId", requestId
            ));
            
        } catch (Exception e) {
            logger.error("[{}] Error deactivating user {}: {}", requestId, id, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Error deactivating user", "requestId", requestId)
            );
        }
    }

    // DEBUG: Check current user's authentication details
    @GetMapping("/debug/auth")
    public ResponseEntity<?> debugAuth(HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.info("[{}] Debug auth request - IP: {}", requestId, request.getRemoteAddr());
        
        try {
            // Get auth header
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "No valid Bearer token found",
                    "authHeader", authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null",
                    "requestId", requestId
                ));
            }
            
            String token = authHeader.substring(7).trim();
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            
            // Get Spring Security context
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            var authorities = auth != null ? auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(java.util.stream.Collectors.toList()) : java.util.List.of();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "email", email,
                "role", role,
                "springAuthorities", authorities,
                "isAuthenticated", auth != null ? auth.isAuthenticated() : false,
                "requestId", requestId
            ));
            
        } catch (Exception e) {
            logger.error("[{}] Error in debug auth: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Debug error: " + e.getMessage(), "requestId", requestId)
            );
        }
    }
    
    // 7. POST /admin/users/{id}/activate → Reactivate user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable UUID id, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.info("[{}] Activating user with ID: {} - IP: {}", 
            requestId, id, request.getRemoteAddr());
            
        try {
            UserEntity user = userService.getUserById(id);
            if (user == null) {
                logger.warn("[{}] User not found for activation: {}", requestId, id);
                return ResponseEntity.status(404).body(
                    Map.of("success", false, "message", "User not found", "requestId", requestId)
                );
            }
            
            if (user.isActive()) {
                logger.info("[{}] User {} is already active", requestId, user.getEmail());
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "User is already active",
                    "userId", id,
                    "requestId", requestId
                ));
            }
            
            logger.info("[{}] Activating user: {} (ID: {})", requestId, user.getEmail(), id);
            userService.setActive(id, true);
            
            logger.info("[{}] Successfully activated user: {}", requestId, user.getEmail());
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "User activated successfully",
                "userId", id,
                "requestId", requestId
            ));
            
        } catch (Exception e) {
            logger.error("[{}] Error activating user {}: {}", requestId, id, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Error activating user", "requestId", requestId)
            );
        }
    }
}


