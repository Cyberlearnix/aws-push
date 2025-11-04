package com.instructor.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorResponse {
    private Long id;
    private UUID userId;
    private String email;
    private String name;
    private String department;
    private String designation;
    private String qualification;
    private String bio;
    private String specialization;
    private Integer experienceYears;
    private boolean active;
}
