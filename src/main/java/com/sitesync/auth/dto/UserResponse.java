package com.sitesync.auth.dto;

import com.sitesync.auth.entity.User;

public class UserResponse {
    public String id;
    public String name;
    public String email;
    public String phoneNumber;
    public String role;
    public String profilePhotoUrl;
    public String companyName;
    public String assignedProjectId;
    public boolean emailVerified;
    public String createdAt;

    public static UserResponse fromEntity(User user) {
        UserResponse dto = new UserResponse();
        dto.id = user.getId().toString();
        dto.name = user.getName();
        dto.email = user.getEmail();
        dto.phoneNumber = user.getPhoneNumber();
        dto.role = user.getRole().toWireValue();
        dto.profilePhotoUrl = user.getProfilePhotoUrl();
        dto.companyName = user.getCompanyName();
        dto.assignedProjectId = user.getAssignedProjectId() != null
                ? user.getAssignedProjectId().toString() : null;
        dto.emailVerified = user.isEmailVerified();
        dto.createdAt = user.getCreatedAt().toString();
        return dto;
    }
}