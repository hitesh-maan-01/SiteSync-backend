package com.sitesync.analytics.dto;

import java.util.List;

/**
 * Single response DTO covering all 5 analytics sections for one project.
 * Returned by one endpoint so Flutter makes one network call, not 5.
 * All sections are computed server-side — Flutter only renders.
 */
public class ProjectAnalyticsResponse {

    public int windowDays;              // 7 / 30 / 90
    public AttendanceTrends attendance;
    public DeliveryPerformance deliveries;
    public MaterialConsumption materials;
    public MilestoneVelocity milestones;
    public IssueLogSummary issues;

    // ── Attendance ────────────────────────────────────────────────────────────

    public static class AttendanceTrends {
        public int totalWorkers;
        public double averageDailyAttendanceRate; // 0.0–1.0
        public int totalCheckins;
        public int flaggedCheckins;  // outside geofence
        public List<DailyAttendanceStat> dailyBreakdown;
    }

    public static class DailyAttendanceStat {
        public String date;       // yyyy-MM-dd
        public int present;
        public int absent;
        public int flagged;

        public DailyAttendanceStat(String date, int present,
                int absent, int flagged) {
            this.date = date;
            this.present = present;
            this.absent = absent;
            this.flagged = flagged;
        }
    }

    // ── Deliveries ────────────────────────────────────────────────────────────

    public static class DeliveryPerformance {
        public int total;
        public int onTime;
        public int late;
        public int pending;
        public int cancelled;
        public double onTimeRate;     // 0.0–1.0
        public double avgDelayHours;  // average hours late for LATE orders
    }

    // ── Materials ─────────────────────────────────────────────────────────────

    public static class MaterialConsumption {
        public double totalStockIn;   // sum of all positive deltas
        public double totalConsumed;  // sum of all negative deltas (abs)
        public List<MaterialStat> topMaterials; // top 5 by quantity consumed
    }

    public static class MaterialStat {
        public String materialName;
        public String unit;
        public double stockIn;
        public double consumed;
        public double currentStock;

        public MaterialStat(String materialName, String unit,
                double stockIn, double consumed, double currentStock) {
            this.materialName = materialName;
            this.unit = unit;
            this.stockIn = stockIn;
            this.consumed = consumed;
            this.currentStock = currentStock;
        }
    }

    // ── Milestones ────────────────────────────────────────────────────────────

    public static class MilestoneVelocity {
        public int total;
        public int completed;
        public int inProgress;
        public int pending;
        public int delayed;
        public double completionRate;   // 0.0–1.0
        public List<MilestoneStat> milestones;
    }

    public static class MilestoneStat {
        public String title;
        public String status;
        public double progressPercent;
        public String targetDate;
        public String completedDate;
        public boolean isOnTime; // completedDate <= targetDate

        public MilestoneStat(String title, String status,
                double progressPercent, String targetDate,
                String completedDate, boolean isOnTime) {
            this.title = title;
            this.status = status;
            this.progressPercent = progressPercent;
            this.targetDate = targetDate;
            this.completedDate = completedDate;
            this.isOnTime = isOnTime;
        }
    }

    // ── Issues ────────────────────────────────────────────────────────────────

    public static class IssueLogSummary {
        public int total;
        public int open;
        public int inProgress;
        public int resolved;
        public int critical;
        public int high;
        public int medium;
        public int low;
        public Double avgResolutionHours; // null if no resolved issues
    }
}