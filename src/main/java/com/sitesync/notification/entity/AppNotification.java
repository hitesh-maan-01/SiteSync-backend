package com.sitesync.notification.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Persisted record of every push notification sent to a user.
 * Dual-purpose: powers the in-app Notification Center (history + read status)
 * and ensures notifications survive across app restarts, unlike FCM-only delivery
 * which is fire-and-forget with no delivery guarantee.
 *
 * Named AppNotification (not Notification) to avoid collision with
 * java.util.Notification.
 */
@Entity
@Table(name = "notifications")
public class AppNotification {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationSeverity type;

    // Wire value matching FCM data['type'] — used for deep-link routing on Flutter side.
    // Stored separately from severity so routing and visual styling are independent concerns.
    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    @Column(name = "action_label")
    private String actionLabel;

    // GoRouter route path, e.g. "/supervisor-dash/milestones/{projectId}"
    // Flutter NotificationCenterScreen uses context.push(actionRoute) directly.
    @Column(name = "action_route")
    private String actionRoute;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public NotificationSeverity getType() { return type; }
    public void setType(NotificationSeverity type) { this.type = type; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getActionLabel() { return actionLabel; }
    public void setActionLabel(String actionLabel) { this.actionLabel = actionLabel; }

    public String getActionRoute() { return actionRoute; }
    public void setActionRoute(String actionRoute) { this.actionRoute = actionRoute; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}