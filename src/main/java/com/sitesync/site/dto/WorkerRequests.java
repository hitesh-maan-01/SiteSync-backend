package com.sitesync.site.dto;

import jakarta.validation.constraints.NotBlank;

public class WorkerRequests {

    /** POST /api/workers/onboard - body: { projectId, name, phoneNumber, role? } */
    public static class OnboardWorkerRequest {
        @NotBlank public String projectId;
        @NotBlank public String name;
        @NotBlank public String phoneNumber;
        public String role;
    }
}