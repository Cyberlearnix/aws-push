package com.instructor.service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private UUID instructorId;
    private String title;
    private String description;
    private boolean published;
}
