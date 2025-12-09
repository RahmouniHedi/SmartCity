/**
 * Smart City Control Center Dashboard
 *
 * This client application demonstrates:
 * 1. Consuming SOAP Web Service (Alert Service) - Module A
 * 2. Consuming REST API (Incident Service) - Module B
 * 3. Visual dashboard with NO console output
 * 4. Separation of concerns - Client never accesses database directly
 *
 * @author Smart City Team
 */

// Configuration
const CONFIG = {
    SOAP_ENDPOINT: 'http://localhost:8080/alert-service',
    REST_API_BASE: 'http://localhost:8080/incident-rest-service/api',
    AUTO_REFRESH_INTERVAL: 30000, // 30 seconds
    SOAP_NAMESPACE: 'http://service.alert.smartcity.com/'
};

// Global state
let autoRefreshTimer = null;
let allIncidents = [];
let allAlerts = [];

/**
 * Initialize dashboard on page load
 */
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
    setupAutoRefresh();
});

/**
 * Initialize the dashboard
 */
function initializeDashboard() {
    updateConnectionStatus(true);
    refreshDashboard();
}

/**
 * Update connection status indicator
 */
function updateConnectionStatus(connected) {
    const statusDot = document.getElementById('connectionStatus');
    const statusText = document.getElementById('statusText');

    if (connected) {
        statusDot.classList.remove('disconnected');
        statusText.textContent = 'Connected';
    } else {
        statusDot.classList.add('disconnected');
        statusText.textContent = 'Disconnected';
    }
}

/**
 * Refresh entire dashboard
 */
async function refreshDashboard() {
    try {
        await Promise.all([
            loadAllAlerts(),
            loadAllIncidents()
        ]);
        updateStatistics();
        showToast('Dashboard refreshed successfully', 'success');
    } catch (error) {
        showToast('Error refreshing dashboard: ' + error.message, 'error');
        updateConnectionStatus(false);
    }
}

/**
 * Setup auto-refresh functionality
 */
function setupAutoRefresh() {
    const checkbox = document.getElementById('autoRefresh');

    checkbox.addEventListener('change', function() {
        if (this.checked) {
            startAutoRefresh();
        } else {
            stopAutoRefresh();
        }
    });

    // Start auto-refresh by default
    if (checkbox.checked) {
        startAutoRefresh();
    }
}

function startAutoRefresh() {
    stopAutoRefresh(); // Clear any existing timer
    autoRefreshTimer = setInterval(refreshDashboard, CONFIG.AUTO_REFRESH_INTERVAL);
}

function stopAutoRefresh() {
    if (autoRefreshTimer) {
        clearInterval(autoRefreshTimer);
        autoRefreshTimer = null;
    }
}

/**
 * ============================================================================
 * SOAP CLIENT - Alert Service (Module A)
 * ============================================================================
 */

/**
 * Call SOAP Web Service using XMLHttpRequest
 * This demonstrates consuming the SOAP service without generated stubs
 */
