package com.sitesync.analytics.controller;

import com.sitesync.analytics.dto.ProjectAnalyticsResponse;
import com.sitesync.analytics.service.ProjectAnalyticsService;
import com.sitesync.auth.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class ProjectAnalyticsController {

    private final ProjectAnalyticsService analyticsService;

    /**
     * GET /api/v1/analytics/project/{projectId}?days=30
     * days must be 7, 30, or 90. Defaults to 30 if omitted.
     * Returns all 5 analytics sections in one response.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<ProjectAnalyticsResponse>> getAnalytics(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {

        if (days!=1 && days != 7 && days != 30 && days != 90) {
            throw new IllegalArgumentException(
                    "days must be 7, 30, or 90.");
        }

        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.getAnalytics(
                        UUID.fromString(projectId), days)));
    }
}