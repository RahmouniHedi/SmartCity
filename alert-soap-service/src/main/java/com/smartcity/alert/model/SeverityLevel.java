package com.smartcity.alert.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB enumeration for alert severity levels.
 * Mapped to the SeverityLevel simpleType in Alert.xsd
 */
@XmlType(name = "SeverityLevel", namespace = "http://smartcity.com/alert")
@XmlEnum
public enum SeverityLevel {

    @XmlEnumValue("INFO")
    INFO("INFO", 1, "Informational"),

    @XmlEnumValue("WARNING")
    WARNING("WARNING", 2, "Warning"),

    @XmlEnumValue("SEVERE")
    SEVERE("SEVERE", 3, "Severe"),

    @XmlEnumValue("CRITICAL")
    CRITICAL("CRITICAL", 4, "Critical");

    private final String value;
    private final int priority;
    private final String displayName;

    SeverityLevel(String value, int priority, String displayName) {
        this.value = value;
        this.priority = priority;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public int getPriority() {
        return priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SeverityLevel fromValue(String value) {
        for (SeverityLevel level : SeverityLevel.values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid severity level: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}