package com.instructor.service.dto;

import com.instructor.service.entity.ContentType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class ModuleRequest implements Serializable {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String content;
    
    private String videoUrl;
    
    private String thumbnailUrl;
    
    private ContentType contentType = ContentType.TEXT;
    
    // Transient fields for file uploads (not persisted)
    private transient MultipartFile videoFile;
    private transient MultipartFile thumbnailFile;
}
