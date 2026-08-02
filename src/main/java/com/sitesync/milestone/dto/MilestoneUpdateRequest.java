package com.sitesync.milestone.dto;

import com.sitesync.milestone.entity.MilestoneStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneUpdateRequest {

    @NotNull(message = "Status is required")
    private MilestoneStatus status;

    // Optional free-text note supervisor can attach with the update
    private String remarks;
}