package com.sitesync.site.converter;

import com.sitesync.site.entity.MaterialUnit;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MaterialUnitConverter implements AttributeConverter<MaterialUnit, String> {
    @Override
    public String convertToDatabaseColumn(MaterialUnit unit) {
        return unit == null ? null : unit.toWireValue();
    }

    @Override
    public MaterialUnit convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : MaterialUnit.fromWireValue(dbValue);
    }
}