package com.sitesync.site.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.site.dto.WorkerRequests.OnboardWorkerRequest;
import com.sitesync.site.dto.WorkerResponse;
import com.sitesync.site.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @GetMapping("/api/projects/{projectId}/workers")
    public ResponseEntity<ApiResponse<List<WorkerResponse>>> getWorkers(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        List<WorkerResponse> workers = workerService.getWorkersForProject(
                UUID.fromString(projectId), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(workers));
    }

    @PostMapping("/api/workers/onboard")
    public ResponseEntity<ApiResponse<WorkerResponse>> onboardWorker(
            @Valid @RequestBody OnboardWorkerRequest request,
            Authentication authentication
    ) {
        WorkerResponse worker = workerService.onboardWorker(
                UUID.fromString(request.projectId), currentUserId(authentication),
                request.name, request.phoneNumber, request.role);
        return ResponseEntity.ok(ApiResponse.ok(worker, "Worker onboarded."));
    }

    @DeleteMapping("/api/workers/{id}")
    public ResponseEntity<ApiResponse<Void>> removeWorker(@PathVariable String id) {
        workerService.removeWorker(UUID.fromString(id));
        return ResponseEntity.ok(ApiResponse.ok(null, "Worker removed."));
    }
}