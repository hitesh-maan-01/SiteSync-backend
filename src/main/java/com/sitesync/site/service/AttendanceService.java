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

    public List<AttendanceResponse> getAttendanceForProject(UUID projectId, UUID requesterId) {
        projectService.getProjectById(projectId, requesterId);
        return attendanceRepository.findByProjectIdOrderByTimestampDesc(projectId).stream()
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

        String photoUrl = fileStorageService.store(photo, "attendance");

        Attendance attendance = new Attendance();
        attendance.setProjectId(projectId);
        attendance.setWorkerId(workerId);
        attendance.setWorkerName(workerName);
        attendance.setStatus(AttendanceStatus.fromWireValue(status));
        attendance.setMethod(AttendanceMethod.PHOTO_GPS);
        attendance.setTimestamp(Instant.now());
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

        Attendance attendance = new Attendance();
        attendance.setProjectId(projectId);
        attendance.setWorkerId(workerId);
        attendance.setWorkerName(workerName);
        attendance.setStatus(AttendanceStatus.fromWireValue(status));
        attendance.setMethod(AttendanceMethod.MANUAL);
        attendance.setTimestamp(Instant.now());
        attendance.setMarkedByUserId(markedByUserId);
        attendance.setManualReason(manualReason);

        Attendance saved = attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(saved);
    }
}