# AI Digital Signage - Microservices

## 📋 Overview

This project consists of microservices for real-time audience analytics and digital signage management.

## 🏗️ Architecture

```
TDengine (Time-Series DB)
    ↓ Extract
analytics-etl-service (ETL)
    ↓ Transform & Load
SQLite (Analytics DB)
    ↓ Read
digital-signage-service (Backend API)
    ↓ REST API
digital-signage-dashboard (Frontend)
```

## 🚀 Services & Access Points

| Service | Type | Port | URL | Description |
|---------|------|------|-----|-------------|
| **TDengine Web UI** | Database | 6060 | http://localhost:6060 | TDengine web console (Login: root/taosdata) |
| **TDengine REST API** | Database | 6041 | http://localhost:6041/rest/sql | Time-series database REST API |
| **SQLite Browser** | Tool | 3000 | http://localhost:3000 | Database viewer for SQLite |
| **analytics-etl-service** | ETL | - | - | Scheduled ETL (runs every 5 min, incremental) |
| **digital-signage-service** | Backend | 8080 | http://localhost:8080 | Spring Boot REST API |
| **Swagger UI** | API Docs | 8080 | http://localhost:8080/swagger-ui.html | Interactive API documentation |
| **digital-signage-dashboard** | Frontend | 5174 | http://localhost:5174 | Vue.js Dashboard |

## 📊 API Endpoints

### digital-signage-service (Backend)

**🔷 Swagger UI (Interactive API Documentation):**
- **URL:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Description:** Interactive API explorer with request/response examples

**REST API Endpoints:**
- `GET /api/dashboard/overview` - Get dashboard analytics summary
- `GET /api/dashboard/age-distribution` - Get age distribution data
- `GET /api/dashboard/gender-distribution` - Get gender distribution data
- `GET /api/dashboard/emotion-distribution` - Get emotion distribution data
- `GET /api/dashboard/advertisements` - Get advertisement performance

**Example:**
```bash
curl http://localhost:8080/api/dashboard/overview
```

### TDengine Access

**Web UI (Browser Access):**
- **URL:** http://localhost:6060
- **Username:** root
- **Password:** taosdata
- **Description:** TDengine web-based management console

**REST API (Programmatic Access):**
- **Base URL:** http://localhost:6041/rest/sql
- **Authentication:** Basic (root:taosdata)
- **Database:** digital_signage

**Example:**
```powershell
$headers = @{'Authorization' = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('root:taosdata'))}
Invoke-RestMethod -Uri 'http://localhost:6041/rest/sql/digital_signage' -Method Post -Headers $headers -Body 'SELECT COUNT(*) FROM gaze_events'
```

## 🔄 Startup Flow (Correct Order)

### Prerequisites
- Java 21 (SapMachine JDK 21)
- Maven 3.9+
- Node.js 18+ and npm
- Docker (for TDengine and SQLite Browser)

### Step 1: Start Infrastructure Services

```powershell
# Start TDengine container
docker start tdengine-tsdb

# Verify TDengine is running
docker ps --filter "name=tdengine-tsdb"

# Start SQLite Browser (optional)
docker start sqlite-browser
```

### Step 2: Initialize TDengine with Data

**Load mock data into TDengine:**

```powershell
# Option 1: Via REST API
$headers = @{'Authorization' = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('root:taosdata'))}
Invoke-RestMethod -Uri 'http://localhost:6041/rest/sql/digital_signage' -Method Post -Headers $headers -Body 'SELECT COUNT(*) FROM gaze_events'

# Option 2: Execute SQL file in container
cd microservices/analytics-etl-service
docker cp load_data.sql tdengine-tsdb:/tmp/load.sql
docker exec tdengine-tsdb taos -s "USE digital_signage" -f /tmp/load.sql
```

**Verify data loaded:**
```powershell
$headers = @{'Authorization' = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('root:taosdata'))}
Invoke-RestMethod -Uri 'http://localhost:6041/rest/sql/digital_signage' -Method Post -Headers $headers -Body 'SELECT COUNT(*) FROM gaze_events'
# Expected: 57 records
```

### Step 3: Start Backend Service (API)

