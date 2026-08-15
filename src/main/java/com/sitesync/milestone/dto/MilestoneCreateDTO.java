package com.sitesync.milestone.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Lightweight DTO for milestone definitions submitted at project creation
 * time. No id/status/progress — those are assigned by the service layer.
 * sequenceOrder is derived from the list index in ProjectService, not
 * sent by the client, to avoid index-vs-order drift.
 */
public class MilestoneCreateDTO {

    @NotBlank
    public String title;

    // ISO-8601 date string (yyyy-MM-dd) — nullable, not all milestones
    // have a known target date at project creation time.
    public String targetDate;
}