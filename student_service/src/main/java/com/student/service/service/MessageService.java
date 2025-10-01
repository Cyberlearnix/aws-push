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
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesForCourse(UUID studentId, UUID courseId) {
        log.info("Fetching messages for student {} in course {}", studentId, courseId);
        
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<Message> messages = messageRepository.findMessagesByStudentIdAndCourseIdOrderByCreatedAt(studentId, courseId);
        
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public void markMessageAsRead(UUID studentId, UUID messageId) {
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
    
    private MessageResponse mapToResponse(Message message) {
        if (message == null) {
            return null;
        }
        
        MessageResponse response = new MessageResponse();
        response.setId(message.getId() != null ? message.getId().toString() : null);
        response.setStudentId(message.getStudentId() != null ? message.getStudentId().toString() : null);
        
        // Set student name if student entity is available
        if (message.getStudent() != null) {
            String studentName = message.getStudent().getFirstName();
            if (message.getStudent().getLastName() != null) {
                studentName += " " + message.getStudent().getLastName();
            }
            response.setStudentName(studentName);
        }
        
        response.setInstructorId(message.getInstructorId() != null ? message.getInstructorId().toString() : null);
        response.setInstructorName(message.getInstructorName());
        response.setCourseId(message.getCourseId() != null ? message.getCourseId().toString() : null);
        response.setSubject(message.getSubject());
        response.setContent(message.getContent());
        
        // Handle enum to string conversion
        if (message.getType() != null) {
            response.setType(message.getType().name());
        }
        
        if (message.getStatus() != null) {
            response.setStatus(message.getStatus().name());
        }
        
        response.setReadAt(message.getReadAt());
        response.setRepliedAt(message.getRepliedAt());
        response.setParentMessageId(message.getParentMessageId() != null ? 
                                  message.getParentMessageId().toString() : null);
        response.setAttachmentPath(message.getAttachmentPath());
        response.setAttachmentName(message.getAttachmentName());
        response.setCreatedAt(message.getCreatedAt());
        response.setUpdatedAt(message.getUpdatedAt());
        return response;
    }
}








