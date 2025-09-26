package com.instructor.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Module {
    private Long id;
    private String title;
    private String content; // could be markdown, url, etc.
}
