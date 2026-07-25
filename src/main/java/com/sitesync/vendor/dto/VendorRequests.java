package com.sitesync.vendor.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class VendorRequests {

    /** POST /api/vendors - body: { name, phoneNumber, materialTags, notes? } */
    public static class CreateVendorRequest {
        @NotBlank public String name;
        @NotBlank public String phoneNumber;
        public List<String> materialTags;
        public String notes;
    }

    /**
     * PUT /api/vendors/{id} - Flutter's VendorRepository.updateVendor()
     * sends the FULL VendorModel.toJson(), which includes id and
     * contractorId fields. Both are accepted here (so Jackson doesn't
     * choke on unexpected properties) but deliberately UNUSED by
     * VendorService.updateVendor - the real id comes from the URL path,
     * and contractorId is derived from the authenticated user, never
     * trusted from the request body. Accepting-but-ignoring these two
     * fields, rather than requiring Flutter to omit them, avoids a
     * client-side change for a body shape that's otherwise fine.
     */
    public static class UpdateVendorRequest {
        public String id;
        @NotBlank public String name;
        @NotBlank public String phoneNumber;
        public List<String> materialTags;
        public String contractorId;
        public String notes;
        public String createdAt;
    }
}