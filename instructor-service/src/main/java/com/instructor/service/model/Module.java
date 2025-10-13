package com.instructor.service.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module {
    private Long id;
    private String title;
    private String content; // could be markdown, url, etc.
}
