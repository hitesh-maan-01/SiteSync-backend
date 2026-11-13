package com.sitesync.project.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {

    @Id
   @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "contractor_id", nullable = false)
    private UUID contractorId;

    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Column(name = "geofence_center_lat", nullable = false)
    private Double geofenceCenterLat;

    @Column(name = "geofence_center_lng", nullable = false)
    private Double geofenceCenterLng;

    @Column(name = "geofence_radius_meters", nullable = false)
    private Double geofenceRadiusMeters;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;

    @Column(name = "progress_percent")
    private Double progressPercent;

    

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

     @Column(name = "client_name")
    private String clientName;

    @Column(name = "estimated_budget")
    private Double estimatedBudget;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getContractorId() { return contractorId; }
    public void setContractorId(UUID contractorId) { this.contractorId = contractorId; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public Double getGeofenceCenterLat() { return geofenceCenterLat; }
    public void setGeofenceCenterLat(Double geofenceCenterLat) { this.geofenceCenterLat = geofenceCenterLat; }

    public Double getGeofenceCenterLng() { return geofenceCenterLng; }
    public void setGeofenceCenterLng(Double geofenceCenterLng) { this.geofenceCenterLng = geofenceCenterLng; }

    public Double getGeofenceRadiusMeters() { return geofenceRadiusMeters; }
    public void setGeofenceRadiusMeters(Double geofenceRadiusMeters) { this.geofenceRadiusMeters = geofenceRadiusMeters; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getExpectedEndDate() { return expectedEndDate; }
    public void setExpectedEndDate(LocalDate expectedEndDate) { this.expectedEndDate = expectedEndDate; }

    public Double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Double progressPercent) { this.progressPercent = progressPercent; }


    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public Double getEstimatedBudget() { return estimatedBudget; }
    public void setEstimatedBudget(Double estimatedBudget) { this.estimatedBudget = estimatedBudget; }
}