-- ================================================
-- Agent Schema Migration - V1
-- Purpose: Create agents table with indexes and sample data
-- Optimized for H2 in-memory database (local development)
-- Date: 2026-01-18
-- ================================================

-- ==================== AGENTS TABLE ====================
CREATE TABLE IF NOT EXISTS agents (
    agent_code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    branch_code VARCHAR(6) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    create_dt TIMESTAMP NOT NULL,
    update_dt TIMESTAMP NOT NULL
);

-- Agents indexes
CREATE INDEX IF NOT EXISTS idx_agents_branch_code ON agents(branch_code);
CREATE INDEX IF NOT EXISTS idx_agents_channel ON agents(channel);
CREATE INDEX IF NOT EXISTS idx_agents_status ON agents(status);
CREATE INDEX IF NOT EXISTS idx_agents_branch_status ON agents(branch_code, status);
CREATE INDEX IF NOT EXISTS idx_agents_channel_status ON agents(channel, status);
CREATE INDEX IF NOT EXISTS idx_agents_create_dt ON agents(create_dt DESC);

-- ==================== SAMPLE AGENTS DATA ====================
INSERT INTO agents (agent_code, name, branch_code, channel, status, create_dt, update_dt) 
VALUES 
    ('AGT001', 'John Smith', 'BR0001', 'DIRECT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AGT002', 'Sarah Johnson', 'BR0001', 'BROKER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AGT003', 'Michael Chen', 'BR0002', 'BANCASSURANCE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AGT004', 'Emily Davis', 'BR0002', 'ONLINE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AGT005', 'Robert Wilson', 'BR0003', 'TELEMARKETING', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AGT006', 'Lisa Anderson', 'BR0003', 'DIRECT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AGT007', 'David Martinez', 'BR0001', 'BROKER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AGT008', 'Jennifer Taylor', 'BR0004', 'BANCASSURANCE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AGT009', 'James Brown', 'BR0004', 'ONLINE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AGT010', 'Maria Garcia', 'BR0005', 'DIRECT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==================== COMMENTS ====================
COMMENT ON TABLE agents IS 'Agent master data table';
