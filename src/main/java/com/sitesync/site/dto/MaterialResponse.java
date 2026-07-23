package com.sitesync.site.dto;

import com.sitesync.site.entity.Material;

public class MaterialResponse {
    public String id;
    public String projectId;
    public String materialName;
    public double quantity;
    public String unit;
    public String vendorId;
    public String vendorName;
    public String deliveryPhotoUrl;
    public Double unitCost;
    public Double totalCost;
    public String currency;
    public String loggedByUserId;
    public String timestamp;
    public String notes;

    public static MaterialResponse fromEntity(Material material) {
        MaterialResponse dto = new MaterialResponse();
        dto.id = material.getId().toString();
        dto.projectId = material.getProjectId().toString();
        dto.materialName = material.getMaterialName();
        dto.quantity = material.getQuantity();
        dto.unit = material.getUnit().toWireValue();
        dto.vendorId = material.getVendorId().toString();
        dto.vendorName = material.getVendorName();
        dto.deliveryPhotoUrl = material.getDeliveryPhotoUrl();
        dto.unitCost = material.getUnitCost();
        dto.totalCost = material.getTotalCost();
        dto.currency = material.getCurrency();
        dto.loggedByUserId = material.getLoggedByUserId().toString();
        dto.timestamp = material.getTimestamp().toString();
        dto.notes = material.getNotes();
        return dto;
    }
}