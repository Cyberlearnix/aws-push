package com.userservice.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadPhotoRequestDTO {
    private String photo; // store URL or key according to existing logic
}


