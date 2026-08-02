package com.sitesync.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Sends a single push notification to one device token via Firebase
 * Admin SDK. Depends on FirebaseConfig having already initialized
 * FirebaseApp — FirebaseMessaging.getInstance() will throw clearly if
 * that never happened, so a misconfiguration fails loudly rather than
 * silently no-op-ing.
 *
 * The data payload keys (type, projectId) mirror what
 * notification_service.dart's onNotificationTap handler expects for
 * deep-linking — see main.dart's NotificationType switch.
 */
@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    /**
     * Sends a push. Failures (invalid/expired token, network issue) are
     * logged, not thrown — a push failing to send should never fail the
     * business operation that triggered it.
     */
    public void send(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("Skipping push - no FCM token on file for this recipient.");
            return;
        }

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("Failed to send push notification: {}", e.getMessage());
        }
    }
}