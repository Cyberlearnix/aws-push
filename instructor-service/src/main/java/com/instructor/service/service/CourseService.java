package com.instructor.service.service;

import com.instructor.service.dto.CourseRequest;
import com.instructor.service.dto.CourseResponse;
import com.instructor.service.dto.ModuleRequest;
import com.instructor.service.dto.UserPublicDTO;
import com.instructor.service.entity.*;
import com.instructor.service.exception.CourseNotFoundException;
import com.instructor.service.exception.UnauthorizedAccessException;
import com.instructor.service.mapper.EntityMapper;
import com.instructor.service.repository.CourseRepository;
import com.instructor.service.repository.InstructorRepository;
import com.instructor.service.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService implements CourseServiceInterface {

    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final ModuleRepository moduleRepository;
    private final EntityMapper entityMapper;
    private final RestTemplate restTemplate;
    
    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Transactional
    public CourseResponse createCourse(CourseRequest request, UUID instructorId, String authToken) {
        log.info("🔹 createCourse: Starting course creation for instructorId: {}", instructorId);
        log.debug("🔹 Request details: {}", request);

        try {
            // Validate request
            if (request == null) {
                throw new IllegalArgumentException("Course request cannot be null");
            }

            log.debug("🔹 Fetching user details from user service...");
            String url = userServiceUrl + "/api/users/" + instructorId;
            log.debug("🔹 Calling user service URL: {}", url);
            
            // Create headers with the auth token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Make the request with the auth token
            ResponseEntity<UserPublicDTO> response = restTemplate.exchange(
                url, 
                HttpMethod.GET,
                entity, 
                UserPublicDTO.class
            );
            
            UserPublicDTO userDetails = response.getBody();
            log.info("✅ Retrieved user details for instructorId: {}", instructorId);

            if (userDetails == null) {
                log.error("❌ User details not found for instructorId: {}", instructorId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User details not found for ID: " + instructorId);
            }

            // Check if user has INSTRUCTOR role
            if (userDetails.getRole() == null || !userDetails.getRole().equals("INSTRUCTOR")) {
                log.warn("⛔ User {} does not have INSTRUCTOR role. Current role: {}", 
                    instructorId, userDetails.getRole());
                throw new UnauthorizedAccessException("User must have INSTRUCTOR role to create courses");
            }

            // Get instructor from repository or throw exception if not found
            Instructor instructor = instructorRepository.findByUserUuid(instructorId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instructor not found with ID: " + instructorId));

            log.debug("🔹 Creating new course...");
            CourseEntity course = entityMapper.toEntity(request, instructor);

            log.debug("🔹 Saving course to database...");
            course = courseRepository.save(course);
            log.info("✅ Successfully created course with ID: {}", course.getId());

            CourseResponse courseResponse = entityMapper.toResponse(course);
            log.debug("🔹 Mapped response: {}", courseResponse);
            return courseResponse;
            
        } catch (ResponseStatusException e) {
            log.error("❌ Error in createCourse for instructorId: {} - {}", instructorId, e.getReason());
            throw e;
        } catch (Exception e) {
            String errorMessage = "Error creating course for instructorId: " + instructorId + " - " + e.getMessage();
            log.error(errorMessage, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, e);
        }
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCoursesByInstructor(UUID instructorUuid) {
        log.debug("getAllCoursesByInstructor: looking up courses for instructor with UUID {}", instructorUuid);
        
        // Find the instructor by UUID to get their ID
        Instructor instructor = instructorRepository.findByUserUuid(instructorUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Instructor not found with UUID: " + instructorUuid));
        
        // Get all courses for this instructor using their ID
        List<CourseEntity> courses = courseRepository.findByInstructorId(instructor.getId());
        
        log.debug("getAllCoursesByInstructor: Found {} courses for instructor {}", courses.size(), instructorUuid);
        return courses.stream()
                .map(entityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        log.debug("getAllCourses: fetching all published courses");
        return courseRepository.findByPublishedTrue()
                .stream()
                .map(entityMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CourseResponse getCourseDetails(UUID instructorId, Long courseId) {
        log.debug("getCourseDetails: looking up course {} for instructor {}", courseId, instructorId);
        CourseEntity course = getCourseEntityOrThrow(instructorId, courseId);
        return entityMapper.toResponse(course);
    }

    @Transactional
    public CourseResponse updateCourse(UUID instructorId, Long courseId, CourseRequest request) {
        log.debug("Updating course {} for instructor {}", courseId, instructorId);
        
        CourseEntity course = getCourseEntityOrThrow(instructorId, courseId);
        
        // Update fields if they are not null in the request
        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        if (request.getCategory() != null) course.setCategory(request.getCategory().name());
        if (request.getLevel() != null) course.setLevel(request.getLevel().name());

        course = courseRepository.save(course);
        return entityMapper.toResponse(course);
    }

    @Transactional
    public void deleteCourse(UUID instructorId, Long courseId) {
        log.debug("deleteCourse: deleting course {} for instructor {}", courseId, instructorId);
        CourseEntity course = getCourseEntityOrThrow(instructorId, courseId);
        courseRepository.delete(course);
    }

    @Transactional
    public ModuleEntity addModule(UUID instructorId, Long courseId, ModuleRequest request) {
        log.debug("addModule: adding module '{}' to course {} for instructor {}", 
            request.getTitle(), courseId, instructorId);
            
        CourseEntity course = getCourseEntityOrThrow(instructorId, courseId);
        
        // Get the instructor entity by user UUID
        Instructor instructor = instructorRepository.findByUserUuid(instructorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Instructor not found with user ID: " + instructorId));
        
        // Create and populate the module
        ModuleEntity module = ModuleEntity.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .course(course)
            .instructor(instructor)
            .videoUrl(request.getVideoUrl())
            .thumbnailUrl(request.getThumbnailUrl())
            .contentType(request.getContentType() != null ? request.getContentType() : ContentType.TEXT)
            .orderIndex(course.getModules().size())
            .published(false)
            .build();
        
        log.debug("Saving module: {}", module);
        return moduleRepository.save(module);
    }
    
    // Kept for backward compatibility
    @Transactional
    public ModuleEntity addModule(UUID instructorId, Long courseId, String title, String content) {
        ModuleRequest request = new ModuleRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setContentType(ContentType.TEXT);
        return addModule(instructorId, courseId, request);
    }

    @Transactional
    public ModuleEntity updateModule(UUID instructorId, Long courseId, Long moduleId, String title, String content) {
        log.debug("updateModule: updating module {} for course {} and instructor {}", moduleId, courseId, instructorId);
        getCourseEntityOrThrow(instructorId, courseId);
        
        return moduleRepository.findById(moduleId)
                .map(module -> {
                    if (title != null) module.setTitle(title);
                    if (content != null) module.setContent(content);
                    return moduleRepository.save(module);
                })
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
    }

    @Transactional
    public void deleteModule(UUID instructorId, Long courseId, Long moduleId) {
        log.debug("deleteModule: deleting module {} from course {} for instructor {}", moduleId, courseId, instructorId);
        getCourseEntityOrThrow(instructorId, courseId);
        moduleRepository.deleteById(moduleId);
    }

    private CourseEntity getCourseEntityOrThrow(UUID instructorId, Long courseId) {
        log.debug("getCourseEntityOrThrow: looking up course {} for instructor {}", courseId, instructorId);
        
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
                
        if (course.getInstructor() == null || !course.getInstructor().getUserId().equals(instructorId)) {
            throw new UnauthorizedAccessException("You don't have permission to access this course");
        }
        
        return course;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCourseStatus(Long courseId, boolean active) {
        log.debug("updateCourseStatus: setting course {} active status to {}", courseId, active);
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        course.setActive(active);
        courseRepository.save(course);
    }
}
