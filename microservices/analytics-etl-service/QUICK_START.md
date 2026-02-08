# Quick Start Guide: Analytics ETL Service

## 🚀 Quick Start (5 Steps)

### Step 1: Start TDengine

```bash
cd ../../docker/tdengine
docker-compose up -d
```

Verify: `docker ps | grep tdengine`

### Step 2: Load Mock Data

```bash
cd ../../microservices/analytics-etl-service

# Load mock gaze events into TDengine
docker exec -i tdengine-tsdb taos < src/main/resources/db/tdengine/tdengine_mock_data.sql
```

### Step 3: Verify SQLite Database

```bash
# Ensure digital-signage-service database exists
ls -lh ../digital-signage-service/data/digital-signage.db
```

### Step 4: Run ETL

```bash
mvn spring-boot:run
```

Expected output:
```
╔════════════════════════════════════════════════════════╗
║     Analytics ETL Service - Starting Pipeline         ║
╚════════════════════════════════════════════════════════╝

[EXTRACT] Reading gaze events from TDengine...
[EXTRACT] Found 60 session end events
[TRANSFORM] Aggregating gaze events into analytics...
[TRANSFORM] Created dashboard analytics and 12 ad analytics
[LOAD] Clearing old analytics...
[LOAD] Saving dashboard analytics...
[LOAD] Saving 12 advertisement analytics...

╔════════════════════════════════════════════════════════╗
║     Analytics ETL Pipeline - Completed Successfully   ║
╚════════════════════════════════════════════════════════╝
```

### Step 5: Verify Results

```bash
# Check SQLite data
sqlite3 ../digital-signage-service/data/digital-signage.db "SELECT * FROM metrics_kpi;"

# Or use SQLite browser
open http://localhost:3000

# Or query via dashboard API (if digital-signage-service is running)
curl http://localhost:8080/api/dashboard/overview
```

## 📊 What Gets Transformed

### TDengine (Source)
```
gaze_events table
  ├── viewer_id: "2941ca1b"
  ├── session_duration: 12.3
  ├── engagement_rate: 0.8
  ├── age: 32
  ├── gender: "Male"
  ├── emotion: "neutral"
  └── ad_name: "Summer Sale 2026"
```

### SQLite (Target)
```
metrics_kpi table
  ├── total_audience: 60
  ├── total_views: 60
  ├── total_ads: 12
  └── avg_view_seconds: 18.5

advertisement table
  ├── ad_name: "Summer Sale 2026"
  ├── total_viewers: 6
  ├── look_yes: 5
  └── look_no: 1

age_distribution, gender_distribution, emotion_distribution...
```

## 🔍 Verification Commands

### TDengine Data Check
```bash
docker exec -it tdengine-tsdb taos

# Inside taos CLI:
USE digital_signage;
SELECT COUNT(*) FROM gaze_events WHERE event_type='session_end';
SELECT ad_name, COUNT(*) FROM gaze_events WHERE event_type='session_end' GROUP BY ad_name;
```

### SQLite Data Check
```bash
sqlite3 ../digital-signage-service/data/digital-signage.db

# Inside sqlite CLI:
SELECT * FROM metrics_kpi;
SELECT ad_name, total_viewers, look_yes, look_no FROM advertisement ORDER BY total_viewers DESC;
```

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| `Connection refused` to TDengine | Run `docker-compose up -d` in docker/tdengine |
| `No data found` | Load mock data: `docker exec -i tdengine-tsdb taos < ...` |
| `SQLite database not found` | Run digital-signage-service first to create database |
| `Database is locked` | Stop digital-signage-service before ETL |

## 📅 Schedule ETL

### Linux/Mac (Cron)
```bash
# Edit crontab
crontab -e

# Run every 5 minutes
*/5 * * * * cd /path/to/analytics-etl-service && mvn spring-boot:run
```

### Windows (Task Scheduler)
```powershell
# Run every 5 minutes
schtasks /create /tn "Analytics ETL" /tr "C:\path\to\run-etl.bat" /sc minute /mo 5
```

## 📚 Architecture Flow

```
+------------------+       +------------------+       +------------------+
|   TDengine       |       |   ETL Service    |       |     SQLite       |
|  (Time Series)   | ----> |  (Transform)     | ----> | (Analytics DB)   |
+------------------+       +------------------+       +------------------+
  Gaze Events                Aggregation               Dashboard Data
  (Raw logs)                 (Business logic)          (API responses)
```

## 🎯 Next Steps

1. ✅ ETL completed successfully
2. Start `digital-signage-service` to serve API
3. Open `digital-signage-dashboard` to view analytics
4. Schedule ETL for periodic updates
