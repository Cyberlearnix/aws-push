package com.instructor.service.mapper;

import com.instructor.service.dto.*;
import com.instructor.service.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

// Category and level are handled as strings

@Slf4j
@Component
public class EntityMapper {

    // Course mapping
    public CourseResponse toResponse(CourseEntity entity) {
        if (entity == null) return null;
        
        return CourseResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .category(entity.getCategory() != null ? CourseCategory.valueOf(entity.getCategory()) : null)
                .level(entity.getLevel() != null ? CourseLevel.valueOf(entity.getLevel()) : null)
                .instructorId(entity.getInstructor() != null ? entity.getInstructor().getUserId() : null)
                .published(entity.getPublished())
                .active(entity.getActive())
                .build();
    }

    public CourseEntity toEntity(CourseRequest request, Instructor instructor) {
        if (request == null) return null;
        
        return CourseEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory() != null ? request.getCategory().toString() : null)
                .level(request.getLevel() != null ? request.getLevel().toString() : null)
                .instructor(instructor)
                .published(false) // Default to not published
                .active(true) // Default to active
                .build();
    }

    // Announcement mapping
    public AnnouncementEntity createAnnouncementEntity(Long courseId, String title, String message, CourseEntity course) {
        return AnnouncementEntity.builder()
                .title(title)
                .message(message)
                .course(course)
                .active(true)
                .build();
    }

    public com.instructor.service.dto.Announcement toLegacyAnnouncement(AnnouncementEntity entity) {
        if (entity == null) return null;
        
        return com.instructor.service.dto.Announcement.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .active(entity.getActive())
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .build();
    }

    // Message mapping
    public MessageEntity createMessageEntity(Long courseId, String subject, String body, CourseEntity course) {
        return MessageEntity.builder()
                .subject(subject)
                .message(body)
                .course(course)
                .active(true)
                .build();
    }

    public com.instructor.service.dto.Message toLegacyMessage(MessageEntity entity) {
        if (entity == null) return null;
        
        return com.instructor.service.dto.Message.builder()
                .id(entity.getId())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .active(entity.getActive())
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .build();
    }
    
    // Module mapping
    public ModuleResponse toModuleResponse(ModuleEntity entity) {
        if (entity == null) return null;
        
        return ModuleResponse.success(
            entity.getId(),
            entity.getCourse() != null ? entity.getCourse().getId() : null,
            entity.getTitle(),
            entity.getContent() != null ? entity.getContent() : "",
            entity.getVideoUrl(),
            entity.getThumbnailUrl(),
            entity.getContentType() != null ? entity.getContentType().name() : null
        );
    }
    
    // Additional DTO conversion methods
    public com.instructor.service.model.Announcement toAnnouncement(AnnouncementEntity entity) {
        if (entity == null) return null;
        
        return com.instructor.service.model.Announcement.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null)
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .build();
    }
    
    public com.instructor.service.model.Message toMessage(MessageEntity entity) {
        if (entity == null) return null;
        
        return com.instructor.service.model.Message.builder()
                .id(entity.getId())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null)
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .build();
    }
}