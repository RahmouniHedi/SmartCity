package com.smartcity.incident.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database configuration and connection management utility.
 * Provides singleton connection pooling and table initialization.
 *
 * Configuration for MySQL:
 * - Database: smartcity_db
 * - Table: incidents
 * - User: root (change in production)
 *
 * @author Smart City Team
 */
public class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/smartcity_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password"; // Change in production!

    // Alternative: Use environment variables for security
    // private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static DatabaseConfig instance;
    private Connection connection;

    private DatabaseConfig() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
            throw new RuntimeException("Failed to load database driver", e);
        }
    }

    /**
     * Get singleton instance of DatabaseConfig.
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * Get database connection. Creates a new connection if needed.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            LOGGER.info("Creating new database connection...");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            LOGGER.info("Database connection established successfully");
        }
        return connection;
    }

    /**
     * Initialize database schema (create tables if they don't exist).
     */
    public void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create database if not exists
            String createDbSql = "CREATE DATABASE IF NOT EXISTS smartcity_db";
            stmt.executeUpdate(createDbSql);
            LOGGER.info("Database 'smartcity_db' ready");

            // Use the database
            stmt.executeUpdate("USE smartcity_db");

            // Create incidents table
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS incidents (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    type VARCHAR(100) NOT NULL,
                    description TEXT NOT NULL,
                    location VARCHAR(255) NOT NULL,
                    reported_by VARCHAR(100) NOT NULL,
                    status VARCHAR(50) NOT NULL DEFAULT 'REPORTED',
                    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    priority INT DEFAULT 3,
                    assigned_to VARCHAR(100),
                    INDEX idx_status (status),
                    INDEX idx_priority (priority),
                    INDEX idx_reported_at (reported_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;

            stmt.executeUpdate(createTableSql);
            LOGGER.info("Table 'incidents' ready");

            // Insert sample data if table is empty
            insertSampleDataIfEmpty(conn);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Insert sample incidents for demonstration (if table is empty).
     */
    private void insertSampleDataIfEmpty(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM incidents";
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                    INSERT INTO incidents (type, description, location, reported_by, status, priority) VALUES
                    ('Fire', 'Small fire in apartment building', '123 Main St', 'John Doe', 'IN_PROGRESS', 1),
                    ('Medical Emergency', 'Person collapsed in park', 'Central Park', 'Jane Smith', 'ACKNOWLEDGED', 1),
                    ('Traffic Accident', 'Two-car collision at intersection', '5th Ave & Oak St', 'Officer Johnson', 'RESOLVED', 2),
                    ('Power Outage', 'Entire block without electricity', 'Residential Area B', 'Anonymous', 'REPORTED', 3),
                    ('Water Main Break', 'Street flooding from broken pipe', 'Industrial District', 'City Worker', 'IN_PROGRESS', 2)
                    """;

                stmt.executeUpdate(insertSql);
                LOGGER.info("Sample incident data inserted");
            }
        }
    }

    /**
     * Close database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        }
    }

    /**
     * Test database connectivity.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }
}