package com.smartcity.incident.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration for incident status lifecycle.
 */
public enum IncidentStatus {
    REPORTED("REPORTED", "Newly reported"),
    ACKNOWLEDGED("ACKNOWLEDGED", "Acknowledged by dispatcher"),
    IN_PROGRESS("IN_PROGRESS", "Response team dispatched"),
    RESOLVED("RESOLVED", "Incident resolved"),
    CLOSED("CLOSED", "Case closed");

    private final String value;
    private final String description;

    IncidentStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static IncidentStatus fromValue(String value) {
        for (IncidentStatus status : IncidentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid incident status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}