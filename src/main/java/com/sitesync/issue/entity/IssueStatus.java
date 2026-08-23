package com.sitesync.issue.entity;

public enum IssueStatus {
    OPEN, IN_PROGRESS, RESOLVED;

    public String toWireValue() {
        return name().toLowerCase();
    }

    public static IssueStatus fromWireValue(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}