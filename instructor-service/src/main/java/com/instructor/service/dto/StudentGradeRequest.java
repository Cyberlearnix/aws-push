package com.instructor.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentGradeRequest {
    @NotNull
    private Long studentId;
    @NotNull
    private Double grade;
    private String remarks;
}
