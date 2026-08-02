package com.sitesync.project.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.project.dto.ProjectOverviewResponse;
import com.sitesync.project.service.ProjectOverviewService;
import com.sitesync.project.service.ProjectService;
import com.sitesync.project.dto.ProjectRequests.CreateProjectRequest;
import com.sitesync.project.dto.ProjectRequests.UpdateProjectRequest;
import com.sitesync.project.dto.ProjectResponse;
import com.sitesync.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Every mapping here corresponds exactly to a constant in
 * api_endpoints.dart, checked directly against project_repository.dart.
 * Note GET and POST both live at /api/projects - list vs create is
 * differentiated purely by HTTP method, matching how Flutter calls it.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

   private final ProjectService projectService;
    private final ProjectOverviewService projectOverviewService;
    public ProjectController(ProjectService projectService, ProjectOverviewService projectOverviewService) {
        this.projectService = projectService;
        this.projectOverviewService = projectOverviewService;
    }


    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(Authentication authentication) {
        List<ProjectResponse> projects = projectService.getProjectsForContractor(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(projects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable String id,
            Authentication authentication
    ) {
        ProjectResponse project = projectService.getProjectById(UUID.fromString(id), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(project));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {
        ProjectResponse project = projectService.createProject(currentUserId(authentication), request);
        return ResponseEntity.ok(ApiResponse.ok(project, "Project created."));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable String id,
            @RequestBody UpdateProjectRequest request,
            Authentication authentication
    ) {
        ProjectResponse project = projectService.updateProject(
                UUID.fromString(id), currentUserId(authentication), request);
        return ResponseEntity.ok(ApiResponse.ok(project, "Project updated."));
    }

    // GET /api/projects/{id}/overview
    @GetMapping("/{id}/overview")
    public ResponseEntity<ApiResponse<ProjectOverviewResponse>> getOverview(
            @PathVariable String id,
            Authentication authentication
    ) {
        ProjectOverviewResponse overview = projectOverviewService.getOverview(
                UUID.fromString(id), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(overview));
    }
}