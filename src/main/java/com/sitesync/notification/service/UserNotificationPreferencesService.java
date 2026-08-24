package com.sitesync.notification.service;

import com.sitesync.notification.dto.NotificationPreferencesResponse;
import com.sitesync.notification.dto.UpdateNotificationPreferenceRequest;
import com.sitesync.notification.entity.UserNotificationPreferences;
import com.sitesync.notification.repository.UserNotificationPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserNotificationPreferencesService {

    private final UserNotificationPreferencesRepository prefsRepository;

    /**
     * Returns current preferences for a user.
     * If no row exists yet, returns all-true defaults without creating a row —
     * the row is created lazily only on first update.
     */
    public NotificationPreferencesResponse getPreferences(UUID userId) {
        return prefsRepository.findByUserId(userId)
                .map(NotificationPreferencesResponse::fromEntity)
                .orElse(NotificationPreferencesResponse.defaults());
    }

    /**
     * Updates a single preference key. Creates the preferences row
     * if it doesn't exist yet (upsert via findOrCreate).
     * Throws IllegalArgumentException for unknown keys so the Flutter
     * side gets a clean 400 rather than a silent no-op.
     */
    @Transactional
    public NotificationPreferencesResponse updatePreference(
            UUID userId, UpdateNotificationPreferenceRequest request) {

        UserNotificationPreferences prefs = prefsRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    UserNotificationPreferences newPrefs =
                            new UserNotificationPreferences();
                    newPrefs.setUserId(userId);
                    return newPrefs;
                });

        switch (request.key) {
            case "deliveryAlerts"    -> prefs.setDeliveryAlerts(request.enabled);
            case "attendanceSummary" -> prefs.setAttendanceSummary(request.enabled);
            case "milestoneAlerts"   -> prefs.setMilestoneAlerts(request.enabled);
            case "issueReported"     -> prefs.setIssueReported(request.enabled);
            default -> throw new IllegalArgumentException(
                    "Unknown notification preference key: " + request.key);
        }

        return NotificationPreferencesResponse.fromEntity(
                prefsRepository.save(prefs));
    }

    /**
     * Called by PushNotificationService before sending any push —
     * checks whether the recipient has this notification type enabled.
     * Returns true (send) when no preference row exists yet (defaults to on).
     */
    public boolean isEnabled(UUID userId, String notificationType) {
        return prefsRepository.findByUserId(userId)
                .map(prefs -> switch (notificationType) {
                    case "materialDelay"      -> prefs.isDeliveryAlerts();
                    case "checkInRedFlag"     -> prefs.isAttendanceSummary();
                    case "milestoneCompleted" -> prefs.isMilestoneAlerts();
                    case "issueReported"      -> prefs.isIssueReported();
                    default                   -> true; // unknown type → send by default
                })
                .orElse(true); // no row → all defaults on
    }
}