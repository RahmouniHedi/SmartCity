package com.smartcity.alert.service;

import com.smartcity.alert.model.Alert;
import com.smartcity.alert.model.SeverityLevel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Repository for managing Alert entities with XML persistence.
 * Uses JAXB for marshalling/unmarshalling and maintains an in-memory cache.
 *
 * @author Smart City Team
 */
public class AlertRepository {

    private final ConcurrentHashMap<String, Alert> alertCache;
    private final AtomicInteger idCounter;
    private final JAXBContext jaxbContext;
    private final File xmlStorageFile;

    private static final String STORAGE_PATH = System.getProperty("user.home") +
            "/smartcity/alerts.xml";

    public AlertRepository() throws Exception {
        this.alertCache = new ConcurrentHashMap<>();
        this.idCounter = new AtomicInteger(1);
        this.jaxbContext = JAXBContext.newInstance(Alert.class, AlertsWrapper.class);

        // Ensure storage directory exists
        this.xmlStorageFile = new File(STORAGE_PATH);
        xmlStorageFile.getParentFile().mkdirs();

        // Load existing alerts from XML if file exists
        if (xmlStorageFile.exists()) {
            loadAlertsFromXml();
        } else {
            // Initialize with sample data for demo
            initializeSampleData();
            saveAlertsToXml();
        }
    }

    /**
     * Save a new alert and persist to XML.
     */
    public Alert save(Alert alert) throws Exception {
        if (alert.getId() == null || alert.getId().isEmpty()) {
            alert.setId("ALERT-" + idCounter.getAndIncrement());
        }
        alertCache.put(alert.getId(), alert);
        saveAlertsToXml();
        return alert;
    }

    /**
     * Find alert by ID.
     */
    public Alert findById(String id) {
        return alertCache.get(id);
    }

    /**
     * Get all alerts.
     */
    public List<Alert> findAll() {
        return new ArrayList<>(alertCache.values());
    }

    /**
     * Find alerts by severity.
     */
    public List<Alert> findBySeverity(SeverityLevel severity) {
        return alertCache.values().stream()
                .filter(alert -> alert.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /**
     * Find alerts by region.
     */
    public List<Alert> findByRegion(String region) {
        return alertCache.values().stream()
                .filter(alert -> region.equalsIgnoreCase(alert.getRegion()))
                .collect(Collectors.toList());
    }

    /**
     * Delete alert by ID.
     */
    public boolean delete(String id) throws Exception {
        Alert removed = alertCache.remove(id);
        if (removed != null) {
            saveAlertsToXml();
            return true;
        }
        return false;
    }

    /**
     * Get the XML storage file path.
     */
    public File getXmlStorageFile() {
        return xmlStorageFile;
    }

    /**
     * Save all alerts to XML file using JAXB Marshaller.
     */
    private void saveAlertsToXml() throws Exception {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        AlertsWrapper wrapper = new AlertsWrapper();
        wrapper.setAlerts(new ArrayList<>(alertCache.values()));

        marshaller.marshal(wrapper, xmlStorageFile);
    }

    /**
     * Load alerts from XML file using JAXB Unmarshaller.
     */
    private void loadAlertsFromXml() throws Exception {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        AlertsWrapper wrapper = (AlertsWrapper) unmarshaller.unmarshal(xmlStorageFile);

        if (wrapper.getAlerts() != null) {
            for (Alert alert : wrapper.getAlerts()) {
                alertCache.put(alert.getId(), alert);

                // Update counter to avoid ID collisions
                try {
                    int id = Integer.parseInt(alert.getId().replace("ALERT-", ""));
                    if (id >= idCounter.get()) {
                        idCounter.set(id + 1);
                    }
                } catch (NumberFormatException e) {
                    // Ignore non-numeric IDs
                }
            }
        }
    }

    /**
     * Initialize sample data for demonstration.
     */
    private void initializeSampleData() {
        Alert alert1 = new Alert("ALERT-1", SeverityLevel.CRITICAL,
                "Major earthquake detected. Evacuate coastal areas immediately.",
                "San Francisco Bay Area", "USGS");

        Alert alert2 = new Alert("ALERT-2", SeverityLevel.SEVERE,
                "Wildfire spreading rapidly. Residents advised to prepare for evacuation.",
                "Southern California", "CAL FIRE");

        Alert alert3 = new Alert("ALERT-3", SeverityLevel.WARNING,
                "Heavy rainfall expected. Flood watch in effect.",
                "Pacific Northwest", "NOAA");

        Alert alert4 = new Alert("ALERT-4", SeverityLevel.CRITICAL,
                "Hazardous material spill on Highway 101. Avoid the area.",
                "Silicon Valley", "EPA");

        Alert alert5 = new Alert("ALERT-5", SeverityLevel.INFO,
                "Emergency drill scheduled for 2 PM. This is a test.",
                "Downtown", "City Emergency Services");

        alertCache.put(alert1.getId(), alert1);
        alertCache.put(alert2.getId(), alert2);
        alertCache.put(alert3.getId(), alert3);
        alertCache.put(alert4.getId(), alert4);
        alertCache.put(alert5.getId(), alert5);

        idCounter.set(6);
    }

    /**
     * Wrapper class for JAXB list serialization.
     */
    @javax.xml.bind.annotation.XmlRootElement(name = "alerts",
            namespace = "http://smartcity.com/alert")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    public static class AlertsWrapper {
        @javax.xml.bind.annotation.XmlElement(name = "alert",
                namespace = "http://smartcity.com/alert")
        private List<Alert> alerts;

        public List<Alert> getAlerts() {
            return alerts;
        }

        public void setAlerts(List<Alert> alerts) {
            this.alerts = alerts;
        }
    }
}