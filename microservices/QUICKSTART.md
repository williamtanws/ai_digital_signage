# 🚀 Quick Start Guide

## One-Command Test Flow

Run this to verify the complete pipeline:

```powershell
# 1. Verify TDengine (Source)
$headers = @{'Authorization' = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('root:taosdata'))}
Invoke-RestMethod -Uri 'http://localhost:6041/rest/sql/digital_signage' -Method Post -Headers $headers -Body 'SELECT COUNT(*) FROM gaze_events'

# 2. Verify Backend API (SQLite → API)
Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/overview" -Method Get | ConvertTo-Json

# 3. Open Dashboard
Start-Process "http://localhost:5174"
```

## Startup Order (Quick Reference)

1. **TDengine** (Docker) - Already running
2. **Load Data** - Execute `load_data.sql` manually
3. **Backend API** - Start digital-signage-service (port 8080)
4. **Run ETL** - Execute analytics-etl-service once
5. **Dashboard** - Start digital-signage-dashboard (port 5174)

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                   TDengine (Time-Series DB)                 │
│  Table: gaze_events | Records: 57 | Port: 6041             │
└────────────────────────────┬────────────────────────────────┘
                             │ Extract (JDBC)
                             ▼
┌─────────────────────────────────────────────────────────────┐
│              analytics-etl-service (ETL Process)            │
│  Extract → Transform (Aggregate) → Load                    │
└────────────────────────────┬────────────────────────────────┘
                             │ Load (JDBC)
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                SQLite (Analytics Database)                  │
│  Tables: metrics_kpi, age_distribution, etc. | 52 KB       │
└────────────────────────────┬────────────────────────────────┘
                             │ Read (JPA)
                             ▼
┌─────────────────────────────────────────────────────────────┐
│           digital-signage-service (Backend API)             │
│  REST API: /api/dashboard/* | Port: 8080                   │
└────────────────────────────┬────────────────────────────────┘
                             │ HTTP GET
                             ▼
┌─────────────────────────────────────────────────────────────┐
│        digital-signage-dashboard (Vue.js Frontend)          │
│  Charts & KPIs | Port: 5174 | Vite Dev Server              │
└─────────────────────────────────────────────────────────────┘
```

## Testing Checklist

- [x] TDengine has 57 records
- [x] SQLite database populated (52 KB)
- [x] Backend API returns data
- [x] Dashboard displays charts
- [x] All ports accessible

## Access Points

| Service | URL |
|---------|-----|
| Dashboard | http://localhost:5174 |
| API Docs | http://localhost:8080/api/dashboard/overview |
| SQLite Browser | http://localhost:3000 |
| TDengine Web UI | http://localhost:6060 (root/taosdata) |
| TDengine REST | http://localhost:6041/rest/sql |

---
See [README.md](./README.md) for complete documentation.
