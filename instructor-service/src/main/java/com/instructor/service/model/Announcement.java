package com.instructor.service.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {
    private Long id;
    private Long courseId;
    private String title;
    private String message;
    private Instant createdAt;
}
