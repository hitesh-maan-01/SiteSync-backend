package com.sitesync.site.service;

import com.sitesync.project.service.ProjectService;
import com.sitesync.site.dto.AttendanceResponse;
import com.sitesync.site.entity.Attendance;
import com.sitesync.site.entity.AttendanceMethod;
import com.sitesync.site.entity.AttendanceStatus;
import com.sitesync.site.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Each public method corresponds to a SiteRepository method on the
 * Flutter side handling attendance - see site_repository.dart's doc
 * comments for the exact contract. Every method verifies project access
 * via ProjectService.getProjectById first (throws AccessDeniedException
 * if the requester isn't the owning contractor or assigned supervisor)
 * rather than duplicating that authorization rule here.
 *
 * One punch per worker per day: both markAttendanceWithPhoto and
 * markAttendanceManual funnel through assertNotAlreadyMarkedToday
 * before constructing a new Attendance row, backed by a DB-level
 * unique constraint on (worker_id, date) as a second line of defense
 * against race conditions (e.g. two rapid double-taps).
 */
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final FileStorageService fileStorageService;
    private final ProjectService projectService;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            FileStorageService fileStorageService,
            ProjectService projectService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.fileStorageService = fileStorageService;
        this.projectService = projectService;
    }

    /**
     * Full all-time attendance history for a project — used where the
     * complete record set is needed (e.g. a future attendance report
     * screen), distinct from the date-scoped methods below.
     */
    public List<AttendanceResponse> getAttendanceForProject(UUID projectId, UUID requesterId) {
        projectService.getProjectById(projectId, requesterId);
        return attendanceRepository.findByProjectIdOrderByTimestampDesc(projectId).stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Today-only attendance for a project — used by the supervisor's
     * mark-attendance screen, so a worker who's already been marked
     * today is reflected accurately (see the one-punch-per-day fix).
     * Delegates to getAttendanceForProjectOnDate with today's date, so
     * both methods share one query path rather than duplicating the
     * Instant-range logic.
     */
    public List<AttendanceResponse> getTodaysAttendanceForProject(UUID projectId, UUID requesterId) {
        return getAttendanceForProjectOnDate(projectId, requesterId, LocalDate.now(ZoneId.systemDefault()));
    }

    /**
     * Attendance for a project on a specific arbitrary date — backs the
     * Attendance Dashboard's date-strip navigation. Converts the given
     * LocalDate into a start/end Instant range in the server's zone and
     * queries against the timestamp column directly, rather than the
     * separately-backfilled `date` column, avoiding any LocalDate-vs-
     * Instant comparison mismatch at the JPA/SQL level.
     */
    public List<AttendanceResponse> getAttendanceForProjectOnDate(
            UUID projectId, UUID requesterId, LocalDate date
    ) {
        projectService.getProjectById(projectId, requesterId);

        ZoneId zone = ZoneId.systemDefault();
        Instant startOfDay = date.atStartOfDay(zone).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant();

        return attendanceRepository.findByProjectIdAndDateRange(projectId, startOfDay, endOfDay).stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AttendanceResponse markAttendanceWithPhoto(
            UUID projectId,
            UUID requesterId,
            UUID workerId,
            String workerName,
            String status,
            Double locationLatitude,
            Double locationLongitude,
            Double locationAccuracyMeters,
            Boolean withinGeofence,
            MultipartFile photo
    ) {
        projectService.getProjectById(projectId, requesterId);
        assertNotAlreadyMarkedToday(workerId, workerName);

        String photoUrl = fileStorageService.store(photo, "attendance");

        Instant now = Instant.now();
        Attendance attendance = new Attendance();
        attendance.setProjectId(projectId);
        attendance.setWorkerId(workerId);
        attendance.setWorkerName(workerName);
        attendance.setStatus(AttendanceStatus.fromWireValue(status));
        attendance.setMethod(AttendanceMethod.PHOTO_GPS);
        attendance.setTimestamp(now);
        attendance.setDate(LocalDate.ofInstant(now, ZoneId.systemDefault()));
        attendance.setPhotoUrl(photoUrl);
        attendance.setLocationLat(locationLatitude);
        attendance.setLocationLng(locationLongitude);
        attendance.setLocationAccuracyMeters(locationAccuracyMeters);
        attendance.setWithinGeofence(withinGeofence);

        Attendance saved = attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(saved);
    }

    public AttendanceResponse markAttendanceManual(
            UUID projectId,
            UUID requesterId,
            UUID workerId,
            String workerName,
            String status,
            UUID markedByUserId,
            String manualReason
    ) {
        projectService.getProjectById(projectId, requesterId);
        assertNotAlreadyMarkedToday(workerId, workerName);

        Instant now = Instant.now();
        Attendance attendance = new Attendance();
        attendance.setProjectId(projectId);
        attendance.setWorkerId(workerId);
        attendance.setWorkerName(workerName);
        attendance.setStatus(AttendanceStatus.fromWireValue(status));
        attendance.setMethod(AttendanceMethod.MANUAL);
        attendance.setTimestamp(now);
        attendance.setDate(LocalDate.ofInstant(now, ZoneId.systemDefault()));
        attendance.setMarkedByUserId(markedByUserId);
        attendance.setManualReason(manualReason);

        Attendance saved = attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(saved);
    }

    /**
     * Throws IllegalArgumentException (routed by GlobalExceptionHandler
     * to a clean 400 with a real message) if this worker already has an
     * attendance row for today's date. The DB's unique constraint on
     * (worker_id, date) is the authoritative guard against a race between
     * two concurrent requests; this check exists to fail fast with a
     * readable message before hitting that constraint violation, which
     * would otherwise surface as an opaque DataIntegrityViolationException.
     */
    private void assertNotAlreadyMarkedToday(UUID workerId, String workerName) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        if (attendanceRepository.existsByWorkerIdAndDate(workerId, today)) {
            throw new IllegalArgumentException(
                    workerName + " has already been marked for attendance today."
            );
        }
    }
}