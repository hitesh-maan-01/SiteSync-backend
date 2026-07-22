package com.sitesync.project.entity;

/**
 * Wire values must match Dart's projectStatusToString exactly, which is
 * status.name on the enum - i.e. the enum constant's own camelCase name
 * (ProjectStatus.onHold -> "onHold"), NOT "on_hold" or "onhold". Flutter's
 * projectStatusFromString does accept those two lowercase variants
 * defensively when reading, but never writes them - so this enum's
 * toWireValue() must emit "onHold" to match what Flutter sends back and
 * forth consistently.
 */
public enum ProjectStatus {
    ACTIVE("active"),
    ON_HOLD("onHold"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String wireValue;

    ProjectStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    public String toWireValue() {
        return wireValue;
    }

    public static ProjectStatus fromWireValue(String value) {
        String normalized = value.trim();
        for (ProjectStatus status : values()) {
            if (status.wireValue.equalsIgnoreCase(normalized)) return status;
        }
        if (normalized.equalsIgnoreCase("onhold") || normalized.equalsIgnoreCase("on_hold")) {
            return ON_HOLD;
        }
        throw new IllegalArgumentException("Unknown project status: " + value);
    }
}