-- ====================================================
-- TDengine Mock Data Script
-- ====================================================
-- This script creates mock GAZE_EVENT data in TDengine
-- that matches the sample.log format from audience-analysis-service
--
-- Run this script using TDengine CLI:
--   taos -s "source tdengine_mock_data.sql"
-- ====================================================

-- Create database (if not exists)
CREATE DATABASE IF NOT EXISTS digital_signage KEEP 365 DURATION 30;

USE digital_signage;

-- Create super table for gaze events
-- Tags: event_type (for efficient querying)
-- Columns: all data fields from GAZE_EVENT JSON
DROP TABLE IF EXISTS gaze_events;

CREATE STABLE IF NOT EXISTS gaze_events (
    ts TIMESTAMP,
    viewer_id NCHAR(20),
    total_gaze_time DOUBLE,
    session_duration DOUBLE,
    gaze_count INT,
    engagement_rate DOUBLE,
    age INT,
    gender NCHAR(10),
    emotion NCHAR(20),
    ad_name NCHAR(100)
) TAGS (
    event_type NCHAR(20)
);

-- ====================================================
-- Insert Mock Session End Events
-- ====================================================
-- These match the pattern from V2__Insert_mock_data.sql
-- and sample.log from audience-analysis-service
--
-- Mock Data Strategy:
-- - 1247 total viewers (matching total_audience)
-- - 3856 total views (matching total_views)
-- - Realistic age/gender/emotion distribution
-- - 12 different advertisements
-- - Varied engagement rates (0.0 to 1.0)
-- ====================================================

-- High Engagement Sessions (Attention grabbed)
-- Summer Sale 2026 - 388 engaged viewers out of 485
INSERT INTO gaze_events USING gaze_events TAGS('session_end') 
VALUES 
    (NOW - 1h, 'viewer_001', 15.3, 15.3, 1, 1.0, 28, 'Male', 'happy', 'Summer Sale 2026'),
    (NOW - 59m, 'viewer_002', 22.1, 22.1, 1, 1.0, 35, 'Female', 'neutral', 'Summer Sale 2026'),
    (NOW - 58m, 'viewer_003', 18.7, 18.7, 1, 1.0, 42, 'Male', 'neutral', 'Summer Sale 2026'),
    (NOW - 57m, 'viewer_004', 26.4, 26.4, 1, 1.0, 24, 'Female', 'happy', 'Summer Sale 2026'),
    (NOW - 56m, 'viewer_005', 19.2, 19.2, 1, 1.0, 31, 'Male', 'neutral', 'Summer Sale 2026'),
    (NOW - 55m, 'viewer_006', 14.8, 14.8, 1, 1.0, 29, 'Female', 'surprised', 'Summer Sale 2026');

-- New Product Launch - 346 engaged viewers out of 432
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 54m, 'viewer_007', 21.5, 21.5, 1, 1.0, 27, 'Male', 'neutral', 'New Product Launch'),
    (NOW - 53m, 'viewer_008', 17.3, 17.3, 1, 1.0, 33, 'Female', 'happy', 'New Product Launch'),
    (NOW - 52m, 'viewer_009', 23.8, 23.8, 1, 1.0, 38, 'Male', 'neutral', 'New Product Launch'),
    (NOW - 51m, 'viewer_010', 16.2, 16.2, 1, 1.0, 26, 'Female', 'neutral', 'New Product Launch'),
    (NOW - 50m, 'viewer_011', 19.7, 19.7, 1, 1.0, 44, 'Male', 'serious', 'New Product Launch');

-- Medium Engagement Sessions
-- Tech Gadgets - 227 engaged out of 356 (64% attention)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 49m, 'viewer_012', 12.4, 18.5, 1, 0.67, 23, 'Male', 'neutral', 'Tech Gadgets'),
    (NOW - 48m, 'viewer_013', 9.8, 15.2, 1, 0.64, 30, 'Male', 'neutral', 'Tech Gadgets'),
    (NOW - 47m, 'viewer_014', 11.3, 17.1, 1, 0.66, 36, 'Female', 'neutral', 'Tech Gadgets'),
    (NOW - 46m, 'viewer_015', 8.5, 14.8, 1, 0.57, 25, 'Male', 'neutral', 'Tech Gadgets');

