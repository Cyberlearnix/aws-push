package com.instructor.service.service;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.instructor.service.config.GoogleDriveConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Drive driveService;
    private final GoogleDriveConfig googleDriveConfig;

    @Value("${file.allowed-extensions}")
    private String allowedExtensions;

    /**
     * Uploads a file to Google Drive and returns the web content link
     * @param file The file to upload
     * @return Web content link to the uploaded file
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";

            // Validate file extension
            if (!isValidFileExtension(fileExtension)) {
                throw new IllegalArgumentException("Invalid file type. Allowed types: " + allowedExtensions);
            }

            // Create file metadata
            String fileName = UUID.randomUUID().toString() + fileExtension;
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(googleDriveConfig.getFolderId()));

            // Upload file content
            ByteArrayContent mediaContent = new ByteArrayContent(
                file.getContentType(),
                file.getBytes()
            );

            // Upload file to Google Drive
            File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, mimeType")
                .execute();

            // Make the file publicly accessible and get the direct preview link
            if (uploadedFile != null && uploadedFile.getId() != null) {
                setFilePublic(uploadedFile.getId());
                
                // Generate direct preview link based on file type
                String fileId = uploadedFile.getId();
                String mimeType = uploadedFile.getMimeType();
                String previewLink;
                
                if (mimeType != null && mimeType.startsWith("video/")) {
                    // For videos, use the embeddable video URL format
                    previewLink = "https://drive.google.com/file/d/" + fileId + "/preview";
                    log.info("Generated video preview URL: {}", previewLink);
                } else if (mimeType != null && mimeType.startsWith("image/")) {
                    // For images, use the direct preview URL
                    previewLink = "https://drive.google.com/uc?export=view&id=" + fileId;
                } else {
                    // For other file types, use the direct download link
                    previewLink = "https://drive.google.com/uc?export=download&id=" + fileId;
                }
                
                log.info("File uploaded to Google Drive. ID: {}, Type: {}, Preview: {}", 
                        fileId, mimeType, previewLink);
                return previewLink;
            }

            throw new RuntimeException("Failed to upload file to Google Drive");

        } catch (Exception ex) {
            log.error("Could not upload file to Google Drive: {}", ex.getMessage(), ex);
            throw new RuntimeException("Could not upload file to Google Drive: " + ex.getMessage(), ex);
        }
    }

    /**
     * Sets the file's permission to public
     * @param fileId The ID of the file
     */
    private void setFilePublic(String fileId) {
        try {
            Permission permission = new Permission()
                .setType("anyone")
                .setRole("reader");
            
            driveService.permissions().create(fileId, permission)
                .setFields("id")
                .execute();
        } catch (Exception e) {
            log.error("Error setting file permission to public: {}", e.getMessage(), e);
            throw new RuntimeException("Error setting file permission to public", e);
        }
    }

    /**
     * Validates if the file extension is allowed
     */
    private boolean isValidFileExtension(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            return false;
        }
        String[] allowed = allowedExtensions.split(",");
        for (String ext : allowed) {
            if (ext.trim().equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }
}
