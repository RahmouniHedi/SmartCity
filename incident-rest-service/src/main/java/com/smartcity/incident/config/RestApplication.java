package com.smartcity.incident.config;

import com.smartcity.incident.resource.IncidentResource;
import com.smartcity.incident.util.DatabaseConfig;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * JAX-RS Application configuration.
 * Registers REST resources and configures the application path.
 *
 * The @ApplicationPath annotation defines the base URI for all REST endpoints.
 * With @ApplicationPath("/api"), all endpoints will be accessible under /api/*
 *
 * Example: http://localhost:8080/incident-rest-service/api/incidents
 *
 * @author Smart City Team
 */
@ApplicationPath("/api")
public class RestApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(RestApplication.class.getName());

    public RestApplication() {
        super();

        // Initialize database on application startup
        try {
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            dbConfig.initializeDatabase();
            LOGGER.info("Database initialized successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize database: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }

        LOGGER.info("REST Application initialized");
    }

    /**
     * Register REST resource classes.
     */
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Register resource classes
        classes.add(IncidentResource.class);

        // Add CORS filter if needed
        classes.add(CorsFilter.class);

        LOGGER.info("Registered " + classes.size() + " resource classes");
        return classes;
    }
}