package com.sitesync.site.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

/**
 * Stores uploaded files in Supabase Storage via the S3-compatible API.
 * Replaces the previous local-disk implementation — same method signature,
 * all callers (AttendanceService, DeliveryService, UserProfileService)
 * require zero changes.
 *
 * Files are stored at: <bucket>/<subfolder>/<uuid>.<ext>
 * Public URL returned:  <publicBaseUrl>/<subfolder>/<uuid>.<ext>
 *
 * The bucket must already exist in Supabase Dashboard → Storage → Buckets
 * and must be set to PUBLIC visibility if Flutter loads images directly
 * from the URL (no signed URLs). If you need private files with signed
 * URLs, replace the URL construction below with a presigned URL generation
 * call via s3Client.presignGetObject().
 */
@Service
public class FileStorageService {

    private static final Logger log =
            LoggerFactory.getLogger(FileStorageService.class);

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public FileStorageService(
            S3Client s3Client,
            @Value("${supabase.storage.bucket}") String bucket,
            @Value("${supabase.storage.public-base-url}") String publicBaseUrl
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
    }

    /**
     * Uploads a file to Supabase Storage and returns its public URL.
     *
     * @param file      The uploaded multipart file from a controller request.
     * @param subfolder Logical grouping prefix, e.g. "attendance", "deliveries",
     *                  "profile-photos". Becomes a path prefix in the bucket:
     *                  sitesync-media/attendance/<uuid>.jpg
     * @return          Publicly accessible URL for the stored file.
     * @throws IllegalArgumentException if the file is empty.
     * @throws RuntimeException         if the upload fails after reaching Supabase.
     */
    public String store(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        String objectKey = subfolder + "/" + UUID.randomUUID() + extension;
        String contentType = resolveContentType(file.getContentType(), extension);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromInputStream(
                            file.getInputStream(), file.getSize()));

            String publicUrl = publicBaseUrl + "/" + objectKey;
            log.debug("Stored file at: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to read uploaded file bytes for {}", objectKey, e);
            throw new RuntimeException("Failed to read uploaded file.", e);
        } catch (S3Exception e) {
            log.error("Supabase Storage upload failed for key {}: {} ({})",
                    objectKey, e.awsErrorDetails().errorMessage(),
                    e.statusCode());
            throw new RuntimeException(
                    "Failed to upload file to storage: "
                            + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * Deletes a previously stored file by its full public URL.
     * Callers are not required to use this — orphaned files from
     * superseded profile photos etc. are acceptable for now — but it's
     * provided for explicit cleanup flows if needed later.
     *
     * @param publicUrl The URL returned by a previous store() call.
     */
    public void delete(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) return;

        // Derive the object key by stripping the base URL prefix
        if (!publicUrl.startsWith(publicBaseUrl)) {
            log.warn("delete() called with URL not matching configured bucket: {}",
                    publicUrl);
            return;
        }

        String objectKey = publicUrl.substring(publicBaseUrl.length() + 1);

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
            log.debug("Deleted file: {}", objectKey);
        } catch (S3Exception e) {
            // Non-fatal — log and continue. A failed delete leaves an
            // orphaned file in storage but does not affect business logic.
            log.warn("Failed to delete file {}: {}", objectKey,
                    e.awsErrorDetails().errorMessage());
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename
                .substring(originalFilename.lastIndexOf('.'))
                .toLowerCase();
    }

    private String resolveContentType(String declaredContentType, String extension) {
        // Prefer the browser/client-declared content type if it looks valid
        if (declaredContentType != null
                && declaredContentType.startsWith("image/")) {
            return declaredContentType;
        }
        // Fall back to extension-based detection
        return switch (extension) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png"          -> "image/png";
            case ".webp"         -> "image/webp";
            case ".heic"         -> "image/heic";
            case ".pdf"          -> "application/pdf";
            default              -> "application/octet-stream";
        };
    }
}