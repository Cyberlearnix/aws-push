package com.instructor.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModuleRequest {
    @NotBlank
    private String title;
    private String content; // could be markdown/url/etc
}
