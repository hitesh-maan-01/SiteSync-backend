package com.sitesync.project.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.milestone.entity.Milestone;
import com.sitesync.milestone.repository.MilestoneRepository;
import com.sitesync.project.dto.PublicProjectResponse;
import com.sitesync.project.entity.Project;
import com.sitesync.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Public endpoints — no JWT required. Accessible by anyone with the
 * project link (e.g. the client the contractor shared it with).
 *
 * Security: these endpoints are whitelisted in SecurityConfig's
 * permitAll() list. They expose only non-sensitive project data
 * (name, progress, milestone titles/status) — no contractor IDs,
 * geofence coordinates, budgets, or worker data.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicProjectController {

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;

    // GET /api/public/projects/{projectId}/progress
    @GetMapping("/projects/{projectId}/progress")
    public ResponseEntity<ApiResponse<PublicProjectResponse>> getPublicProgress(
            @PathVariable String projectId) {

        Project project = projectRepository.findById(UUID.fromString(projectId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found."));

        List<Milestone> milestones = milestoneRepository
                .findByProjectIdOrderBySequenceOrderAsc(project.getId());

        return ResponseEntity.ok(ApiResponse.ok(
                PublicProjectResponse.fromEntity(project, milestones)));
    }
}