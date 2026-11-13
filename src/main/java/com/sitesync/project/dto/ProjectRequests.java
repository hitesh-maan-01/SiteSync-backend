package com.sitesync.project.dto;

import com.sitesync.milestone.dto.MilestoneCreateDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;

public class ProjectRequests {

    public static class CreateProjectRequest {
        @NotBlank public String name;
        public String description;

        public String clientName;
        public Double estimatedBudget;

        @NotNull public Double geofenceCenterLat;
        @NotNull public Double geofenceCenterLng;
        @NotNull public Double geofenceRadiusMeters;

        public String startDate;
        public String expectedEndDate;
        public String assignedSupervisorId;
        public List<String> supervisorIds;

        // Milestones defined at project creation — sequenceOrder is
        // derived from list index in ProjectService, never sent by client.
        @Valid
        public List<MilestoneCreateDTO> milestones = Collections.emptyList();
    }

    public static class UpdateProjectRequest {
        public String status;
        public Double progressPercent;
        public String assignedSupervisorId;
    }
}