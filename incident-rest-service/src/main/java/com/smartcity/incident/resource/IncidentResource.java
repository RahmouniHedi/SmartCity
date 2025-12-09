package com.smartcity.incident.resource;

import com.smartcity.incident.model.Incident;
import com.smartcity.incident.model.IncidentStatus;
import com.smartcity.incident.service.IncidentService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RESTful Web Service for Citizen Incident Reporting.
 *
 * Provides full CRUD operations:
 * - GET /incidents - Retrieve all incidents
 * - GET /incidents/{id} - Retrieve specific incident
 * - POST /incidents - Create new incident
 * - PUT /incidents/{id} - Update existing incident
 * - DELETE /incidents/{id} - Delete incident
 *
 * Additional endpoints:
 * - GET /incidents/status/{status} - Filter by status
 * - GET /incidents/highpriority - Get high-priority incidents
 * - PUT /incidents/{id}/status - Update incident status
 * - PUT /incidents/{id}/assign - Assign incident
 *
 * @author Smart City Team
 * @version 1.0
 */
@Path("/incidents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IncidentResource {

    private static final Logger LOGGER = Logger.getLogger(IncidentResource.class.getName());
    private final IncidentService incidentService;

    public IncidentResource() {
        this.incidentService = new IncidentService();
    }

    /**
     * GET /incidents
     * Retrieve all incidents.
     *
     * @return Response containing list of all incidents
     */
    @GET
    public Response getAllIncidents() {
        try {
            List<Incident> incidents = incidentService.findAll();
            LOGGER.info("Retrieved " + incidents.size() + " incidents");

            return Response.ok(incidents).build();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving incidents", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to retrieve incidents: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /incidents/{id}
     * Retrieve a specific incident by ID.
     *
     * @param id The incident ID
     * @return Response containing the incident or 404 if not found
     */
    @GET
    @Path("/{id}")
    public Response getIncident(@PathParam("id") Long id) {
        try {
            Incident incident = incidentService.findById(id);

            if (incident == null) {
                LOGGER.warning("Incident not found with ID: " + id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Incident not found with ID: " + id))
                        .build();
            }

            LOGGER.info("Retrieved incident: " + id);
            return Response.ok(incident).build();

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid incident ID: " + id);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving incident", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to retrieve incident: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * POST /incidents
     * Create a new incident report.
     *
     * @param incident The incident data
     * @return Response containing the created incident with generated ID
     */
    @POST
    public Response createIncident(Incident incident) {
        try {
            Incident createdIncident = incidentService.createIncident(incident);

            LOGGER.info("Created incident with ID: " + createdIncident.getId());

            return Response.status(Response.Status.CREATED)
                    .entity(createdIncident)
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid incident data: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating incident", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to create incident: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * PUT /incidents/{id}
     * Update an existing incident.
     *
     * @param id The incident ID
     * @param incident The updated incident data
     * @return Response containing the updated incident
     */
    @PUT
    @Path("/{id}")
    public Response updateIncident(@PathParam("id") Long id, Incident incident) {
        try {
            // Set the ID from the path parameter
            incident.setId(id);

            Incident updatedIncident = incidentService.updateIncident(incident);

            LOGGER.info("Updated incident: " + id);

            return Response.ok(updatedIncident).build();

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid update request: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating incident", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to update incident: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * DELETE /incidents/{id}
     * Delete an incident.
     *
     * @param id The incident ID
     * @return Response indicating success or failure
     */
    @DELETE
    @Path("/{id}")
    public Response deleteIncident(@PathParam("id") Long id) {
        try {
            boolean deleted = incidentService.deleteIncident(id);

            if (!deleted) {
                LOGGER.warning("Incident not found for deletion: " + id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Incident not found with ID: " + id))
                        .build();
            }

            LOGGER.info("Deleted incident: " + id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Incident deleted successfully");
            response.put("id", id);

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid delete request: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting incident", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to delete incident: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /incidents/status/{status}
     * Get incidents filtered by status.
     *
     * @param statusValue The status value
     * @return Response containing filtered incidents
     */
    @GET
    @Path("/status/{status}")
    public Response getIncidentsByStatus(@PathParam("status") String statusValue) {
        try {
            IncidentStatus status = IncidentStatus.fromValue(statusValue);
            List<Incident> incidents = incidentService.findByStatus(status);

            LOGGER.info("Retrieved " + incidents.size() + " incidents with status: " + statusValue);

            return Response.ok(incidents).build();

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid status: " + statusValue);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse("Invalid status: " + statusValue))
                    .build();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving incidents by status", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to retrieve incidents: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /incidents/highpriority
     * Get high-priority incidents (priority <= 2).
     *
     * @return Response containing high-priority incidents
     */
    @GET
    @Path("/highpriority")
    public Response getHighPriorityIncidents() {
        try {
            List<Incident> incidents = incidentService.findHighPriority();

            LOGGER.info("Retrieved " + incidents.size() + " high-priority incidents");

            return Response.ok(incidents).build();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving high-priority incidents", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to retrieve incidents: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * PUT /incidents/{id}/status
     * Update incident status.
     *
     * @param id The incident ID
     * @param statusUpdate Map containing the new status
     * @return Response containing the updated incident
     */
    @PUT
    @Path("/{id}/status")
    public Response updateIncidentStatus(@PathParam("id") Long id,
                                         Map<String, String> statusUpdate) {
        try {
            String statusValue = statusUpdate.get("status");
            if (statusValue == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("Status field is required"))
                        .build();
            }

            IncidentStatus newStatus = IncidentStatus.fromValue(statusValue);
            Incident updatedIncident = incidentService.updateStatus(id, newStatus);

            LOGGER.info("Updated status for incident " + id + " to " + statusValue);

            return Response.ok(updatedIncident).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating incident status", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to update status: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * PUT /incidents/{id}/assign
     * Assign incident to a responder.
     *
     * @param id The incident ID
     * @param assignment Map containing assignedTo field
     * @return Response containing the updated incident
     */
    @PUT
    @Path("/{id}/assign")
    public Response assignIncident(@PathParam("id") Long id,
                                   Map<String, String> assignment) {
        try {
            String assignedTo = assignment.get("assignedTo");
            if (assignedTo == null || assignedTo.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("assignedTo field is required"))
                        .build();
            }

            Incident updatedIncident = incidentService.assignIncident(id, assignedTo);

            LOGGER.info("Assigned incident " + id + " to " + assignedTo);

            return Response.ok(updatedIncident).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning incident", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to assign incident: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /incidents/health
     * Health check endpoint.
     *
     * @return Response indicating service health
     */
    @GET
    @Path("/health")
    public Response healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Incident REST API");
        health.put("timestamp", System.currentTimeMillis());

        return Response.ok(health).build();
    }

    /**
     * Helper method to create error response.
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}