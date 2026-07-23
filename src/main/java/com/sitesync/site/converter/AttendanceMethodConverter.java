package com.sitesync.site.converter;

import com.sitesync.site.entity.AttendanceMethod;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AttendanceMethodConverter implements AttributeConverter<AttendanceMethod, String> {
    @Override
    public String convertToDatabaseColumn(AttendanceMethod method) {
        return method == null ? null : method.toWireValue();
    }

    @Override
    public AttendanceMethod convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : AttendanceMethod.fromWireValue(dbValue);
    }
}