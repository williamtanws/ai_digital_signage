# Analytics ETL Service - Architecture Diagram

## Complete End-to-End Analytics Pipeline

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                      EDGE AI DIGITAL SIGNAGE PLATFORM                           │
└─────────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────┐
│  Edge AI Device      │
│  (Raspberry Pi 5)    │
│                      │
│  ├─ Camera           │
│  ├─ Face Detection   │
│  ├─ Gaze Tracking    │
│  └─ Demographics     │
└──────────────────────┘
         │
         │ Generates GAZE_EVENT logs
         ▼
┌──────────────────────┐
│ audience-analysis-   │
│      service         │
│    (Python)          │
│                      │
│  Emits:              │
│  • gaze_start        │
│  • session_end       │
│  • heartbeat         │
└──────────────────────┘
         │
         │ Writes events
         ▼
┌──────────────────────────────────────────────────────────────────┐
│                         TDengine                                 │
│                    (Time-Series Database)                        │
│                                                                  │
│  Table: gaze_events                                              │
│  ├─ ts (timestamp)                                               │
│  ├─ event_type (gaze_start/session_end/heartbeat)               │
│  ├─ viewer_id (unique identifier)                                │
│  ├─ session_duration, total_gaze_time, engagement_rate          │
│  ├─ age, gender, emotion (demographics)                          │
│  └─ ad_name (advertisement being displayed)                      │
│                                                                  │
│  Volume: High-frequency time-series data                         │
│  Retention: 365 days                                             │
└──────────────────────────────────────────────────────────────────┘
         │
         │ ETL Process (Batch or Scheduled)
         ▼
┌──────────────────────────────────────────────────────────────────┐
│                  ✨ ANALYTICS ETL SERVICE ✨                      │
│                     (Spring Boot + Java 21)                      │
│                                                                  │
│  ┌────────────┐     ┌─────────────┐     ┌───────────┐          │
│  │  EXTRACT   │ ──> │  TRANSFORM  │ ──> │   LOAD    │          │
│  └────────────┘     └─────────────┘     └───────────┘          │
│        │                   │                    │               │
│        │                   │                    │               │
│   Read from          Aggregate into      Write to              │
│   TDengine           Analytics           SQLite                │
│                                                                  │
│  Domain Layer:                                                   │
│  ├─ GazeEvent                                                    │
│  ├─ DashboardAnalytics                                           │
│  └─ AdAnalytics                                                  │
│                                                                  │
│  Application Layer:                                              │
│  └─ AnalyticsEtlService                                          │
│     • Counts unique viewers                                      │
│     • Calculates avg session duration                            │
│     • Groups by age/gender/emotion                               │
│     • Aggregates per-ad engagement                               │
│                                                                  │
│  Infrastructure Layer:                                           │
│  ├─ TDengineGazeEventRepository (Extract)                        │
│  └─ SqliteAnalyticsRepository (Load)                             │
└──────────────────────────────────────────────────────────────────┘
         │
         │ Populates analytics tables
         ▼
┌──────────────────────────────────────────────────────────────────┐
│                          SQLite                                  │
│                    (Analytics Database)                          │
│                                                                  │
│  Tables:                                                         │
│  ├─ metrics_kpi (total_audience, total_views, avg_view_seconds) │
│  ├─ age_distribution (children, teenagers, young_adults...)     │
│  ├─ gender_distribution (male, female)                          │
│  ├─ emotion_distribution (neutral, serious, happy, surprised)   │
│  └─ advertisement (ad_name, total_viewers, look_yes, look_no)   │
│                                                                  │
│  Volume: Aggregated metrics, low volume                          │
│  Purpose: Fast query for dashboard                               │
└──────────────────────────────────────────────────────────────────┘
         │
         │ Serves via REST API
         ▼
┌──────────────────────────────────────────────────────────────────┐
│               digital-signage-service                            │
│              (Spring Boot + Hexagonal)                           │
│                                                                  │
│  REST Endpoint:                                                  │
│  GET /api/dashboard/overview                                     │
│                                                                  │
│  Returns JSON with:                                              │
│  • KPIs (audience, views, avg time)                              │
│  • Demographics (age, gender, emotion)                           │
│  • Ad performance & attention                                    │
└──────────────────────────────────────────────────────────────────┘
         │
         │ API calls
         ▼
