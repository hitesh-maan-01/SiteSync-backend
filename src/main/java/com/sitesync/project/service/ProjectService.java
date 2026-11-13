package com.sitesync.project.service;

import com.sitesync.auth.entity.User;
import com.sitesync.auth.repository.UserRepository;
import com.sitesync.milestone.dto.MilestoneCreateDTO;
import com.sitesync.milestone.entity.Milestone;
import com.sitesync.milestone.entity.MilestoneStatus;
import com.sitesync.milestone.repository.MilestoneRepository;
import com.sitesync.notification.entity.NotificationSeverity;
import com.sitesync.notification.service.PushNotificationService;
import com.sitesync.project.dto.ProjectRequests.*;
import com.sitesync.project.dto.ProjectResponse;
import com.sitesync.project.entity.Project;
import com.sitesync.project.entity.ProjectStatus;
import com.sitesync.project.entity.ProjectSupervisor;
import com.sitesync.project.repository.ProjectRepository;
import com.sitesync.project.repository.ProjectSupervisorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectSupervisorRepository projectSupervisorRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    // ── Query helpers ─────────────────────────────────────────────────────────

    public List<ProjectResponse> getProjectsForContractor(UUID contractorId) {
        return projectRepository
                .findByContractorIdOrderByCreatedAtDesc(contractorId)
                .stream()
                .map(p -> ProjectResponse.fromEntity(p, supervisorIds(p.getId())))
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(UUID projectId, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found."));

        boolean isContractor = project.getContractorId().equals(requesterId);
        boolean isSupervisor = projectSupervisorRepository
                .existsByIdProjectIdAndIdUserId(projectId, requesterId);

        if (!isContractor && !isSupervisor) {
            throw new IllegalArgumentException("Access denied.");
        }

        return ProjectResponse.fromEntity(project, supervisorIds(projectId));
    }

    /**
     * Returns all projects a supervisor is assigned to.
     * Used by the new SupervisorDashboard project-picker.
     */
    public List<ProjectResponse> getProjectsForSupervisor(UUID supervisorId) {
        List<UUID> projectIds = projectSupervisorRepository
                .findProjectIdsByUserId(supervisorId);

        return projectIds.stream()
                .map(id -> projectRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(p -> ProjectResponse.fromEntity(p, supervisorIds(p.getId())))
                .collect(Collectors.toList());
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public ProjectResponse createProject(UUID contractorId,
            CreateProjectRequest request) {

        Project project = new Project();
        project.setContractorId(contractorId);
        project.setName(request.name);
        project.setDescription(request.description);
        project.setClientName(request.clientName);
        project.setEstimatedBudget(request.estimatedBudget);
        project.setGeofenceCenterLat(request.geofenceCenterLat);
        project.setGeofenceCenterLng(request.geofenceCenterLng);
        project.setGeofenceRadiusMeters(request.geofenceRadiusMeters);
        project.setStatus(ProjectStatus.ACTIVE);

        if (request.startDate != null && !request.startDate.isBlank()) {
            project.setStartDate(LocalDate.parse(request.startDate));
        }
        if (request.expectedEndDate != null && !request.expectedEndDate.isBlank()) {
            project.setExpectedEndDate(LocalDate.parse(request.expectedEndDate));
        }

        Project saved = projectRepository.save(project);

        // Assign supervisors (M:N — can be multiple at creation time)
        if (request.supervisorIds != null) {
            for (String supId : request.supervisorIds) {
                if (supId != null && !supId.isBlank()) {
                    projectSupervisorRepository.save(
                            new ProjectSupervisor(
                                    saved.getId(),
                                    UUID.fromString(supId),
                                    contractorId));
                }
            }
        }

        // Save initial milestones
        if (request.milestones != null) {
            for (int i = 0; i < request.milestones.size(); i++) {
                MilestoneCreateDTO dto = request.milestones.get(i);
                Milestone milestone = new Milestone();
                milestone.setProject(saved);
                milestone.setTitle(dto.title);
                milestone.setStatus(MilestoneStatus.PENDING);
                milestone.setProgressPercent(0.0);
                milestone.setSequenceOrder(i + 1);
                if (dto.targetDate != null && !dto.targetDate.isBlank()) {
                    milestone.setTargetDate(LocalDate.parse(dto.targetDate));
                }
                milestoneRepository.save(milestone);
            }
        }

        return ProjectResponse.fromEntity(saved,
                supervisorIds(saved.getId()));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UUID requesterId,
            UpdateProjectRequest request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found."));

        if (!project.getContractorId().equals(requesterId)) {
            throw new IllegalArgumentException(
                    "Only the contractor can update this project.");
        }

        if (request.status != null) {
            project.setStatus(
                    ProjectStatus.valueOf(request.status.toUpperCase()));
        }
        if (request.progressPercent != null) {
            project.setProgressPercent(request.progressPercent);
        }

        return ProjectResponse.fromEntity(
                projectRepository.save(project),
                supervisorIds(projectId));
    }

    // ── Supervisor assignment (M:N) ───────────────────────────────────────────

    /**
     * Adds a supervisor to a project. Idempotent — adding the same
     * supervisor twice is a no-op (join table has a composite PK).
     */
    @Transactional
    public ProjectResponse addSupervisor(UUID projectId, UUID contractorId,
            UUID supervisorId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found."));

        if (!project.getContractorId().equals(contractorId)) {
            throw new IllegalArgumentException("Access denied.");
        }

        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Supervisor not found."));

        if (!com.sitesync.auth.entity.UserRole.SUPERVISOR
                .equals(supervisor.getRole())) {
            throw new IllegalArgumentException("User is not a supervisor.");
        }

        // Idempotent — save only if not already assigned
        if (!projectSupervisorRepository.existsByIdProjectIdAndIdUserId(
                projectId, supervisorId)) {
            projectSupervisorRepository.save(
                    new ProjectSupervisor(projectId, supervisorId, contractorId));
        }

        // Notify the newly assigned supervisor
        notifySupervisorAssigned(supervisor, project);

        return ProjectResponse.fromEntity(project, supervisorIds(projectId));
    }

    /**
     * Removes a supervisor from a project. Safe to call if not assigned.
     */
    @Transactional
    public ProjectResponse removeSupervisor(UUID projectId, UUID contractorId,
            UUID supervisorId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found."));

        if (!project.getContractorId().equals(contractorId)) {
            throw new IllegalArgumentException("Access denied.");
        }

        projectSupervisorRepository
                .deleteByIdProjectIdAndIdUserId(projectId, supervisorId);

        return ProjectResponse.fromEntity(project, supervisorIds(projectId));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<String> supervisorIds(UUID projectId) {
        return projectSupervisorRepository
                .findByIdProjectId(projectId)
                .stream()
                .map(ps -> ps.getId().getUserId().toString())
                .collect(Collectors.toList());
    }

    private void notifySupervisorAssigned(User supervisor, Project project) {
        try {
            if (supervisor.getFcmToken() == null) return;
            pushNotificationService.sendAndPersist(
                    supervisor.getFcmToken(),
                    supervisor.getId(),
                    "New Site Assignment 🏗️",
                    "You have been assigned to \"" + project.getName() + "\".",
                    NotificationSeverity.INFO,
                    "siteAssigned",
                    "View Site",
                    "/supervisor-dash",
                    Map.of("projectId", project.getId().toString())
            );
        } catch (Exception e) {
            // Non-fatal
        }
    }
}