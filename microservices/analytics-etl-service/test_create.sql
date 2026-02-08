USE digital_signage;
CREATE STABLE gaze_events (ts TIMESTAMP, viewer_id VARCHAR(20), gaze_time DOUBLE, session_duration DOUBLE, interested INT, attention_rate DOUBLE, age INT, gender VARCHAR(10), emotion VARCHAR(20), ad_name VARCHAR(50)) TAGS (event_type VARCHAR(20));
SHOW STABLES;
