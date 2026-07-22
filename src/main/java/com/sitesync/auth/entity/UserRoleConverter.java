package com.sitesync.auth.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Ensures the database column stores "contractor"/"supervisor"/"client"
 * (matching schema.sql's check constraint) rather than Java's uppercase
 * enum names. Without this, @Enumerated(EnumType.STRING) would persist
 * "CONTRACTOR" and violate the check constraint on every insert.
 */
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        return role == null ? null : role.toWireValue();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : UserRole.fromWireValue(dbValue);
    }
}