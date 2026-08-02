package com.sitesync.inventory.dto;

import com.sitesync.inventory.entity.StockLedgerEntry;

public class StockLedgerEntryResponse {
    public String id;
    public String projectId;
    public String materialName;
    public String unit;
    public double quantityDelta;
    public String transactionType;
    public String referenceId;
    public String notes;
    public String loggedByUserId;
    public String timestamp;

    public static StockLedgerEntryResponse fromEntity(StockLedgerEntry entry) {
        StockLedgerEntryResponse dto = new StockLedgerEntryResponse();
        dto.id = entry.getId().toString();
        dto.projectId = entry.getProjectId().toString();
        dto.materialName = entry.getMaterialName();
        dto.unit = entry.getUnit().toWireValue();
        dto.quantityDelta = entry.getQuantityDelta();
        dto.transactionType = entry.getTransactionType().toWireValue();
        dto.referenceId = entry.getReferenceId() != null ? entry.getReferenceId().toString() : null;
        dto.notes = entry.getNotes();
        dto.loggedByUserId = entry.getLoggedByUserId().toString();
        dto.timestamp = entry.getTimestamp().toString();
        return dto;
    }
}