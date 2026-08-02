package com.sitesync.project.service;

import com.sitesync.auth.entity.UserRole;
import com.sitesync.auth.repository.UserRepository;
import com.sitesync.project.dto.ProjectRequests.CreateProjectRequest;
import com.sitesync.project.dto.ProjectRequests.UpdateProjectRequest;
import com.sitesync.project.dto.ProjectResponse;
import com.sitesync.project.entity.Project;
import com.sitesync.project.entity.ProjectStatus;
import com.sitesync.project.exception.ProjectNotFoundException;
import com.sitesync.project.repository.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Each public method corresponds to a ProjectRepository method on the
 * Flutter side - see project_repository.dart's doc comments for the
 * exact contract each of these must satisfy.
 */
@Service
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectResponse> getProjectsForContractor(UUID contractorId) {
        return projectRepository.findByContractorIdOrderByCreatedAtDesc(contractorId).stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Used by BOTH the contractor (their own project) AND the supervisor
     * (their single assigned project, for the geofence check on
     * manual_attendance_page.dart). Access is allowed if the requester is
     * either the owning contractor OR the project's assigned supervisor -
     * anyone else gets 403, not 404 (a 404 would leak whether the ID
     * exists at all to someone who shouldn't know).
     */
    public ProjectResponse getProjectById(UUID projectId, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        boolean isOwningContractor = project.getContractorId().equals(requesterId);
        boolean isAssignedSupervisor = requesterId.equals(project.getAssignedSupervisorId());

        if (!isOwningContractor && !isAssignedSupervisor) {
            throw new AccessDeniedException("You don't have access to this project.");
        }

        return ProjectResponse.fromEntity(project);
    }

    public ProjectResponse createProject(UUID contractorId, CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.name);
        project.setDescription(request.description);
        project.setContractorId(contractorId);
        project.setGeofenceCenterLat(request.geofenceCenterLat);
        project.setGeofenceCenterLng(request.geofenceCenterLng);
        project.setGeofenceRadiusMeters(request.geofenceRadiusMeters);
        project.setStartDate(parseDateOrNull(request.startDate));
        project.setExpectedEndDate(parseDateOrNull(request.expectedEndDate));

        if (request.assignedSupervisorId != null && !request.assignedSupervisorId.isBlank()) {
            UUID supervisorId = UUID.fromString(request.assignedSupervisorId);
            validateSupervisorAssignment(supervisorId, contractorId);
            project.setAssignedSupervisorId(supervisorId);
        }

        Project saved = projectRepository.save(project);
        return ProjectResponse.fromEntity(saved);
    }

    /**
     * Only the owning contractor may update a project (status changes,
     * supervisor reassignment, progress) - a supervisor can read their
     * project but never mutate it through this endpoint.
     *
     * Currently unused by any Flutter screen yet (no UI calls
     * ProjectRepository.updateProject as of this writing) - built now
     * since the contract already exists and a project-detail edit flow
     * or supervisor-reassignment feature will need it soon.
     */
    public ProjectResponse updateProject(UUID projectId, UUID requesterId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        if (!project.getContractorId().equals(requesterId)) {
            throw new AccessDeniedException("Only the owning contractor can update this project.");
        }

        if (request.status != null) {
            project.setStatus(ProjectStatus.fromWireValue(request.status));
        }
        if (request.progressPercent != null) {
            project.setProgressPercent(request.progressPercent);
        }
        if (request.assignedSupervisorId != null) {
            if (request.assignedSupervisorId.isBlank()) {
                project.setAssignedSupervisorId(null);
            } else {
                UUID supervisorId = UUID.fromString(request.assignedSupervisorId);
                validateSupervisorAssignment(supervisorId, requesterId);
                project.setAssignedSupervisorId(supervisorId);
            }
        }

        Project saved = projectRepository.save(project);
        return ProjectResponse.fromEntity(saved);
    }

    /**
     * Flutter sends DateTime.toIso8601String(), e.g.
     * "2026-01-15T00:00:00.000" - a full timestamp even though the date
     * picker only sets a date (Dart doesn't truncate a midnight time
     * component). Parsed via Instant then converted to a LocalDate in
     * UTC, rather than a fragile substring/manual split, so this doesn't
     * silently break if Flutter's exact timestamp format ever varies
     * (e.g. gains a timezone offset suffix).
     */
    /**
     * A contractor may only assign a supervisor account THEY invited —
     * checked via invited_by_contractor_id (see
     * migration_001_supervisor_invite.sql / AuthService.inviteSupervisor).
     * Without this check, a contractor could assign any arbitrary UUID —
     * including another contractor's own account ID — as a project's
     * supervisor, which would be a real authorization hole, not just bad
     * data (that user would then satisfy getProjectById's
     * isAssignedSupervisor check and gain access to the project).
     */
    private void validateSupervisorAssignment(UUID supervisorId, UUID contractorId) {
        var supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new IllegalArgumentException("Supervisor not found."));

        boolean isSupervisorRole = supervisor.getRole() == UserRole.SUPERVISOR;
        boolean invitedByThisContractor = contractorId.equals(supervisor.getInvitedByContractorId());

        if (!isSupervisorRole || !invitedByThisContractor) {
            throw new AccessDeniedException("You can only assign supervisors you've invited.");
        }
    }

    private LocalDate parseDateOrNull(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isBlank()) return null;
        // If it's already a bare date (no 'T'), parse directly.
        if (!isoDateTime.contains("T")) return LocalDate.parse(isoDateTime);
        return Instant.parse(
                isoDateTime.endsWith("Z") ? isoDateTime : isoDateTime + "Z"
        ).atZone(ZoneOffset.UTC).toLocalDate();
    }
}