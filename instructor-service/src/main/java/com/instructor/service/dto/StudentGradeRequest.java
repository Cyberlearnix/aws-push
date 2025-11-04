package com.instructor.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StudentGradeRequest {
    @NotNull
    private UUID studentId;
    @NotNull
    private Double grade;
    private String remarks;
}
