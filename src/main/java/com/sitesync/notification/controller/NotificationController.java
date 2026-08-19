package com.sitesync.notification.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.notification.dto.NotificationResponse;
import com.sitesync.notification.service.NotificationHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationHistoryService notificationHistoryService;

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    // GET /api/v1/notifications
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationHistoryService.getForUser(currentUserId(authentication))));
    }

    // PUT /api/v1/notifications/{id}/read
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String id,
            Authentication authentication) {
        notificationHistoryService.markAsRead(
                UUID.fromString(id), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(null, "Marked as read."));
    }

    // PUT /api/v1/notifications/read-all
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            Authentication authentication) {
        notificationHistoryService.markAllAsRead(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(null, "All marked as read."));
    }

    // GET /api/v1/notifications/unread-count
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationHistoryService.getUnreadCount(
                        currentUserId(authentication))));
    }
}