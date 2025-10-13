package com.instructor.service.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    private Long id;
    private UUID instructorId;
    private String title;
    private String description;
    private boolean published;
    @Builder.Default
    private List<Module> modules = new ArrayList<>();
}
