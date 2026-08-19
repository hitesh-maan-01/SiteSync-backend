package com.sitesync.notification.dto;

import com.sitesync.notification.entity.AppNotification;

public class NotificationResponse {
    public String id;
    public String userId;
    public String title;
    public String body;
    public String type;           // CRITICAL / WARNING / SUCCESS / INFO
    public String notificationType; // FCM routing type
    public String actionLabel;
    public String actionRoute;
    public boolean isRead;
    public String createdAt;

    public static NotificationResponse fromEntity(AppNotification n) {
        NotificationResponse dto = new NotificationResponse();
        dto.id = n.getId().toString();
        dto.userId = n.getUserId().toString();
        dto.title = n.getTitle();
        dto.body = n.getBody();
        dto.type = n.getType().name();
        dto.notificationType = n.getNotificationType();
        dto.actionLabel = n.getActionLabel();
        dto.actionRoute = n.getActionRoute();
        dto.isRead = n.isRead();
        dto.createdAt = n.getCreatedAt().toString();
        return dto;
    }
}