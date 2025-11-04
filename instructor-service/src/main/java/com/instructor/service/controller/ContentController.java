package com.instructor.service.controller;

import com.instructor.service.dto.ModuleRequest;
import com.instructor.service.dto.ModuleResponse;
import com.instructor.service.dto.ModuleUploadRequest;
import com.instructor.service.entity.ModuleEntity;
import com.instructor.service.mapper.EntityMapper;
import com.instructor.service.entity.ContentType;
import com.instructor.service.service.ContentService;
import com.instructor.service.service.CourseService;
import com.instructor.service.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ContentController {

    private final CourseService courseService;
    private final ContentService contentService;
    private final EntityMapper entityMapper;
    private final FileStorageService fileStorageService;

    // POST /instructors/{id}/courses/{courseId}/modules/upload → Add a new module/lesson with file uploads
    @PostMapping(value = "/{instructorId}/courses/{courseId}/modules/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModuleResponse> addModuleWithFiles(
            @PathVariable("instructorId") UUID instructorId,
            @PathVariable Long courseId,
            @Valid @RequestPart("module") ModuleRequest request,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile
    ) {
        log.info("Received request to add module with files to course {} by instructor {}", courseId, instructorId);
        return handleModuleCreation(instructorId, courseId, request, videoFile, thumbnailFile);
    }
    
    @PostMapping(value = "/{instructorId}/{courseId}/modules/upload", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModuleResponse> addModuleWithFilesShortPath(
            @PathVariable("instructorId") String instructorId,
            @PathVariable("courseId") Long courseId,
            @RequestParam(value = "moduleName", required = false) String moduleName,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "videoFile", required = false) MultipartFile file,
            HttpServletRequest request
    ) {
        log.info("Received request to add module to course {} by instructor {}", courseId, instructorId);
        
        // Log all request parameters and parts
        log.info("Request parameters: {}", request.getParameterMap());
        try {
            log.info("Content-Type: {}", request.getContentType());
            if (request instanceof StandardMultipartHttpServletRequest) {
                StandardMultipartHttpServletRequest multipartRequest = (StandardMultipartHttpServletRequest) request;
                log.info("Multipart parts: {}", multipartRequest.getFileMap().keySet());
            }
        } catch (Exception e) {
            log.warn("Error while logging request details: {}", e.getMessage());
        }
        
        if (file == null || file.isEmpty()) {
            log.error("No file part found in the request");
            throw new IllegalArgumentException("File is required");
        }
        
        log.info("Received file: name={}, size={}, type={}", 
            file.getOriginalFilename(), file.getSize(), file.getContentType());
            
        // Set default values if not provided
        moduleName = (moduleName != null) ? moduleName : "Untitled Module";
        description = (description != null) ? description : "";
        
        // Create a ModuleRequest from the provided parameters
        ModuleRequest moduleRequest = new ModuleRequest();
        moduleRequest.setTitle(moduleName);
        moduleRequest.setContent(description);
        
        // Handle file based on content type
        if (file.getContentType() != null && file.getContentType().startsWith("video/")) {
            // For videos, set the content type and pass the file to be processed
            moduleRequest.setContentType(ContentType.VIDEO);
            return handleModuleCreation(UUID.fromString(instructorId), courseId, moduleRequest, file, null);
        } else {
            // For non-video files, store them and set the content
            String fileUrl = fileStorageService.storeFile(file);
            moduleRequest.setContent(fileUrl); // Store the file URL in the content field
            return handleModuleCreation(UUID.fromString(instructorId), courseId, moduleRequest, null, null);
        }
    }
    
    // POST /instructors/{id}/courses/{courseId}/modules → Add a new module/lesson with JSON
    @PostMapping(value = "/modules", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ModuleResponse> addModule(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @Valid @RequestBody ModuleRequest request
    ) {
        log.info("Received JSON request to add module to course {} by instructor {}", courseId, instructorId);
        return handleModuleCreation(instructorId, courseId, request, null, null);
    }
    
    private ResponseEntity<ModuleResponse> handleModuleCreation(
            UUID instructorId,
            Long courseId,
            ModuleRequest request,
            MultipartFile videoFile,
            MultipartFile thumbnailFile
    ) {
        log.info("Received request to add module to course {} by instructor {}", courseId, instructorId);
        try {
            // Handle file uploads if present
            if (videoFile != null && !videoFile.isEmpty()) {
                String videoUrl = fileStorageService.storeFile(videoFile);
                request.setVideoUrl(videoUrl);
                request.setContentType(ContentType.VIDEO);
            }
            
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                String thumbnailUrl = fileStorageService.storeFile(thumbnailFile);
                request.setThumbnailUrl(thumbnailUrl);
            }
            
            ModuleEntity module = courseService.addModule(instructorId, courseId, request);
            log.info("Successfully added module with ID: {}", module.getId());
            return ResponseEntity.ok(entityMapper.toModuleResponse(module));
        } catch (Exception e) {
            log.error("Error adding module to course {}: {}", courseId, e.getMessage(), e);
            throw e;
        }
    }

    // PUT /{instructorId}/courses/{courseId}/modules/{moduleId} → Update module content
    @PutMapping("/{instructorId}/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable("instructorId") UUID instructorId,
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Valid @RequestBody ModuleRequest request
    ) {
        ModuleEntity module = courseService.updateModule(instructorId, courseId, moduleId, request.getTitle(), request.getContent());
        return ResponseEntity.ok(entityMapper.toModuleResponse(module));
    }

    // DELETE /{instructorId}/courses/{courseId}/modules/{moduleId} → Delete a module
    @DeleteMapping("/{instructorId}/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<Void> deleteModule(
            @PathVariable("instructorId") UUID instructorId,
            @PathVariable Long courseId,
            @PathVariable Long moduleId
    ) {
        courseService.deleteModule(instructorId, courseId, moduleId);
        return ResponseEntity.noContent().build();
    }

    // POST /{instructorId}/courses/{courseId}/upload → Upload course resources (stub)
    @PostMapping("/{instructorId}/courses/{courseId}/upload")
    public ResponseEntity<?> uploadResource(
            @PathVariable("instructorId") UUID instructorId,
            @PathVariable Long courseId,
            @RequestPart("file") MultipartFile file
    ) {
        String saved = contentService.uploadResource(instructorId, courseId, file);
        return ResponseEntity.ok(java.util.Map.of("uploaded", saved));
    }
}
