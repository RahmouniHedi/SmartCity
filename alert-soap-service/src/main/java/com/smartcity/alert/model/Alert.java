package com.smartcity.alert.model;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * JAXB-annotated Alert entity representing a government emergency alert.
 * This class is mapped to the Alert.xsd schema.
 *
 * @author Smart City Team
 * @version 1.0
 */
@XmlRootElement(name = "alert", namespace = "http://smartcity.com/alert")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Alert", propOrder = {
        "id",
        "severity",
        "message",
        "region",
        "timestamp",
        "issuer"
})
public class Alert {

    @XmlElement(required = true, namespace = "http://smartcity.com/alert")
    private String id;

    @XmlElement(required = true, namespace = "http://smartcity.com/alert")
    @XmlSchemaType(name = "string")
    private SeverityLevel severity;

    @XmlElement(required = true, namespace = "http://smartcity.com/alert")
    private String message;

    @XmlElement(required = true, namespace = "http://smartcity.com/alert")
    private String region;

    @XmlElement(required = true, namespace = "http://smartcity.com/alert")
    @XmlSchemaType(name = "dateTime")
    private String timestamp;

    @XmlElement(namespace = "http://smartcity.com/alert")
    private String issuer;

    // Constructors
    public Alert() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public Alert(String id, SeverityLevel severity, String message, String region) {
        this.id = id;
        this.severity = severity;
        this.message = message;
        this.region = region;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public Alert(String id, SeverityLevel severity, String message, String region, String issuer) {
        this(id, severity, message, region);
        this.issuer = issuer;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    // Business methods
    public boolean isCritical() {
        return severity == SeverityLevel.CRITICAL;
    }

    public boolean isSevereOrHigher() {
        return severity == SeverityLevel.SEVERE || severity == SeverityLevel.CRITICAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alert alert = (Alert) o;
        return Objects.equals(id, alert.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id='" + id + '\'' +
                ", severity=" + severity +
                ", message='" + message + '\'' +
                ", region='" + region + '\'' +
                ", timestamp=" + timestamp +
                ", issuer='" + issuer + '\'' +
                '}';
    }
}