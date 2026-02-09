# Analytics ETL Service

Extract-Transform-Load (ETL) pipeline that processes gaze events from TDengine and sends analytics to digital-signage-service via REST API.

## Overview

This service bridges the audience analysis pipeline and the dashboard by:

1. **EXTRACT**: Reading GAZE_EVENT records from TDengine time-series database
2. **TRANSFORM**: Aggregating raw events into higher-level analytics
3. **LOAD**: Sending transformed data to digital-signage-service REST API

**Purpose**: Academic demonstration of end-to-end analytics pipeline  
**Architecture**: Hexagonal (Domain → Application → Infrastructure) + Microservice  
**Execution Model**: Batch ETL (run manually or scheduled)  
**Database Ownership**: Each service owns its database (microservice best practice)

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    ANALYTICS ETL PIPELINE                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────┐      ┌─────────────┐      ┌────────────┐ │
│  │   EXTRACT   │  →   │  TRANSFORM  │  →   │    LOAD    │ │
│  └─────────────┘      └─────────────┘      └────────────┘ │
│        ↓                     ↓                     ↓       │
│   TDengine            Aggregation            REST API      │
│  (Gaze Events)        (Analytics)        (POST to Service) │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                              ↓
            digital-signage-service updates SQLite
```

### Hexagonal Architecture Layers

```
Domain Layer (Pure Business Logic)
  ├── GazeEvent.java              - Raw event entity
  ├── DashboardAnalytics.java     - Aggregated dashboard data
  ├── AdAnalytics.java            - Per-ad analytics
  ├── GazeEventRepository.java    - Extract port
  └── AnalyticsRepository.java    - Load port

Application Layer (Use Cases)
  └── AnalyticsEtlService.java    - ETL orchestration

Infrastructure Layer (Adapters)
  ├── TDengineGazeEventRepository.java    - Extract adapter (TDengine)
  ├── RestClientAnalyticsRepository.java  - Load adapter (REST API)
  ├── FileEtlMetadataRepository.java      - Metadata storage (file-based)
  ├── RestClientConfig.java               - REST client configuration
  └── DatabaseConfig.java                 - TDengine data source config
```

## Prerequisites

- **Java 21+** (LTS)
- **Maven 3.6+**
- **Docker Desktop** (for TDengine)
- **TDengine** running on localhost:6041
- **digital-signage-service** running on localhost:8080

## Quick Start

### Step 1: Start TDengine

```bash
cd ../../docker/tdengine
docker-compose up -d

# Verify TDengine is running
docker ps --filter "name=tdengine-tsdb" --format "{{.Status}}"
```

TDengine endpoints:
- **REST API**: `http://localhost:6041` (used by ETL)
- **Native Protocol**: `localhost:6030`
- **Default credentials**: root/taosdata

### 2. Load Mock Data into TDengine

**Option 1: Primary Dataset (105 records with diverse demographics)**
```bash
# Load primary dataset with children, teenagers, various emotions
docker cp tdengine_init.sql tdengine-tsdb:/tmp/init.sql
docker exec tdengine-tsdb taos -f /tmp/init.sql
```

**Option 2: Extended Dataset (300 records for comprehensive testing)**
```bash
# Load extended mock data
docker exec -i tdengine-tsdb taos < src/main/resources/db/tdengine/tdengine_mock_data.sql
```

Both scripts include:
- `digital_signage` database
- `gaze_events` super table (with evt_type TAG)
- Diverse demographics (children, teenagers, adults, seniors)
- Multiple emotions (happy, neutral, serious, surprised)
- "Did not look" records (attention_rate = 0)
- Sample session_end events with demographics and engagement data

### 3. Verify SQLite Database

Ensure the SQLite database exists (created by digital-signage-service):

```bash
ls -lh ../digital-signage-service/data/digital-signage.db
```

## Running the Service

### Option 1: Maven

```bash
# Build the project
mvn clean package

# Run the ETL process
mvn spring-boot:run
```

### Option 2: JAR

```bash
# Build
mvn clean package

# Run
java -jar target/analytics-etl-service-1.0.0-SNAPSHOT.jar
```

