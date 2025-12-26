# 🏙️ Smart City Disaster Management System

A distributed enterprise application demonstrating advanced Java technologies including SOAP Web Services, REST APIs, XML processing (XPath, JAXB, XSD), and modern web client architecture.

## 📋 Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Key Features](#key-features)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Module Details](#module-details)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Screenshots](#screenshots)
- [Project Structure](#project-structure)
- [Design Patterns](#design-patterns)
- [Future Enhancements](#future-enhancements)
- [Contributors](#contributors)
- [License](#license)

---

## 🎯 Overview

The Smart City Disaster Management System is a distributed application designed for university coursework that demonstrates:

- **Three-tier distributed architecture** with complete separation of concerns
- **SOAP Web Service** for government agency alert broadcasting with **XPath filtering**
- **RESTful API** for citizen incident reporting with full CRUD operations
- **Web-based dashboard** that consumes both services without direct database access
- **XML technology mastery**: XSD schema design, JAXB marshalling, XPath queries

### Business Use Case

The system enables:
1. **Government agencies** to broadcast emergency alerts (earthquakes, wildfires, hazmat spills)
2. **Citizens** to report incidents via a lightweight REST API
3. **Control center operators** to monitor both alert streams and incident reports in real-time

---

## 🏗️ Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                    CLIENT TIER (Module C)                       │
│         Web Dashboard - HTML5 + CSS3 + JavaScript              │
│              (NO direct database access)                        │
└────────────────────────────────────────────────────────────────┘
                         ↓ HTTP ↓ HTTP
        ┌────────────────┴──────┴────────────────┐
        ↓                                         ↓
┌─────────────────────┐              ┌─────────────────────┐
│  APPLICATION TIER   │              │  APPLICATION TIER   │
│    (Module A)       │              │    (Module B)       │
│  Alert SOAP Service │              │ Incident REST API   │
│   - JAX-WS (SOAP)   │              │   - JAX-RS (REST)   │
│   - JAXB            │              │   - JSON            │
│   - XPath Queries   │              │   - JDBC            │
└─────────────────────┘              └─────────────────────┘
        ↓                                         ↓
┌─────────────────────┐              ┌─────────────────────┐
│    DATA TIER        │              │    DATA TIER        │
│  alerts.xml (JAXB)  │              │  MySQL Database     │
│  XPath Processing   │              │  incidents table    │
└─────────────────────┘              └─────────────────────┘
```

**Key Principle**: The client never accesses the database directly. All data flows through web services.

---

## ✨ Key Features

### Module A: Alert SOAP Service
- ✅ **SOAP Web Service** with JAX-WS annotations (`@WebService`)
- ✅ **XSD Schema** definition for Alert entity with strict validation
- ✅ **JAXB Marshalling/Unmarshalling** for Java ↔ XML conversion
- ✅ **XPath Filtering**: `getCriticalAlerts()` uses XPath expression `//ns:alert[ns:severity='CRITICAL']`
- ✅ WSDL generation for service contract
- ✅ Namespace-aware XML processing

### Module B: Incident REST API
- ✅ **Full CRUD Operations** (Create, Read, Update, Delete)
- ✅ **RESTful Endpoints** following HTTP conventions
- ✅ **MySQL Database** integration with JDBC
- ✅ **JSON Serialization** using Jackson
- ✅ DAO Pattern for data access
- ✅ CORS filter for cross-origin requests

### Module C: Web Client Dashboard
- ✅ **Modern Web Interface** with HTML5, CSS3, JavaScript
- ✅ **SOAP Client** consuming Alert Service
- ✅ **REST Client** consuming Incident API
- ✅ **Real-time Updates** with auto-refresh
- ✅ **NO Console Output** - fully visual interface
- ✅ **Responsive Design** for multiple screen sizes

---

## 🛠️ Technologies Used

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Backend** | Java 11 | Core programming language |
| | JAX-WS (Metro) | SOAP web service framework |
| | JAX-RS (Jersey) | RESTful web service framework |
| | JAXB | XML binding and marshalling |
| | XPath | XML querying and filtering |
| | JDBC | Database connectivity |
| **Database** | MySQL 8.0 | Relational database |
| **Frontend** | HTML5, CSS3 | Web interface structure and styling |
| | JavaScript (ES6) | Client-side logic and API consumption |
| **Build** | Maven 3.6+ | Project management and build |
| **Server** | Apache Tomcat 9.0 | Servlet container |

---

## 📦 Prerequisites

- **Java JDK 11+**: [Download Here](https://adoptium.net/)
- **Apache Maven 3.6+**: [Download Here](https://maven.apache.org/download.cgi)
- **MySQL 8.0+**: [Download Here](https://dev.mysql.com/downloads/mysql/)
- **Apache Tomcat 9.0+**: [Download Here](https://tomcat.apache.org/download-90.cgi)

---

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/smart-city-disaster-management.git
cd smart-city-disaster-management
```

### 2. Setup MySQL Database
```sql
CREATE DATABASE smartcity_db;
```

### 3. Configure Database Connection
Edit `incident-rest-service/src/main/java/com/smartcity/incident/util/DatabaseConfig.java`:
```java
private static final String DB_PASSWORD = "your_mysql_password";
```

### 4. Build All Modules
```bash
mvn clean install
```

### 5. Deploy to Tomcat
```bash
cp alert-soap-service/target/*.war $TOMCAT_HOME/webapps/
cp incident-rest-service/target/*.war $TOMCAT_HOME/webapps/
cp control-center-client/target/*.war $TOMCAT_HOME/webapps/

$TOMCAT_HOME/bin/startup.sh
```

### 6. Access the Dashboard
Open browser: **http://localhost:8080/control-center-client/**

---

## 📘 Module Details

### Module A: Alert SOAP Service

**Location**: `alert-soap-service/`

**Key Classes**:
- `Alert.java` - JAXB-annotated entity mapped to `Alert.xsd`
- `AlertWebService.java` - SOAP service with `@WebService` annotation
- `XPathProcessor.java` - **Critical component** demonstrating XPath queries
- `AlertRepository.java` - In-memory XML storage with JAXB persistence

**WSDL Access**: http://localhost:8080/alert-soap-service?wsdl

**Key Operations**:
```java
@WebMethod
public List<Alert> getCriticalAlerts(); // Uses XPath: //ns:alert[ns:severity='CRITICAL']

@WebMethod
public String broadcastAlert(Alert alert);
```

### Module B: Incident REST API

**Location**: `incident-rest-service/`

**Key Classes**:
- `Incident.java` - Entity model with Jackson annotations
- `IncidentResource.java` - REST controller with `@Path`, `@GET`, `@POST`, etc.
- `IncidentService.java` - Business logic layer
- `IncidentDAO.java` - Data access object with JDBC

**API Base URL**: http://localhost:8080/incident-rest-service/api

**Endpoints**:
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/incidents` | Get all incidents |
| GET | `/incidents/{id}` | Get incident by ID |
| POST | `/incidents` | Create new incident |
| PUT | `/incidents/{id}` | Update incident |
| DELETE | `/incidents/{id}` | Delete incident |
| GET | `/incidents/highpriority` | Get high-priority incidents |

### Module C: Web Client

**Location**: `control-center-client/`

**Files**:
- `index.html` - Dashboard structure
- `css/dashboard.css` - Responsive styling
- `js/dashboard.js` - SOAP/REST client logic

**Features**:
- Real-time statistics dashboard
- Side-by-side alerts and incidents view
- "Critical Only" button (demonstrates XPath filtering)
- Report new incidents form
- Broadcast new alerts form
- Auto-refresh every 30 seconds

---

## 🧪 Testing

### Test SOAP Service

**Using cURL**:
```bash
curl -X POST http://localhost:8080/alert-soap-service \
  -H "Content-Type: text/xml" \
  -d '<?xml version="1.0"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <ns:getCriticalAlerts xmlns:ns="http://service.alert.smartcity.com/"/>
  </soap:Body>
</soap:Envelope>'
```

**Using SoapUI**:
1. Import WSDL: http://localhost:8080/alert-soap-service?wsdl
2. Generate sample requests
3. Test `getCriticalAlerts` operation

### Test REST API

**Get All Incidents**:
```bash
curl http://localhost:8080/incident-rest-service/api/incidents
```

**Create Incident**:
```bash
curl -X POST http://localhost:8080/incident-rest-service/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "type": "Fire",
    "description": "Smoke detected",
    "location": "123 Main St",
    "reportedBy": "John Doe",
    "priority": 1
  }'
```

**Run Unit Tests**:
```bash
mvn test
```

---

## 📖 API Documentation

### SOAP Operations

#### getCriticalAlerts()
**Description**: Returns alerts with CRITICAL severity using XPath filtering.

**XPath Expression**: `//ns:alert[ns:severity='CRITICAL']`

**Request**:
```xml
<ns:getCriticalAlerts xmlns:ns="http://service.alert.smartcity.com/"/>
```

**Response**:
```xml
<return>
  <id>ALERT-1</id>
  <severity>CRITICAL</severity>
  <message>Major earthquake detected</message>
  <region>San Francisco Bay Area</region>
  ...
</return>
```

### REST Endpoints

Full API documentation with examples available in [API_DOCS.md](API_DOCS.md)

---

## 🎨 Screenshots

### Dashboard Overview
![Dashboard](docs/screenshots/dashboard.png)
*Live control center with real-time alerts and incidents*

### Critical Alerts (XPath Filtered)
![Critical Alerts](docs/screenshots/critical-alerts.png)
*Demonstration of XPath filtering showing only CRITICAL severity alerts*

### Incident Reporting
![Report Form](docs/screenshots/report-incident.png)
*Citizen incident reporting interface*

---

## 📂 Project Structure

```
disaster-management-system/
│
├── README.md
├── pom.xml (parent POM)
│
├── alert-soap-service/
│   ├── src/main/java/com/smartcity/alert/
│   │   ├── model/          # Alert, SeverityLevel (JAXB)
│   │   ├── service/        # AlertWebService, AlertRepository
│   │   └── util/           # XPathProcessor ⭐
│   ├── src/main/resources/
│   │   └── xsd/Alert.xsd   # XML Schema ⭐
│   └── pom.xml
│
├── incident-rest-service/
│   ├── src/main/java/com/smartcity/incident/
│   │   ├── model/          # Incident, IncidentStatus
│   │   ├── resource/       # IncidentResource (REST)
│   │   ├── service/        # IncidentService
│   │   ├── dao/            # IncidentDAO
│   │   ├── config/         # RestApplication, CorsFilter
│   │   └── util/           # DatabaseConfig
│   └── pom.xml
│
└── control-center-client/
    ├── src/main/webapp/
    │   ├── index.html      # Dashboard HTML
    │   ├── css/dashboard.css
    │   └── js/dashboard.js # SOAP + REST client ⭐
    └── pom.xml

⭐ = Key components demonstrating core requirements
```

---

## 🎯 Design Patterns

This project implements several enterprise design patterns:

1. **Repository Pattern**: `AlertRepository` abstracts data storage
2. **Service Layer Pattern**: `IncidentService` encapsulates business logic
3. **DAO Pattern**: `IncidentDAO` isolates database access
4. **MVC Pattern**: Clear separation of Model, View (web), Controller (REST/SOAP)
5. **Factory Pattern**: JAXB context creation
6. **Singleton Pattern**: `DatabaseConfig` instance

---

## 🔮 Future Enhancements

- [ ] Add OAuth2 authentication
- [ ] Implement WebSocket for real-time push notifications
- [ ] Add geographical mapping (Google Maps integration)
- [ ] Deploy as microservices with Docker
- [ ] Implement message queue (RabbitMQ/Kafka) for async processing
- [ ] Add machine learning for incident prediction
- [ ] Create mobile app (Android/iOS)

---

## 👥 Contributors

- **Your Name** - Initial work - [YourGitHub](https://github.com/yourusername)

---

## 📄 License

This project is created for educational purposes as part of a university coursework. Feel free to use it as a reference for your own projects.

---

## 🙏 Acknowledgments

- University professor for project guidance
- Java EE community for excellent documentation
- Stack Overflow for troubleshooting assistance

---

## 📞 Support

For questions or issues:
- Open an issue on GitHub
- Email: your.email@university.edu
- Office hours: Monday/Wednesday 2-4 PM

---

## 📚 Additional Documentation

- [Deployment Guide](DEPLOYMENT.md)
- [Architecture Justification](ARCHITECTURE.md)
- [API Reference](API_DOCS.md)
- [Testing Guide](TESTING.md)

---

**Built with ❤️ using Java Enterprise Technologies**
