package com.sitesync.site.entity;

/** Wire values match Dart's attendanceMethodToString (method.name): "photoGps", "manual". */
public enum AttendanceMethod {
    PHOTO_GPS("photoGps"),
    MANUAL("manual");

    private final String wireValue;

    AttendanceMethod(String wireValue) {
        this.wireValue = wireValue;
    }

    public String toWireValue() {
        return wireValue;
    }

    public static AttendanceMethod fromWireValue(String value) {
        String normalized = value.trim();
        for (AttendanceMethod method : values()) {
            if (method.wireValue.equalsIgnoreCase(normalized)) return method;
        }
        if (normalized.equalsIgnoreCase("photogps") || normalized.equalsIgnoreCase("photo_gps")) {
            return PHOTO_GPS;
        }
        throw new IllegalArgumentException("Unknown attendance method: " + value);
    }
}