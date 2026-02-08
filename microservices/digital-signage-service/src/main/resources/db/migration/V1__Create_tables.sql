-- Flyway Migration V1: Create initial database schema
-- Digital Signage Dashboard Tables

-- Metrics KPI Table (stores overall dashboard statistics)
CREATE TABLE metrics_kpi (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    total_audience INTEGER NOT NULL,
    total_views INTEGER NOT NULL,
    total_ads INTEGER NOT NULL,
    avg_view_seconds REAL NOT NULL
);

-- Age Distribution Table (stores audience age demographics)
CREATE TABLE age_distribution (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    children INTEGER NOT NULL,
    teenagers INTEGER NOT NULL,
    young_adults INTEGER NOT NULL,
    mid_aged INTEGER NOT NULL,
    seniors INTEGER NOT NULL
);

-- Gender Distribution Table (stores audience gender demographics)
CREATE TABLE gender_distribution (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    male INTEGER NOT NULL,
    female INTEGER NOT NULL
);

-- Emotion Distribution Table (stores audience emotion analysis)
CREATE TABLE emotion_distribution (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    neutral INTEGER NOT NULL,
    serious INTEGER NOT NULL,
    happy INTEGER NOT NULL,
    surprised INTEGER NOT NULL
);

-- Advertisement Table (stores ad performance and attention metrics)
CREATE TABLE advertisement (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ad_name VARCHAR(100) NOT NULL UNIQUE,
    total_viewers INTEGER NOT NULL,
    look_yes INTEGER NOT NULL,
    look_no INTEGER NOT NULL
);

-- Create index for advertisement lookups
CREATE INDEX idx_advertisement_ad_name ON advertisement(ad_name);
CREATE INDEX idx_advertisement_viewers ON advertisement(total_viewers DESC);
