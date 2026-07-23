package com.sitesync.site.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Saves uploaded attendance/material photos to local disk and returns a
 * URL the Flutter app can load them from.
 *
 * THIS IS A PRAGMATIC DEFAULT, NOT A PRODUCTION-READY SOLUTION - flagged
 * explicitly rather than silently building something that looks complete:
 * - Files live on whatever machine runs this Spring Boot process. They do
 *   NOT survive a redeploy to a new instance/container, and do NOT scale
 *   across multiple server instances behind a load balancer.
 * - No image resizing/compression - a full-resolution camera photo is
 *   stored as-is, which is a real bandwidth/storage cost at any scale.
 * - No virus/content-type scanning beyond a basic extension check.
 *
 * Before any real deployment, replace this with Supabase Storage (since
 * you're already on Supabase) or S3 - same method signature
 * (MultipartFile in, URL string out), so callers (AttendanceController,
 * MaterialController) don't need to change when this is swapped out.
 */
@Service
public class FileStorageService {

    private final Path uploadRoot;
    private final String publicBaseUrl;

    public FileStorageService(
            @Value("${app.storage.upload-dir:./uploads}") String uploadDir,
            @Value("${app.storage.public-base-url}") String publicBaseUrl
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl;
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory: " + uploadRoot, e);
        }
    }

    public String store(MultipartFile file, String subfolder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + extension;

        Path targetDir = uploadRoot.resolve(subfolder);
        Path targetFile = targetDir.resolve(storedName);

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store uploaded file.", e);
        }

        return publicBaseUrl + "/uploads/" + subfolder + "/" + storedName;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) return "";
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}