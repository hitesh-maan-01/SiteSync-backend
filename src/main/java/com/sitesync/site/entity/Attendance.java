package com.sitesync.site.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Column(name = "worker_name", nullable = false)
    private String workerName;

    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(nullable = false)
    private AttendanceMethod method;

    @Column(name = "\"timestamp\"", nullable = false)
    private Instant timestamp;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "location_lat")
    private Double locationLat;

    @Column(name = "location_lng")
    private Double locationLng;

    @Column(name = "location_accuracy_meters")
    private Double locationAccuracyMeters;

    @Column(name = "within_geofence")
    private Boolean withinGeofence;

    @Column(name = "marked_by_user_id")
    private UUID markedByUserId;

    @Column(name = "manual_reason")
    private String manualReason;

    @PrePersist
    void onCreate() {
        if (timestamp == null) timestamp = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public UUID getWorkerId() { return workerId; }
    public void setWorkerId(UUID workerId) { this.workerId = workerId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public AttendanceMethod getMethod() { return method; }
    public void setMethod(AttendanceMethod method) { this.method = method; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Double getLocationLat() { return locationLat; }
    public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }

    public Double getLocationLng() { return locationLng; }
    public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }

    public Double getLocationAccuracyMeters() { return locationAccuracyMeters; }
    public void setLocationAccuracyMeters(Double locationAccuracyMeters) { this.locationAccuracyMeters = locationAccuracyMeters; }

    public Boolean getWithinGeofence() { return withinGeofence; }
    public void setWithinGeofence(Boolean withinGeofence) { this.withinGeofence = withinGeofence; }

    public UUID getMarkedByUserId() { return markedByUserId; }
    public void setMarkedByUserId(UUID markedByUserId) { this.markedByUserId = markedByUserId; }

    public String getManualReason() { return manualReason; }
    public void setManualReason(String manualReason) { this.manualReason = manualReason; }
}