```powershell
cd microservices/digital-signage-service

# Set Java 21
$env:JAVA_HOME = 'C:\Program Files\SapMachine\JDK\21'

# Start backend (this will create empty SQLite schema)
mvn spring-boot:run
```

**Verify backend started:**
```powershell
# Check port 8080
netstat -ano | findstr ":8080.*LISTENING"

# Test API (will return empty data initially)
Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/overview" -Method Get
```

### Step 4: Run ETL Service (Continuous Scheduled)

```powershell
cd microservices/analytics-etl-service

# Set Java 21
$env:JAVA_HOME = 'C:\Program Files\SapMachine\JDK\21'

# Run ETL process (Runs continuously, scheduled every 5 minutes)
mvn spring-boot:run
```

**🚨 NEW: Incremental ETL Behavior**
- **First Run**: Extracts ALL events from TDengine
- **Subsequent Runs**: Only fetches NEW events (timestamp-based)
- **Scheduling**: Auto-runs every 5 minutes
- **Stays Running**: Service runs continuously, no longer exits
- **Metadata Tracking**: Stores last processed timestamp in SQLite

**ETL Process will:**
1. ✅ Check last processed timestamp (incremental mode)
2. ✅ Extract NEW gaze events from TDengine (only after last timestamp)
3. ✅ Transform into analytics (dashboard + advertisement data)
4. ✅ Clear old SQLite analytics tables
5. ✅ Load new analytics into SQLite
6. ✅ Update last processed timestamp
7. ✅ Wait 5 minutes, then repeat

**Expected Output:**
```
╔════════════════════════════════════════════════════════╗
║     Analytics ETL Service - Starting Pipeline         ║
╚════════════════════════════════════════════════════════╝
[EXTRACT] Found 57 session end events
[TRANSFORM] Created dashboard analytics and 12 ad analytics
[LOAD] Dashboard analytics saved successfully
[LOAD] Advertisement analytics saved successfully
╔════════════════════════════════════════════════════════╗
║     Analytics ETL Pipeline - Completed Successfully   ║
╚════════════════════════════════════════════════════════╝
```

### Step 5: Start Frontend Dashboard

```powershell
cd microservices/digital-signage-dashboard

# Install dependencies (first time only)
npm install

# Start Vite dev server
npm run dev
```

**Access Dashboard:** http://localhost:5174

### Step 6: Verify Complete Data Flow

```powershell
# 1. Check TDengine has source data
$headers = @{'Authorization' = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('root:taosdata'))}
Invoke-RestMethod -Uri 'http://localhost:6041/rest/sql/digital_signage' -Method Post -Headers $headers -Body 'SELECT COUNT(*) FROM gaze_events'

# 2. Check backend API returns analytics
Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/overview" -Method Get

# 3. Open dashboard in browser
Start-Process "http://localhost:5174"
```

## 🔍 Data Flow Verification

### 1. TDengine (Source)
- **Table:** `gaze_events` (Super Table)
- **Tag:** `evt_type` = 'session_end'
- **Columns:** ts, viewer_id, gaze_time, session_duration, interested, attention_rate, age, gender, emotion, ad_name

**Query:**
```sql
SELECT COUNT(*) FROM gaze_events WHERE evt_type = 'session_end';
-- Expected: 57 records
```

### 2. SQLite (Target)
- **Database:** `./digital-signage-service/data/digital-signage.db`
- **Tables:**
  - `metrics_kpi` - Overall dashboard KPIs
  - `age_distribution` - Age group breakdown
  - `gender_distribution` - Gender breakdown
  - `emotion_distribution` - Emotion breakdown
  - `advertisement` - Ad performance metrics

**Access via SQLite Browser:** http://localhost:3000

### 3. Backend API Response

```json
{
  "totalAudience": 57,
  "totalViews": 57,
  "totalAds": 12,
  "avgViewSeconds": 17.57,
  "ageDistribution": {
    "children": 3,
    "teenagers": 3,
    "youngAdults": 21,
    "midAged": 26,
    "seniors": 4
  },
  "genderDistribution": {
    "male": 30,
    "female": 27
  },
  "emotionDistribution": {
    "neutral": 34,
    "serious": 7,
    "happy": 13,
    "surprised": 3
  }
}
```

