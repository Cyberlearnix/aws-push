package com.instructor.service.service;

import com.instructor.service.entity.AnnouncementEntity;
import com.instructor.service.entity.CourseEntity;
import com.instructor.service.entity.MessageEntity;
import com.instructor.service.mapper.EntityMapper;
import com.instructor.service.model.Announcement;
import com.instructor.service.model.Message;
import com.instructor.service.repository.AnnouncementRepository;
import com.instructor.service.repository.CourseRepository;
import com.instructor.service.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunicationService {

    private final AnnouncementRepository announcementRepository;
    private final MessageRepository messageRepository;
    private final CourseRepository courseRepository;
    private final EntityMapper entityMapper;
    private static final Logger log = LoggerFactory.getLogger(CommunicationService.class);

    @Transactional
    public Announcement createAnnouncement(Long courseId, String title, String message, UUID instructorId) {
        log.debug("Creating announcement for course: {}, instructor: {}", courseId, instructorId);
        
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
                
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the course instructor can create announcements");
        }
                
        AnnouncementEntity announcement = AnnouncementEntity.builder()
                .title(title)
                .message(message)
                .course(course)
                .instructorId(instructorId)
                .build();
                
        announcement = announcementRepository.save(announcement);
        return entityMapper.toAnnouncement(announcement);
    }

    @Transactional
    public Message sendMessage(Long courseId, String subject, String body, UUID senderId, String senderName) {
        log.debug("Sending message for course: {}, sender: {}", courseId, senderId);
        
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
                
        // Verify sender is either the instructor or enrolled in the course
        if (!course.getInstructor().getId().equals(senderId)) {
            // TODO: Add check for enrolled students once enrollment is implemented
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only course participants can send messages");
        }
                
        MessageEntity message = MessageEntity.builder()
                .subject(subject)
                .message(body)
                .senderId(senderId)
                .senderName(senderName)
                .course(course)
                .build();
                
        message = messageRepository.save(message);
        return entityMapper.toMessage(message);
    }

    @Transactional(readOnly = true)
    public List<Announcement> getAnnouncements(Long courseId) {
        log.debug("Fetching announcements for course: {}", courseId);
        
        if (!courseRepository.existsById(courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        
        return announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(entityMapper::toAnnouncement)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Message> getMessages(Long courseId) {
        log.debug("Fetching messages for course: {}", courseId);
        
        if (!courseRepository.existsById(courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        
        return messageRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(entityMapper::toMessage)
                .collect(Collectors.toList());
    }
}
