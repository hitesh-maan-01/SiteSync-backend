package com.sitesync.site.repository;

import com.sitesync.site.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByProjectIdOrderByTimestampDesc(UUID projectId);

    List<Attendance> findByProjectIdAndTimestampBetweenOrderByTimestampDesc(
            UUID projectId, Instant start, Instant end);

    boolean existsByWorkerIdAndDate(UUID workerId, LocalDate date);

    /**
     * Compares against the timestamp column via an Instant range rather
     * than the `date` column directly — timestamp is always populated
     * (set on every insert since the entity existed), whereas `date` was
     * only backfilled by the migration and could be null on any row that
     * bypassed the updated AttendanceService. Using timestamp here avoids
     * depending on that backfill being complete or correct.
     */
    @Query("""
        SELECT a FROM Attendance a
        WHERE a.projectId = :projectId
        AND a.timestamp >= :startOfDay AND a.timestamp < :endOfDay
        ORDER BY a.timestamp DESC
        """)
    List<Attendance> findByProjectIdAndDateRange(
            @Param("projectId") UUID projectId,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay
    );
}