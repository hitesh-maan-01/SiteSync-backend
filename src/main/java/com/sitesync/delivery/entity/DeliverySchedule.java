package com.sitesync.delivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(name = "quantity")
    private String quantity; // e.g. "50 bags", "2 truckloads"

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "expected_date", nullable = false)
    private LocalDate expectedDate;

    @Column(name = "actual_date")
    private LocalDate actualDate; // filled when confirmed

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "confirmed_by") // userId of supervisor who confirmed
    private UUID confirmedBy;

    @Column(name = "photo_url")
    private String photoUrl; // photo taken at confirmation

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy; // admin userId

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────────────
    // Status enum
    // ─────────────────────────────────────────────

    public enum Status {
        PENDING,    // scheduled, not yet delivered
        CONFIRMED,  // supervisor confirmed delivery
        REJECTED,   // supervisor rejected (wrong material, short quantity, etc.)
        OVERDUE     // past expected date, still pending — set by OverdueDeliveryScheduler
    }
}