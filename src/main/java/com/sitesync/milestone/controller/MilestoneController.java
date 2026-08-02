package com.sitesync.milestone.controller;

import com.sitesync.milestone.dto.MilestoneResponse;
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
            @PathVariable String projectId
    ) {
        return ResponseEntity.ok(
                milestoneService.getMilestonesByProject(UUID.fromString(projectId))
        );
    }

    // PATCH /api/v1/milestones/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<MilestoneResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody MilestoneUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                milestoneService.updateMilestoneStatus(
                        UUID.fromString(id), request, currentUserId(authentication)
                )
        );
    }
}