async function callSOAPService(operation, soapBody) {
    const soapEnvelope = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:ns="${CONFIG.SOAP_NAMESPACE}">
    <soap:Header/>
    <soap:Body>
        <ns:${operation}>
            ${soapBody}
        </ns:${operation}>
    </soap:Body>
</soap:Envelope>`;

    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open('POST', CONFIG.SOAP_ENDPOINT, true);
        xhr.setRequestHeader('Content-Type', 'text/xml; charset=utf-8');
        xhr.setRequestHeader('SOAPAction', operation);

        xhr.onload = function() {
            if (xhr.status === 200) {
                const parser = new DOMParser();
                const xmlDoc = parser.parseFromString(xhr.responseText, 'text/xml');
                resolve(xmlDoc);
            } else {
                reject(new Error(`SOAP call failed: ${xhr.status}`));
            }
        };

        xhr.onerror = function() {
            reject(new Error('Network error calling SOAP service'));
        };

        xhr.send(soapEnvelope);
    });
}

/**
 * Load all alerts from SOAP service
 */
async function loadAllAlerts() {
    try {
        const response = await callSOAPService('getAllAlerts', '');
        allAlerts = parseAlertsFromSOAP(response);
        displayAlerts(allAlerts);
        updateConnectionStatus(true);
    } catch (error) {
        displayError('alertsContainer', 'Failed to load alerts: ' + error.message);
        updateConnectionStatus(false);
    }
}

/**
 * Load CRITICAL alerts using XPath-filtered SOAP method
 * This demonstrates the key requirement: getCriticalAlerts() with XPath
 */
async function loadCriticalAlerts() {
    try {
        const response = await callSOAPService('getCriticalAlerts', '');
        const criticalAlerts = parseAlertsFromSOAP(response);
        displayAlerts(criticalAlerts);
        showToast(`Showing ${criticalAlerts.length} critical alerts (XPath filtered)`, 'success');
    } catch (error) {
        displayError('alertsContainer', 'Failed to load critical alerts: ' + error.message);
    }
}

/**
 * Broadcast new alert via SOAP service
 */
async function broadcastAlert(event) {
    event.preventDefault();

    const alert = {
        severity: document.getElementById('alertSeverity').value,
        message: document.getElementById('alertMessage').value,
        region: document.getElementById('alertRegion').value,
        issuer: document.getElementById('alertIssuer').value
    };

    const soapBody = `
        <alert>
            <id></id>
            <severity>${escapeXml(alert.severity)}</severity>
            <message>${escapeXml(alert.message)}</message>
            <region>${escapeXml(alert.region)}</region>
            <timestamp>${new Date().toISOString()}</timestamp>
            <issuer>${escapeXml(alert.issuer)}</issuer>
        </alert>
    `;

    try {
        await callSOAPService('broadcastAlert', soapBody);
        showToast('Alert broadcasted successfully!', 'success');
        closeModal('broadcastModal');
        document.getElementById('broadcastForm').reset();
        await loadAllAlerts();
    } catch (error) {
        showToast('Failed to broadcast alert: ' + error.message, 'error');
    }
}

/**
 * Parse alerts from SOAP response XML
 */
function parseAlertsFromSOAP(xmlDoc) {
    const alerts = [];
    const alertNodes = xmlDoc.getElementsByTagName('return');

    for (let node of alertNodes) {
        const alert = {
            id: getNodeText(node, 'id'),
            severity: getNodeText(node, 'severity'),
            message: getNodeText(node, 'message'),
            region: getNodeText(node, 'region'),
            timestamp: getNodeText(node, 'timestamp'),
            issuer: getNodeText(node, 'issuer')
        };
        alerts.push(alert);
    }

    return alerts;
}

/**
 * Display alerts in the UI
 */
function displayAlerts(alerts) {
    const container = document.getElementById('alertsContainer');
    const badge = document.getElementById('alertsBadge');

    badge.textContent = alerts.length;

    if (alerts.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📭</div>
                <p>No alerts to display</p>
            </div>
        `;
        return;
    }

    container.innerHTML = alerts.map(alert => `
        <div class="alert-card ${alert.severity.toLowerCase()}">
            <div class="card-header">
                <span class="card-title">${alert.region}</span>
                <span class="severity-badge ${alert.severity.toLowerCase()}">${alert.severity}</span>
            </div>
            <div class="card-content">
                <p>${alert.message}</p>
            </div>
            <div class="card-meta">
                <span>🏛️ ${alert.issuer || 'Unknown'}</span>
                <span>🕒 ${formatTimestamp(alert.timestamp)}</span>
                <span>🆔 ${alert.id}</span>
            </div>
        </div>
    `).join('');
}

/**
 * ============================================================================
 * REST CLIENT - Incident Service (Module B)
 * ============================================================================
 */

/**
 * Make REST API call
 */
async function callRESTAPI(endpoint, method = 'GET', body = null) {
    const url = `${CONFIG.REST_API_BASE}${endpoint}`;

    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(url, options);

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'REST API call failed');
        }

        return await response.json();
    } catch (error) {
        throw new Error(`REST API error: ${error.message}`);
    }
}

/**
 * Load all incidents from REST API
 */
async function loadAllIncidents() {
    try {
        allIncidents = await callRESTAPI('/incidents');
        displayIncidents(allIncidents);
        updateConnectionStatus(true);
    } catch (error) {
        displayError('incidentsContainer', 'Failed to load incidents: ' + error.message);
        updateConnectionStatus(false);
    }
}

/**
 * Load high-priority incidents
 */
async function loadHighPriorityIncidents() {
    try {
        const incidents = await callRESTAPI('/incidents/highpriority');
        displayIncidents(incidents);
        showToast(`Showing ${incidents.length} high-priority incidents`, 'success');
    } catch (error) {
        displayError('incidentsContainer', 'Failed to load high-priority incidents: ' + error.message);
    }
}

/**
 * Filter incidents by status
 */
function filterIncidents() {
    const status = document.getElementById('statusFilter').value;

    if (!status) {
        displayIncidents(allIncidents);
        return;
    }

    const filtered = allIncidents.filter(inc => inc.status === status);
    displayIncidents(filtered);
}

/**
 * Submit new incident report via REST API
 */
async function submitIncident(event) {
    event.preventDefault();

    const incident = {
        type: document.getElementById('incidentType').value,
        location: document.getElementById('incidentLocation').value,
        description: document.getElementById('incidentDescription').value,
        reportedBy: document.getElementById('incidentReporter').value,
        priority: parseInt(document.getElementById('incidentPriority').value),
        status: 'REPORTED'
    };

    try {
        await callRESTAPI('/incidents', 'POST', incident);
        showToast('Incident reported successfully!', 'success');
        closeModal('reportModal');
        document.getElementById('reportForm').reset();
        await loadAllIncidents();
    } catch (error) {
        showToast('Failed to report incident: ' + error.message, 'error');
    }
}

