# Smart City Disaster Management System
## Deployment and Build Guide

---

## Prerequisites

### Required Software
1. **Java Development Kit (JDK) 11 or higher**
    - Download from: https://adoptium.net/
    - Verify installation: `java -version`

2. **Apache Maven 3.6+**
    - Download from: https://maven.apache.org/download.cgi
    - Verify installation: `mvn -version`

3. **MySQL Server 8.0+**
    - Download from: https://dev.mysql.com/downloads/mysql/
    - Or use Docker: `docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password mysql:8.0`

4. **Apache Tomcat 9.0+ or any Java EE Application Server**
    - Download from: https://tomcat.apache.org/download-90.cgi
    - Alternative: WildFly, GlassFish, or Jetty

---

## Database Setup

### Step 1: Create Database
```sql
CREATE DATABASE smartcity_db;
USE smartcity_db;
```

### Step 2: Configure Database Connection
Edit `incident-rest-service/src/main/java/com/smartcity/incident/util/DatabaseConfig.java`:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/smartcity_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_password_here";
```

### Step 3: Initialize Tables
The application will automatically create the `incidents` table and populate sample data on first run.

---

## Building the Project

### Build All Modules
From the root directory (`disaster-management-system/`):

```bash
mvn clean install
```

This will:
- Compile all Java source code
- Run unit tests
- Generate JAXB classes from XSD
- Package each module as a WAR file

### Build Individual Modules
```bash
# Module A: SOAP Service
cd alert-soap-service
mvn clean package

# Module B: REST Service
cd incident-rest-service
mvn clean package

# Module C: Web Client
cd control-center-client
mvn clean package
```

---

## Deployment

### Option 1: Deploy to Tomcat

#### 1. Start Tomcat
```bash
cd /path/to/tomcat
bin/startup.sh   # Linux/Mac
bin/startup.bat  # Windows
```

#### 2. Deploy WAR Files
Copy the generated WAR files to Tomcat's `webapps` directory:

```bash
# From project root
cp alert-soap-service/target/alert-soap-service.war /path/to/tomcat/webapps/
cp incident-rest-service/target/incident-rest-service.war /path/to/tomcat/webapps/
cp control-center-client/target/control-center-client.war /path/to/tomcat/webapps/
```

Tomcat will automatically deploy the applications.

#### 3. Verify Deployment
- **SOAP Service WSDL**: http://localhost:8080/alert-soap-service/AlertWebService?wsdl
- **REST API Health**: http://localhost:8080/incident-rest-service/api/incidents/health
- **Web Client**: http://localhost:8080/control-center-client/

### Option 2: Run Standalone (Development)

#### SOAP Service Standalone
```bash
cd alert-soap-service
mvn exec:java -Dexec.mainClass="com.smartcity.alert.service.AlertWebService"
```

#### REST Service with Embedded Jetty
Add to `incident-rest-service/pom.xml`:
```xml
<plugin>
    <groupId>org.eclipse.jetty</groupId>
    <artifactId>jetty-maven-plugin</artifactId>
    <version>9.4.51.v20230217</version>
</plugin>
```

Then run:
```bash
cd incident-rest-service
mvn jetty:run
```

---

## Testing the Services

### 1. Test SOAP Service

#### Using SoapUI or Postman
**Endpoint**: `http://localhost:8080/alert-soap-service`

**Sample SOAP Request (Get All Alerts)**:
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:ns="http://service.alert.smartcity.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <ns:getAllAlerts/>
   </soapenv:Body>
</soapenv:Envelope>
```

**Sample SOAP Request (Get Critical Alerts - XPath Demo)**:
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:ns="http://service.alert.smartcity.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <ns:getCriticalAlerts/>
   </soapenv:Body>
</soapenv:Envelope>
```

#### Using cURL
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

### 2. Test REST API

#### Get All Incidents
```bash
curl http://localhost:8080/incident-rest-service/api/incidents
```

#### Create New Incident (POST)
```bash
curl -X POST http://localhost:8080/incident-rest-service/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "type": "Fire",
    "description": "Smoke detected in Building A",
    "location": "123 Main Street",
    "reportedBy": "John Doe",
    "priority": 1
  }'
```

#### Update Incident Status (PUT)
```bash
curl -X PUT http://localhost:8080/incident-rest-service/api/incidents/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}'
```

#### Delete Incident
```bash
curl -X DELETE http://localhost:8080/incident-rest-service/api/incidents/1
```

