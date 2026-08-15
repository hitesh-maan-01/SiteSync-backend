package com.sitesync.milestone.controller;

import com.sitesync.milestone.dto.LogProgressRequest;
import com.sitesync.milestone.dto.MilestoneResponse;
import com.sitesync.milestone.dto.MilestoneUpdateLogResponse;
import com.sitesync.milestone.dto.MilestoneUpdateRequest;
import com.sitesync.milestone.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    // GET /api/v1/milestones/project/{projectId}
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<MilestoneResponse>> getByProject(
            @PathVariable String projectId) {
        return ResponseEntity.ok(
                milestoneService.getMilestonesByProject(UUID.fromString(projectId)));
    }

    // PATCH /api/v1/milestones/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<MilestoneResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody MilestoneUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(milestoneService.updateMilestoneStatus(
                UUID.fromString(id), request, currentUserId(authentication)));
    }

    // POST /api/v1/milestones/{id}/log-progress
    @PostMapping("/{id}/log-progress")
    public ResponseEntity<MilestoneResponse> logProgress(
            @PathVariable String id,
            @Valid @RequestBody LogProgressRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(milestoneService.logProgress(
                UUID.fromString(id), request, currentUserId(authentication)));
    }

    // GET /api/v1/milestones/{id}/logs
    @GetMapping("/{id}/logs")
    public ResponseEntity<List<MilestoneUpdateLogResponse>> getLogs(
            @PathVariable String id) {
        return ResponseEntity.ok(
                milestoneService.getLogsForMilestone(UUID.fromString(id)));
    }
}