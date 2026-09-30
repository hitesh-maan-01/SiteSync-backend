package com.sitesync.site.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.site.dto.AttendanceRequests.ManualAttendanceRequest;
import com.sitesync.site.dto.AttendanceResponse;
import com.sitesync.site.dto.KioskScanRequest;
import com.sitesync.site.dto.KioskScanResponse;
import com.sitesync.site.service.AttendanceService;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Two @PostMapping handlers share the same path
 * (ApiEndpoints.markAttendance = "/api/attendance/mark") but are
 * differentiated by consumes - Spring dispatches to whichever matches
 * the request's actual Content-Type. This mirrors
 * SiteRepository.markAttendance()'s own branching: multipart when
 * photoFilePath is provided, plain JSON otherwise.
 *
 * The multipart handler's parameter names match Flutter's flattened
 * multipart field names exactly - location.latitude, location.longitude,
 * location.accuracyMeters, withinGeofence, photo - since
 * SiteRepository._flatten() turns the nested location object into
 * dot-notation top-level form fields before sending.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    // GET /api/attendance/project/{projectId}/today
    @GetMapping("/project/{projectId}/today")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getTodaysAttendance(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        List<AttendanceResponse> records = attendanceService.getTodaysAttendanceForProject(
                UUID.fromString(projectId), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    // GET /api/attendance/project/{projectId}/date/{date}
    @GetMapping("/project/{projectId}/date/{date}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceForDate(
            @PathVariable String projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        List<AttendanceResponse> records = attendanceService.getAttendanceForProjectOnDate(
                UUID.fromString(projectId), currentUserId(authentication), date);
        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendance(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        List<AttendanceResponse> records = attendanceService.getAttendanceForProject(
                UUID.fromString(projectId), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    @PostMapping(value = "/mark", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendanceWithPhoto(
            @RequestParam("projectId") String projectId,
            @RequestParam("workerId") String workerId,
            @RequestParam("workerName") String workerName,
            @RequestParam("status") String status,
            @RequestParam(value = "location.latitude", required = false) Double locationLatitude,
            @RequestParam(value = "location.longitude", required = false) Double locationLongitude,
            @RequestParam(value = "location.accuracyMeters", required = false) Double locationAccuracyMeters,
            @RequestParam(value = "withinGeofence", required = false) Boolean withinGeofence,
            @RequestParam("photo") MultipartFile photo,
            Authentication authentication
    ) {
        AttendanceResponse response = attendanceService.markAttendanceWithPhoto(
                UUID.fromString(projectId),
                currentUserId(authentication),
                UUID.fromString(workerId),
                workerName,
                status,
                locationLatitude,
                locationLongitude,
                locationAccuracyMeters,
                withinGeofence,
                photo
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Attendance marked."));
    }

    @PostMapping(value = "/mark", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendanceManual(
            @Valid @RequestBody ManualAttendanceRequest request,
            Authentication authentication
    ) {
        AttendanceResponse response = attendanceService.markAttendanceManual(
                UUID.fromString(request.projectId),
                currentUserId(authentication),
                UUID.fromString(request.workerId),
                request.workerName,
                request.status,
                UUID.fromString(request.markedByUserId),
                request.manualReason
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Attendance marked."));
    }

        // POST /api/v1/projects/{projectId}/attendance/kiosk-scan
    @PostMapping("/project/{projectId}/kiosk-scan")
    public ResponseEntity<ApiResponse<KioskScanResponse>> kioskScan(
            @PathVariable String projectId,
            @Valid @RequestBody KioskScanRequest request,
            Authentication authentication) {

        KioskScanResponse response = attendanceService.processKioskScan(
                UUID.fromString(projectId),
                currentUserId(authentication),
                request);

        return ResponseEntity.ok(ApiResponse.ok(response,
                response.alreadyCheckedIn
                        ? "Worker already checked in today."
                        : "Check-in recorded."));
    }
}