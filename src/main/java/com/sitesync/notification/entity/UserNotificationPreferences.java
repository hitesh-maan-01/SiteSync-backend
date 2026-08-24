package com.sitesync.notification.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * One row per user — created lazily on first preference update
 * (see UserNotificationPreferencesService.upsert).
 * Missing row = all defaults (true for every type) so existing
 * users are unaffected until they change a preference.
 */
@Entity
@Table(name = "user_notification_preferences")
public class UserNotificationPreferences {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "delivery_alerts", nullable = false)
    private boolean deliveryAlerts = true;

    @Column(name = "attendance_summary", nullable = false)
    private boolean attendanceSummary = true;

    @Column(name = "milestone_alerts", nullable = false)
    private boolean milestoneAlerts = true;

    @Column(name = "issue_reported", nullable = false)
    private boolean issueReported = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public boolean isDeliveryAlerts() { return deliveryAlerts; }
    public void setDeliveryAlerts(boolean v) { this.deliveryAlerts = v; }

    public boolean isAttendanceSummary() { return attendanceSummary; }
    public void setAttendanceSummary(boolean v) { this.attendanceSummary = v; }

    public boolean isMilestoneAlerts() { return milestoneAlerts; }
    public void setMilestoneAlerts(boolean v) { this.milestoneAlerts = v; }

    public boolean isIssueReported() { return issueReported; }
    public void setIssueReported(boolean v) { this.issueReported = v; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}