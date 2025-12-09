package com.smartcity.incident.service;

import com.smartcity.incident.dao.IncidentDAO;
import com.smartcity.incident.model.Incident;
import com.smartcity.incident.model.IncidentStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business service layer for Incident operations.
 * Provides business logic and validation before delegating to DAO.
 *
 * @author Smart City Team
 */
public class IncidentService {

    private static final Logger LOGGER = Logger.getLogger(IncidentService.class.getName());
    private final IncidentDAO incidentDAO;

    public IncidentService() {
        this.incidentDAO = new IncidentDAO();
    }

    /**
     * Create a new incident with validation.
     */
    public Incident createIncident(Incident incident) throws SQLException {
        // Validate incident data
        validateIncident(incident);

        // Set default values if not provided
        if (incident.getStatus() == null) {
            incident.setStatus(IncidentStatus.REPORTED);
        }
        if (incident.getPriority() == null) {
            incident.setPriority(3); // Default medium priority
        }

        return incidentDAO.create(incident);
    }

    /**
     * Find incident by ID.
     */
    public Incident findById(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid incident ID");
        }
        return incidentDAO.findById(id);
    }

    /**
     * Find all incidents.
     */
    public List<Incident> findAll() throws SQLException {
        return incidentDAO.findAll();
    }

    /**
     * Update an existing incident.
     */
    public Incident updateIncident(Incident incident) throws SQLException {
        // Validate incident exists
        Incident existing = incidentDAO.findById(incident.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Incident not found with ID: " + incident.getId());
        }

        // Validate incident data
        validateIncident(incident);

        return incidentDAO.update(incident);
    }

    /**
     * Delete an incident.
     */
    public boolean deleteIncident(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid incident ID");
        }

        return incidentDAO.delete(id);
    }

    /**
     * Find incidents by status.
     */
    public List<Incident> findByStatus(IncidentStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return incidentDAO.findByStatus(status);
    }

    /**
     * Find high-priority incidents.
     */
    public List<Incident> findHighPriority() throws SQLException {
        return incidentDAO.findHighPriority();
    }

    /**
     * Update incident status.
     */
    public Incident updateStatus(Long id, IncidentStatus newStatus) throws SQLException {
        Incident incident = findById(id);
        if (incident == null) {
            throw new IllegalArgumentException("Incident not found with ID: " + id);
        }

        incident.setStatus(newStatus);
        return incidentDAO.update(incident);
    }

    /**
     * Assign incident to a responder.
     */
    public Incident assignIncident(Long id, String assignedTo) throws SQLException {
        Incident incident = findById(id);
        if (incident == null) {
            throw new IllegalArgumentException("Incident not found with ID: " + id);
        }

        incident.setAssignedTo(assignedTo);
        incident.setStatus(IncidentStatus.ACKNOWLEDGED);
        return incidentDAO.update(incident);
    }

    /**
     * Validate incident data.
     */
    private void validateIncident(Incident incident) {
        if (incident == null) {
            throw new IllegalArgumentException("Incident cannot be null");
        }

        if (incident.getType() == null || incident.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Incident type is required");
        }

        if (incident.getDescription() == null || incident.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Incident description is required");
        }

        if (incident.getLocation() == null || incident.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Incident location is required");
        }

        if (incident.getReportedBy() == null || incident.getReportedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("Reporter information is required");
        }

        if (incident.getPriority() != null && (incident.getPriority() < 1 || incident.getPriority() > 5)) {
            throw new IllegalArgumentException("Priority must be between 1 (highest) and 5 (lowest)");
        }
    }
}