package com.sitesync.milestone.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Append-only log of daily progress entries for a milestone.
 * Current milestone progress = SUM(progressAddedPercent) clamped to 100,
 * stored redundantly on Milestone.progressPercent for read performance
 * (see MilestoneService.logProgress for the transactional update pattern).
 */
@Entity
@Table(name = "milestone_update_logs")
public class MilestoneUpdateLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "milestone_id", nullable = false)
    private UUID milestoneId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "progress_added_percent", nullable = false)
    private Double progressAddedPercent;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "logged_by_user_id", nullable = false)
    private UUID loggedByUserId;

    @Column(name = "logged_at", nullable = false, updatable = false)
    private Instant loggedAt;

    @PrePersist
    void onCreate() {
        if (loggedAt == null) loggedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMilestoneId() { return milestoneId; }
    public void setMilestoneId(UUID milestoneId) { this.milestoneId = milestoneId; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public Double getProgressAddedPercent() { return progressAddedPercent; }
    public void setProgressAddedPercent(Double progressAddedPercent) { this.progressAddedPercent = progressAddedPercent; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public UUID getLoggedByUserId() { return loggedByUserId; }
    public void setLoggedByUserId(UUID loggedByUserId) { this.loggedByUserId = loggedByUserId; }

    public Instant getLoggedAt() { return loggedAt; }
    public void setLoggedAt(Instant loggedAt) { this.loggedAt = loggedAt; }
}