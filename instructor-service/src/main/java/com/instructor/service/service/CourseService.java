package com.instructor.service.service;

import com.instructor.service.dto.CourseRequest;
import com.instructor.service.dto.CourseResponse;
import com.instructor.service.entity.CourseEntity;
import com.instructor.service.entity.InstructorEntity;
import com.instructor.service.entity.ModuleEntity;
import com.instructor.service.mapper.EntityMapper;
import com.instructor.service.model.Course;
import com.instructor.service.model.Module;
import com.instructor.service.repository.CourseRepository;
import com.instructor.service.repository.InstructorRepository;
import com.instructor.service.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final ModuleRepository moduleRepository;
    private final EntityMapper entityMapper;

    public CourseResponse createCourse(UUID instructorId, CourseRequest request) {
        InstructorEntity instructor = instructorRepository.findByUserId(instructorId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));
        
        CourseEntity courseEntity = entityMapper.toEntity(request, instructor);
        courseEntity = courseRepository.save(courseEntity);
        
        return entityMapper.toResponse(courseEntity);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCoursesByInstructor(UUID instructorId) {
        InstructorEntity instructor = instructorRepository.findByUserId(instructorId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));
        
        return courseRepository.findByInstructor(instructor)
                .stream()
                .map(entityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseDetails(UUID instructorId, Long courseId) {
        CourseEntity course = getCourseEntityOrThrow(instructorId, courseId);
        return entityMapper.toResponse(course);
    }

    public CourseResponse updateCourse(UUID instructorId, Long courseId, CourseRequest request) {
        CourseEntity course = getCourseEntityOrThrow(instructorId, courseId);
        
        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getPublished() != null) course.setPublished(request.getPublished());
        
        course = courseRepository.save(course);
        return entityMapper.toResponse(course);
    }

    public void deleteCourse(UUID instructorId, Long courseId) {
        CourseEntity course = getCourseEntityOrThrow(instructorId, courseId);
        courseRepository.delete(course);
    }

    public Module addModule(UUID instructorId, Long courseId, String title, String content) {
        CourseEntity course = getCourseEntityOrThrow(instructorId, courseId);
        
        // Get max order index for this course
        Integer maxIndex = moduleRepository.findMaxOrderIndexByCourse(course);
        
        ModuleEntity moduleEntity = ModuleEntity.builder()
                .title(title)
                .content(content)
                .course(course)
                .orderIndex(maxIndex + 1)
                .build();
        
        moduleEntity = moduleRepository.save(moduleEntity);
        return entityMapper.toLegacyModule(moduleEntity);
    }

    public Module updateModule(UUID instructorId, Long courseId, Long moduleId, String title, String content) {
        CourseEntity course = getCourseEntityOrThrow(instructorId, courseId);
        
        ModuleEntity module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NoSuchElementException("Module not found"));
        
        // Verify the module belongs to the course
        if (!Objects.equals(module.getCourse().getId(), courseId)) {
            throw new IllegalArgumentException("Module does not belong to the specified course");
        }
        
        if (title != null) module.setTitle(title);
        if (content != null) module.setContent(content);
        
        module = moduleRepository.save(module);
        return entityMapper.toLegacyModule(module);
    }

    public void deleteModule(UUID instructorId, Long courseId, Long moduleId) {
        CourseEntity course = getCourseEntityOrThrow(instructorId, courseId);
        
        ModuleEntity module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NoSuchElementException("Module not found"));
        
        // Verify the module belongs to the course
        if (!Objects.equals(module.getCourse().getId(), courseId)) {
            throw new IllegalArgumentException("Module does not belong to the specified course");
        }
        
        moduleRepository.delete(module);
    }

    private CourseEntity getCourseEntityOrThrow(UUID instructorId, Long courseId) {
        InstructorEntity instructor = instructorRepository.findByUserId(instructorId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));
        
        return courseRepository.findByInstructor(instructor)
                .stream()
                .filter(course -> Objects.equals(course.getId(), courseId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Course not found or not owned by instructor"));
    }
}
