package com.sitesync.vendor.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    /**
     * Maps directly to Postgres's native text[] column type. Requires
     * Hibernate 6.2+ (bundled with recent Spring Boot 3.x versions) -
     * @JdbcTypeCode(SqlTypes.ARRAY) tells Hibernate to use the array JDBC
     * type rather than trying to serialize this as a single JSON/string
     * column, which would NOT match schema.sql's actual column type and
     * would fail at runtime with a type mismatch against the real table.
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "material_tags", nullable = false, columnDefinition = "text[]")
    private List<String> materialTags = List.of();

    @Column(name = "contractor_id", nullable = false)
    private UUID contractorId;

    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<String> getMaterialTags() { return materialTags; }
    public void setMaterialTags(List<String> materialTags) { this.materialTags = materialTags; }

    public UUID getContractorId() { return contractorId; }
    public void setContractorId(UUID contractorId) { this.contractorId = contractorId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}