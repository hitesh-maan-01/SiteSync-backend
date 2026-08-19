package com.sitesync.project.service;

import com.sitesync.auth.entity.User;
import com.sitesync.auth.entity.UserRole;
import com.sitesync.auth.repository.UserRepository;
import com.sitesync.milestone.dto.MilestoneCreateDTO;
import com.sitesync.milestone.entity.Milestone;
import com.sitesync.milestone.entity.MilestoneStatus;
import com.sitesync.milestone.repository.MilestoneRepository;
import com.sitesync.project.dto.ProjectRequests.CreateProjectRequest;
import com.sitesync.project.dto.ProjectRequests.UpdateProjectRequest;
import com.sitesync.project.dto.ProjectResponse;
import com.sitesync.project.entity.Project;
import com.sitesync.project.entity.ProjectStatus;
import com.sitesync.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            MilestoneRepository milestoneRepository,
       UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
           this.userRepository = userRepository;
    }

    public List<ProjectResponse> getProjectsForContractor(UUID contractorId) {
        return projectRepository.findByContractorIdOrderByCreatedAtDesc(contractorId)
                .stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(UUID projectId, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));
        if (!project.getContractorId().equals(requesterId) &&
                !requesterId.equals(project.getAssignedSupervisorId())) {
            throw new IllegalArgumentException("Access denied.");
        }
        return ProjectResponse.fromEntity(project);
    }

    /**
     * Creates the project and — within the same @Transactional — saves
     * all submitted milestones with PENDING status, 0% progress, and
     * sequenceOrder derived from their list index (1-based) so the
     * client never needs to manage ordering explicitly.
     */
    @Transactional
    public ProjectResponse createProject(UUID contractorId, CreateProjectRequest request) {
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
        if (request.assignedSupervisorId != null && !request.assignedSupervisorId.isBlank()) {
            project.setAssignedSupervisorId(UUID.fromString(request.assignedSupervisorId));
        }

        Project saved = projectRepository.save(project);

        // Sync users.assigned_project_id at creation time too
if (request.assignedSupervisorId != null &&
        !request.assignedSupervisorId.isBlank()) {
    UUID supervisorId = UUID.fromString(request.assignedSupervisorId);
    userRepository.findById(supervisorId).ifPresent(supervisor -> {
        supervisor.setAssignedProjectId(saved.getId());
        userRepository.save(supervisor);
    });
}

        // Save milestones in the same transaction — sequenceOrder is
        // 1-based index so the list order from the client is preserved.
        if (request.milestones != null) {
            for (int i = 0; i < request.milestones.size(); i++) {
                MilestoneCreateDTO dto = request.milestones.get(i);
                Milestone milestone = new Milestone();
                
                // ✅ FIX APPLIED HERE: Passing the entire 'saved' project object
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

        return ProjectResponse.fromEntity(saved);
    }

   @Transactional
public ProjectResponse updateProject(
        UUID projectId, UUID requesterId, UpdateProjectRequest request) {

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found."));

    if (!project.getContractorId().equals(requesterId)) {
        throw new IllegalArgumentException(
                "Only the contractor can update this project.");
    }

    if (request.status != null) {
        project.setStatus(ProjectStatus.valueOf(request.status.toUpperCase()));
    }
    if (request.progressPercent != null) {
        project.setProgressPercent(request.progressPercent);
    }

    // Supervisor assignment — must stay in sync across BOTH tables.
    // projects.assigned_supervisor_id and users.assigned_project_id
    // are two sides of the same relationship; writing only one causes
    // SupervisorDashboard to show "No project assigned" even after
    // assignment, because it reads from users.assigned_project_id.
    if (request.assignedSupervisorId != null) {
        UUID newSupervisorId = UUID.fromString(request.assignedSupervisorId);

        // 1. Unlink previous supervisor (if any) so their
        //    assigned_project_id is cleared when reassigned.
        UUID previousSupervisorId = project.getAssignedSupervisorId();
        if (previousSupervisorId != null &&
                !previousSupervisorId.equals(newSupervisorId)) {
            userRepository.findById(previousSupervisorId).ifPresent(prev -> {
                prev.setAssignedProjectId(null);
                userRepository.save(prev);
            });
        }

        // 2. Set project's supervisor reference
        project.setAssignedSupervisorId(newSupervisorId);

        // 3. Set supervisor's project reference — this is the missing write
        User supervisor = userRepository.findById(newSupervisorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Supervisor not found: " + newSupervisorId));

        if (supervisor.getRole() != UserRole.SUPERVISOR) {
            throw new IllegalArgumentException(
                    "User is not a supervisor.");
        }

        supervisor.setAssignedProjectId(projectId);
        userRepository.save(supervisor);
    }

    return ProjectResponse.fromEntity(projectRepository.save(project));
}
}