package com.smartcity.alert.util;

import com.smartcity.alert.model.Alert;
import com.smartcity.alert.model.SeverityLevel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced XPath processor for filtering and querying Alert XML documents.
 * This class demonstrates mastery of XML technologies including DOM parsing,
 * XPath querying, and namespace handling.
 *
 * Key Features:
 * - Filter alerts by severity using XPath expressions
 * - Query alerts by region and time range
 * - Support for complex XPath predicates
 * - Namespace-aware XML processing
 *
 * @author Smart City Team
 * @version 1.0
 */
public class XPathProcessor {

    private final DocumentBuilder documentBuilder;
    private final XPath xpath;
    private static final String NAMESPACE_URI = "http://smartcity.com/alert";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public XPathProcessor() throws Exception {
        // Initialize DOM parser with namespace awareness
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        dbFactory.setValidating(false);
        this.documentBuilder = dbFactory.newDocumentBuilder();

        // Initialize XPath with namespace context
        XPathFactory xpathFactory = XPathFactory.newInstance();
        this.xpath = xpathFactory.newXPath();

        // Set namespace context for XPath queries
        xpath.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if ("ns".equals(prefix)) {
                    return NAMESPACE_URI;
                }
                return javax.xml.XMLConstants.NULL_NS_URI;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                if (NAMESPACE_URI.equals(namespaceURI)) {
                    return "ns";
                }
                return null;
            }

