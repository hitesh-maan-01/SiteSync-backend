package com.sitesync.site.entity;

/** Wire values match Dart's attendanceStatusToString (status.name): "present", "absent", "halfDay". */
public enum AttendanceStatus {
    PRESENT("present"),
    ABSENT("absent"),
    HALF_DAY("halfDay");

    private final String wireValue;

    AttendanceStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    public String toWireValue() {
        return wireValue;
    }

    public static AttendanceStatus fromWireValue(String value) {
        String normalized = value.trim();
        for (AttendanceStatus status : values()) {
            if (status.wireValue.equalsIgnoreCase(normalized)) return status;
        }
        if (normalized.equalsIgnoreCase("halfday") || normalized.equalsIgnoreCase("half_day")) {
            return HALF_DAY;
        }
        throw new IllegalArgumentException("Unknown attendance status: " + value);
    }
}