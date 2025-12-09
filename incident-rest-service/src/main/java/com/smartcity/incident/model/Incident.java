package com.smartcity.incident.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Incident entity representing citizen-reported emergencies.
 * This class is serialized to/from JSON for REST API operations.
 *
 * @author Smart City Team
 * @version 1.0
 */
public class Incident {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("location")
    private String location;

    @JsonProperty("reportedBy")
    private String reportedBy;

    @JsonProperty("status")
    private IncidentStatus status;

    @JsonProperty("reportedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reportedAt;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("assignedTo")
    private String assignedTo;

    // Constructors
    public Incident() {
        this.status = IncidentStatus.REPORTED;
        this.reportedAt = LocalDateTime.now();
        this.priority = 3; // Default medium priority
    }

    public Incident(String type, String description, String location, String reportedBy) {
        this();
        this.type = type;
        this.description = description;
        this.location = location;
        this.reportedBy = reportedBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    // Business methods
    public boolean isHighPriority() {
        return priority != null && priority <= 2;
    }

    public boolean isResolved() {
        return status == IncidentStatus.RESOLVED;
    }

    public void markInProgress() {
        this.status = IncidentStatus.IN_PROGRESS;
    }

    public void markResolved() {
        this.status = IncidentStatus.RESOLVED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Incident incident = (Incident) o;
        return Objects.equals(id, incident.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Incident{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", reportedBy='" + reportedBy + '\'' +
                ", status=" + status +
                ", reportedAt=" + reportedAt +
                ", priority=" + priority +
                ", assignedTo='" + assignedTo + '\'' +
                '}';
    }
}