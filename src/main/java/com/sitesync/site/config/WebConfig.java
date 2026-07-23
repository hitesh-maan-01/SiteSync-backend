package com.sitesync.site.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves files saved by FileStorageService at /uploads/** - must match
 * that service's publicBaseUrl + "/uploads/..." URL construction exactly,
 * or every photo URL returned to Flutter (AttendanceResponse.photoUrl,
 * MaterialResponse.deliveryPhotoUrl) will 404 when the app tries to
 * display them.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public WebConfig(@Value("${app.storage.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replaceAll("/$", "") + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}