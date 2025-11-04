package com.instructor.service.security;

import com.instructor.service.client.UserServiceClient;
import com.instructor.service.dto.UserDTO;
import com.instructor.service.entity.Instructor;
import com.instructor.service.entity.Role;
import com.instructor.service.enums.UserRole;
import com.instructor.service.repository.InstructorRepository;
import com.instructor.service.service.InstructorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;
    private final InstructorRepository instructorRepository;
    private final InstructorService instructorService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        
        try {
            String token = "";
            
            // Try to get the token from the security context
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getCredentials() != null) {
                    token = authentication.getCredentials().toString();
                    log.debug("Using token from security context");
                } else {
                    log.warn("No authentication credentials found in security context");
                }
            } catch (Exception e) {
                log.warn("Could not get authentication from security context: {}", e.getMessage());
            }
            
            // If we still don't have a token, try to get it from the request attributes
            if (token.isEmpty()) {
                try {
                    Object tokenObj = RequestContextHolder.getRequestAttributes()
                        .getAttribute("token", RequestAttributes.SCOPE_REQUEST);
                    if (tokenObj != null) {
                        token = tokenObj.toString();
                        log.debug("Using token from request attributes");
                    }
                } catch (Exception e) {
                    log.warn("Could not get token from request attributes: {}", e.getMessage());
                }
            }
            
            log.debug("Calling user service with token: {}", 
                    token.isEmpty() ? "[empty]" : token.substring(0, Math.min(10, token.length())) + "...");
            ResponseEntity<UserDTO> response = userServiceClient.getUserByEmail(
                token.isEmpty() ? "" : "Bearer " + token, 
                email
            );
            
            if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Failed to fetch user with email: {}. Status: {}", 
                         email, response != null ? response.getStatusCode() : "No response");
                throw new UsernameNotFoundException("Error authenticating user: " + 
                    (response != null ? response.getStatusCode() : "No response from user service"));
            }
            
            UserDTO user = response.getBody();
            
            // Check if user is active
            if (user.getIsActive() != null && !user.getIsActive()) {
                log.warn("User account is disabled for email: {}", email);
                throw new UsernameNotFoundException("User account is disabled");
            }

            // Map UserRole to GrantedAuthority
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (user.getRole() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            } else {
                log.warn("No role found for user with email: {}", email);
                throw new UsernameNotFoundException("No role assigned to user");
            }

            log.debug("Successfully loaded user with email: {} and role: {}", email, user.getRole());
            
            // If user is an INSTRUCTOR, ensure they have an instructor profile
            if (user.getRole() == UserRole.INSTRUCTOR) {
                ensureInstructorProfileExists(user);
            }
            
            return new User(
                    user.getEmail(),
                    user.getPassword() != null ? user.getPassword() : "",
                    true, // enabled
                    true, // accountNonExpired
                    true, // credentialsNonExpired
                    user.getIsActive() != null ? user.getIsActive() : true, // accountNonLocked
                    authorities
            );
        } catch (Exception e) {
            log.error("Error fetching user from user-service: {}", e.getMessage(), e);
            throw new UsernameNotFoundException("Error authenticating user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ensures that an instructor profile exists for the given user.
     * If no profile exists, creates a new one with minimal required fields.
     *
     * @param user the user to check/create instructor profile for
     */
    private void ensureInstructorProfileExists(UserDTO user) {
        try {
            UUID userId = user.getId();
            if (!instructorRepository.existsByUserId(userId)) {
                log.info("Creating new instructor profile for user: {} (ID: {})", user.getEmail(), userId);
                
                Instructor newInstructor = Instructor.builder()
                    .userId(userId)
                    .email(user.getEmail())
                    .name(user.getFullName() != null ? user.getFullName() : "Instructor")
                    .role(Role.INSTRUCTOR)
                    .active(true)
                    .build();
                
                instructorRepository.save(newInstructor);
                log.info("Successfully created instructor profile for user: {}", user.getEmail());
            } else {
                log.debug("Instructor profile already exists for user: {}", user.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to ensure instructor profile exists for user: " + user.getEmail(), e);
            // Don't fail authentication if we can't create the instructor profile
        }
    }
}
