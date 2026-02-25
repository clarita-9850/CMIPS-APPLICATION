-- =====================================================
-- V1: Create job_definition table
-- Purpose: Stores job configurations and schedules
-- =====================================================

CREATE TABLE job_definition (
    id                  BIGSERIAL PRIMARY KEY,
    job_name            VARCHAR(255) NOT NULL,
    job_type            VARCHAR(100) NOT NULL,
    description         TEXT,
    cron_expression     VARCHAR(100),
    timezone            VARCHAR(50) DEFAULT 'America/Los_Angeles',
    status              VARCHAR(20) DEFAULT 'ACTIVE',
    enabled             BOOLEAN DEFAULT true,
    priority            INTEGER DEFAULT 5,
    max_retries         INTEGER DEFAULT 3,
    timeout_seconds     INTEGER DEFAULT 3600,
    job_parameters      JSONB DEFAULT '{}',
    target_roles        TEXT[] DEFAULT '{}',
    target_counties     TEXT[] DEFAULT '{}',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted_at          TIMESTAMP,

    CONSTRAINT uk_job_definition_name UNIQUE (job_name),
    CONSTRAINT chk_job_definition_priority CHECK (priority BETWEEN 1 AND 10),
    CONSTRAINT chk_job_definition_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_HOLD', 'ON_ICE'))
);

-- Indexes for common queries
CREATE INDEX idx_job_definition_status ON job_definition(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_job_definition_job_type ON job_definition(job_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_job_definition_enabled ON job_definition(enabled) WHERE deleted_at IS NULL;
CREATE INDEX idx_job_definition_created_at ON job_definition(created_at DESC);

-- Comments
COMMENT ON TABLE job_definition IS 'Stores batch job configurations and schedules';
COMMENT ON COLUMN job_definition.job_name IS 'Unique name identifying the job';
COMMENT ON COLUMN job_definition.job_type IS 'Type/category of job (e.g., REPORT, ETL, SYNC)';
COMMENT ON COLUMN job_definition.cron_expression IS 'Quartz cron expression for scheduling';
COMMENT ON COLUMN job_definition.status IS 'ACTIVE: runs normally, ON_HOLD: paused with blocked deps, ON_ICE: skipped but deps run, INACTIVE: disabled';
COMMENT ON COLUMN job_definition.job_parameters IS 'JSON object with job-specific parameters';
COMMENT ON COLUMN job_definition.target_roles IS 'Array of roles this job targets';
COMMENT ON COLUMN job_definition.target_counties IS 'Array of county codes this job targets';
