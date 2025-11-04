package com.instructor.service.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ModuleUploadRequest {
    private String moduleName;
    private String description;
    private MultipartFile file;
}
