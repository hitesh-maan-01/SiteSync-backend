package com.sitesync.auth.dto;

import java.util.List;

import com.sitesync.auth.entity.User;

/**
 * Field names here are camelCase and must match user_model.dart's
 * UserModel.fromJson exactly: id, name, phoneNumber, role, email,
 * profilePhotoUrl, createdAt, managedProjectIds, companyName,
 * assignedProjectId. Do not rename these without updating Flutter too.
 */
public class UserResponse {
    public String id;
    public String name;
    public String phoneNumber;
    public String role;
    public String email;
    public String profilePhotoUrl;
    public String createdAt;
    public List<String> managedProjectIds;
    public String companyName;
    public String assignedProjectId;

    public static UserResponse fromEntity(User user) {
        UserResponse dto = new UserResponse();
        dto.id = user.getId().toString();
        dto.name = user.getName();
        dto.phoneNumber = user.getPhoneNumber();
        dto.role = user.getRole().toWireValue();
        dto.email = user.getEmail();
        dto.profilePhotoUrl = user.getProfilePhotoUrl();
        dto.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
        dto.companyName = user.getCompanyName();
        dto.assignedProjectId = user.getAssignedProjectId() != null
                ? user.getAssignedProjectId().toString() : null;
        return dto;
    }
}