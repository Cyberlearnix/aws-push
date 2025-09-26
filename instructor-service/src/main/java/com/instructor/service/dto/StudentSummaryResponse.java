package com.instructor.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentSummaryResponse {
    private Long studentId;
    private String name;
    private String email;
    private double progressPercent;
}
