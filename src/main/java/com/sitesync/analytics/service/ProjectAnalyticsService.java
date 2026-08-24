package com.sitesync.analytics.service;

import com.sitesync.analytics.dto.ProjectAnalyticsResponse;
import com.sitesync.analytics.dto.ProjectAnalyticsResponse.*;
import com.sitesync.issue.entity.IssueStatus;
import com.sitesync.issue.entity.IssueUrgency;
import com.sitesync.issue.repository.IssueLogRepository;
import com.sitesync.milestone.entity.MilestoneStatus;
import com.sitesync.milestone.repository.MilestoneRepository;
import com.sitesync.delivery.entity.PurchaseOrderStatus;
import com.sitesync.delivery.repository.PurchaseOrderRepository;
import com.sitesync.inventory.repository.StockLedgerRepository;
import com.sitesync.site.repository.AttendanceRepository;
import com.sitesync.site.repository.WorkerRepository;
import com.sitesync.site.entity.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectAnalyticsService {

    private final AttendanceRepository attendanceRepository;
    private final WorkerRepository workerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final MilestoneRepository milestoneRepository;
    private final IssueLogRepository issueLogRepository;

    public ProjectAnalyticsResponse getAnalytics(UUID projectId, int days) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalDate windowStart = today.minusDays(days - 1);
        Instant startInstant = windowStart.atStartOfDay(zone).toInstant();
        Instant endInstant = today.plusDays(1).atStartOfDay(zone).toInstant();

        ProjectAnalyticsResponse response = new ProjectAnalyticsResponse();
        response.windowDays = days;
        response.attendance = buildAttendance(
                projectId, windowStart, today, startInstant, endInstant);
        response.deliveries = buildDeliveries(
                projectId, startInstant, endInstant);
        response.materials = buildMaterials(
                projectId, startInstant, endInstant);
        response.milestones = buildMilestones(projectId);
        response.issues = buildIssues(projectId, startInstant, endInstant);
        return response;
    }

    // ── Attendance ────────────────────────────────────────────────────────────

    private AttendanceTrends buildAttendance(
            UUID projectId, LocalDate windowStart, LocalDate today,
            Instant startInstant, Instant endInstant) {

        int totalWorkers = workerRepository
                .findByProjectIdOrderByOnboardedAtAsc(projectId).size();

        var records = attendanceRepository
                .findByProjectIdAndTimestampBetweenOrderByTimestampDesc(
                        projectId, startInstant, endInstant);

        // Group by date
        Map<LocalDate, List<com.sitesync.site.entity.Attendance>> byDate =
                records.stream().collect(Collectors.groupingBy(
                        a -> a.getDate() != null
                                ? a.getDate()
                                : LocalDate.ofInstant(
                                        a.getTimestamp(), ZoneId.systemDefault())));

        List<DailyAttendanceStat> daily = new ArrayList<>();
        for (LocalDate d = windowStart; !d.isAfter(today); d = d.plusDays(1)) {
            var dayRecords = byDate.getOrDefault(d, List.of());
            int present = (int) dayRecords.stream().filter(a ->
                    a.getStatus() == AttendanceStatus.PRESENT ||
                    a.getStatus() == AttendanceStatus.HALF_DAY).count();
            int flagged = (int) dayRecords.stream().filter(a ->
                    Boolean.FALSE.equals(a.getWithinGeofence())).count();
            int absent = Math.max(0, totalWorkers - present);
            daily.add(new DailyAttendanceStat(
                    d.toString(), present, absent, flagged));
        }

        int totalCheckins = records.size();
        int flaggedTotal = (int) records.stream().filter(a ->
                Boolean.FALSE.equals(a.getWithinGeofence())).count();
        double avgRate = daily.stream()
                .filter(d -> (d.present + d.absent) > 0)
                .mapToDouble(d -> (double) d.present / (d.present + d.absent))
                .average().orElse(0.0);

        AttendanceTrends trends = new AttendanceTrends();
        trends.totalWorkers = totalWorkers;
        trends.averageDailyAttendanceRate = avgRate;
        trends.totalCheckins = totalCheckins;
        trends.flaggedCheckins = flaggedTotal;
        trends.dailyBreakdown = daily;
        return trends;
    }

    // ── Deliveries ────────────────────────────────────────────────────────────

    private DeliveryPerformance buildDeliveries(
            UUID projectId, Instant start, Instant end) {

        var orders = purchaseOrderRepository
                .findByProjectIdOrderByExpectedAtDesc(projectId)
                .stream()
                .filter(o -> o.getCreatedAt() != null &&
                        !o.getCreatedAt().isBefore(start) &&
                        o.getCreatedAt().isBefore(end))
                .collect(Collectors.toList());

        int total = orders.size();
        int onTime = 0, late = 0, pending = 0, cancelled = 0;
        double totalDelayHours = 0;
        int lateCount = 0;

        for (var o : orders) {
            switch (o.getStatus()) {
                case DELIVERED -> {
                    if (o.getDeliveredAt() != null &&
                            o.getExpectedAt() != null &&
                            o.getDeliveredAt().isAfter(o.getExpectedAt())) {
                        late++;
                        totalDelayHours += Duration.between(
                                o.getExpectedAt(),
                                o.getDeliveredAt()).toMinutes() / 60.0;
                        lateCount++;
                    } else {
                        onTime++;
                    }
                }
                case LATE       -> late++;
                case PENDING    -> pending++;
                case CANCELLED  -> cancelled++;
            }
        }

        DeliveryPerformance perf = new DeliveryPerformance();
        perf.total = total;
        perf.onTime = onTime;
        perf.late = late;
        perf.pending = pending;
        perf.cancelled = cancelled;
        perf.onTimeRate = total > 0
                ? (double) onTime / (total - pending - cancelled) : 1.0;
        perf.avgDelayHours = lateCount > 0
                ? totalDelayHours / lateCount : 0.0;
        return perf;
    }

    // ── Materials ─────────────────────────────────────────────────────────────

    private MaterialConsumption buildMaterials(
            UUID projectId, Instant start, Instant end) {

        // All-time stock entries (current stock must reflect entire history,
        // not just the window — window filter applies to the trend, not
        // the current balance)
        var allEntries = stockLedgerRepository
                .findByProjectIdOrderByTimestampDesc(projectId);

        // Window entries for trend stats
        var windowEntries = allEntries.stream()
                .filter(e -> !e.getTimestamp().isBefore(start) &&
                        e.getTimestamp().isBefore(end))
                .collect(Collectors.toList());

        double totalIn = windowEntries.stream()
                .filter(e -> e.getQuantityDelta() > 0)
                .mapToDouble(e -> e.getQuantityDelta()).sum();
        double totalOut = windowEntries.stream()
                .filter(e -> e.getQuantityDelta() < 0)
                .mapToDouble(e -> Math.abs(e.getQuantityDelta())).sum();

        // Per-material stats (all-time for current stock, window for in/out)
        Map<String, double[]> byMaterial = new LinkedHashMap<>();
        // [0]=allTimeIn, [1]=allTimeOut, [2]=windowIn, [3]=windowOut, [4]=unit(ignored)
        for (var e : allEntries) {
            byMaterial.computeIfAbsent(e.getMaterialName(),
                    k -> new double[5]);
            if (e.getQuantityDelta() > 0) {
                byMaterial.get(e.getMaterialName())[0] += e.getQuantityDelta();
                if (!e.getTimestamp().isBefore(start) &&
                        e.getTimestamp().isBefore(end)) {
                    byMaterial.get(e.getMaterialName())[2] += e.getQuantityDelta();
                }
            } else {
                byMaterial.get(e.getMaterialName())[1] +=
                        Math.abs(e.getQuantityDelta());
                if (!e.getTimestamp().isBefore(start) &&
                        e.getTimestamp().isBefore(end)) {
                    byMaterial.get(e.getMaterialName())[3] +=
                            Math.abs(e.getQuantityDelta());
                }
            }
        }

        // Unit lookup
        Map<String, String> unitMap = new HashMap<>();
        for (var e : allEntries) {
            unitMap.putIfAbsent(e.getMaterialName(),
                    e.getUnit().toWireValue());
        }

        List<MaterialStat> top = byMaterial.entrySet().stream()
                .map(entry -> {
                    double[] v = entry.getValue();
                    double currentStock = v[0] - v[1];
                    return new MaterialStat(
                            entry.getKey(),
                            unitMap.getOrDefault(entry.getKey(), ""),
                            v[2],   // window stock-in
                            v[3],   // window consumed
                            currentStock);
                })
                .sorted(Comparator.comparingDouble(
                        (MaterialStat s) -> s.consumed).reversed())
                .limit(5)
                .collect(Collectors.toList());

        MaterialConsumption mat = new MaterialConsumption();
        mat.totalStockIn = totalIn;
        mat.totalConsumed = totalOut;
        mat.topMaterials = top;
        return mat;
    }

    // ── Milestones ────────────────────────────────────────────────────────────

    private MilestoneVelocity buildMilestones(UUID projectId) {
        var milestones = milestoneRepository
                .findByProjectIdOrderBySequenceOrderAsc(projectId);

        int completed = 0, inProgress = 0, pending = 0, delayed = 0;
        List<MilestoneStat> stats = new ArrayList<>();

        for (var m : milestones) {
            switch (m.getStatus()) {
                case COMPLETED  -> completed++;
                case IN_PROGRESS -> inProgress++;
                case PENDING    -> pending++;
                case DELAYED    -> delayed++;
            }

            boolean isOnTime = true;
            if (m.getStatus() == MilestoneStatus.COMPLETED
                    && m.getCompletedDate() != null
                    && m.getTargetDate() != null) {
                isOnTime = !m.getCompletedDate().isAfter(m.getTargetDate());
            } else if (m.getStatus() != MilestoneStatus.COMPLETED
                    && m.getTargetDate() != null
                    && m.getTargetDate().isBefore(LocalDate.now())) {
                isOnTime = false;
            }

            stats.add(new MilestoneStat(
                    m.getTitle(),
                    m.getStatus().name().toLowerCase(),
                    m.getProgressPercent(),
                    m.getTargetDate() != null
                            ? m.getTargetDate().toString() : null,
                    m.getCompletedDate() != null
                            ? m.getCompletedDate().toString() : null,
                    isOnTime));
        }

        MilestoneVelocity velocity = new MilestoneVelocity();
        velocity.total = milestones.size();
        velocity.completed = completed;
        velocity.inProgress = inProgress;
        velocity.pending = pending;
        velocity.delayed = delayed;
        velocity.completionRate = milestones.isEmpty()
                ? 0.0 : (double) completed / milestones.size();
        velocity.milestones = stats;
        return velocity;
    }

    // ── Issues ────────────────────────────────────────────────────────────────

    private IssueLogSummary buildIssues(
            UUID projectId, Instant start, Instant end) {

        var allIssues = issueLogRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId);
        var windowIssues = allIssues.stream()
                .filter(i -> !i.getCreatedAt().isBefore(start) &&
                        i.getCreatedAt().isBefore(end))
                .collect(Collectors.toList());

        IssueLogSummary summary = new IssueLogSummary();
        summary.total = windowIssues.size();
        summary.open = (int) windowIssues.stream().filter(
                i -> i.getStatus() == IssueStatus.OPEN).count();
        summary.inProgress = (int) windowIssues.stream().filter(
                i -> i.getStatus() == IssueStatus.IN_PROGRESS).count();
        summary.resolved = (int) windowIssues.stream().filter(
                i -> i.getStatus() == IssueStatus.RESOLVED).count();
        summary.critical = (int) windowIssues.stream().filter(
                i -> i.getUrgency() == IssueUrgency.CRITICAL).count();
        summary.high = (int) windowIssues.stream().filter(
                i -> i.getUrgency() == IssueUrgency.HIGH).count();
        summary.medium = (int) windowIssues.stream().filter(
                i -> i.getUrgency() == IssueUrgency.MEDIUM).count();
        summary.low = (int) windowIssues.stream().filter(
                i -> i.getUrgency() == IssueUrgency.LOW).count();

        // Avg resolution time (hours) — only for issues resolved in window
        var resolved = windowIssues.stream()
                .filter(i -> i.getStatus() == IssueStatus.RESOLVED
                        && i.getResolvedAt() != null)
                .collect(Collectors.toList());
        summary.avgResolutionHours = resolved.isEmpty() ? null :
                resolved.stream()
                        .mapToDouble(i -> Duration.between(
                                i.getCreatedAt(),
                                i.getResolvedAt()).toMinutes() / 60.0)
                        .average().orElse(0.0);
        return summary;
    }
}