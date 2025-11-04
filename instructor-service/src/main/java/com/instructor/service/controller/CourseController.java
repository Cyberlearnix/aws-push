package com.instructor.service.controller;

import com.instructor.service.dto.CourseRequest;
import com.instructor.service.dto.CourseResponse;
import com.instructor.service.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseController {

    private final WebApplicationContext applicationContext;

    private final CourseService courseService;

    @PostConstruct
    public void init() {
        log.info("\n" +
                "========================================\n" +
                "CourseController initialized with base path: /api/courses\n" +
                "Available endpoints:\n" +
                "  POST   /api/courses - Create a new course\n" +
                "  GET    /api/courses - Get all courses for current instructor\n" +
                "  GET    /api/courses/{id} - Get course by ID\n" +
                "  PUT    /api/courses/{id} - Update a course\n" +
                "  DELETE /api/courses/{id} - Delete a course\n" +
                "  (Legacy) POST /api/courses/legacy/{instructorId} - Create course (legacy)\n" +
                "========================================");

        // Log all registered endpoints
        try {
            RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
            
            log.info("\n===== Registered Endpoints =====");
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                RequestMappingInfo mappingInfo = entry.getKey();
                String methods = mappingInfo.getMethodsCondition().getMethods().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));
                String patterns = mappingInfo.getPatternsCondition().getPatterns().stream()
                        .collect(Collectors.joining(", "));
                log.info("{} {}", methods, patterns);
            }
            log.info("==============================");
        } catch (Exception e) {
            log.error("Error listing registered endpoints: {}", e.getMessage(), e);
        }
    }

    /**
     * Create a new course
     * @param request Course creation request
     * @return Created course details
     */
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseRequest request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Received request to create course with data: {}", request);
        
        try {
            // Get the authenticated user's ID from the security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Creating course for user: {}", username);
            
            // Convert username to UUID if needed
            UUID instructorId = UUID.fromString(username);
            
            CourseResponse response = courseService.createCourse(request, instructorId, authToken);
            log.info("Successfully created course with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating course: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // LEGACY FORMAT: POST /api/courses/legacy/{instructorId} → Create a new course (backward compatibility)
    @PostMapping(
        value = "/legacy/{instructorId}",
        consumes = {"application/json"},
        produces = {"application/json"}
    )
    public ResponseEntity<CourseResponse> createCourseLegacy(
            @PathVariable("instructorId") UUID instructorId,
            @Valid @RequestBody CourseRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("Received LEGACY request to create course for instructor: {} with data: {}", instructorId, request);
        log.info("Authorization header present: {}", authHeader != null);
        
        try {
            validateInstructorAccess(instructorId);
            log.info("Creating course for instructor (legacy): {}", instructorId);
            
            if (authHeader == null || authHeader.trim().isEmpty()) {
                log.error("Authorization header is required for legacy course creation");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is required");
            }
            
            CourseResponse response = courseService.createCourse(request, instructorId, authHeader);
            log.info("Successfully created course with ID: {} (legacy)", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating course (legacy): {}", e.getMessage(), e);
            throw e;
        }
    }

    // NEW FORMAT: GET /instructors/courses → Get all courses created by instructor
    @GetMapping("/instructors/courses")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        UUID instructorId = getCurrentUserId();
        return ResponseEntity.ok(courseService.getAllCoursesByInstructor(instructorId));
    }

    // OLD FORMAT: GET /instructors/{id}/courses → Get all courses (backward compatibility)
    @GetMapping("/instructors/{id}/courses")
    public ResponseEntity<List<CourseResponse>> getAllCoursesLegacy(
            @PathVariable("id") UUID instructorId
    ) {
        validateInstructorAccess(instructorId);
        return ResponseEntity.ok(courseService.getAllCoursesByInstructor(instructorId));
    }

    // NEW FORMAT: GET /instructors/courses/{courseId} → Get details of a specific course
    @GetMapping("/instructors/courses/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable Long courseId
    ) {
        UUID instructorId = getCurrentUserId();
        return ResponseEntity.ok(courseService.getCourseDetails(instructorId, courseId));
    }

    // OLD FORMAT: GET /instructors/{id}/courses/{courseId} → Get details of a specific course (backward compatibility)
    @GetMapping("/{id}/courses/{courseId}")
    public ResponseEntity<CourseResponse> getCourseLegacy(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        validateInstructorAccess(instructorId);
        return ResponseEntity.ok(courseService.getCourseDetails(instructorId, courseId));
    }

    // NEW FORMAT: PUT /instructors/courses/{courseId} → Update course info
    @PutMapping("/instructors/courses/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequest request
    ) {
        UUID instructorId = getCurrentUserId();
        return ResponseEntity.ok(courseService.updateCourse(instructorId, courseId, request));
    }

    // OLD FORMAT: PUT /instructors/{id}/courses/{courseId} → Update course info (backward compatibility)
    @PutMapping("/{id}/courses/{courseId}")
    public ResponseEntity<CourseResponse> updateCourseLegacy(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequest request
    ) {
        validateInstructorAccess(instructorId);
        return ResponseEntity.ok(courseService.updateCourse(instructorId, courseId, request));
    }

    // NEW FORMAT: DELETE /instructors/courses/{courseId} → Delete/unpublish a course
    @DeleteMapping("/instructors/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId
    ) {
        UUID instructorId = getCurrentUserId();
        courseService.deleteCourse(instructorId, courseId);
        return ResponseEntity.noContent().build();
    }

    // OLD FORMAT: DELETE /instructors/{id}/courses/{courseId} → Delete/unpublish a course (backward compatibility)
    @DeleteMapping("/{id}/courses/{courseId}")
    public ResponseEntity<Void> deleteCourseLegacy(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        validateInstructorAccess(instructorId);
        courseService.deleteCourse(instructorId, courseId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return UUID.fromString(authentication.getName());
    }

    private void validateInstructorAccess(UUID requestedInstructorId) {
        UUID currentUserId = getCurrentUserId();
        if (!currentUserId.equals(requestedInstructorId)) {
            throw new RuntimeException("Access denied: You can only access your own instructor resources");
        }
    }
}
