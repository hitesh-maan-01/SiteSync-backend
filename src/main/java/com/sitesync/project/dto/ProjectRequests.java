package com.sitesync.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProjectRequests {

    /** POST /api/projects - contractorId is NOT in this body; derived from the authenticated user. */
    public static class CreateProjectRequest {
        @NotBlank public String name;
        public String description;
        @NotNull public Double geofenceCenterLat;
        @NotNull public Double geofenceCenterLng;
        @NotNull public Double geofenceRadiusMeters;
        public String startDate;
        public String expectedEndDate;
        public String assignedSupervisorId;
    }

    /** PATCH /api/projects/{id} - all fields optional, only provided ones are updated. */
    public static class UpdateProjectRequest {
        public String status;
        public Double progressPercent;
        public String assignedSupervisorId;
    }
}