The service will:
1. Connect to TDengine and SQLite
2. Extract all `session_end` events
3. Transform into dashboard and ad analytics
4. Clear old data and insert new analytics
5. Exit with summary

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
# TDengine Configuration (Source)
tdengine:
  url: jdbc:TAOS-RS://localhost:6041
  username: root
  password: taosdata
  database: digital_signage

# SQLite Configuration (Target)
# MUST point to the same database used by digital-signage-service
sqlite:
  url: jdbc:sqlite:../digital-signage-service/data/digital-signage.db
```

## ETL Process Details

### Extract

**Source**: TDengine `gaze_events` table

Queries:
```sql
-- Get all completed sessions
SELECT * FROM gaze_events 
WHERE event_type = 'session_end'
```

Each event contains:
- `viewer_id`: Unique identifier
- `session_duration`: Time spent viewing
- `total_gaze_time`: Time spent looking at screen
- `engagement_rate`: Ratio of gaze time to session time
- `age`, `gender`, `emotion`: Demographics
- `ad_name`: Advertisement being shown

### Transform

**Aggregations**:

1. **Dashboard Analytics**
   - Total audience: `COUNT(DISTINCT viewer_id)`
   - Total views: `COUNT(*)`
   - Avg view seconds: `AVG(session_duration)`
   - Age distribution: Group by age ranges
   - Gender distribution: Group by gender
   - Emotion distribution: Group by emotion

2. **Advertisement Analytics**
   - Total viewers per ad: `COUNT(*) GROUP BY ad_name`
   - Engaged viewers: `COUNT(*) WHERE engagement_rate >= 0.5`
   - Non-engaged: `COUNT(*) WHERE engagement_rate < 0.5`

### Load

**Target**: digital-signage-service REST API endpoint: `POST /api/analytics/update`

**Request Payload**:
```json
{
  "dashboardMetrics": {
    "totalAudience": 1247,
    "totalViews": 3856,
    "avgViewSeconds": 24.5,
    "children": 150,
    "teenagers": 225,
    "youngAdults": 437,
    ...
  },
  "adMetrics": [
    {
      "adName": "Summer Sale 2026",
      "totalViewers": 485,
      "lookYes": 388,
      "lookNo": 97
    },
    ...
  ]
}
```

**Result**: digital-signage-service receives the data and updates its SQLite database tables:
- `metrics_kpi` - Overall dashboard metrics
- `age_distribution` - Age demographics
- `gender_distribution` - Gender demographics
- `emotion_distribution` - Emotion analysis
- `advertisement` - Per-ad performance

**Strategy**: Truncate and reload (full refresh)

## Mock Data

The TDengine mock data script creates realistic gaze events matching the pattern from `audience-analysis-service`:

### Sample Event Types

```json
{
  "event": "session_end",
  "viewer_id": "2941ca1b",
  "session_stats": {
    "total_gaze_time": 3.1,
    "session_duration": 3.1,
    "gaze_count": 1,
    "engagement_rate": 1.0
  },
  "demographics": {
    "age": 33,
    "gender": "Female",
    "emotion": "neutral"
  }
}
```

### Mock Data Statistics

Matches the expected analytics from digital-signage-service:

- **Total Audience**: ~60 unique viewers (sample)
- **Total Views**: ~60 sessions
- **Avg View Time**: ~15-20 seconds
- **Age Distribution**: Children 12%, Teenagers 18%, Young Adults 35%, Mid-Aged 25%, Seniors 10%
- **Gender**: Male 52%, Female 48%
- **Emotions**: Neutral 45%, Serious 25%, Happy 22%, Surprised 8%
- **Advertisements**: 12 different ads with varied engagement

## Verification

### Check TDengine Data

```bash
# Connect to TDengine
docker exec -it tdengine-tsdb taos

# Verify data
USE digital_signage;

SELECT COUNT(*) as total_sessions 
FROM gaze_events 
WHERE event_type='session_end';

