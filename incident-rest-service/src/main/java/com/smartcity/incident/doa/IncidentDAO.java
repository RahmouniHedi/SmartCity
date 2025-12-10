package com.smartcity.incident.dao;

import com.smartcity.incident.model.Incident;
import com.smartcity.incident.model.IncidentStatus;
import com.smartcity.incident.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Incident entity.
 * Handles all database operations (CRUD) for incidents.
 *
 * @author Smart City Team
 */
public class IncidentDAO {

    private static final Logger LOGGER = Logger.getLogger(IncidentDAO.class.getName());
    private final DatabaseConfig dbConfig;

    public IncidentDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    /**
     * Create a new incident in the database.
     *
     * @param incident The incident to create
     * @return The created incident with generated ID
     * @throws SQLException if database operation fails
     */
    public Incident create(Incident incident) throws SQLException {
        String sql = "INSERT INTO incidents (type, description, location, reported_by, status, priority, assigned_to) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, incident.getType());
            pstmt.setString(2, incident.getDescription());
            pstmt.setString(3, incident.getLocation());
            pstmt.setString(4, incident.getReportedBy());
            pstmt.setString(5, incident.getStatus().getValue());
            pstmt.setInt(6, incident.getPriority());
            pstmt.setString(7, incident.getAssignedTo());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating incident failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    incident.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating incident failed, no ID obtained.");
                }
            }

            LOGGER.info("Created incident with ID: " + incident.getId());
            return incident;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating incident", e);
            throw e;
        }
    }

    /**
     * Find an incident by ID.
     *
     * @param id The incident ID
     * @return The incident, or null if not found
     * @throws SQLException if database operation fails
     */
    public Incident findById(Long id) throws SQLException {
        String sql = "SELECT * FROM incidents WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToIncident(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding incident by ID: " + id, e);
            throw e;
        }
    }

    /**
     * Find all incidents.
     *
     * @return List of all incidents
     * @throws SQLException if database operation fails
     */
    public List<Incident> findAll() throws SQLException {
        String sql = "SELECT * FROM incidents ORDER BY reported_at DESC";
        List<Incident> incidents = new ArrayList<>();

        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                incidents.add(mapResultSetToIncident(rs));
            }

            LOGGER.info("Retrieved " + incidents.size() + " incidents");
            return incidents;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all incidents", e);
            throw e;
        }
    }

    /**
     * Update an existing incident.
     *
     * @param incident The incident to update
     * @return The updated incident
     * @throws SQLException if database operation fails
     */
    public Incident update(Incident incident) throws SQLException {
        String sql = "UPDATE incidents " +
                     "SET type = ?, description = ?, location = ?, reported_by = ?, " +
                     "status = ?, priority = ?, assigned_to = ? " +
                     "WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, incident.getType());
            pstmt.setString(2, incident.getDescription());
            pstmt.setString(3, incident.getLocation());
            pstmt.setString(4, incident.getReportedBy());
            pstmt.setString(5, incident.getStatus().getValue());
            pstmt.setInt(6, incident.getPriority());
            pstmt.setString(7, incident.getAssignedTo());
            pstmt.setLong(8, incident.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating incident failed, no rows affected.");
            }

            LOGGER.info("Updated incident with ID: " + incident.getId());
            return incident;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating incident", e);
            throw e;
        }
    }

    /**
     * Delete an incident by ID.
     *
     * @param id The incident ID
     * @return true if deleted, false if not found
     * @throws SQLException if database operation fails
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM incidents WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();

            boolean deleted = affectedRows > 0;
            if (deleted) {
                LOGGER.info("Deleted incident with ID: " + id);
            } else {
                LOGGER.warning("No incident found with ID: " + id);
            }

            return deleted;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting incident with ID: " + id, e);
            throw e;
        }
    }

    /**
     * Find incidents by status.
     *
     * @param status The incident status
     * @return List of incidents with the specified status
     * @throws SQLException if database operation fails
     */
    public List<Incident> findByStatus(IncidentStatus status) throws SQLException {
        String sql = "SELECT * FROM incidents WHERE status = ? ORDER BY reported_at DESC";
        List<Incident> incidents = new ArrayList<>();

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.getValue());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    incidents.add(mapResultSetToIncident(rs));
                }
            }

            return incidents;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding incidents by status", e);
            throw e;
        }
    }

    /**
     * Find high-priority incidents (priority <= 2).
     *
     * @return List of high-priority incidents
     * @throws SQLException if database operation fails
     */
    public List<Incident> findHighPriority() throws SQLException {
        String sql = "SELECT * FROM incidents WHERE priority <= 2 ORDER BY priority, reported_at DESC";
        List<Incident> incidents = new ArrayList<>();

        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                incidents.add(mapResultSetToIncident(rs));
            }

            return incidents;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding high-priority incidents", e);
            throw e;
        }
    }

    /**
     * Map ResultSet row to Incident object.
     */
    private Incident mapResultSetToIncident(ResultSet rs) throws SQLException {
        Incident incident = new Incident();

        incident.setId(rs.getLong("id"));
        incident.setType(rs.getString("type"));
        incident.setDescription(rs.getString("description"));
        incident.setLocation(rs.getString("location"));
        incident.setReportedBy(rs.getString("reported_by"));
        incident.setStatus(IncidentStatus.fromValue(rs.getString("status")));

        Timestamp timestamp = rs.getTimestamp("reported_at");
        if (timestamp != null) {
            incident.setReportedAt(timestamp.toLocalDateTime());
        }

        incident.setPriority(rs.getInt("priority"));
        incident.setAssignedTo(rs.getString("assigned_to"));

        return incident;
    }
}