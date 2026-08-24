package com.sitesync.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.sitesync.notification.entity.NotificationSeverity;
import com.sitesync.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PushNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(PushNotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserNotificationPreferencesService prefsService;

    public PushNotificationService(
            NotificationRepository notificationRepository,
            UserNotificationPreferencesService prefsService) {
        this.notificationRepository = notificationRepository;
        this.prefsService = prefsService;
    }

    /**
     * Checks recipient's notification preferences BEFORE sending.
     * If the user has disabled this notification type, the push is
     * skipped entirely — but the notification IS still persisted to
     * history so they can read it in the Notification Center later.
     * This matches the expected UX: "I turned off push alerts for
     * deliveries" should mean no banner/sound, not "I never want to
     * know about deliveries at all."
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
            Map<String, String> extraData) {

        // 1. Always persist to notification history regardless of preference.
        //    User can still read it in the Notification Center.
        persistToHistory(recipientUserId, title, body, severity,
                notificationType, actionLabel, actionRoute);

        // 2. Only send FCM push if user has this type enabled.
        boolean pushEnabled = prefsService.isEnabled(
                recipientUserId, notificationType);

        if (!pushEnabled) {
            log.debug("Push suppressed for user {} type {} (user preference)",
                    recipientUserId, notificationType);
            return;
        }

        // 3. Build data map and send
        Map<String, String> data = new HashMap<>(
                extraData != null ? extraData : Map.of());
        data.put("type", notificationType);
        if (actionLabel != null) data.put("actionLabel", actionLabel);
        if (actionRoute != null) data.put("actionRoute", actionRoute);

        send(fcmToken, title, body, data);
    }

    private void persistToHistory(
            UUID userId, String title, String body,
            NotificationSeverity severity, String notificationType,
            String actionLabel, String actionRoute) {
        try {
            com.sitesync.notification.entity.AppNotification record =
                    new com.sitesync.notification.entity.AppNotification();
            record.setUserId(userId);
            record.setTitle(title);
            record.setBody(body);
            record.setType(severity);
            record.setNotificationType(notificationType);
            record.setActionLabel(actionLabel);
            record.setActionRoute(actionRoute);
            notificationRepository.save(record);
        } catch (Exception e) {
            log.warn("Failed to persist notification history: {}",
                    e.getMessage());
        }
    }

    public void send(String fcmToken, String title, String body,
                     Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("Skipping push — no FCM token.");
            return;
        }

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM send failed: {}", e.getMessage());
        }
    }
}