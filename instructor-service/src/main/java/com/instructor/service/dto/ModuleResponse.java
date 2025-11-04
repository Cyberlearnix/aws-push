package com.instructor.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleResponse {
    private Long id;
    private Long courseId;
    private String title;
    private String content;
    private String videoUrl;
    private String thumbnailUrl;
    private String contentType;
    private String status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Success response factory method
    public static ModuleResponse success(Long id, Long courseId, String title, String content, 
                                        String videoUrl, String thumbnailUrl, String contentType) {
        return ModuleResponse.builder()
                .id(id)
                .courseId(courseId)
                .title(title)
                .content(content)
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .contentType(contentType)
                .status("SUCCESS")
                .message("Module created successfully")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
