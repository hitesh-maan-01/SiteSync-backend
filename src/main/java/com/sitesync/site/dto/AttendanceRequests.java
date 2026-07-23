package com.sitesync.site.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Only the manual (no-photo, plain JSON) attendance path uses a request
 * DTO. The photoGps path is multipart/form-data with flattened fields
 * (location.latitude, location.longitude, etc. - see
 * SiteRepository._flatten() on the Flutter side) bound directly via
 * @RequestParam in AttendanceController, since a DTO can't cleanly
 * represent a mix of scalar form fields and a file part the way
 * @RequestBody represents JSON.
 */
public class AttendanceRequests {

    /** POST /api/attendance/mark (JSON body, manual fallback only) */
    public static class ManualAttendanceRequest {
        @NotBlank public String projectId;
        @NotBlank public String workerId;
        @NotBlank public String workerName;
        @NotBlank public String status;
        @NotBlank public String markedByUserId;
        public String manualReason;
    }
}