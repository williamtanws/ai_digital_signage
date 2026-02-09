# Quick Start Guide: Analytics ETL Service

## 🚀 Quick Start (5 Steps)

### Step 1: Start TDengine

```bash
cd ../../docker/tdengine
docker-compose up -d
```

Verify: `docker ps | grep tdengine`

### Step 2: Load Mock Data (Choose One)

```bash
cd ../../microservices/analytics-etl-service

# Option A: Primary dataset (105 diverse records) - RECOMMENDED
docker cp tdengine_init.sql tdengine-tsdb:/tmp/init.sql
docker exec tdengine-tsdb taos -f /tmp/init.sql

# Option B: Extended dataset (300 records)
docker exec -i tdengine-tsdb taos < src/main/resources/db/tdengine/tdengine_mock_data.sql
```

**Data includes:** Children (ages 5-12), teenagers (13-19), adults, seniors, diverse emotions, "did not look" records

### Step 3: Start Backend Service

```bash
cd ../digital-signage-service
$env:JAVA_HOME = "C:\Program Files\SapMachine\JDK\21"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=sqlite"
```

Wait for: `Started DigitalSignageServiceApplication`

### Step 4: Run ETL Service

```bash
cd ../analytics-etl-service
$env:JAVA_HOME = "C:\Program Files\SapMachine\JDK\21"
mvn spring-boot:run
```

**Expected output:**
```
╔════════════════════════════════════════════════════════╗
║     Analytics ETL Service - Starting Pipeline         ║
║     Mode: Continuous (Scheduled every 5 minutes)      ║
╚════════════════════════════════════════════════════════╝

[EXTRACT] Found 100 NEW session_end events
[TRANSFORM] Created dashboard analytics and 5 ad analytics
[LOAD] Sending analytics update to digital-signage-service...
Successfully sent analytics to digital-signage-service

╔════════════════════════════════════════════════════════╗
║     Initial ETL Pipeline - Completed Successfully     ║
║     Next run: In 5 minutes                            ║
╚════════════════════════════════════════════════════════╝
```

### Step 5: Verify Data Populated Successfully

```powershell
# Check dashboard API response
$result = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/overview"
$result | ConvertTo-Json -Depth 10
```

**Expected response includes:**
```json
{
  "totalAudience": 100,
  "ageDistribution": {
    "children": 5,
    "teenagers": 8,
    "youngAdults": 38,
    "midAged": 42,
    "seniors": 7
  },
  "emotionDistribution": {
    "neutral": 57,
    "serious": 9,
    "happy": 28,
    "surprised": 6
  }
}
```

✅ **Verify diversity:** Children present, teenagers present, serious emotion present

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
