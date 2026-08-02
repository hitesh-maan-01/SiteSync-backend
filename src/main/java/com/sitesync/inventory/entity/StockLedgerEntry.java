package com.sitesync.inventory.entity;

import com.sitesync.site.entity.MaterialUnit;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * A single append-only stock movement. Current stock for a
 * (project, material) pair is SUM(quantityDelta) computed on read
 * (see StockLedgerRepository.sumQuantityForMaterial) - there is
 * deliberately no separate "current stock" row that could drift out of
 * sync with its own transaction history.
 */
@Entity
@Table(name = "stock_ledger")
public class StockLedgerEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(nullable = false)
    private MaterialUnit unit;

    @Column(name = "quantity_delta", nullable = false)
    private Double quantityDelta;

    @Column(name = "transaction_type", nullable = false)
    private StockTransactionType transactionType;

    @Column(name = "reference_id")
    private UUID referenceId;

    private String notes;

    @Column(name = "logged_by_user_id", nullable = false)
    private UUID loggedByUserId;

    @Column(name = "\"timestamp\"", nullable = false)
    private Instant timestamp;

    @PrePersist
    void onCreate() {
        if (timestamp == null) timestamp = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public MaterialUnit getUnit() { return unit; }
    public void setUnit(MaterialUnit unit) { this.unit = unit; }

    public Double getQuantityDelta() { return quantityDelta; }
    public void setQuantityDelta(Double quantityDelta) { this.quantityDelta = quantityDelta; }

    public StockTransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(StockTransactionType transactionType) { this.transactionType = transactionType; }

    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public UUID getLoggedByUserId() { return loggedByUserId; }
    public void setLoggedByUserId(UUID loggedByUserId) { this.loggedByUserId = loggedByUserId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}