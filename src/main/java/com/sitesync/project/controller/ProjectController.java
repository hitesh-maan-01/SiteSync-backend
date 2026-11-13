package com.sitesync.project.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.project.dto.ProjectRequests.*;
import com.sitesync.project.dto.ProjectResponse;
import com.sitesync.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    private UUID currentUserId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                projectService.getProjectsForContractor(currentUserId(auth))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                projectService.createProject(currentUserId(auth), request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                projectService.getProjectById(
                        UUID.fromString(id), currentUserId(auth))));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable String id,
            @RequestBody UpdateProjectRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                projectService.updateProject(
                        UUID.fromString(id), currentUserId(auth), request)));
    }

    // ── Supervisor assignment endpoints (M:N) ─────────────────────────────────

    // POST /api/projects/{id}/supervisors
    // Body: { "supervisorId": "uuid" }
    @PostMapping("/{id}/supervisors")
    public ResponseEntity<ApiResponse<ProjectResponse>> addSupervisor(
            @PathVariable String id,
            @RequestBody java.util.Map<String, String> body,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                projectService.addSupervisor(
                        UUID.fromString(id),
                        currentUserId(auth),
                        UUID.fromString(body.get("supervisorId"))),
                "Supervisor assigned."));
    }

    // DELETE /api/projects/{id}/supervisors/{supervisorId}
    @DeleteMapping("/{id}/supervisors/{supervisorId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> removeSupervisor(
            @PathVariable String id,
            @PathVariable String supervisorId,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                projectService.removeSupervisor(
                        UUID.fromString(id),
                        currentUserId(auth),
                        UUID.fromString(supervisorId)),
                "Supervisor removed."));
    }

    // GET /api/projects/my-sites (supervisor calls this)
    @GetMapping("/my-sites")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMySites(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                projectService.getProjectsForSupervisor(currentUserId(auth))));
    }
}