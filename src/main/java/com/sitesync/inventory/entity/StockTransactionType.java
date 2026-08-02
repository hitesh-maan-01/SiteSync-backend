package com.sitesync.inventory.entity;

public enum StockTransactionType {
    DELIVERY("delivery"),
    CONSUMPTION("consumption"),
    ADJUSTMENT("adjustment");

    private final String wireValue;

    StockTransactionType(String wireValue) {
        this.wireValue = wireValue;
    }

    public String toWireValue() {
        return wireValue;
    }

    public static StockTransactionType fromWireValue(String value) {
        for (StockTransactionType type : values()) {
            if (type.wireValue.equalsIgnoreCase(value.trim())) return type;
        }
        throw new IllegalArgumentException("Unknown stock transaction type: " + value);
    }
}