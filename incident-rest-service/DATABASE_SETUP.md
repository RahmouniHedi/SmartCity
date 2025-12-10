# Database Connection Setup Guide

## Problem: Database Connection Failed

The incident-rest-service requires MySQL to be running but MySQL is not currently installed or started on your system.

## Solution Options

### Option 1: Install and Start MySQL (Recommended)

1. **Download MySQL:**
   - Download MySQL Community Server from: https://dev.mysql.com/downloads/mysql/
   - Or download XAMPP (includes MySQL): https://www.apachefriends.org/

2. **Start MySQL Service:**
   ```powershell
   # If using MySQL Community Server
   net start MySQL80
   # or
   net start MySQL
   
   # If using XAMPP, start it from XAMPP Control Panel
   ```

3. **Verify MySQL is Running:**
   ```powershell
   Get-Service | Where-Object {$_.Name -like "*mysql*"}
   ```

4. **Test Connection:**
   ```powershell
   mysql -u root -p
   # (Press Enter when prompted for password if no password is set)
   ```

5. **Redeploy the Application:**
   After MySQL is running, deploy the WAR file to TomEE/Tomcat.
   The application will automatically create the database and tables.

### Option 2: Update Database Configuration

If your MySQL has a different configuration, update `DatabaseConfig.java`:

**Location:** `incident-rest-service/src/main/java/com/smartcity/incident/util/DatabaseConfig.java`

```java
private static final String DB_HOST = "localhost";  // Change if needed
private static final String DB_PORT = "3306";       // Change if needed
private static final String DB_USER = "root";       // Change if needed
private static final String DB_PASSWORD = "";       // ADD YOUR PASSWORD HERE
```

After changes, rebuild:
```powershell
cd C:\9raya\TP-SOA\SmartCity\incident-rest-service
mvn clean package
```

### Option 3: Use XAMPP (Easiest)

1. **Install XAMPP:**
   - Download from: https://www.apachefriends.org/
   - Install to default location

2. **Start MySQL:**
   - Open XAMPP Control Panel
   - Click "Start" button next to MySQL
   - MySQL will start on port 3306

3. **Deploy Application:**
   - Copy `incident-rest-service.war` to TomEE webapps folder
   - The application will automatically connect and create the database

### Option 4: Manual Database Setup

If you prefer to manually create the database:

1. **Start MySQL and login:**
   ```sql
   mysql -u root -p
   ```

2. **Create Database and Table:**
   ```sql
   CREATE DATABASE smartcity_db;
   USE smartcity_db;
   
   CREATE TABLE incidents (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       type VARCHAR(100) NOT NULL,
       description TEXT NOT NULL,
       location VARCHAR(255) NOT NULL,
       reported_by VARCHAR(100) NOT NULL,
       status VARCHAR(50) NOT NULL DEFAULT 'REPORTED',
       reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       priority INT DEFAULT 3,
       assigned_to VARCHAR(100),
       INDEX idx_status (status),
       INDEX idx_priority (priority),
       INDEX idx_reported_at (reported_at)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
   ```

3. **Insert Sample Data (Optional):**
   ```sql
   INSERT INTO incidents (type, description, location, reported_by, status, priority) VALUES 
   ('Fire', 'Small fire in apartment building', '123 Main St', 'John Doe', 'IN_PROGRESS', 1),
   ('Medical Emergency', 'Person collapsed in park', 'Central Park', 'Jane Smith', 'ACKNOWLEDGED', 1),
   ('Traffic Accident', 'Two-car collision at intersection', '5th Ave & Oak St', 'Officer Johnson', 'RESOLVED', 2);
   ```

## What Was Fixed

1. **Enhanced Database Connection:**
   - Added proper connection string with parameters
   - Split database creation from table creation
   - Added connection pooling support

2. **Added DatabaseInitListener:**
   - Initializes database on application startup
   - Provides detailed error messages
   - Tests connection before initialization

3. **Improved Error Handling:**
   - Better error messages in logs
   - Graceful handling of MySQL not running
   - Clear instructions in console output

4. **Updated web.xml:**
   - Configured DatabaseInitListener
   - Added proper servlet configuration

5. **Added index.html:**
   - Service information page
   - API documentation
   - Testing instructions

## Verification

After MySQL is running and the application is deployed:

1. **Check Logs:**
   Look for these messages in TomEE/Tomcat logs:
   ```
   ✓ Database connection test successful
   ✓ Database initialization completed successfully
   ```

2. **Test the API:**
   ```powershell
   # Open in browser:
   http://localhost:8080/incident-rest-service/
   
   # Or test with curl:
   curl http://localhost:8080/incident-rest-service/api/incidents
   ```

3. **Expected Response:**
   ```json
   [
     {
       "id": 1,
       "type": "Fire",
       "description": "Small fire in apartment building",
       "location": "123 Main St",
       "status": "IN_PROGRESS",
       "priority": 1
     }
   ]
   ```

## Troubleshooting

### Error: "Access denied for user 'root'@'localhost'"
- MySQL password is required
- Update DB_PASSWORD in DatabaseConfig.java
- Rebuild: `mvn clean package`

### Error: "Communications link failure"
- MySQL is not running
- Start MySQL: `net start MySQL80`

### Error: "Unknown database 'smartcity_db'"
- This should auto-create, but you can create manually (see Option 4)

### Port 3306 already in use
- Another MySQL instance is running
- Stop it: `net stop MySQL`
- Or change DB_PORT in DatabaseConfig.java

## Quick Start Checklist

- [ ] MySQL installed
- [ ] MySQL service started
- [ ] Database credentials correct in DatabaseConfig.java
- [ ] Application rebuilt: `mvn clean package`
- [ ] WAR file deployed to TomEE/Tomcat
- [ ] Service accessible at http://localhost:8080/incident-rest-service/

## Current Status

✅ incident-rest-service.war rebuilt successfully
✅ Database connection logic fixed
✅ Enhanced error handling added
❌ MySQL needs to be installed/started
⏳ Ready for deployment once MySQL is running

