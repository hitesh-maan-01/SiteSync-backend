package com.sitesync.issue.entity;

public enum IssueUrgency {
    LOW, MEDIUM, HIGH, CRITICAL;

    public String toWireValue() {
        return name().toLowerCase();
    }

    public static IssueUrgency fromWireValue(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}