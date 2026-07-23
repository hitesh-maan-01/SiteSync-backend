package com.sitesync.site.repository;

import com.sitesync.site.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByProjectIdOrderByTimestampDesc(UUID projectId);

    List<Attendance> findByProjectIdAndTimestampBetweenOrderByTimestampDesc(
            UUID projectId, Instant start, Instant end);
}