            @Override
            public java.util.Iterator<String> getPrefixes(String namespaceURI) {
                return null;
            }
        });
    }

    /**
     * CRITICAL METHOD: Filters and returns only CRITICAL severity alerts from XML file.
     * This demonstrates XPath mastery by using predicates to filter nodes.
     *
     * XPath Expression: //ns:alert[ns:severity='CRITICAL']
     *
     * @param xmlFile The XML file containing alerts
     * @return List of critical alerts
     * @throws Exception if XML parsing or XPath evaluation fails
     */
    public List<Alert> filterCriticalAlerts(File xmlFile) throws Exception {
        Document document = documentBuilder.parse(xmlFile);
        document.getDocumentElement().normalize();

        // XPath expression to filter only CRITICAL alerts
        String expression = "//ns:alert[ns:severity='CRITICAL']";

        XPathExpression xpathExpr = xpath.compile(expression);
        NodeList nodeList = (NodeList) xpathExpr.evaluate(document, XPathConstants.NODESET);

        return parseAlertNodes(nodeList);
    }

    /**
     * Query alerts by severity level using XPath.
     *
     * @param xmlFile The XML file containing alerts
     * @param severity The severity level to filter by
     * @return List of alerts matching the severity
     * @throws Exception if XML parsing or XPath evaluation fails
     */
    public List<Alert> queryAlertsBySeverity(File xmlFile, SeverityLevel severity) throws Exception {
        Document document = documentBuilder.parse(xmlFile);
        document.getDocumentElement().normalize();

        String expression = String.format("//ns:alert[ns:severity='%s']", severity.getValue());

        XPathExpression xpathExpr = xpath.compile(expression);
        NodeList nodeList = (NodeList) xpathExpr.evaluate(document, XPathConstants.NODESET);

        return parseAlertNodes(nodeList);
    }

    /**
     * Query alerts by region using XPath.
     *
     * @param xmlFile The XML file containing alerts
     * @param region The region to filter by
     * @return List of alerts for the specified region
     * @throws Exception if XML parsing or XPath evaluation fails
     */
    public List<Alert> queryAlertsByRegion(File xmlFile, String region) throws Exception {
        Document document = documentBuilder.parse(xmlFile);
        document.getDocumentElement().normalize();

        String expression = String.format("//ns:alert[ns:region='%s']", region);

        XPathExpression xpathExpr = xpath.compile(expression);
        NodeList nodeList = (NodeList) xpathExpr.evaluate(document, XPathConstants.NODESET);

        return parseAlertNodes(nodeList);
    }

    /**
     * Advanced query: Get alerts with severity >= SEVERE (SEVERE or CRITICAL)
     *
     * @param xmlFile The XML file containing alerts
     * @return List of severe or critical alerts
     * @throws Exception if XML parsing or XPath evaluation fails
     */
    public List<Alert> querySevereAndCriticalAlerts(File xmlFile) throws Exception {
        Document document = documentBuilder.parse(xmlFile);
        document.getDocumentElement().normalize();

        String expression = "//ns:alert[ns:severity='SEVERE' or ns:severity='CRITICAL']";

        XPathExpression xpathExpr = xpath.compile(expression);
        NodeList nodeList = (NodeList) xpathExpr.evaluate(document, XPathConstants.NODESET);

        return parseAlertNodes(nodeList);
    }

    /**
     * Count alerts by severity using XPath count() function.
     *
     * @param xmlFile The XML file containing alerts
     * @param severity The severity level to count
     * @return Number of alerts with specified severity
     * @throws Exception if XML parsing or XPath evaluation fails
     */
    public int countAlertsBySeverity(File xmlFile, SeverityLevel severity) throws Exception {
        Document document = documentBuilder.parse(xmlFile);
        document.getDocumentElement().normalize();

        String expression = String.format("count(//ns:alert[ns:severity='%s'])", severity.getValue());

        XPathExpression xpathExpr = xpath.compile(expression);
        Double count = (Double) xpathExpr.evaluate(document, XPathConstants.NUMBER);

        return count.intValue();
    }

    /**
     * Get the most recent alert using XPath sorting (advanced).
     * Note: XPath 1.0 doesn't have built-in sorting, so we parse all and sort in Java.
     *
     * @param xmlFile The XML file containing alerts
     * @return The most recent alert, or null if no alerts exist
     * @throws Exception if XML parsing fails
     */
    public Alert getMostRecentAlert(File xmlFile) throws Exception {
        Document document = documentBuilder.parse(xmlFile);
        document.getDocumentElement().normalize();

        String expression = "//ns:alert";

        XPathExpression xpathExpr = xpath.compile(expression);
        NodeList nodeList = (NodeList) xpathExpr.evaluate(document, XPathConstants.NODESET);

        List<Alert> alerts = parseAlertNodes(nodeList);

        return alerts.stream()
                .max((a1, a2) -> a1.getTimestamp().compareTo(a2.getTimestamp()))
                .orElse(null);
    }

    /**
     * Parse NodeList of alert elements into Alert objects.
     *
     * @param nodeList The NodeList containing alert elements
     * @return List of parsed Alert objects
     */
    private List<Alert> parseAlertNodes(NodeList nodeList) {
        List<Alert> alerts = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                Alert alert = parseAlertElement(element);
                alerts.add(alert);
            }
        }

        return alerts;
    }

    /**
     * Parse a single alert element into an Alert object.
     *
     * @param element The alert XML element
     * @return Parsed Alert object
     */
    private Alert parseAlertElement(Element element) {
        Alert alert = new Alert();

        alert.setId(getElementTextContent(element, "id"));
        alert.setSeverity(SeverityLevel.fromValue(getElementTextContent(element, "severity")));
        alert.setMessage(getElementTextContent(element, "message"));
        alert.setRegion(getElementTextContent(element, "region"));

        String timestampStr = getElementTextContent(element, "timestamp");
        if (timestampStr != null && !timestampStr.isEmpty()) {
            alert.setTimestamp(LocalDateTime.parse(timestampStr, FORMATTER));
        }

        String issuer = getElementTextContent(element, "issuer");
        if (issuer != null && !issuer.isEmpty()) {
            alert.setIssuer(issuer);
        }

        return alert;
    }

    /**
     * Helper method to get text content of a child element.
     *
     * @param parent The parent element
     * @param tagName The tag name to search for
     * @return The text content, or null if not found
     */
    private String getElementTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    /**
     * Validate if an XML file contains valid alert structure.
     *
     * @param xmlFile The XML file to validate
     * @return true if valid, false otherwise
     */
    public boolean validateAlertStructure(File xmlFile) {
        try {
            Document document = documentBuilder.parse(xmlFile);
            document.getDocumentElement().normalize();

            String expression = "//ns:alert";
            XPathExpression xpathExpr = xpath.compile(expression);
            NodeList nodeList = (NodeList) xpathExpr.evaluate(document, XPathConstants.NODESET);

            return nodeList.getLength() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}