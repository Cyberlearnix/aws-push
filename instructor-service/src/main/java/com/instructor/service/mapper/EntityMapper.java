package com.instructor.service.mapper;

import com.instructor.service.dto.*;
import com.instructor.service.entity.*;
import com.instructor.service.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    // Course mapping
    public CourseResponse toResponse(CourseEntity entity) {
        if (entity == null) return null;
        return CourseResponse.builder()
                .id(entity.getId())
                .instructorId(entity.getInstructor().getUserId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .published(entity.getPublished())
                .build();
    }

    public CourseEntity toEntity(CourseRequest request, InstructorEntity instructor) {
        if (request == null) return null;
        return CourseEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .published(Boolean.TRUE.equals(request.getPublished()))
                .instructor(instructor)
                .build();
    }

    // Module mapping
    public ModuleResponse toResponse(ModuleEntity entity) {
        if (entity == null) return null;
        return ModuleResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .build();
    }

    public ModuleEntity toEntity(ModuleRequest request, CourseEntity course) {
        if (request == null) return null;
        return ModuleEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .course(course)
                .build();
    }

    // Legacy model support - convert Entity to legacy Model
    public Course toLegacyModel(CourseEntity entity) {
        if (entity == null) return null;
        List<com.instructor.service.model.Module> modules = entity.getModules().stream()
                .map(this::toLegacyModule)
                .collect(Collectors.toList());
        
        return Course.builder()
                .id(entity.getId())
                .instructorId(entity.getInstructor().getUserId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .published(entity.getPublished())
                .modules(modules)
                .build();
    }

    public com.instructor.service.model.Module toLegacyModule(ModuleEntity entity) {
        if (entity == null) return null;
        return com.instructor.service.model.Module.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .build();
    }

    public Announcement toLegacyAnnouncement(AnnouncementEntity entity) {
        if (entity == null) return null;
        return Announcement.builder()
                .id(entity.getId())
                .courseId(entity.getCourse().getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt().toInstant(java.time.ZoneOffset.UTC))
                .build();
    }

    public Message toLegacyMessage(MessageEntity entity) {
        if (entity == null) return null;
        return Message.builder()
                .id(entity.getId())
                .courseId(entity.getCourse().getId())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt().toInstant(java.time.ZoneOffset.UTC))
                .build();
    }

    // Entity creation from legacy models
    public AnnouncementEntity createAnnouncementEntity(Long courseId, String title, String message, CourseEntity course) {
        return AnnouncementEntity.builder()
                .title(title)
                .message(message)
                .course(course)
                .build();
    }

    public MessageEntity createMessageEntity(Long courseId, String subject, String message, CourseEntity course) {
        return MessageEntity.builder()
                .subject(subject)
                .message(message)
                .course(course)
                .build();
    }
}