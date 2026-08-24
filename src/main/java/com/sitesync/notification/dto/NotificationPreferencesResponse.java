package com.sitesync.notification.dto;

import com.sitesync.notification.entity.UserNotificationPreferences;

public class NotificationPreferencesResponse {
    public boolean deliveryAlerts;
    public boolean attendanceSummary;
    public boolean milestoneAlerts;
    public boolean issueReported;

    public static NotificationPreferencesResponse fromEntity(
            UserNotificationPreferences prefs) {
        NotificationPreferencesResponse dto =
                new NotificationPreferencesResponse();
        dto.deliveryAlerts = prefs.isDeliveryAlerts();
        dto.attendanceSummary = prefs.isAttendanceSummary();
        dto.milestoneAlerts = prefs.isMilestoneAlerts();
        dto.issueReported = prefs.isIssueReported();
        return dto;
    }

    /**
     * Returns all-true defaults when no preference row exists yet —
     * avoids null checks at every call site.
     */
    public static NotificationPreferencesResponse defaults() {
        NotificationPreferencesResponse dto =
                new NotificationPreferencesResponse();
        dto.deliveryAlerts = true;
        dto.attendanceSummary = true;
        dto.milestoneAlerts = true;
        dto.issueReported = true;
        return dto;
    }
}