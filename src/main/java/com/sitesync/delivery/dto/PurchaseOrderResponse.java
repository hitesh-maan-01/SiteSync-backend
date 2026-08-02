package com.sitesync.delivery.dto;

import com.sitesync.delivery.entity.PurchaseOrder;

public class PurchaseOrderResponse {
    public String id;
    public String projectId;
    public String vendorId;
    public String materialName;
    public double quantity;
    public String unit;
    public String expectedAt;
    public String status;
    public String deliveredAt;
    public String deliveryPhotoUrl;
    public String deliveredMaterialId;
    public String createdByUserId;
    public String createdAt;

    public static PurchaseOrderResponse fromEntity(PurchaseOrder order) {
        PurchaseOrderResponse dto = new PurchaseOrderResponse();
        dto.id = order.getId().toString();
        dto.projectId = order.getProjectId().toString();
        dto.vendorId = order.getVendorId().toString();
        dto.materialName = order.getMaterialName();
        dto.quantity = order.getQuantity();
        dto.unit = order.getUnit().toWireValue();
        dto.expectedAt = order.getExpectedAt().toString();
        dto.status = order.getStatus().toWireValue();
        dto.deliveredAt = order.getDeliveredAt() != null ? order.getDeliveredAt().toString() : null;
        dto.deliveryPhotoUrl = order.getDeliveryPhotoUrl();
        dto.deliveredMaterialId = order.getDeliveredMaterialId() != null
                ? order.getDeliveredMaterialId().toString() : null;
        dto.createdByUserId = order.getCreatedByUserId().toString();
        dto.createdAt = order.getCreatedAt().toString();
        return dto;
    }
}