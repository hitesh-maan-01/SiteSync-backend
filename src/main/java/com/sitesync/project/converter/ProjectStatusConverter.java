package com.sitesync.project.converter;

import com.sitesync.project.entity.ProjectStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProjectStatusConverter implements AttributeConverter<ProjectStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProjectStatus status) {
        return status == null ? null : status.toWireValue();
    }

    @Override
    public ProjectStatus convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : ProjectStatus.fromWireValue(dbValue);
    }
}