### 4. Dashboard UI
- KPI Cards: Total Audience, Total Views, Total Ads, Avg View Time
- Age Distribution Bar Chart
- Gender Distribution Pie Chart
- Emotion Distribution Bar Chart
- Advertisement Performance Table

## 🛠️ Development

### Project Structure

```
microservices/
├── sat-common/              # Shared library for DDD/Hexagonal architecture
├── sat-template/            # Microservices template
├── digital-signage-service/ # Backend API (Spring Boot)
│   ├── src/
│   ├── data/
│   │   └── digital-signage.db   # SQLite database
│   └── pom.xml
├── analytics-etl-service/   # ETL Service (Spring Boot)
│   ├── src/
│   ├── tdengine_init.sql    # TDengine schema
│   ├── load_data.sql        # Mock data (57 records)
│   └── pom.xml
└── digital-signage-dashboard/ # Frontend (Vue.js 3 + Vite)
    ├── src/
    ├── package.json
    └── vite.config.js
```

### Tech Stack

**Backend:**
- Spring Boot 3.5.10
- Java 21
- SQLite 3.47
- Flyway Migration
- Hexagonal Architecture (DDD)

**ETL:**
- Spring Boot 3.5.10
- TDengine JDBC Driver
- HikariCP Connection Pool

**Frontend:**
- Vue.js 3.5.13
- Vite 6.4.1
- Chart.js 4.4.7
- Axios 1.7.9

**Databases:**
- TDengine 3.4.0.2 (Time-series, source)
- SQLite 3.47 (Analytics, target)

## 🔧 Troubleshooting

### TDengine Connection Issues

**Problem:** Cannot connect to TDengine
```
Solution:
1. Check container: docker ps --filter "name=tdengine-tsdb"
2. Check logs: docker logs tdengine-tsdb
3. Restart: docker restart tdengine-tsdb
4. Verify ports: netstat -ano | findstr ":6060 :6041"
5. Web UI: http://localhost:6060 (login: root/taosdata)
6. REST API: http://localhost:6041/rest/sql
```

### ETL Service Fails

**Problem:** SQL syntax error or data source issues
```
Solution:
1. Verify TDengine table exists: SHOW STABLES;
2. Check data: SELECT COUNT(*) FROM gaze_events;
3. Verify column names match (evt_type, not event_type)
4. Check @Qualifier annotation in TDengineGazeEventRepository
```

### Backend Returns Empty Data

**Problem:** API returns zeros or empty arrays
```
Solution:
1. Run ETL service first: cd analytics-etl-service; mvn spring-boot:run
2. Check SQLite database: open in SQLite Browser (port 3000)
3. Verify tables populated: SELECT COUNT(*) FROM metrics_kpi;
4. Restart backend if needed
```

### Dashboard Shows No Data

**Problem:** Charts empty or showing zeros
```
Solution:
1. Check backend API: curl http://localhost:8080/api/dashboard/overview
2. Check browser console: F12 → Console tab
3. Verify API proxy in vite.config.js: /api → http://localhost:8080
4. Check CORS configuration in backend
```

## 📝 Notes

- **ETL Service:** Runs once on startup, then exits. Re-run to refresh analytics.
- **Data Refresh:** To refresh analytics, stop backend → run ETL → restart backend.
- **TDengine Schema:** Uses `evt_type` tag (not `event_type` - reserved keyword).
- **SQLite Lock:** Only one writer at a time. Stop backend before running ETL.

## 🔗 Useful Commands

```powershell
# Check all service ports
netstat -ano | findstr ":8080 :5174 :3000 :6041 :6060"

# Stop all Java processes
Get-Process java | Stop-Process -Force

# Clean build all services
cd microservices/digital-signage-service; mvn clean package -DskipTests
cd microservices/analytics-etl-service; mvn clean package -DskipTests

# View TDengine data
docker exec -it tdengine-tsdb taos -s "USE digital_signage; SELECT COUNT(*) FROM gaze_events;"

# Check SQLite database size
Get-Item "microservices/digital-signage-service/data/digital-signage.db" | Select-Object Length, LastWriteTime
```

## 📚 Additional Resources

- [TDengine Documentation](https://docs.tdengine.com/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Vue.js Documentation](https://vuejs.org/)
- [Chart.js Documentation](https://www.chartjs.org/)

---

**Last Updated:** February 8, 2026