┌──────────────────────────────────────────────────────────────────┐
│              digital-signage-dashboard                           │
│                (Vue.js + Vite + Chart.js)                        │
│                                                                  │
│  UI Components:                                                  │
│  ├─ KPI Cards (audience, views, avg time)                        │
│  ├─ Age Distribution Chart (bar chart)                           │
│  ├─ Gender Distribution Chart (pie chart)                        │
│  ├─ Emotion Distribution Chart (pie chart)                       │
│  ├─ Top Performing Ads (bar chart)                               │
│  └─ Ad Attention Rates (bar chart)                               │
│                                                                  │
│  Accessible at: http://localhost:5175                            │
└──────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      DATA FLOW SUMMARY                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Raw Events → TDengine → ETL Service → SQLite → API → Dashboard │
│  (Logs)      (Store)     (Transform)   (Cache)  (Serve) (View) │
│                                                                 │
│  High        Time        Aggregation   Relational REST   Web   │
│  Volume      Series      Analytics     Database   API    UI    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Transformation Example

### Before (TDengine Raw Event)
```json
{
  "ts": "2026-02-08T10:58:58.214976Z",
  "event_type": "session_end",
  "viewer_id": "2941ca1b",
  "session_duration": 3.1,
  "total_gaze_time": 3.1,
  "gaze_count": 1,
  "engagement_rate": 1.0,
  "age": 33,
  "gender": "Female",
  "emotion": "neutral",
  "ad_name": "Summer Sale 2026"
}
```

### After (SQLite Aggregated Analytics)
```sql
-- KPI Metrics (aggregated across all sessions)
INSERT INTO metrics_kpi VALUES (1247, 3856, 12, 24.5);

-- Advertisement Analytics (grouped by ad_name)
INSERT INTO advertisement VALUES ('Summer Sale 2026', 485, 388, 97);

-- Demographics (counted and grouped)
INSERT INTO age_distribution VALUES (150, 225, 437, 312, 123);
INSERT INTO gender_distribution VALUES (648, 599);
INSERT INTO emotion_distribution VALUES (561, 312, 274, 100);
```

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Edge Device** | Raspberry Pi 5 + Camera | Capture audience data |
| **Analysis Service** | Python + Hailo AI | Face detection, gaze tracking |
| **Time-Series DB** | TDengine | Store raw gaze events |
| **ETL Service** | Spring Boot + Java 21 | Transform events to analytics |
| **Analytics DB** | SQLite | Store aggregated metrics |
| **API Service** | Spring Boot + Hexagonal | Serve dashboard data |
| **Frontend** | Vue.js + Vite | Visualize analytics |

## Hexagonal Architecture in ETL Service

```
┌─────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                        │
│                  (Pure Business Logic)                  │
│                                                         │
│  GazeEvent ←──────── Domain Entities                    │
│  DashboardAnalytics                                     │
│  AdAnalytics                                            │
│                                                         │
│  GazeEventRepository ←── Domain Interfaces (Ports)      │
│  AnalyticsRepository                                    │
└─────────────────────────────────────────────────────────┘
                         ↑
                    depends on
                         │
┌─────────────────────────────────────────────────────────┐
│                  APPLICATION LAYER                      │
│                    (Use Cases)                          │
│                                                         │
│  AnalyticsEtlService ←── Orchestrates ETL Process       │
│    │                                                    │
│    ├─ Extract from TDengine (via port)                 │
│    ├─ Transform (business rules)                       │
│    └─ Load to SQLite (via port)                        │
└─────────────────────────────────────────────────────────┘
                         ↑
                   implements
                         │
┌─────────────────────────────────────────────────────────┐
│               INFRASTRUCTURE LAYER                      │
│            (Framework & External Systems)               │
│                                                         │
│  TDengineGazeEventRepository ←── Implements Extract     │
│    └─ Uses JDBC, SQL queries                            │
│                                                         │
│  SqliteAnalyticsRepository ←── Implements Load          │
│    └─ Uses JDBC, SQL inserts                            │
│                                                         │
│  DatabaseConfig ←── Spring configuration                │
└─────────────────────────────────────────────────────────┘
```

## Key Design Decisions

### ✅ Why Batch ETL (not streaming)?
- **Simplicity**: Easy to understand and maintain
- **Academic focus**: Demonstrates concepts, not production scale
- **Sufficient**: Dashboard doesn't need real-time updates
- **Schedulable**: Can run periodically (cron, systemd timer)

### ✅ Why TDengine?
- **Time-series optimized**: Perfect for gaze event logs
- **High write throughput**: Handles edge device data ingestion
- **Query efficiency**: Fast aggregation queries
- **Open source**: Free for academic use

### ✅ Why SQLite for analytics?
- **Same DB as dashboard**: No schema duplication
- **Fast reads**: In-process, zero network latency
- **Simple**: No additional infrastructure
- **Portable**: Single file database

### ✅ Why Hexagonal Architecture?
- **Testable**: Can mock TDengine/SQLite for tests
- **Flexible**: Easy to swap databases (e.g., PostgreSQL)
- **Clean separation**: Business logic isolated from frameworks
- **Follows enterprise patterns**: Demonstrates best practices
