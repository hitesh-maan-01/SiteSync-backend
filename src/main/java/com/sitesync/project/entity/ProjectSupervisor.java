package com.sitesync.project.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Join table entity for the M:N relationship between Projects and Supervisors.
 * Composite PK (projectId + userId) enforced both here and in the DB.
 */
@Entity
@Table(name = "project_supervisors")
public class ProjectSupervisor {

    @EmbeddedId
    private ProjectSupervisorId id;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "assigned_by")
    private UUID assignedBy; // contractorId who made the assignment

    @PrePersist
    void onCreate() {
        if (assignedAt == null) assignedAt = Instant.now();
    }

    public ProjectSupervisor() {}

    public ProjectSupervisor(UUID projectId, UUID userId, UUID assignedBy) {
        this.id = new ProjectSupervisorId(projectId, userId);
        this.assignedBy = assignedBy;
    }

    public ProjectSupervisorId getId() { return id; }
    public void setId(ProjectSupervisorId id) { this.id = id; }

    public UUID getProjectId() { return id.getProjectId(); }
    public UUID getUserId()    { return id.getUserId(); }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

    public UUID getAssignedBy() { return assignedBy; }
    public void setAssignedBy(UUID assignedBy) { this.assignedBy = assignedBy; }
}