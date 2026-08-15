package com.sitesync.milestone.dto;

import com.sitesync.milestone.entity.MilestoneStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class LogProgressRequest {

    @NotNull
    @Min(1)
    @Max(100)
    public Double progressAddedPercent;

    public String remarks;

    // Optional: supervisor can force-complete or mark delayed
    // alongside logging progress. Null = keep existing status.
    public MilestoneStatus statusOverride;
}