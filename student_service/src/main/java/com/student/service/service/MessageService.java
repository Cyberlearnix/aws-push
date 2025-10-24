package com.student.service.service;

import com.student.service.dto.MessageResponse;
import com.student.service.entity.Message;
import com.student.service.entity.Student;
import com.student.service.exception.ResourceNotFoundException;
import com.student.service.repository.MessageRepository;
import com.student.service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final StudentRepository studentRepository;
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(UUID studentId) {
        log.info("Fetching messages for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<Message> messages = messageRepository.findByStudentId(studentId);
        
        return messages.stream()
                .map(message -> mapToResponse(message, studentId))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesForCourse(UUID studentId, Long courseId) {
        log.info("Fetching messages for student {} in course {}", studentId, courseId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<Message> messages = messageRepository.findMessagesByStudentIdAndCourseIdOrderByCreatedAt(studentId, courseId);
        
        return messages.stream()
                .map(message -> mapToResponse(message, studentId))
                .collect(Collectors.toList());
    }
    
    public void markMessageAsRead(UUID studentId, Long messageId) {
        log.info("Marking message {} as read for student {}", messageId, studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        
        // Verify that the message belongs to the student
        if (!message.getStudentId().equals(studentId)) {
            throw new ResourceNotFoundException("Message not found");
        }
        
        if (message.getStatus() == Message.MessageStatus.UNREAD) {
            message.setStatus(Message.MessageStatus.READ);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }
        
        log.info("Successfully marked message {} as read for student {}", messageId, studentId);
    }
    
    @Transactional(readOnly = true)
    public Long getUnreadMessageCount(UUID studentId) {
        log.info("Getting unread message count for student {}", studentId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        return messageRepository.countUnreadMessagesByStudentId(studentId);
    }
    
    private MessageResponse mapToResponse(Message message, UUID studentId) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setStudentId(studentId);
        response.setInstructorId(message.getInstructorId());
        response.setCourseId(message.getCourseId());
        response.setSubject(message.getSubject());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setStatus(message.getStatus());
        response.setReadAt(message.getReadAt());
        response.setRepliedAt(message.getRepliedAt());
        response.setParentMessageId(message.getParentMessageId());
        response.setAttachmentPath(message.getAttachmentPath());
        response.setAttachmentName(message.getAttachmentName());
        response.setCreatedAt(message.getCreatedAt());
        response.setUpdatedAt(message.getUpdatedAt());
        return response;
    }
}








