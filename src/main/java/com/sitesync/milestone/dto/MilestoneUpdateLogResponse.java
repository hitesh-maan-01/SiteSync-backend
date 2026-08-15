package com.sitesync.milestone.dto;

import com.sitesync.milestone.entity.MilestoneUpdateLog;

public class MilestoneUpdateLogResponse {
    public String id;
    public String milestoneId;
    public double progressAddedPercent;
    public String remarks;
    public String loggedByUserId;
    public String loggedAt;

    public static MilestoneUpdateLogResponse fromEntity(MilestoneUpdateLog log) {
        MilestoneUpdateLogResponse dto = new MilestoneUpdateLogResponse();
        dto.id = log.getId().toString();
        dto.milestoneId = log.getMilestoneId().toString();
        dto.progressAddedPercent = log.getProgressAddedPercent();
        dto.remarks = log.getRemarks();
        dto.loggedByUserId = log.getLoggedByUserId().toString();
        dto.loggedAt = log.getLoggedAt().toString();
        return dto;
    }
}