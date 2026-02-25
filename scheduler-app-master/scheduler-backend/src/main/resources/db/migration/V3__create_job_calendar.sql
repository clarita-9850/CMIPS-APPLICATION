-- =====================================================
-- V3: Create job_calendar table
-- Purpose: Defines blackout dates and special schedules
-- =====================================================

CREATE TABLE job_calendar (
    id                  BIGSERIAL PRIMARY KEY,
    calendar_name       VARCHAR(100) NOT NULL,
    description         TEXT,
    calendar_type       VARCHAR(50) NOT NULL,
    is_active           BOOLEAN DEFAULT true,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),

    CONSTRAINT uk_job_calendar_name UNIQUE (calendar_name),
    CONSTRAINT chk_calendar_type CHECK (calendar_type IN ('BLACKOUT', 'HOLIDAY', 'MAINTENANCE', 'CUSTOM'))
);

-- Calendar dates (the actual dates in each calendar)
CREATE TABLE job_calendar_date (
    id                  BIGSERIAL PRIMARY KEY,
    calendar_id         BIGINT NOT NULL,
    calendar_date       DATE NOT NULL,
    description         VARCHAR(255),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_calendar_date_calendar FOREIGN KEY (calendar_id)
        REFERENCES job_calendar(id) ON DELETE CASCADE,
    CONSTRAINT uk_calendar_date UNIQUE (calendar_id, calendar_date)
);

-- Job to calendar association (which jobs use which calendars)
CREATE TABLE job_calendar_assignment (
    id                  BIGSERIAL PRIMARY KEY,
    job_id              BIGINT NOT NULL,
    calendar_id         BIGINT NOT NULL,
    assignment_type     VARCHAR(50) DEFAULT 'EXCLUDE',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(100),

    CONSTRAINT fk_calendar_assignment_job FOREIGN KEY (job_id)
        REFERENCES job_definition(id) ON DELETE CASCADE,
    CONSTRAINT fk_calendar_assignment_calendar FOREIGN KEY (calendar_id)
        REFERENCES job_calendar(id) ON DELETE CASCADE,
    CONSTRAINT uk_job_calendar_assignment UNIQUE (job_id, calendar_id),
    CONSTRAINT chk_assignment_type CHECK (assignment_type IN ('EXCLUDE', 'INCLUDE_ONLY'))
);

-- Indexes
CREATE INDEX idx_job_calendar_active ON job_calendar(is_active) WHERE is_active = true;
CREATE INDEX idx_calendar_date_date ON job_calendar_date(calendar_date);
CREATE INDEX idx_calendar_assignment_job ON job_calendar_assignment(job_id);

-- Comments
COMMENT ON TABLE job_calendar IS 'Defines named calendars for scheduling exclusions or inclusions';
COMMENT ON TABLE job_calendar_date IS 'Individual dates that belong to a calendar';
COMMENT ON TABLE job_calendar_assignment IS 'Associates jobs with calendars for scheduling rules';
COMMENT ON COLUMN job_calendar_assignment.assignment_type IS 'EXCLUDE: skip job on calendar dates, INCLUDE_ONLY: run job only on calendar dates';
