package com.sitesync.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockRequests {

    /** POST /api/inventory/consume - body: { projectId, materialName, quantity, unit, notes? } */
    public static class LogConsumptionRequest {
        @NotBlank public String projectId;
        @NotBlank public String materialName;
        @NotNull public Double quantity;
        @NotBlank public String unit;
        public String notes;
    }
}