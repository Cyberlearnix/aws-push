package com.instructor.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank
    private String subject;
    @NotBlank
    private String message;
}
