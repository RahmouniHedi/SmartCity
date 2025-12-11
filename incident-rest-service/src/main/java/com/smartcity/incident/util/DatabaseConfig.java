package com.smartcity.incident.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "smartcity_db";
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_URL_WITHOUT_DB = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Change in production!

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
        LOGGER.info("Initializing database...");

        // First, ensure the database exists
        try (Connection conn = DriverManager.getConnection(DB_URL_WITHOUT_DB, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Create database if not exists
            String createDbSql = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stmt.executeUpdate(createDbSql);
            LOGGER.info("Database '" + DB_NAME + "' is ready");

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Could not create database (it may already exist or MySQL is not running): " + e.getMessage());
            // Continue anyway - the database might already exist
        }

        // Now connect to the specific database and create tables
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create incidents table
            String createTableSql = "CREATE TABLE IF NOT EXISTS incidents (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "type VARCHAR(100) NOT NULL," +
                    "description TEXT NOT NULL," +
                    "location VARCHAR(255) NOT NULL," +
                    "reported_by VARCHAR(100) NOT NULL," +
                    "status VARCHAR(50) NOT NULL DEFAULT 'REPORTED'," +
                    "reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "priority INT DEFAULT 3," +
                    "assigned_to VARCHAR(100)," +
                    "INDEX idx_status (status)," +
                    "INDEX idx_priority (priority)," +
                    "INDEX idx_reported_at (reported_at)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

            stmt.executeUpdate(createTableSql);
            LOGGER.info("Table 'incidents' is ready");

            // Insert sample data if table is empty
            insertSampleDataIfEmpty(conn);

            LOGGER.info("Database initialization completed successfully");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database tables: " + e.getMessage(), e);
            LOGGER.severe("Please ensure MySQL is running on " + DB_HOST + ":" + DB_PORT);
            LOGGER.severe("You can start MySQL with: net start MySQL (Windows) or systemctl start mysql (Linux)");
            throw new RuntimeException("Database initialization failed - MySQL may not be running", e);
        }
    }

    /**
     * Insert sample incidents for demonstration (if table is empty).
     */
    private void insertSampleDataIfEmpty(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM incidents";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO incidents (type, description, location, reported_by, status, priority) VALUES " +
                        "('Incendie', 'Feu de broussailles près de la forêt, risque de propagation', 'Bizerte - Corniche', 'Ahmed Tounsi', 'IN_PROGRESS', 1)," +
                        "('Urgence Médicale', 'Malaise cardiaque dans un café', 'Sousse - Sahloul', 'Sami Ben Amor', 'ACKNOWLEDGED', 1)," +
                        "('Accident Route', 'Collision entre un camion et une voiture', 'Tunis - Route X', 'Police Circulation', 'RESOLVED', 2)," +
                        "('Panne Électricité', 'Coupure de courant dans tout le quartier depuis 2h', 'Sfax - Route El Ain', 'Anonyme', 'REPORTED', 3)," +
                        "('Fuite d''eau', 'Rupture de canalisation principale, inondation de la rue', 'Ariana - Ennasr 2', 'Syndic Immeuble', 'IN_PROGRESS', 2)";
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