SELECT ad_name, COUNT(*) as viewers 
FROM gaze_events 
WHERE event_type='session_end' 
GROUP BY ad_name 
ORDER BY viewers DESC;
```

### Check SQLite Data

```bash
# Use SQLite CLI
sqlite3 ../digital-signage-service/data/digital-signage.db

# Or use the SQLite browser
# http://localhost:3000

SELECT * FROM metrics_kpi;
SELECT * FROM advertisement ORDER BY total_viewers DESC;
```

### Test Dashboard API

After running ETL, the digital-signage-service should return the transformed data:

```bash
curl http://localhost:8080/api/dashboard/overview
```

## Scheduling ETL

For production, schedule the ETL with:

### Cron (Linux/Mac)

```bash
# Run ETL every 5 minutes
*/5 * * * * cd /path/to/analytics-etl-service && java -jar target/analytics-etl-service-1.0.0-SNAPSHOT.jar
```

### Windows Task Scheduler

```powershell
# Create scheduled task
$action = New-ScheduledTaskAction -Execute "java.exe" -Argument "-jar C:\path\to\analytics-etl-service-1.0.0-SNAPSHOT.jar"
$trigger = New-ScheduledTaskTrigger -Once -At (Get-Date) -RepetitionInterval (New-TimeSpan -Minutes 5)
Register-ScheduledTask -TaskName "Analytics ETL" -Action $action -Trigger $trigger
```

### Systemd Timer (Linux)

Create `/etc/systemd/system/analytics-etl.service`:

```ini
[Unit]
Description=Analytics ETL Service
After=network.target

[Service]
Type=oneshot
ExecStart=/usr/bin/java -jar /opt/analytics-etl-service/analytics-etl-service-1.0.0-SNAPSHOT.jar
WorkingDirectory=/opt/analytics-etl-service
User=analytics
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Create `/etc/systemd/system/analytics-etl.timer`:

```ini
[Unit]
Description=Run Analytics ETL every 5 minutes
Requires=analytics-etl.service

[Timer]
OnBootSec=1min
OnUnitActiveSec=5min
Unit=analytics-etl.service

[Install]
WantedBy=timers.target
```

Enable:
```bash
sudo systemctl enable analytics-etl.timer
sudo systemctl start analytics-etl.timer
```

## Logs

View ETL execution logs:

```bash
# During execution
mvn spring-boot:run

# From systemd journal (if using systemd)
journalctl -u analytics-etl.service -f
```

## Development Notes

### No Authentication
For prototype purposes, both TDengine and SQLite connections are unauthenticated or use default credentials.

### Batch Processing
This is a batch ETL process, not streaming. Each run:
1. Truncates existing analytics
2. Re-aggregates all data
3. Inserts fresh analytics

### Schema Consistency
The service **MUST** use the same SQLite schema as digital-signage-service. Do not modify the database schema.

### Error Handling
- If TDengine is unavailable: Service exits with error
- If SQLite is locked: Waits and retries
- If no data: Logs warning and exits gracefully

## Troubleshooting

### TDengine Connection Failed

```
Error: Connection refused
```

**Solution**: Verify TDengine is running:
```bash
docker ps | grep tdengine
curl http://localhost:6041/rest/sql
```

### SQLite Database Locked

```
Error: database is locked
```

**Solution**: Stop digital-signage-service before running ETL, or configure SQLite with WAL mode.

### No Data Found

```
[ETL] No gaze events found, skipping transformation
```

**Solution**: Run the TDengine mock data script:
```bash
docker exec -i tdengine-tsdb taos < src/main/resources/db/tdengine/tdengine_mock_data.sql
```

## Future Enhancements

- [ ] Incremental ETL (process only new events)
- [ ] Streaming ETL with Spring Cloud Stream
- [ ] Error recovery and retry logic
- [ ] ETL metrics and monitoring
- [ ] Historical data archival
- [ ] Data quality checks and validation

## License

This is an academic prototype for evaluation purposes.

## Related Services

- **audience-analysis-service**: Generates gaze events (Python)
- **digital-signage-service**: Dashboard API (Spring Boot)
- **digital-signage-dashboard**: Frontend UI (Vue.js)
