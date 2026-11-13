package com.sitesync.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    // On Render: set FIREBASE_CREDENTIALS_JSON = paste entire JSON contents
    @Value("${FIREBASE_CREDENTIALS_JSON:}")
    private String credentialsJson;

    // Local dev fallback: still reads from file
    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream stream;
            if (credentialsJson != null && !credentialsJson.isBlank()) {
                stream = new ByteArrayInputStream(
                    credentialsJson.getBytes(StandardCharsets.UTF_8));
            } else {
                stream = getClass().getClassLoader()
                    .getResourceAsStream("sitesync-firebase-adminsdk.json");
                if (stream == null) throw new IllegalStateException(
                    "Firebase credentials missing. Set FIREBASE_CREDENTIALS_JSON env var.");
            }
            FirebaseApp.initializeApp(FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build());
        }
    }
}