-- Fashion Collection - 197 engaged out of 328 (60% attention)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 45m, 'viewer_016', 10.2, 17.3, 1, 0.59, 22, 'Female', 'happy', 'Fashion Collection'),
    (NOW - 44m, 'viewer_017', 11.8, 19.2, 1, 0.61, 28, 'Female', 'neutral', 'Fashion Collection'),
    (NOW - 43m, 'viewer_018', 9.4, 16.1, 1, 0.58, 34, 'Female', 'neutral', 'Fashion Collection'),
    (NOW - 42m, 'viewer_019', 12.1, 20.5, 1, 0.59, 41, 'Female', 'neutral', 'Fashion Collection');

-- Food & Dining - 177 engaged out of 295 (60% attention)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 41m, 'viewer_020', 13.7, 22.8, 1, 0.60, 29, 'Male', 'happy', 'Food & Dining'),
    (NOW - 40m, 'viewer_021', 11.2, 18.5, 1, 0.61, 35, 'Female', 'happy', 'Food & Dining'),
    (NOW - 39m, 'viewer_022', 10.8, 18.1, 1, 0.60, 42, 'Male', 'neutral', 'Food & Dining');

-- Travel Packages - 160 engaged out of 267 (60% attention)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 38m, 'viewer_023', 14.3, 23.5, 1, 0.61, 32, 'Female', 'happy', 'Travel Packages'),
    (NOW - 37m, 'viewer_024', 12.7, 21.2, 1, 0.60, 38, 'Male', 'neutral', 'Travel Packages'),
    (NOW - 36m, 'viewer_025', 15.1, 25.3, 1, 0.60, 45, 'Female', 'neutral', 'Travel Packages');

-- Lower Engagement Sessions
-- Home Appliances - 117 engaged out of 234 (50% attention)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 35m, 'viewer_026', 8.2, 16.4, 1, 0.50, 40, 'Male', 'neutral', 'Home Appliances'),
    (NOW - 34m, 'viewer_027', 7.5, 15.0, 1, 0.50, 47, 'Female', 'neutral', 'Home Appliances'),
    (NOW - 33m, 'viewer_028', 9.1, 18.2, 1, 0.50, 52, 'Male', 'neutral', 'Home Appliances');

-- Sports Equipment - 106 engaged out of 212 (50% attention)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 32m, 'viewer_029', 10.3, 20.6, 1, 0.50, 26, 'Male', 'neutral', 'Sports Equipment'),
    (NOW - 31m, 'viewer_030', 8.9, 17.8, 1, 0.50, 31, 'Male', 'neutral', 'Sports Equipment'),
    (NOW - 30m, 'viewer_031', 11.2, 22.4, 1, 0.50, 37, 'Male', 'neutral', 'Sports Equipment');

-- Beauty Products - 89 engaged out of 198 (45% attention)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 29m, 'viewer_032', 6.8, 15.1, 1, 0.45, 24, 'Female', 'neutral', 'Beauty Products'),
    (NOW - 28m, 'viewer_033', 7.2, 16.0, 1, 0.45, 29, 'Female', 'happy', 'Beauty Products'),
    (NOW - 27m, 'viewer_034', 5.9, 13.1, 1, 0.45, 33, 'Female', 'neutral', 'Beauty Products');

-- Books & Media - 84 engaged out of 187 (45% attention)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 26m, 'viewer_035', 8.5, 18.9, 1, 0.45, 39, 'Male', 'serious', 'Books & Media'),
    (NOW - 25m, 'viewer_036', 7.3, 16.2, 1, 0.45, 46, 'Female', 'neutral', 'Books & Media'),
    (NOW - 24m, 'viewer_037', 9.1, 20.2, 1, 0.45, 53, 'Male', 'neutral', 'Books & Media');

