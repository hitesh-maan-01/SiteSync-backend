package com.sitesync.project.dto;

import com.sitesync.project.entity.Project;
import java.util.List;

public class ProjectResponse {
    public String id;
    public String name;
    public String description;
    public String clientName;
    public Double estimatedBudget;
    public String contractorId;
    public String status;
    public Double geofenceCenterLat;
    public Double geofenceCenterLng;
    public Double geofenceRadiusMeters;
    public String startDate;
    public String expectedEndDate;
    public Double progressPercent;
    // M:N — list of supervisor IDs instead of single assignedSupervisorId
    public List<String> supervisorIds;
    public String createdAt;

    public static ProjectResponse fromEntity(Project project,
            List<String> supervisorIds) {
        ProjectResponse dto = new ProjectResponse();
        dto.id = project.getId().toString();
        dto.name = project.getName();
        dto.description = project.getDescription();
        dto.clientName = project.getClientName();
        dto.estimatedBudget = project.getEstimatedBudget();
        dto.contractorId = project.getContractorId().toString();
        dto.status = project.getStatus().name().toLowerCase();
        dto.geofenceCenterLat = project.getGeofenceCenterLat();
        dto.geofenceCenterLng = project.getGeofenceCenterLng();
        dto.geofenceRadiusMeters = project.getGeofenceRadiusMeters();
        dto.startDate = project.getStartDate() != null
                ? project.getStartDate().toString() : null;
        dto.expectedEndDate = project.getExpectedEndDate() != null
                ? project.getExpectedEndDate().toString() : null;
        dto.progressPercent = project.getProgressPercent();
        dto.supervisorIds = supervisorIds;
        dto.createdAt = project.getCreatedAt() != null
                ? project.getCreatedAt().toString() : null;
        return dto;
    }

    /** Convenience overload for contexts where supervisors aren't needed. */
    public static ProjectResponse fromEntity(Project project) {
        return fromEntity(project, List.of());
    }
}