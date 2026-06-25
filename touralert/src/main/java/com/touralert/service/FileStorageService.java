package com.touralert.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // Define storage path targeted directly at our static resource folder
    private final Path storageLocation = Paths.get("src/main/resources/static/uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(this.storageLocation);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize local storage directories for image uploads.", e);
        }
    }

    public String storeFile(MultipartFile file) {
        // Sanitize path names to prevent Directory Traversal attacks
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Security Exception: Filename contains invalid path sequences " + originalFilename);
            }

            // Generate unique identifier token so files with identical names don't overwrite each other
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String secureFilename = UUID.randomUUID().toString() + fileExtension;

            // Resolve target path destination
            Path targetLocation = this.storageLocation.resolve(secureFilename);
            
            // Stream write bytes directly onto server drive
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative public web address route URL path
            return "/uploads/" + secureFilename;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to save asset file " + originalFilename + ". Please verify disk access.", e);
        }
    }
}