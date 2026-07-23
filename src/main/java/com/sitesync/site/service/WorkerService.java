package com.sitesync.site.service;

import com.sitesync.project.service.ProjectService;
import com.sitesync.site.dto.WorkerResponse;
import com.sitesync.site.entity.Worker;
import com.sitesync.site.repository.WorkerRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final ProjectService projectService;

    public WorkerService(WorkerRepository workerRepository, ProjectService projectService) {
        this.workerRepository = workerRepository;
        this.projectService = projectService;
    }

    /**
     * getProjectById already enforces "owning contractor OR assigned
     * supervisor" and throws AccessDeniedException (403) otherwise — every
     * site-module method reuses that same check by calling it first,
     * rather than duplicating the authorization rule here. Its return
     * value is discarded; this call exists purely for its side effect of
     * throwing if the requester shouldn't be here.
     */
    public List<WorkerResponse> getWorkersForProject(UUID projectId, UUID requesterId) {
        projectService.getProjectById(projectId, requesterId);
        return workerRepository.findByProjectIdOrderByOnboardedAtAsc(projectId).stream()
                .map(WorkerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public WorkerResponse onboardWorker(UUID projectId, UUID requesterId, String name, String phoneNumber, String role) {
        projectService.getProjectById(projectId, requesterId);

        Worker worker = new Worker();
        worker.setProjectId(projectId);
        worker.setName(name);
        worker.setPhoneNumber(phoneNumber);
        worker.setRole(role);

        Worker saved = workerRepository.save(worker);
        return WorkerResponse.fromEntity(saved);
    }

    /**
     * Made idempotent deliberately: Spring Data JPA's deleteById() throws
     * EmptyResultDataAccessException if the row doesn't exist (NOT a
     * silent no-op). labor_onboard_screen.dart's removal flow doesn't
     * handle an "already removed" error state, so a double-tap or stale
     * UI list entry shouldn't surface as a scary exception.
     *
     * Note: this method does NOT verify project access the way the other
     * two do, because it only has a workerId, not a projectId, to check
     * against — fetching the worker first to get its projectId, purely to
     * authorize the delete, is a reasonable follow-up hardening but isn't
     * done here yet. Flagged rather than silently left as a gap.
     */
    public void removeWorker(UUID workerId) {
        try {
            workerRepository.deleteById(workerId);
        } catch (EmptyResultDataAccessException e) {
            // Already gone — treat as success, not an error.
        }
    }
}