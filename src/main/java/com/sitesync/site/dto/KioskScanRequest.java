package com.sitesync.site.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * POST /api/v1/projects/{projectId}/attendance/kiosk-scan
 * supervisorId is NOT in the payload — extracted from JWT principal
 * in the controller so it can't be spoofed by the client.
 */
public class KioskScanRequest {

    @NotBlank
    public String workerId;

    // ISO-8601 timestamp sent by the client — used as a hint only.
    // The server sets the authoritative timestamp at insert time.
    // Kept in the payload for audit logging (shows when the badge
    // was physically presented vs when the server processed it).
    public String timestamp;
}