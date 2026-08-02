package com.sitesync.delivery.entity;

public enum PurchaseOrderStatus {
    PENDING("pending"),
    DELIVERED("delivered"),
    LATE("late"),
    CANCELLED("cancelled");

    private final String wireValue;

    PurchaseOrderStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    public String toWireValue() {
        return wireValue;
    }

    public static PurchaseOrderStatus fromWireValue(String value) {
        for (PurchaseOrderStatus status : values()) {
            if (status.wireValue.equalsIgnoreCase(value.trim())) return status;
        }
        throw new IllegalArgumentException("Unknown purchase order status: " + value);
    }
}