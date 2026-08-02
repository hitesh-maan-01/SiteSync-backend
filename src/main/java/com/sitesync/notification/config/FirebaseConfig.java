package com.sitesync.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Initializes the Firebase Admin SDK - a SEPARATE, SERVER-SIDE credential
 * from the google-services.json/GoogleService-Info.plist files documented
 * in FIREBASE_SETUP.md (those are client-side app registration files;
 * this is a service account key for the backend to authenticate AS your
 * Firebase project when calling the FCM send API).
 *
 * Setup required (not yet done anywhere else in this codebase):
 *   1. Firebase Console -> Project Settings -> Service Accounts
 *   2. "Generate new private key" -> downloads a JSON file
 *   3. Place it somewhere the server can read (NOT in source control -
 *      add to .gitignore) and point FIREBASE_SERVICE_ACCOUNT_PATH at it.
 *
 * If this file/env var is missing, initialize() throws at startup rather
 * than silently disabling push - a backend that can't send the alerts
 * this whole feature exists for should fail loudly, not pretend to work.
 */
@Component
public class FirebaseConfig {

    private final String serviceAccountPath;

    public FirebaseConfig(@Value("${app.firebase.service-account-path}") String serviceAccountPath) {
        this.serviceAccountPath = serviceAccountPath;
    }

    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }
        }
    }
}