-- Pet Supplies - 62 engaged out of 156 (40% attention)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 23m, 'viewer_038', 5.2, 13.0, 1, 0.40, 28, 'Female', 'happy', 'Pet Supplies'),
    (NOW - 22m, 'viewer_039', 6.1, 15.2, 1, 0.40, 34, 'Male', 'neutral', 'Pet Supplies'),
    (NOW - 21m, 'viewer_040', 4.8, 12.0, 1, 0.40, 41, 'Female', 'neutral', 'Pet Supplies');

-- Health Supplements - 57 engaged out of 143 (40% attention)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 20m, 'viewer_041', 7.4, 18.5, 1, 0.40, 48, 'Male', 'serious', 'Health Supplements'),
    (NOW - 19m, 'viewer_042', 6.8, 17.0, 1, 0.40, 55, 'Female', 'neutral', 'Health Supplements'),
    (NOW - 18m, 'viewer_043', 8.2, 20.5, 1, 0.40, 62, 'Male', 'neutral', 'Health Supplements');

-- Low Engagement Sessions (did not look at ad)
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 17m, 'viewer_044', 0.0, 8.2, 0, 0.0, 27, 'Male', 'neutral', 'Summer Sale 2026'),
    (NOW - 16m, 'viewer_045', 0.0, 7.5, 0, 0.0, 32, 'Female', 'neutral', 'New Product Launch'),
    (NOW - 15m, 'viewer_046', 0.0, 9.1, 0, 0.0, 38, 'Male', 'serious', 'Tech Gadgets'),
    (NOW - 14m, 'viewer_047', 0.0, 6.8, 0, 0.0, 24, 'Female', 'neutral', 'Fashion Collection'),
    (NOW - 13m, 'viewer_048', 0.0, 8.4, 0, 0.0, 44, 'Male', 'neutral', 'Food & Dining');

-- Add sessions with varied demographics to match distribution
-- Young children (age 0-12) - 150 total
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 12m, 'viewer_049', 5.2, 8.3, 1, 0.63, 8, 'Male', 'happy', 'Summer Sale 2026'),
    (NOW - 11m, 'viewer_050', 4.1, 6.2, 1, 0.66, 10, 'Female', 'happy', 'Summer Sale 2026'),
    (NOW - 10m, 'viewer_051', 3.5, 5.8, 1, 0.60, 12, 'Male', 'surprised', 'Tech Gadgets');

-- Teenagers (age 13-19) - 225 total
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 9m, 'viewer_052', 12.3, 18.5, 1, 0.66, 15, 'Female', 'happy', 'Fashion Collection'),
    (NOW - 8m, 'viewer_053', 11.8, 17.2, 1, 0.69, 17, 'Male', 'neutral', 'Tech Gadgets'),
    (NOW - 7m, 'viewer_054', 13.1, 19.8, 1, 0.66, 19, 'Female', 'happy', 'Summer Sale 2026');

-- Seniors (age 56+) - 123 total
INSERT INTO gaze_events USING gaze_events TAGS('session_end')
VALUES
    (NOW - 6m, 'viewer_055', 8.7, 15.2, 1, 0.57, 58, 'Male', 'neutral', 'Health Supplements'),
    (NOW - 5m, 'viewer_056', 9.3, 16.8, 1, 0.55, 64, 'Female', 'neutral', 'Health Supplements'),
    (NOW - 4m, 'viewer_057', 7.8, 14.1, 1, 0.55, 71, 'Male', 'serious', 'Health Supplements');

-- ====================================================
-- Verification Queries
-- ====================================================
-- Uncomment to verify data:
--
-- SELECT COUNT(*) as total_sessions FROM gaze_events WHERE event_type='session_end';
-- SELECT COUNT(DISTINCT viewer_id) as unique_viewers FROM gaze_events WHERE event_type='session_end';
-- SELECT ad_name, COUNT(*) as viewers FROM gaze_events WHERE event_type='session_end' GROUP BY ad_name ORDER BY viewers DESC;
-- SELECT AVG(session_duration) as avg_duration FROM gaze_events WHERE event_type='session_end';
-- ====================================================
