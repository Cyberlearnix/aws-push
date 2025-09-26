package com.instructor.service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class ContentService {

    // courseId -> list of resource names
    private final Map<Long, List<String>> courseResources = new ConcurrentHashMap<>();

    public String uploadResource(UUID instructorId, Long courseId, MultipartFile file) {
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("resource.bin");
        courseResources.computeIfAbsent(courseId, k -> new ArrayList<>()).add(name);
        return name;
    }

    public List<String> listResources(Long courseId) {
        return courseResources.getOrDefault(courseId, Collections.emptyList());
    }
}
