package com.sitesync.vendor.dto;

import com.sitesync.vendor.entity.Vendor;
import java.util.List;

public class VendorResponse {
    public String id;
    public String name;
    public String phoneNumber;
    public List<String> materialTags;
    public String contractorId;
    public String notes;
    public String createdAt;

    public static VendorResponse fromEntity(Vendor vendor) {
        VendorResponse dto = new VendorResponse();
        dto.id = vendor.getId().toString();
        dto.name = vendor.getName();
        dto.phoneNumber = vendor.getPhoneNumber();
        dto.materialTags = vendor.getMaterialTags();
        dto.contractorId = vendor.getContractorId().toString();
        dto.notes = vendor.getNotes();
        dto.createdAt = vendor.getCreatedAt() != null ? vendor.getCreatedAt().toString() : null;
        return dto;
    }
}