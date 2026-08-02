package com.sitesync.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PurchaseOrderRequests {

    /** POST /api/purchase-orders - body: { projectId, vendorId, materialName, quantity, unit, expectedAt } */
    public static class CreatePurchaseOrderRequest {
        @NotBlank public String projectId;
        @NotBlank public String vendorId;
        @NotBlank public String materialName;
        @NotNull public Double quantity;
        @NotBlank public String unit;
        @NotBlank public String expectedAt;
    }
}