package com.smartcity.alert.service;

import com.smartcity.alert.model.Alert;
import com.smartcity.alert.model.SeverityLevel;
import com.smartcity.alert.util.XPathProcessor;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Endpoint;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SOAP Web Service for Government Agency Alert Broadcasting.
 *
 * This service provides operations for:
 * - Broadcasting emergency alerts to the public
 * - Retrieving all alerts
 * - Filtering critical alerts using XPath queries
 * - Querying alerts by region
 *
 * The service demonstrates:
 * - JAX-WS SOAP annotations
 * - XML technology integration (XPath)
 * - JAXB marshalling/unmarshalling
 * - Separation of concerns (business logic vs. web service layer)
 *
 * @author Smart City Team
 * @version 1.0
 */
@WebService(
        name = "AlertService",
        serviceName = "AlertWebService",
        targetNamespace = "http://service.alert.smartcity.com/",
        portName = "AlertServicePort"
)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public class AlertWebService {

    private static final Logger LOGGER = Logger.getLogger(AlertWebService.class.getName());

    private final AlertRepository alertRepository;
    private final XPathProcessor xpathProcessor;

    /**
     * Default constructor - initializes repository and XPath processor.
     */
    public AlertWebService() {
        try {
            this.alertRepository = new AlertRepository();
            this.xpathProcessor = new XPathProcessor();
            LOGGER.info("AlertWebService initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize AlertWebService", e);
            throw new RuntimeException("Service initialization failed", e);
        }
    }

    /**
     * Broadcast a new emergency alert.
     *
     * @param alert The alert to broadcast
     * @return Success message with alert ID
     */
    @WebMethod(operationName = "broadcastAlert")
    public String broadcastAlert(
            @WebParam(name = "alert") Alert alert) {

        try {
            if (alert == null) {
                return "Error: Alert cannot be null";
            }

            // Validate alert data
            if (alert.getMessage() == null || alert.getMessage().isEmpty()) {
                return "Error: Alert message is required";
            }
            if (alert.getRegion() == null || alert.getRegion().isEmpty()) {
                return "Error: Alert region is required";
            }
            if (alert.getSeverity() == null) {
                return "Error: Alert severity is required";
            }

            Alert savedAlert = alertRepository.save(alert);

            String message = String.format(
                    "Alert broadcasted successfully. ID: %s, Severity: %s, Region: %s",
                    savedAlert.getId(),
                    savedAlert.getSeverity(),
                    savedAlert.getRegion()
            );

            LOGGER.info(message);
            return message;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting alert", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Retrieve all alerts in the system.
     *
     * @return List of all alerts
     */
    @WebMethod(operationName = "getAllAlerts")
    public List<Alert> getAllAlerts() {
        try {
            List<Alert> alerts = alertRepository.findAll();
            LOGGER.info("Retrieved " + alerts.size() + " alerts");
            return alerts;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving alerts", e);
            throw new RuntimeException("Failed to retrieve alerts", e);
        }
    }

    /**
     * CRITICAL METHOD: Get only CRITICAL severity alerts using XPath filtering.
     *
     * This method demonstrates XML technology mastery by:
     * 1. Reading the alerts XML file
     * 2. Using XPath expression: //ns:alert[ns:severity='CRITICAL']
     * 3. Filtering and returning only critical alerts
     *
     * This is a key requirement for the university project.
     *
     * @return List of critical alerts extracted via XPath
     */
    @WebMethod(operationName = "getCriticalAlerts")
    public List<Alert> getCriticalAlerts() {
        try {
            // Use XPath to filter critical alerts from XML file
            List<Alert> criticalAlerts = xpathProcessor.filterCriticalAlerts(
                    alertRepository.getXmlStorageFile()
            );

            LOGGER.info("Retrieved " + criticalAlerts.size() + " critical alerts using XPath");
            return criticalAlerts;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving critical alerts via XPath", e);
            throw new RuntimeException("XPath query failed", e);
        }
    }

    /**
     * Get alerts for a specific region.
     *
     * @param region The region to query
     * @return List of alerts for the specified region
     */
    @WebMethod(operationName = "getAlertsByRegion")
    public List<Alert> getAlertsByRegion(
            @WebParam(name = "region") String region) {

        try {
            if (region == null || region.isEmpty()) {
                throw new IllegalArgumentException("Region cannot be null or empty");
            }

            // Can use either repository or XPath - demonstrating XPath
            List<Alert> alerts = xpathProcessor.queryAlertsByRegion(
                    alertRepository.getXmlStorageFile(),
                    region
            );

            LOGGER.info("Retrieved " + alerts.size() + " alerts for region: " + region);
            return alerts;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving alerts by region", e);
            throw new RuntimeException("Failed to query alerts by region", e);
        }
    }

    /**
     * Get alerts by severity level.
     *
     * @param severity The severity level (INFO, WARNING, SEVERE, CRITICAL)
     * @return List of alerts with the specified severity
     */
    @WebMethod(operationName = "getAlertsBySeverity")
    public List<Alert> getAlertsBySeverity(
            @WebParam(name = "severity") String severity) {

        try {
            SeverityLevel severityLevel = SeverityLevel.fromValue(severity);

            List<Alert> alerts = xpathProcessor.queryAlertsBySeverity(
                    alertRepository.getXmlStorageFile(),
                    severityLevel
            );

            LOGGER.info("Retrieved " + alerts.size() + " alerts with severity: " + severity);
            return alerts;

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid severity level: " + severity, e);
            throw new RuntimeException("Invalid severity level: " + severity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving alerts by severity", e);
            throw new RuntimeException("Failed to query alerts by severity", e);
        }
    }

    /**
     * Get all severe and critical alerts (combined).
     *
     * @return List of severe and critical alerts
     */
    @WebMethod(operationName = "getHighPriorityAlerts")
    public List<Alert> getHighPriorityAlerts() {
        try {
            List<Alert> alerts = xpathProcessor.querySevereAndCriticalAlerts(
                    alertRepository.getXmlStorageFile()
            );

            LOGGER.info("Retrieved " + alerts.size() + " high priority alerts");
            return alerts;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving high priority alerts", e);
            throw new RuntimeException("Failed to query high priority alerts", e);
        }
    }

    /**
     * Get count of alerts by severity.
     *
     * @param severity The severity level to count
     * @return Number of alerts with the specified severity
     */
    @WebMethod(operationName = "countAlertsBySeverity")
    public int countAlertsBySeverity(
            @WebParam(name = "severity") String severity) {

        try {
            SeverityLevel severityLevel = SeverityLevel.fromValue(severity);

            int count = xpathProcessor.countAlertsBySeverity(
                    alertRepository.getXmlStorageFile(),
                    severityLevel
            );

            LOGGER.info("Count of " + severity + " alerts: " + count);
            return count;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting alerts", e);
            return -1;
        }
    }

    /**
     * Health check method.
     *
     * @return Service status message
     */
    @WebMethod(operationName = "ping")
    public String ping() {
        return "Alert Web Service is operational";
    }

    /**
     * Main method to publish the web service (for standalone deployment).
     */
    public static void main(String[] args) {
        String url = "http://localhost:8080/alert-service";

        LOGGER.info("Starting Alert Web Service at: " + url);
        Endpoint.publish(url, new AlertWebService());
        LOGGER.info("Alert Web Service is now running!");
        LOGGER.info("WSDL available at: " + url + "?wsdl");
    }
}