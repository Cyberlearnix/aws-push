package com.instructor.service.service;

import com.instructor.service.model.Announcement;
import com.instructor.service.model.Message;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CommunicationService {

    private final Map<Long, List<Announcement>> announcements = new ConcurrentHashMap<>();
    private final Map<Long, List<Message>> messages = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    public Announcement postAnnouncement(Long courseId, String title, String message) {
        Announcement a = Announcement.builder()
                .id(idSeq.getAndIncrement())
                .courseId(courseId)
                .title(title)
                .message(message)
                .createdAt(Instant.now())
                .build();
        announcements.computeIfAbsent(courseId, k -> new ArrayList<>()).add(a);
        return a;
    }

    public Message sendMessage(Long courseId, String subject, String body) {
        Message m = Message.builder()
                .id(idSeq.getAndIncrement())
                .courseId(courseId)
                .subject(subject)
                .message(body)
                .createdAt(Instant.now())
                .build();
        messages.computeIfAbsent(courseId, k -> new ArrayList<>()).add(m);
        return m;
    }

    public List<Announcement> listAnnouncements(Long courseId) {
        return announcements.getOrDefault(courseId, Collections.emptyList());
    }

    public List<Message> listMessages(Long courseId) {
        return messages.getOrDefault(courseId, Collections.emptyList());
    }
}
