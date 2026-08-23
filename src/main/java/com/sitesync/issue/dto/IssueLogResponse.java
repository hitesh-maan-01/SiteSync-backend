package com.sitesync.issue.dto;

import com.sitesync.issue.entity.IssueLog;

public class IssueLogResponse {
    public String id;
    public String projectId;
    public String title;
    public String description;
    public String photoUrl;
    public String urgency;
    public String status;
    public String createdByUserId;
    public String resolvedByUserId;
    public String createdAt;
    public String resolvedAt;

    public static IssueLogResponse fromEntity(IssueLog issue) {
        IssueLogResponse dto = new IssueLogResponse();
        dto.id = issue.getId().toString();
        dto.projectId = issue.getProjectId().toString();
        dto.title = issue.getTitle();
        dto.description = issue.getDescription();
        dto.photoUrl = issue.getPhotoUrl();
        dto.urgency = issue.getUrgency().toWireValue();
        dto.status = issue.getStatus().toWireValue();
        dto.createdByUserId = issue.getCreatedByUserId().toString();
        dto.resolvedByUserId = issue.getResolvedByUserId() != null
                ? issue.getResolvedByUserId().toString() : null;
        dto.createdAt = issue.getCreatedAt().toString();
        dto.resolvedAt = issue.getResolvedAt() != null
                ? issue.getResolvedAt().toString() : null;
        return dto;
    }
}