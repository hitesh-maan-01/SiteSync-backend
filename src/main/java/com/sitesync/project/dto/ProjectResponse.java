package com.sitesync.project.dto;

import com.sitesync.project.entity.Project;

/**
 * Field names must match project_model.dart's ProjectModel.fromJson
 * exactly. startDate/expectedEndDate are plain ISO dates
 * (LocalDate.toString() -> "2026-01-15"), which Dart's DateTime.parse()
 * accepts fine (it handles date-only strings, not just full timestamps).
 */
public class ProjectResponse {
    public String id;
    public String name;
    public String description;
    public String contractorId;
    public String status;
    public double geofenceCenterLat;
    public double geofenceCenterLng;
    public double geofenceRadiusMeters;
    public String startDate;
    public String expectedEndDate;
    public Double progressPercent;
    public String assignedSupervisorId;
    public String createdAt;

    public static ProjectResponse fromEntity(Project project) {
        ProjectResponse dto = new ProjectResponse();
        dto.id = project.getId().toString();
        dto.name = project.getName();
        dto.description = project.getDescription();
        dto.contractorId = project.getContractorId().toString();
        dto.status = project.getStatus().toWireValue();
        dto.geofenceCenterLat = project.getGeofenceCenterLat();
        dto.geofenceCenterLng = project.getGeofenceCenterLng();
        dto.geofenceRadiusMeters = project.getGeofenceRadiusMeters();
        dto.startDate = project.getStartDate() != null ? project.getStartDate().toString() : null;
        dto.expectedEndDate = project.getExpectedEndDate() != null ? project.getExpectedEndDate().toString() : null;
        dto.progressPercent = project.getProgressPercent();
        dto.assignedSupervisorId = project.getAssignedSupervisorId() != null
                ? project.getAssignedSupervisorId().toString() : null;
        dto.createdAt = project.getCreatedAt() != null ? project.getCreatedAt().toString() : null;
        return dto;
    }
}