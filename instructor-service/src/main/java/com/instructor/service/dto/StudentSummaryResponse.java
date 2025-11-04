package com.instructor.service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSummaryResponse {
    private UUID studentId;
    private String name;
    private String email;
    private double progressPercent;
}
