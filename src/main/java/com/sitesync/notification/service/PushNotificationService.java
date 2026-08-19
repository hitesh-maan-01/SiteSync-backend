package com.sitesync.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.sitesync.notification.entity.AppNotification;
import com.sitesync.notification.entity.NotificationSeverity;
import com.sitesync.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Sends FCM push AND persists to notification history in one call.
 * Callers (MilestoneService, AttendanceService, etc.) use the new
 * sendAndPersist() overload — the original send() is kept for any
 * call sites not yet migrated or cases where persistence isn't needed.
 *
 * Push failures are still swallowed — a notification failing to send
 * must never roll back the business operation that triggered it.
 * DB persistence failures are also logged-not-thrown for the same reason.
 */
@Service
public class PushNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(PushNotificationService.class);

    private final NotificationRepository notificationRepository;

    public PushNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Primary entry point for new call sites. Sends FCM push and persists
     * a notification history record for the recipient in the same call.
     *
     * @param recipientUserId  The UUID of the user who should receive this.
     *                         Used to persist the history record; FCM token
     *                         is passed separately since it's fetched by callers
     *                         who already have the User entity.
     * @param severity         Visual severity (CRITICAL/WARNING/SUCCESS/INFO)
     *                         shown in the Notification Center UI.
     * @param notificationType FCM data['type'] wire value — drives deep-link
     *                         routing on the Flutter side.
     * @param actionLabel      Optional button label on the notification card.
     * @param actionRoute      Optional GoRouter path — Flutter uses context.push().
     */
    public void sendAndPersist(
            String fcmToken,
            UUID recipientUserId,
            String title,
            String body,
            NotificationSeverity severity,
            String notificationType,
            String actionLabel,
            String actionRoute,
            Map<String, String> extraData
    ) {
        // 1. Build the full data map for FCM
        Map<String, String> data = new java.util.HashMap<>(extraData != null
                ? extraData : Map.of());
        data.put("type", notificationType);
        if (actionLabel != null) data.put("actionLabel", actionLabel);
        if (actionRoute != null) data.put("actionRoute", actionRoute);

        // 2. Send FCM (fire-and-forget, failures logged not thrown)
        send(fcmToken, title, body, data);

        // 3. Persist to notification history
        try {
            AppNotification record = new AppNotification();
            record.setUserId(recipientUserId);
            record.setTitle(title);
            record.setBody(body);
            record.setType(severity);
            record.setNotificationType(notificationType);
            record.setActionLabel(actionLabel);
            record.setActionRoute(actionRoute);
            notificationRepository.save(record);
        } catch (Exception e) {
            // Notification history persistence failure must not propagate —
            // same reasoning as FCM send failures.
            log.warn("Failed to persist notification history for user {}: {}",
                    recipientUserId, e.getMessage());
        }
    }

    /**
     * Original low-level send — kept for backward compatibility and for
     * cases where persistence isn't needed (e.g. token-refresh pings).
     */
    public void send(String fcmToken, String title, String body,
                     Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("Skipping push - no FCM token on file.");
            return;
        }

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                .putAllData(data)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("Failed to send push notification: {}", e.getMessage());
        }
    }
}