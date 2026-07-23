package com.sitesync.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MaterialRequests {

    /** POST /api/materials/log (JSON body, no delivery photo) */
    public static class LogMaterialRequest {
        @NotBlank public String projectId;
        @NotBlank public String materialName;
        @NotNull public Double quantity;
        @NotBlank public String unit;
        @NotBlank public String vendorId;
        @NotBlank public String vendorName;
        public Double unitCost;
        public String notes;
        @NotBlank public String loggedByUserId;
    }
}