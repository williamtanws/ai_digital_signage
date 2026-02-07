-- ================================================
-- User Schema Migration - V2
-- Purpose: Create users table, user_agent junction table with indexes and sample data
-- Optimized for H2 in-memory database (local development)
-- Date: 2026-01-18
-- ================================================

-- ==================== USERS TABLE ====================
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    new_nric VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    create_dt TIMESTAMP NOT NULL,
    update_dt TIMESTAMP NOT NULL
);

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_users_new_nric ON users(new_nric);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_email_status ON users(email, status);
CREATE INDEX IF NOT EXISTS idx_users_nric_status ON users(new_nric, status);
CREATE INDEX IF NOT EXISTS idx_users_create_dt ON users(create_dt DESC);

-- ==================== USER_AGENT JUNCTION TABLE ====================
CREATE TABLE IF NOT EXISTS user_agent (
    user_id VARCHAR(50) NOT NULL,
    agent_code VARCHAR(10) NOT NULL,
    create_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, agent_code),
    CONSTRAINT fk_user_agent_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_agent_agent FOREIGN KEY (agent_code) REFERENCES agents(agent_code) ON DELETE CASCADE
);

-- User-Agent junction indexes
CREATE INDEX IF NOT EXISTS idx_user_agent_user_id ON user_agent(user_id);
CREATE INDEX IF NOT EXISTS idx_user_agent_agent_code ON user_agent(agent_code);
CREATE INDEX IF NOT EXISTS idx_user_agent_composite ON user_agent(user_id, agent_code, create_dt);

-- ==================== SAMPLE USERS DATA ====================
INSERT INTO users (user_id, new_nric, email, name, status, create_dt, update_dt) 
VALUES 
    ('alex.wong', '920101-01-1234', 'alex.wong@allianz.com.my', 'Alex Wong Wei Ming', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('priya.kumar', '930202-02-2345', 'priya.kumar@allianz.com.my', 'Priya Kumar', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('tan.kim.lee', '940303-03-3456', 'tan.kim.lee@allianz.com.my', 'Tan Kim Lee', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('farah.ismail', '950404-04-4567', 'farah.ismail@allianz.com.my', 'Farah Ismail', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('raj.patel', '960505-05-5678', 'raj.patel@allianz.com.my', 'Raj Patel', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('siti.abdullah', '970606-06-6789', 'siti.abdullah@allianz.com.my', 'Siti Abdullah', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('lim.chong.wei', '980707-07-7890', 'lim.chong.wei@allianz.com.my', 'Lim Chong Wei', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('nurul.huda', '990808-08-8901', 'nurul.huda@allianz.com.my', 'Nurul Huda Mohamed', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('david.lee', '000909-09-9012', 'david.lee@allianz.com.my', 'David Lee Kah Wai', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('aminah.binti.ahmad', '011010-10-0123', 'aminah.ahmad@allianz.com.my', 'Aminah Binti Ahmad', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==================== USER-AGENT ASSOCIATIONS ====================
-- Associate each user with corresponding agent (1-to-1 mapping)
INSERT INTO user_agent (user_id, agent_code, create_dt)
VALUES 
    ('alex.wong', 'AGT001', CURRENT_TIMESTAMP),
    ('priya.kumar', 'AGT002', CURRENT_TIMESTAMP),
    ('tan.kim.lee', 'AGT003', CURRENT_TIMESTAMP),
    ('farah.ismail', 'AGT004', CURRENT_TIMESTAMP),
    ('raj.patel', 'AGT005', CURRENT_TIMESTAMP),
    ('siti.abdullah', 'AGT006', CURRENT_TIMESTAMP),
    ('lim.chong.wei', 'AGT007', CURRENT_TIMESTAMP),
    ('nurul.huda', 'AGT008', CURRENT_TIMESTAMP),
    ('david.lee', 'AGT009', CURRENT_TIMESTAMP),
    ('aminah.binti.ahmad', 'AGT010', CURRENT_TIMESTAMP);

-- ==================== COMMENTS ====================
COMMENT ON TABLE users IS 'User master data table';
COMMENT ON TABLE user_agent IS 'Many-to-many relationship between users and agents';
