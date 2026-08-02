package com.sitesync.project.service;

import com.sitesync.project.dto.ProjectOverviewResponse;
import com.sitesync.site.entity.Attendance;
import com.sitesync.site.entity.AttendanceStatus;
import com.sitesync.site.repository.AttendanceRepository;
import com.sitesync.site.repository.WorkerRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Computes the "Live Overview" stat for the contractor dashboard: total
 * enrolled workers vs. how many were marked present (or half-day) today.
 * Deliberately a tiny aggregate endpoint rather than shipping the full
 * worker/attendance lists to Flutter for client-side counting — same
 * reasoning as InventoryService.getCurrentStock computing on the backend.
 */
@Service
public class ProjectOverviewService {

    private final WorkerRepository workerRepository;
    private final AttendanceRepository attendanceRepository;
    private final ProjectService projectService;

    // Both PRESENT and HALF_DAY count as "showed up today" for this stat —
    // a half-day worker was still on site. This is a product judgment
    // call, not a certainty; revisit if the contractor wants a stricter
    // full-day-only count instead.
    private static final Set<AttendanceStatus> PRESENT_STATUSES =
            Set.of(AttendanceStatus.PRESENT, AttendanceStatus.HALF_DAY);

    public ProjectOverviewService(
            WorkerRepository workerRepository,
            AttendanceRepository attendanceRepository,
            ProjectService projectService
    ) {
        this.workerRepository = workerRepository;
        this.attendanceRepository = attendanceRepository;
        this.projectService = projectService;
    }

    public ProjectOverviewResponse getOverview(UUID projectId, UUID requesterId) {
        projectService.getProjectById(projectId, requesterId);

        int totalEnrolled = workerRepository.findByProjectIdOrderByOnboardedAtAsc(projectId).size();

        // "Today" in the server's zone. Not project-timezone-aware yet —
        // fine for a single-country deployment, but worth revisiting if
        // SiteSync ever spans multiple timezones and a project's "today"
        // should follow local site time rather than server time.
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        Instant startOfDay = today.atStartOfDay(zone).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant();

        List<Attendance> todaysAttendance = attendanceRepository
                .findByProjectIdAndTimestampBetweenOrderByTimestampDesc(projectId, startOfDay, endOfDay);

        // A worker could theoretically have more than one attendance row
        // in a day (e.g. corrected/re-marked) — count distinct workers,
        // not raw rows, so a re-mark doesn't inflate the present count.
        long presentCount = todaysAttendance.stream()
                .filter(a -> PRESENT_STATUSES.contains(a.getStatus()))
                .map(Attendance::getWorkerId)
                .distinct()
                .count();

        return new ProjectOverviewResponse(totalEnrolled, (int) presentCount);
    }
}