### 3. Test Web Client
1. Open browser and navigate to: http://localhost:8080/control-center-client/
2. The dashboard should load and display alerts and incidents
3. Test features:
    - Click "Critical Only" to see XPath-filtered alerts
    - Click "Report New Incident" to submit a citizen report
    - Use the auto-refresh toggle

---

## Troubleshooting

### Issue: Port 8080 Already in Use
**Solution**: Change Tomcat's port in `server.xml`:
```xml
<Connector port="8081" protocol="HTTP/1.1" ... />
```

### Issue: Database Connection Failed
**Solution**:
1. Verify MySQL is running: `mysql -u root -p`
2. Check database credentials in `DatabaseConfig.java`
3. Ensure MySQL allows remote connections

### Issue: SOAP Service Not Responding
**Solution**:
1. Check WSDL is accessible: `http://localhost:8080/alert-soap-service?wsdl`
2. Review Tomcat logs: `tail -f /path/to/tomcat/logs/catalina.out`
3. Verify JAX-WS dependencies are included

### Issue: CORS Errors in Web Client
**Solution**: The `CorsFilter` is already configured. If issues persist:
1. Clear browser cache
2. Check browser console for specific errors
3. Verify REST API is running on expected port

### Issue: XPath Not Finding Critical Alerts
**Solution**:
1. Check that `alerts.xml` exists at: `~/smartcity/alerts.xml`
2. Verify XML file has namespace: `xmlns="http://smartcity.com/alert"`
3. Test XPath processor independently

---

## Project Structure Reference

```
disaster-management-system/
│
├── pom.xml                          # Parent POM
│
├── alert-soap-service/              # Module A
│   ├── src/main/java/
│   │   └── com/smartcity/alert/
│   │       ├── model/               # JAXB models (Alert, SeverityLevel)
│   │       ├── service/             # AlertWebService, AlertRepository
│   │       └── util/                # XPathProcessor
│   ├── src/main/resources/
│   │   └── xsd/Alert.xsd           # XML Schema Definition
│   └── pom.xml
│
├── incident-rest-service/           # Module B
│   ├── src/main/java/
│   │   └── com/smartcity/incident/
│   │       ├── model/               # Incident, IncidentStatus
│   │       ├── resource/            # IncidentResource (REST controller)
│   │       ├── service/             # IncidentService
│   │       ├── dao/                 # IncidentDAO
│   │       ├── config/              # RestApplication, CorsFilter
│   │       └── util/                # DatabaseConfig
│   └── pom.xml
│
└── control-center-client/           # Module C
    ├── src/main/webapp/
    │   ├── index.html               # Dashboard HTML
    │   ├── css/dashboard.css        # Styles
    │   └── js/dashboard.js          # Client logic (SOAP + REST)
    └── pom.xml
```

---

## Verification Checklist

- [ ] All three modules build successfully (`mvn clean install`)
- [ ] MySQL database is created and accessible
- [ ] SOAP service WSDL is accessible
- [ ] REST API health check returns 200 OK
- [ ] Web client loads in browser
- [ ] Dashboard displays alerts from SOAP service
- [ ] Dashboard displays incidents from REST API
- [ ] "Critical Only" button filters alerts using XPath
- [ ] New incidents can be created via web form
- [ ] New alerts can be broadcast via web form
- [ ] Incident status can be updated
- [ ] No console output in client (all visual)

---

## Performance Notes

- The system uses in-memory caching for alerts (XML file storage)
- REST API directly accesses MySQL database
- Auto-refresh is set to 30 seconds (configurable in `dashboard.js`)
- SOAP responses are synchronous (can be optimized with async)
- Consider connection pooling for production (e.g., HikariCP)

---

## Next Steps for Production

1. **Security**: Add authentication (OAuth2, JWT)
2. **Monitoring**: Integrate with ELK stack or Prometheus
3. **Scaling**: Deploy behind load balancer (Nginx, HAProxy)
4. **Database**: Use connection pooling, implement caching (Redis)
5. **SSL/TLS**: Enable HTTPS for all services
6. **Logging**: Centralize logs with Logstash or Splunk
7. **Testing**: Add integration tests with Arquillian or REST Assured

---

## Support

For issues or questions:
- Check Tomcat logs: `catalina.out`
- Review Maven build output
- Test services individually before integration
- Verify network connectivity between services