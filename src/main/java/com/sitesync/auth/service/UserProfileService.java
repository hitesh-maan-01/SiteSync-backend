package com.sitesync.auth.service;

import com.sitesync.auth.dto.UserResponse;
import com.sitesync.auth.entity.User;
import com.sitesync.auth.repository.UserRepository;
import com.sitesync.site.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Handles the currently-authenticated user's own profile — separate from
 * any admin-style "manage other users" service, since every method here
 * is implicitly scoped to "self" via the JWT principal, never a
 * caller-supplied user id.
 */
@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public UserProfileService(UserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return UserResponse.fromEntity(user);
    }

    /**
     * Replaces the user's profile photo. No cleanup of the previous
     * photo's file is performed here — FileStorageService's storage
     * backend (local disk / S3 / etc.) is assumed to handle orphaned
     * file lifecycle separately, matching how DeliveryService.markDelivered
     * doesn't clean up anything either. Worth revisiting if storage costs
     * become a concern.
     */
    @Transactional
    public UserResponse updateProfilePhoto(UUID userId, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("A photo is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String photoUrl = fileStorageService.store(photo, "profile-photos");
        user.setProfilePhotoUrl(photoUrl);

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }
}