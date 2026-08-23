package com.sitesync.issue.service;

import com.sitesync.auth.entity.User;
import com.sitesync.auth.repository.UserRepository;
import com.sitesync.issue.dto.IssueLogResponse;
import com.sitesync.issue.entity.IssueLog;
import com.sitesync.issue.entity.IssueStatus;
import com.sitesync.issue.entity.IssueUrgency;
import com.sitesync.issue.repository.IssueLogRepository;
import com.sitesync.notification.entity.NotificationSeverity;
import com.sitesync.notification.service.PushNotificationService;
import com.sitesync.project.entity.Project;
import com.sitesync.project.repository.ProjectRepository;
import com.sitesync.site.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueLogService {

    private final IssueLogRepository issueLogRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PushNotificationService pushNotificationService;

    public List<IssueLogResponse> getIssuesForProject(UUID projectId) {
        return issueLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(IssueLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueLogResponse createIssue(
            UUID projectId,
            UUID createdByUserId,
            String title,
            String description,
            String urgency,
            MultipartFile photo) {

        IssueLog issue = new IssueLog();
        issue.setProjectId(projectId);
        issue.setCreatedByUserId(createdByUserId);
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setUrgency(IssueUrgency.fromWireValue(urgency));
        issue.setStatus(IssueStatus.OPEN);

        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileStorageService.store(photo, "issues");
            issue.setPhotoUrl(photoUrl);
        }

        IssueLog saved = issueLogRepository.save(issue);

        // Notify contractor immediately — issues are always time-sensitive
        notifyContractor(saved, projectId);

        return IssueLogResponse.fromEntity(saved);
    }

    @Transactional
    public IssueLogResponse resolveIssue(
            UUID issueId, UUID resolvedByUserId) {

        IssueLog issue = issueLogRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Issue not found: " + issueId));

        if (issue.getStatus() == IssueStatus.RESOLVED) {
            throw new IllegalArgumentException("Issue is already resolved.");
        }

        issue.setStatus(IssueStatus.RESOLVED);
        issue.setResolvedByUserId(resolvedByUserId);
        issue.setResolvedAt(Instant.now());

        return IssueLogResponse.fromEntity(issueLogRepository.save(issue));
    }

    @Transactional
    public IssueLogResponse updateStatus(
            UUID issueId, UUID userId, String newStatus) {

        IssueLog issue = issueLogRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Issue not found: " + issueId));

        IssueStatus status = IssueStatus.fromWireValue(newStatus);
        issue.setStatus(status);

        if (status == IssueStatus.RESOLVED) {
            issue.setResolvedByUserId(userId);
            issue.setResolvedAt(Instant.now());
        }

        return IssueLogResponse.fromEntity(issueLogRepository.save(issue));
    }

    public long getOpenIssueCount(UUID projectId) {
        return issueLogRepository.countByProjectIdAndStatus(
                projectId, IssueStatus.OPEN);
    }

    private void notifyContractor(IssueLog issue, UUID projectId) {
        try {
            Project project = projectRepository.findById(projectId)
                    .orElse(null);
            if (project == null) return;

            User contractor = userRepository.findById(project.getContractorId())
                    .orElse(null);
            if (contractor == null || contractor.getFcmToken() == null) return;

            NotificationSeverity severity = switch (issue.getUrgency()) {
                case CRITICAL -> NotificationSeverity.CRITICAL;
                case HIGH     -> NotificationSeverity.WARNING;
                default       -> NotificationSeverity.INFO;
            };

            String actionRoute =
                    "/contractor-dash/project/" + projectId + "/issues";

            pushNotificationService.sendAndPersist(
                    contractor.getFcmToken(),
                    contractor.getId(),
                    urgencyPrefix(issue.getUrgency()) + " Issue Reported",
                    "\"" + issue.getTitle() + "\" — tap to review.",
                    severity,
                    "issueReported",
                    "View Issue",
                    actionRoute,
                    Map.of("projectId", projectId.toString(),
                           "issueId", issue.getId().toString())
            );
        } catch (Exception e) {
            // Non-fatal
        }
    }

    private String urgencyPrefix(IssueUrgency urgency) {
        return switch (urgency) {
            case CRITICAL -> "🔴 Critical";
            case HIGH     -> "🟠 High";
            case MEDIUM   -> "🟡 Medium";
            case LOW      -> "🟢 Low";
        };
    }
}