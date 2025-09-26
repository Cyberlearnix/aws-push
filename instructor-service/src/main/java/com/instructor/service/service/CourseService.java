package com.instructor.service.service;

import com.instructor.service.dto.CourseRequest;
import com.instructor.service.dto.CourseResponse;
import com.instructor.service.model.Course;
import com.instructor.service.model.Module;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class CourseService {

    private final Map<UUID, Map<Long, Course>> instructorCourses = new ConcurrentHashMap<>();
    private final AtomicLong courseIdSeq = new AtomicLong(1);
    private final AtomicLong moduleIdSeq = new AtomicLong(1);

    public CourseResponse createCourse(UUID instructorId, CourseRequest request) {
        long courseId = courseIdSeq.getAndIncrement();
        Course course = Course.builder()
                .id(courseId)
                .instructorId(instructorId)
                .title(request.getTitle())
                .description(request.getDescription())
                .published(Boolean.TRUE.equals(request.getPublished()))
                .build();
        instructorCourses
                .computeIfAbsent(instructorId, k -> new ConcurrentHashMap<>())
                .put(courseId, course);
        return toResponse(course);
    }

    public List<CourseResponse> getAllCoursesByInstructor(UUID instructorId) {
        return instructorCourses.getOrDefault(instructorId, Collections.emptyMap())
                .values().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CourseResponse getCourseDetails(UUID instructorId, Long courseId) {
        Course c = getCourseOrThrow(instructorId, courseId);
        return toResponse(c);
    }

    public CourseResponse updateCourse(UUID instructorId, Long courseId, CourseRequest request) {
        Course c = getCourseOrThrow(instructorId, courseId);
        if (request.getTitle() != null) c.setTitle(request.getTitle());
        if (request.getDescription() != null) c.setDescription(request.getDescription());
        if (request.getPublished() != null) c.setPublished(request.getPublished());
        return toResponse(c);
    }

    public void deleteCourse(UUID instructorId, Long courseId) {
        Map<Long, Course> map = instructorCourses.getOrDefault(instructorId, Collections.emptyMap());
        if (!map.containsKey(courseId)) throw new NoSuchElementException("Course not found");
        map.remove(courseId);
    }

    public Module addModule(UUID instructorId, Long courseId, String title, String content) {
        Course c = getCourseOrThrow(instructorId, courseId);
        Module m = Module.builder().id(moduleIdSeq.getAndIncrement()).title(title).content(content).build();
        c.getModules().add(m);
        return m;
    }

    public Module updateModule(UUID instructorId, Long courseId, Long moduleId, String title, String content) {
        Course c = getCourseOrThrow(instructorId, courseId);
        Module m = c.getModules().stream().filter(mm -> Objects.equals(mm.getId(), moduleId))
                .findFirst().orElseThrow(() -> new NoSuchElementException("Module not found"));
        if (title != null) m.setTitle(title);
        if (content != null) m.setContent(content);
        return m;
    }

    public void deleteModule(UUID instructorId, Long courseId, Long moduleId) {
        Course c = getCourseOrThrow(instructorId, courseId);
        boolean removed = c.getModules().removeIf(mm -> Objects.equals(mm.getId(), moduleId));
        if (!removed) throw new NoSuchElementException("Module not found");
    }

    private Course getCourseOrThrow(UUID instructorId, Long courseId) {
        Course c = instructorCourses.getOrDefault(instructorId, Collections.emptyMap()).get(courseId);
        if (c == null) throw new NoSuchElementException("Course not found");
        return c;
    }

    private CourseResponse toResponse(Course c) {
        return CourseResponse.builder()
                .id(c.getId())
                .instructorId(c.getInstructorId())
                .title(c.getTitle())
                .description(c.getDescription())
                .published(c.isPublished())
                .build();
    }
}