/**
 * Update incident status
 */
async function updateIncidentStatus(id, newStatus) {
    try {
        await callRESTAPI(`/incidents/${id}/status`, 'PUT', { status: newStatus });
        showToast('Incident status updated', 'success');
        await loadAllIncidents();
    } catch (error) {
        showToast('Failed to update status: ' + error.message, 'error');
    }
}

/**
 * Delete incident
 */
async function deleteIncident(id) {
    if (!confirm('Are you sure you want to delete this incident?')) {
        return;
    }

    try {
        await callRESTAPI(`/incidents/${id}`, 'DELETE');
        showToast('Incident deleted', 'success');
        await loadAllIncidents();
    } catch (error) {
        showToast('Failed to delete incident: ' + error.message, 'error');
    }
}

/**
 * Display incidents in the UI
 */
function displayIncidents(incidents) {
    const container = document.getElementById('incidentsContainer');
    const badge = document.getElementById('incidentsBadge');

    badge.textContent = incidents.length;

    if (incidents.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📭</div>
                <p>No incidents to display</p>
            </div>
        `;
        return;
    }

    container.innerHTML = incidents.map(incident => `
        <div class="incident-card">
            <div class="card-header">
                <span class="card-title">${incident.type}</span>
                <span class="status-badge ${incident.status.toLowerCase()}">${formatStatus(incident.status)}</span>
            </div>
            <div class="card-content">
                <p><strong>📍 ${incident.location}</strong></p>
                <p>${incident.description}</p>
            </div>
            <div class="card-meta">
                <span>👤 ${incident.reportedBy}</span>
                <span>🕒 ${formatTimestamp(incident.reportedAt)}</span>
                <span>⚡ Priority: ${incident.priority}</span>
            </div>
            <div class="card-actions">
                ${incident.status === 'REPORTED' ?
        `<button class="btn btn-small btn-action" onclick="updateIncidentStatus(${incident.id}, 'ACKNOWLEDGED')">Acknowledge</button>` : ''}
                ${incident.status === 'ACKNOWLEDGED' ?
        `<button class="btn btn-small btn-action" onclick="updateIncidentStatus(${incident.id}, 'IN_PROGRESS')">Start Response</button>` : ''}
                ${incident.status === 'IN_PROGRESS' ?
        `<button class="btn btn-small btn-action" onclick="updateIncidentStatus(${incident.id}, 'RESOLVED')">Mark Resolved</button>` : ''}
                <button class="btn btn-small btn-action" style="background: #ef4444; color: white;" onclick="deleteIncident(${incident.id})">Delete</button>
            </div>
        </div>
    `).join('');
}

/**
 * ============================================================================
 * UI UTILITIES
 * ============================================================================
 */

/**
 * Update statistics cards
 */
function updateStatistics() {
    // Count critical alerts
    const criticalAlerts = allAlerts.filter(a => a.severity === 'CRITICAL').length;
    document.getElementById('criticalAlertsCount').textContent = criticalAlerts;

    // Total alerts
    document.getElementById('totalAlertsCount').textContent = allAlerts.length;

    // Total incidents
    document.getElementById('totalIncidentsCount').textContent = allIncidents.length;

    // Resolved incidents (today)
    const today = new Date().toDateString();
    const resolvedToday = allIncidents.filter(i =>
        i.status === 'RESOLVED' &&
        new Date(i.reportedAt).toDateString() === today
    ).length;
    document.getElementById('resolvedCount').textContent = resolvedToday;
}

/**
 * Show modal
 */
function showReportModal() {
    document.getElementById('reportModal').style.display = 'block';
}

function showBroadcastModal() {
    document.getElementById('broadcastModal').style.display = 'block';
}

/**
 * Close modal
 */
function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

/**
 * Show toast notification
 */
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type}`;
    toast.style.display = 'block';

    setTimeout(() => {
        toast.style.display = 'none';
    }, 3000);
}

/**
 * Display error in container
 */
function displayError(containerId, message) {
    const container = document.getElementById(containerId);
    container.innerHTML = `
        <div class="empty-state">
            <div class="empty-state-icon">⚠️</div>
            <p>${message}</p>
        </div>
    `;
}

/**
 * ============================================================================
 * HELPER FUNCTIONS
 * ============================================================================
 */

/**
 * Get text content from XML node
 */
function getNodeText(parentNode, tagName) {
    const nodes = parentNode.getElementsByTagName(tagName);
    return nodes.length > 0 ? nodes[0].textContent : '';
}

/**
 * Format timestamp for display
 */
function formatTimestamp(timestamp) {
    if (!timestamp) return 'N/A';

    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;

    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}h ago`;

    return date.toLocaleString();
}

/**
 * Format status for display
 */
function formatStatus(status) {
    return status.replace(/_/g, ' ');
}

/**
 * Escape XML special characters
 */
function escapeXml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&apos;");
}

// Close modals when clicking outside
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = 'none';
    }
}