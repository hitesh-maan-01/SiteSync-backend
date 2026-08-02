package com.sitesync.project.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.project.dto.ProjectOverviewResponse;
import com.sitesync.project.service.ProjectOverviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectOverviewController {

    private final ProjectOverviewService projectOverviewService;

    public ProjectOverviewController(ProjectOverviewService projectOverviewService) {
        this.projectOverviewService = projectOverviewService;
    }

    // GET /api/v1/projects/{projectId}/overview
    @GetMapping("/{projectId}/overview")
    public ResponseEntity<ApiResponse<ProjectOverviewResponse>> getOverview(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        UUID requesterId = (UUID) authentication.getPrincipal();
        ProjectOverviewResponse overview = projectOverviewService.getOverview(
                UUID.fromString(projectId), requesterId
        );
        return ResponseEntity.ok(ApiResponse.ok(overview));
    }
}