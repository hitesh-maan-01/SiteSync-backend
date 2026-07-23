package com.sitesync.site.dto;

import com.sitesync.site.entity.Attendance;

/**
 * Field names must match attendance_model.dart's AttendanceModel.fromJson
 * exactly. The nested location object mirrors LocationModel.fromJson's
 * shape (latitude, longitude, accuracyMeters, capturedAt) - reconstructed
 * here from the flat location_lat/location_lng/location_accuracy_meters
 * DB columns, since Postgres has no native nested-object column type
 * (same reasoning Flutter's multipart flattening comment already notes).
 */
public class AttendanceResponse {
    public String id;
    public String projectId;
    public String workerId;
    public String workerName;
    public String status;
    public String method;
    public String timestamp;
    public String photoUrl;
    public LocationDto location;
    public Boolean withinGeofence;
    public String markedByUserId;
    public String manualReason;

    public static class LocationDto {
        public double latitude;
        public double longitude;
        public Double accuracyMeters;
        public String capturedAt;
    }

    public static AttendanceResponse fromEntity(Attendance attendance) {
        AttendanceResponse dto = new AttendanceResponse();
        dto.id = attendance.getId().toString();
        dto.projectId = attendance.getProjectId().toString();
        dto.workerId = attendance.getWorkerId().toString();
        dto.workerName = attendance.getWorkerName();
        dto.status = attendance.getStatus().toWireValue();
        dto.method = attendance.getMethod().toWireValue();
        dto.timestamp = attendance.getTimestamp().toString();
        dto.photoUrl = attendance.getPhotoUrl();
        dto.withinGeofence = attendance.getWithinGeofence();
        dto.markedByUserId = attendance.getMarkedByUserId() != null
                ? attendance.getMarkedByUserId().toString() : null;
        dto.manualReason = attendance.getManualReason();

        if (attendance.getLocationLat() != null && attendance.getLocationLng() != null) {
            LocationDto location = new LocationDto();
            location.latitude = attendance.getLocationLat();
            location.longitude = attendance.getLocationLng();
            location.accuracyMeters = attendance.getLocationAccuracyMeters();
            location.capturedAt = attendance.getTimestamp().toString();
            dto.location = location;
        }

        return dto;
    }
}