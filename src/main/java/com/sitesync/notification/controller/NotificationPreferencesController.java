package com.sitesync.notification.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.notification.dto.NotificationPreferencesResponse;
import com.sitesync.notification.dto.UpdateNotificationPreferenceRequest;
import com.sitesync.notification.service.UserNotificationPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferencesController {

    private final UserNotificationPreferencesService prefsService;

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    // GET /api/v1/users/me/notification-preferences
    // Called on SettingsScreen load to sync local state with backend truth.
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> getPreferences(
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                prefsService.getPreferences(currentUserId(authentication))));
    }

    // PATCH /api/v1/users/me/notification-preferences
    // Body: { "key": "deliveryAlerts", "enabled": false }
    @PatchMapping
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> updatePreference(
            @Valid @RequestBody UpdateNotificationPreferenceRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                prefsService.updatePreference(
                        currentUserId(authentication), request),
                "Preference updated."));
    }
}