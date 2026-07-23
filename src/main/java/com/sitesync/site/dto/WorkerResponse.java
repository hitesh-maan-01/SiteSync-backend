package com.sitesync.site.dto;

import com.sitesync.site.entity.Worker;

public class WorkerResponse {
    public String id;
    public String name;
    public String phoneNumber;
    public String projectId;
    public String role;
    public String onboardedAt;

    public static WorkerResponse fromEntity(Worker worker) {
        WorkerResponse dto = new WorkerResponse();
        dto.id = worker.getId().toString();
        dto.name = worker.getName();
        dto.phoneNumber = worker.getPhoneNumber();
        dto.projectId = worker.getProjectId().toString();
        dto.role = worker.getRole();
        dto.onboardedAt = worker.getOnboardedAt() != null ? worker.getOnboardedAt().toString() : null;
        return dto;
    }
}