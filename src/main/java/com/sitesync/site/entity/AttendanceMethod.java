package com.sitesync.site.entity;

/**
 * Wire values match what's stored in the DB and sent over the API.
 * PHOTO_GPS → "photoGps"
 * MANUAL    → "manual"
 * KIOSK_QR  → "kioskQr"   ← new, follows same camelCase pattern
 */
public enum AttendanceMethod {
    PHOTO_GPS("photoGps"),
    MANUAL("manual"),
    KIOSK_QR("kioskQr");

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
        // Backward-compat aliases for snake_case variants arriving from
        // older clients or direct DB reads
        if (normalized.equalsIgnoreCase("photogps") ||
                normalized.equalsIgnoreCase("photo_gps")) {
            return PHOTO_GPS;
        }
        if (normalized.equalsIgnoreCase("kiosk_qr") ||
                normalized.equalsIgnoreCase("kioskqr")) {
            return KIOSK_QR;
        }
        throw new IllegalArgumentException(
                "Unknown attendance method: " + value);
    }
}