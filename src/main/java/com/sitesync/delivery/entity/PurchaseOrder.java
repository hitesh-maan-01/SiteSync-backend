package com.sitesync.delivery.entity;

import com.sitesync.site.entity.MaterialUnit;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private MaterialUnit unit;

    @Column(name = "expected_at", nullable = false)
    private Instant expectedAt;

    @Column(nullable = false)
    private PurchaseOrderStatus status = PurchaseOrderStatus.PENDING;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "delivery_photo_url")
    private String deliveryPhotoUrl;

    @Column(name = "delivered_material_id")
    private UUID deliveredMaterialId;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public UUID getVendorId() { return vendorId; }
    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public MaterialUnit getUnit() { return unit; }
    public void setUnit(MaterialUnit unit) { this.unit = unit; }

    public Instant getExpectedAt() { return expectedAt; }
    public void setExpectedAt(Instant expectedAt) { this.expectedAt = expectedAt; }

    public PurchaseOrderStatus getStatus() { return status; }
    public void setStatus(PurchaseOrderStatus status) { this.status = status; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public String getDeliveryPhotoUrl() { return deliveryPhotoUrl; }
    public void setDeliveryPhotoUrl(String deliveryPhotoUrl) { this.deliveryPhotoUrl = deliveryPhotoUrl; }

    public UUID getDeliveredMaterialId() { return deliveredMaterialId; }
    public void setDeliveredMaterialId(UUID deliveredMaterialId) { this.deliveredMaterialId = deliveredMaterialId; }

    public UUID getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(UUID createdByUserId) { this.createdByUserId = createdByUserId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}