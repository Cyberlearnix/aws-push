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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunicationService {

    private final AnnouncementRepository announcementRepository;
    private final MessageRepository messageRepository;
    private final CourseRepository courseRepository;
    private final EntityMapper entityMapper;

    public Announcement postAnnouncement(Long courseId, String title, String message) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        
        AnnouncementEntity announcementEntity = entityMapper.createAnnouncementEntity(courseId, title, message, course);
        announcementEntity = announcementRepository.save(announcementEntity);
        
        return entityMapper.toLegacyAnnouncement(announcementEntity);
    }

    public Message sendMessage(Long courseId, String subject, String body) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        
        MessageEntity messageEntity = entityMapper.createMessageEntity(courseId, subject, body, course);
        messageEntity = messageRepository.save(messageEntity);
        
        return entityMapper.toLegacyMessage(messageEntity);
    }

    @Transactional(readOnly = true)
    public List<Announcement> listAnnouncements(Long courseId) {
        return announcementRepository.findByCourseIdAndIsActiveTrueOrderByCreatedAtDesc(courseId)
                .stream()
                .map(entityMapper::toLegacyAnnouncement)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Message> listMessages(Long courseId) {
        return messageRepository.findByCourseIdAndIsActiveTrueOrderByCreatedAtDesc(courseId)
                .stream()
                .map(entityMapper::toLegacyMessage)
                .collect(Collectors.toList());
    }
}
