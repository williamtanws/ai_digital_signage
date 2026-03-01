-- Flyway Migration V3: Add System Health and Research Metrics tables
-- For research validation: performance tracking, face detection accuracy, baseline comparison

-- System Health Table (stores performance and environment metrics)
CREATE TABLE system_health (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    status VARCHAR(50) NOT NULL,
    -- Performance metrics
    current_fps REAL,
    avg_fps REAL,
    min_fps REAL,
    max_fps REAL,
    current_cpu_temp REAL,
    max_cpu_temp REAL,
    cpu_threshold REAL DEFAULT 78.0,
    -- Environment metrics
    temperature_celsius REAL,
    humidity_percent REAL,
    pressure_hpa REAL,
    gas_resistance_ohms REAL,
    noise_db REAL,
    -- Metadata
    uptime VARCHAR(50),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Research Metrics Table (stores validation metrics for academic research)
CREATE TABLE research_metrics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Face detection metrics
    face_detection_accuracy REAL,
    face_detection_confidence REAL,
    frames_processed INTEGER,
    faces_detected INTEGER,
    -- Gaze quality metrics
    primary_method_rate REAL,
    fallback_method_rate REAL,
    gaze_avg_confidence REAL,
    gaze_quality_score VARCHAR(20),
    gaze_recommendation TEXT,
    -- Baseline comparison (static signage)
    baseline_condition VARCHAR(50),
    baseline_avg_engagement REAL,
    baseline_period VARCHAR(100),
    -- Current comparison (dynamic signage)
    current_condition VARCHAR(50),
    current_avg_engagement REAL,
    current_period VARCHAR(100),
    -- Improvement metrics
    improvement_absolute REAL,
    improvement_percentage REAL,
    improvement_significant BOOLEAN,
    -- Metadata
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_system_health_updated ON system_health(last_updated DESC);
CREATE INDEX idx_research_metrics_updated ON research_metrics(last_updated DESC);
