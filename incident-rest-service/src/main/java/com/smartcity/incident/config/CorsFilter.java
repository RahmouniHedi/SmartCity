package com.smartcity.incident.config;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * CORS (Cross-Origin Resource Sharing) filter.
 * Allows the web client to access the REST API from a different origin.
 *
 * This is essential for the web-based control center to communicate
 * with the REST service when they're deployed on different servers/ports.
 *
 * @author Smart City Team
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        // Allow requests from any origin (for development)
        // In production, replace "*" with specific allowed origins
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");

        // Allow specific HTTP methods
        responseContext.getHeaders().add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");

        // Allow specific headers
        responseContext.getHeaders().add("Access-Control-Allow-Headers",
                "Content-Type, Authorization, X-Requested-With");

        // Allow credentials (if needed)
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");

        // Cache preflight response for 1 hour
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
    }
}