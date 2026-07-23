package com.sitesync.site.entity;

/** Wire values match Dart's materialUnitToString (unit.name): kg, ton, bag, cubicMeter, piece, liter, box. */
public enum MaterialUnit {
    KG("kg"),
    TON("ton"),
    BAG("bag"),
    CUBIC_METER("cubicMeter"),
    PIECE("piece"),
    LITER("liter"),
    BOX("box");

    private final String wireValue;

    MaterialUnit(String wireValue) {
        this.wireValue = wireValue;
    }

    public String toWireValue() {
        return wireValue;
    }

    public static MaterialUnit fromWireValue(String value) {
        String normalized = value.trim();
        for (MaterialUnit unit : values()) {
            if (unit.wireValue.equalsIgnoreCase(normalized)) return unit;
        }
        if (normalized.equalsIgnoreCase("cubicmeter") || normalized.equalsIgnoreCase("cubic_meter")) {
            return CUBIC_METER;
        }
        throw new IllegalArgumentException("Unknown material unit: " + value);
    }
}