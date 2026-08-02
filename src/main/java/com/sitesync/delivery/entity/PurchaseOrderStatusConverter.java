package com.sitesync.delivery.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PurchaseOrderStatusConverter implements AttributeConverter<PurchaseOrderStatus, String> {
    @Override
    public String convertToDatabaseColumn(PurchaseOrderStatus status) {
        return status == null ? null : status.toWireValue();
    }

    @Override
    public PurchaseOrderStatus convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : PurchaseOrderStatus.fromWireValue(dbValue);
    }
}