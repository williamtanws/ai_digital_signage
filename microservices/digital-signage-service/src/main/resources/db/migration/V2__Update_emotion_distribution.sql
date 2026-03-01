-- Flyway Migration V2: Update emotion_distribution table
-- Replace 4 emotions with 8 FER2013 emotions

-- SQLite doesn't support DROP COLUMN or ALTER COLUMN,
-- so we need to recreate the table

-- 1. Create new table with 8 emotions
CREATE TABLE emotion_distribution_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    anger INTEGER NOT NULL DEFAULT 0,
    contempt INTEGER NOT NULL DEFAULT 0,
    disgust INTEGER NOT NULL DEFAULT 0,
    fear INTEGER NOT NULL DEFAULT 0,
    happiness INTEGER NOT NULL DEFAULT 0,
    neutral INTEGER NOT NULL DEFAULT 0,
    sadness INTEGER NOT NULL DEFAULT 0,
    surprise INTEGER NOT NULL DEFAULT 0
);

-- 2. Migrate existing data (map old emotions to new)
-- neutral -> neutral
-- serious -> contempt (closest match)
-- happy -> happiness
-- surprised -> surprise
INSERT INTO emotion_distribution_new (id, neutral, contempt, happiness, surprise)
SELECT id, neutral, serious, happy, surprised FROM emotion_distribution;

-- 3. Drop old table
DROP TABLE emotion_distribution;

-- 4. Rename new table
ALTER TABLE emotion_distribution_new RENAME TO emotion_distribution;
