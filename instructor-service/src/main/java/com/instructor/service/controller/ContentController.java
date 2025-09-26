package com.instructor.service.controller;

import com.instructor.service.dto.ModuleRequest;
import com.instructor.service.dto.ModuleResponse;
import com.instructor.service.model.Module;
import com.instructor.service.service.ContentService;
import com.instructor.service.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/instructors/{id}/courses/{courseId}")
@RequiredArgsConstructor
public class ContentController {

    private final CourseService courseService;
    private final ContentService contentService;

    // POST /instructors/{id}/courses/{courseId}/modules → Add a new module/lesson
    @PostMapping("/modules")
    public ResponseEntity<ModuleResponse> addModule(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @Valid @RequestBody ModuleRequest request
    ) {
        Module m = courseService.addModule(instructorId, courseId, request.getTitle(), request.getContent());
        return ResponseEntity.ok(ModuleResponse.builder()
                .id(m.getId())
                .courseId(courseId)
                .title(m.getTitle())
                .content(m.getContent())
                .build());
    }

    // PUT /instructors/{id}/courses/{courseId}/modules/{moduleId} → Update module content
    @PutMapping("/modules/{moduleId}")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Valid @RequestBody ModuleRequest request
    ) {
        Module m = courseService.updateModule(instructorId, courseId, moduleId, request.getTitle(), request.getContent());
        return ResponseEntity.ok(ModuleResponse.builder()
                .id(m.getId())
                .courseId(courseId)
                .title(m.getTitle())
                .content(m.getContent())
                .build());
    }

    // DELETE /instructors/{id}/courses/{courseId}/modules/{moduleId} → Delete a module
    @DeleteMapping("/modules/{moduleId}")
    public ResponseEntity<Void> deleteModule(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @PathVariable Long moduleId
    ) {
        courseService.deleteModule(instructorId, courseId, moduleId);
        return ResponseEntity.noContent().build();
    }

    // POST /instructors/{id}/courses/{courseId}/upload → Upload course resources (stub)
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResource(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @RequestPart("file") MultipartFile file
    ) {
        String saved = contentService.uploadResource(instructorId, courseId, file);
        return ResponseEntity.ok(java.util.Map.of("uploaded", saved));
    }
}
