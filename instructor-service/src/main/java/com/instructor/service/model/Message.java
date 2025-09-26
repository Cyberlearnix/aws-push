package com.instructor.service.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Message {
    private Long id;
    private Long courseId;
    private String subject;
    private String message;
    private Instant createdAt;
}
