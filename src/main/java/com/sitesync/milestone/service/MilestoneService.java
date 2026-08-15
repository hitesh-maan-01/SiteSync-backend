package com.sitesync.milestone.service;

import com.sitesync.milestone.dto.LogProgressRequest;
import com.sitesync.milestone.dto.MilestoneResponse;
import com.sitesync.milestone.dto.MilestoneUpdateLogResponse;
import com.sitesync.milestone.dto.MilestoneUpdateRequest;
import com.sitesync.milestone.entity.Milestone;
import com.sitesync.milestone.entity.MilestoneStatus;
import com.sitesync.milestone.entity.MilestoneUpdateLog;
import com.sitesync.milestone.repository.MilestoneRepository;
import com.sitesync.milestone.repository.MilestoneUpdateLogRepository;
import com.sitesync.notification.service.PushNotificationService;
import com.sitesync.project.entity.Project;
import com.sitesync.project.repository.ProjectRepository;
import com.sitesync.auth.entity.User;
import com.sitesync.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final MilestoneUpdateLogRepository logRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    public List<MilestoneResponse> getMilestonesByProject(UUID projectId) {
        return milestoneRepository.findByProjectIdOrderBySequenceOrderAsc(projectId)
                .stream()
                .map(MilestoneResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Status-only update — used when supervisor explicitly sets a status
     * (e.g. PENDING → IN_PROGRESS) without logging a progress percentage.
     * Enforces:
     * 1. Sequence: no prior milestone can be PENDING.
     * 2. Forward-only: cannot revert to a prior status.
     * 3. Notifies contractor on COMPLETED.
     */
    @Transactional
    public MilestoneResponse updateMilestoneStatus(
            UUID milestoneId, MilestoneUpdateRequest request, UUID userId) {

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Milestone not found: " + milestoneId));

        assertForwardTransition(milestone.getStatus(), request.getStatus());
        assertSequenceUnblocked(milestone);

        milestone.setStatus(request.getStatus());
        milestone.setUpdatedByUserId(userId);

        if (request.getStatus() == MilestoneStatus.COMPLETED) {
            milestone.setCompletedDate(LocalDate.now());
            milestone.setProgressPercent(100.0);
            recomputeProjectProgress(milestone.getProject().getId());
            notifyContractorOfCompletion(milestone);
        }

        return MilestoneResponse.fromEntity(milestoneRepository.save(milestone));
    }

    /**
     * Logs daily progress (+X%) AND optionally transitions status.
     * Enforces the same sequence and forward-only rules.
     * Auto-promotes to COMPLETED when progress reaches 100%.
     * Recomputes project.progressPercent in the same transaction.
     * Notifies contractor on COMPLETED (whether by log hitting 100% or
     * explicit statusOverride).
     */
    @Transactional
    public MilestoneResponse logProgress(
            UUID milestoneId, LogProgressRequest request, UUID userId) {

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Milestone not found: " + milestoneId));

        if (milestone.getStatus() == MilestoneStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Cannot log progress on a completed milestone.");
        }

        assertSequenceUnblocked(milestone);

        // 1. Create log entry
        MilestoneUpdateLog log = new MilestoneUpdateLog();
        log.setMilestoneId(milestoneId);
        log.setProjectId(milestone.getProject().getId());
        log.setProgressAddedPercent(request.progressAddedPercent);
        log.setRemarks(request.remarks);
        log.setLoggedByUserId(userId);
        logRepository.save(log);

        // 2. Recompute milestone total progress (SUM clamped to 100)
        Double total = logRepository.sumProgressForMilestone(milestoneId);
        double clamped = Math.min(100.0, total);
        milestone.setProgressPercent(clamped);

        // 3. Status transitions — strict forward-only
        boolean justCompleted = false;
        if (request.statusOverride != null) {
            assertForwardTransition(milestone.getStatus(), request.statusOverride);
            milestone.setStatus(request.statusOverride);
            if (request.statusOverride == MilestoneStatus.COMPLETED) {
                milestone.setCompletedDate(LocalDate.now());
                justCompleted = true;
            }
        } else if (clamped >= 100.0) {
            milestone.setStatus(MilestoneStatus.COMPLETED);
            milestone.setCompletedDate(LocalDate.now());
            justCompleted = true;
        } else if (milestone.getStatus() == MilestoneStatus.PENDING) {
            // Any progress logged implicitly starts the milestone
            milestone.setStatus(MilestoneStatus.IN_PROGRESS);
        }

        milestone.setUpdatedByUserId(userId);
        Milestone saved = milestoneRepository.save(milestone);

        // 4. Recompute project average progress
        recomputeProjectProgress(milestone.getProject().getId());

        // 5. Notify contractor if just completed
        if (justCompleted) {
            notifyContractorOfCompletion(saved);
        }

        return MilestoneResponse.fromEntity(saved);
    }

    public List<MilestoneUpdateLogResponse> getLogsForMilestone(UUID milestoneId) {
        return logRepository.findByMilestoneIdOrderByLoggedAtDesc(milestoneId)
                .stream()
                .map(MilestoneUpdateLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Enforces PENDING → IN_PROGRESS → COMPLETED only.
     * No skipping (PENDING → COMPLETED), no reverting.
     */
    private void assertForwardTransition(
            MilestoneStatus current, MilestoneStatus next) {

        boolean valid = switch (current) {
            case PENDING -> next == MilestoneStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == MilestoneStatus.COMPLETED
                    || next == MilestoneStatus.DELAYED;
            case DELAYED -> next == MilestoneStatus.IN_PROGRESS
                    || next == MilestoneStatus.COMPLETED;
            case COMPLETED -> false; // no transitions out of COMPLETED
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    String.format("Cannot transition milestone from %s to %s.",
                            current.name(), next.name()));
        }
    }

    /**
     * Enforces sequence: a milestone cannot be started or completed if any
     * earlier milestone (lower sequenceOrder) is still PENDING.
     * A null sequenceOrder is treated as unordered and skips this check.
     */
    private void assertSequenceUnblocked(Milestone milestone) {
        if (milestone.getSequenceOrder() == null) return;

        long blockingCount = milestoneRepository.countBlockingPendingMilestones(
                milestone.getProject().getId(),
                milestone.getSequenceOrder());

        if (blockingCount > 0) {
            throw new IllegalArgumentException(
                    "Complete prior milestones before starting \""
                            + milestone.getTitle() + "\".");
        }
    }

    /**
     * Recomputes Project.progressPercent as the average of all its
     * milestones' stored progressPercent values. Runs within the same
     * @Transactional block as the milestone update.
     */
    private void recomputeProjectProgress(UUID projectId) {
        List<Milestone> milestones = milestoneRepository
                .findByProjectIdOrderBySequenceOrderAsc(projectId);
        if (milestones.isEmpty()) return;

        double average = milestones.stream()
                .mapToDouble(Milestone::getProgressPercent)
                .average()
                .orElse(0.0);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + projectId));
        project.setProgressPercent(Math.min(100.0, average));
        projectRepository.save(project);
    }

    /**
     * Sends FCM push to the project's owning contractor when a milestone
     * completes. Failures are swallowed by PushNotificationService (logged,
     * not thrown) so a missing/expired FCM token never rolls back the
     * milestone completion itself.
     */
    private void notifyContractorOfCompletion(Milestone milestone) {
        try {
            Project project = projectRepository.findById(milestone.getProject().getId())
                    .orElse(null);
            if (project == null) return;

            User contractor = userRepository.findById(project.getContractorId())
                    .orElse(null);
            if (contractor == null || contractor.getFcmToken() == null) return;

            pushNotificationService.send(
                    contractor.getFcmToken(),
                    "Milestone Completed ✅",
                    "\"" + milestone.getTitle() + "\" has been marked complete.",
                    Map.of(
                            "type", "milestoneCompleted",
                            "projectId", milestone.getProject().getId().toString(),
                            "milestoneId", milestone.getId().toString()
                    )
            );
        } catch (Exception e) {
            // Notification send failures must never propagate —
            // the business operation (milestone completion) already
            // succeeded at this point.
        }
    }
}