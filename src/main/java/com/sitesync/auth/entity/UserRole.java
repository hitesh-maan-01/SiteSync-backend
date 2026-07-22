package com.sitesync.auth.entity;

/**
 * Java convention is uppercase enum constants, but Flutter's UserModel
 * expects lowercase role strings ("contractor", "supervisor", "client" -
 * see user_model.dart's userRoleFromString). Do NOT rely on this enum's
 * default toString()/name() when building a JSON response - always go
 * through toWireValue() so a naming-convention mismatch can't silently
 * break Flutter's role parsing.
 */
public enum UserRole {
    CONTRACTOR("contractor"),
    SUPERVISOR("supervisor"),
    CLIENT("client");

    private final String wireValue;

    UserRole(String wireValue) {
        this.wireValue = wireValue;
    }

    public String toWireValue() {
        return wireValue;
    }

    public static UserRole fromWireValue(String value) {
        for (UserRole role : values()) {
            if (role.wireValue.equalsIgnoreCase(value)) return role;
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}