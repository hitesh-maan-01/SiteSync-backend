package com.sitesync.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * PATCH /api/v1/users/me/notification-preferences
 * Body: { "key": "deliveryAlerts", "enabled": false }
 *
 * One toggle at a time — matches the Flutter UI pattern where each
 * Switch fires immediately when flipped, not on a "Save" button.
 */
public class UpdateNotificationPreferenceRequest {

    @NotBlank
    public String key; // deliveryAlerts | attendanceSummary | milestoneAlerts | issueReported

    @NotNull
    public Boolean enabled;
}