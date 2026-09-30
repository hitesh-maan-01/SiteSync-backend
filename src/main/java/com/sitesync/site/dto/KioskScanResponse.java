package com.sitesync.site.dto;

/**
 * Response to a kiosk scan. `alreadyCheckedIn` is true when the worker
 * was already marked today — the HTTP status is still 200 (idempotent)
 * but the UI can show "Already checked in today" instead of the normal
 * success checkmark if desired.
 */
public class KioskScanResponse {
    public boolean success;
    public boolean alreadyCheckedIn;
    public String workerName;
    public String attendanceId;
    public String checkedInAt;

    public static KioskScanResponse checkedIn(
            String workerName, String attendanceId, String checkedInAt) {
        KioskScanResponse r = new KioskScanResponse();
        r.success = true;
        r.alreadyCheckedIn = false;
        r.workerName = workerName;
        r.attendanceId = attendanceId;
        r.checkedInAt = checkedInAt;
        return r;
    }

    public static KioskScanResponse alreadyCheckedIn(String workerName) {
        KioskScanResponse r = new KioskScanResponse();
        r.success = true;
        r.alreadyCheckedIn = true;
        r.workerName = workerName;
        return r;
    }
}