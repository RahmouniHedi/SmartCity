package com.smartcity.incident.config;

import com.smartcity.incident.util.DatabaseConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet context listener for database initialization.
 * This listener ensures the database is initialized when the application starts.
 *
 * @author Smart City Team
 */
@WebListener
public class DatabaseInitListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("=== Smart City Incident Service Starting ===");
        LOGGER.info("Initializing database connection...");

        try {
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();

            // Test connection first
            if (dbConfig.testConnection()) {
                LOGGER.info("✓ Database connection test successful");

                // Initialize schema and data
                dbConfig.initializeDatabase();
                LOGGER.info("✓ Database initialization completed successfully");
            } else {
                LOGGER.severe("✗ Database connection test failed");
                LOGGER.severe("Please ensure MySQL is running on localhost:3306");
                LOGGER.severe("Start MySQL with: net start MySQL80 (or net start MySQL)");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "✗ Failed to initialize database", e);
            LOGGER.severe("=== IMPORTANT: MySQL Connection Failed ===");
            LOGGER.severe("The application will continue to start, but database operations will fail.");
            LOGGER.severe("Please:");
            LOGGER.severe("1. Ensure MySQL server is running: net start MySQL80");
            LOGGER.severe("2. Verify MySQL is listening on port 3306");
            LOGGER.severe("3. Check that root user has no password (or update DatabaseConfig.java)");
            LOGGER.severe("4. Restart the application after MySQL is running");
        }

        LOGGER.info("=== Application Startup Complete ===");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("=== Smart City Incident Service Shutting Down ===");

        try {
            DatabaseConfig.getInstance().closeConnection();
            LOGGER.info("✓ Database connection closed");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        }

        LOGGER.info("=== Application Shutdown Complete ===");
    }
}

