package com.sitesync.auth.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.auth.dto.UpdatePhoneRequest;
import com.sitesync.auth.dto.UserResponse;
import com.sitesync.auth.service.UserProfileService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    // GET /api/v1/users/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        UserResponse user = userProfileService.getCurrentUser(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    // POST /api/v1/users/me/photo (multipart/form-data)
    @PostMapping(value = "/me/photo", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfilePhoto(
            @RequestParam("photo") MultipartFile photo,
            Authentication authentication
    ) {
        UserResponse user = userProfileService.updateProfilePhoto(currentUserId(authentication), photo);
        return ResponseEntity.ok(ApiResponse.ok(user, "Profile photo updated."));
    }

    // POST /api/v1/users/me/phone
@PostMapping("/me/phone")
public ResponseEntity<ApiResponse<UserResponse>> updatePhoneNumber(
        @RequestBody @Valid UpdatePhoneRequest request,
        Authentication authentication
) {
    UserResponse user = userProfileService.updatePhoneNumber(
            currentUserId(authentication), request.phoneNumber);
    return ResponseEntity.ok(ApiResponse.ok(user, "Phone number linked."));
}
}