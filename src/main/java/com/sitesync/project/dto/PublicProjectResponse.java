package com.sitesync.project.dto;

import com.sitesync.milestone.entity.Milestone;
import com.sitesync.milestone.entity.MilestoneStatus;
import com.sitesync.project.entity.Project;
import com.sitesync.project.entity.ProjectStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Public-facing project summary — no auth required to view.
 * Deliberately excludes sensitive fields: contractorId, geofence
 * coordinates, assignedSupervisorId, budget, client name.
 * Only what a client genuinely needs to see site progress.
 */
public class PublicProjectResponse {
    public String projectName;
    public String status;
    public Double progressPercent;
    public String startDate;
    public String expectedEndDate;
    public List<PublicMilestoneItem> milestones;

    public static class PublicMilestoneItem {
        public String title;
        public String status;
        public Double progressPercent;
        public String targetDate;
        public String completedDate;

        public static PublicMilestoneItem fromEntity(Milestone m) {
            PublicMilestoneItem item = new PublicMilestoneItem();
            item.title = m.getTitle();
            item.status = m.getStatus().name();
            item.progressPercent = m.getProgressPercent();
            item.targetDate = m.getTargetDate() != null
                    ? m.getTargetDate().toString() : null;
            item.completedDate = m.getCompletedDate() != null
                    ? m.getCompletedDate().toString() : null;
            return item;
        }
    }

    public static PublicProjectResponse fromEntity(
            Project project, List<Milestone> milestones) {
        PublicProjectResponse dto = new PublicProjectResponse();
        dto.projectName = project.getName();
        dto.status = project.getStatus().name().toLowerCase();
        dto.progressPercent = project.getProgressPercent();
        dto.startDate = project.getStartDate() != null
                ? project.getStartDate().toString() : null;
        dto.expectedEndDate = project.getExpectedEndDate() != null
                ? project.getExpectedEndDate().toString() : null;
        dto.milestones = milestones.stream()
                .map(PublicMilestoneItem::fromEntity)
                .collect(Collectors.toList());
        return dto;
    }
}