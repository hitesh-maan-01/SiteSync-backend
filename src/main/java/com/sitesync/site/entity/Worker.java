package com.sitesync.site.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workers")
public class Worker {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    private String role;

    @Column(name = "onboarded_at", nullable = false, updatable = false)
    private Instant onboardedAt;

    @PrePersist
    void onCreate() {
        if (onboardedAt == null) onboardedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Instant getOnboardedAt() { return onboardedAt; }
    public void setOnboardedAt(Instant onboardedAt) { this.onboardedAt = onboardedAt; }
}