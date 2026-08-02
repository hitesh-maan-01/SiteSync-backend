package com.sitesync.inventory.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StockTransactionTypeConverter implements AttributeConverter<StockTransactionType, String> {
    @Override
    public String convertToDatabaseColumn(StockTransactionType type) {
        return type == null ? null : type.toWireValue();
    }

    @Override
    public StockTransactionType convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : StockTransactionType.fromWireValue(dbValue);
    }
}