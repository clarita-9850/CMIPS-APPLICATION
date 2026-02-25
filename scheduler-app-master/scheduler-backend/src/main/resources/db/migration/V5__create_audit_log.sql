-- =====================================================
-- V5: Create audit_log table
-- Purpose: Tracks all administrative actions for compliance
-- =====================================================

CREATE TABLE audit_log (
    id                  BIGSERIAL PRIMARY KEY,
    entity_type         VARCHAR(100) NOT NULL,
    entity_id           BIGINT NOT NULL,
    action              VARCHAR(50) NOT NULL,
    action_timestamp    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    performed_by        VARCHAR(100) NOT NULL,
    performed_by_role   VARCHAR(100),
    previous_state      JSONB,
    new_state           JSONB,
    change_summary      TEXT,
    ip_address          VARCHAR(45),
    user_agent          TEXT,

    CONSTRAINT chk_audit_action CHECK (action IN (
        'CREATE', 'UPDATE', 'DELETE', 'ENABLE', 'DISABLE',
        'HOLD', 'RESUME', 'ICE', 'TRIGGER', 'STOP', 'RESTART',
        'ADD_DEPENDENCY', 'REMOVE_DEPENDENCY', 'REORDER'
    ))
);

-- Indexes for audit queries
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_log(action_timestamp DESC);
CREATE INDEX idx_audit_performed_by ON audit_log(performed_by);
CREATE INDEX idx_audit_action ON audit_log(action);

-- Partitioning hint: For production, consider partitioning by action_timestamp
-- This table can grow large, so partitioning by month/quarter is recommended

-- Comments
COMMENT ON TABLE audit_log IS 'Immutable audit trail of all administrative actions';
COMMENT ON COLUMN audit_log.entity_type IS 'Type of entity modified (JOB_DEFINITION, JOB_DEPENDENCY, CALENDAR, etc.)';
COMMENT ON COLUMN audit_log.previous_state IS 'JSON snapshot of entity state before change';
COMMENT ON COLUMN audit_log.new_state IS 'JSON snapshot of entity state after change';
COMMENT ON COLUMN audit_log.change_summary IS 'Human-readable description of what changed';

-- =====================================================
-- Additional indexes for job_definition table
-- (commonly needed queries not covered in V1)
-- =====================================================

CREATE INDEX idx_job_definition_cron ON job_definition(cron_expression)
    WHERE cron_expression IS NOT NULL AND deleted_at IS NULL AND enabled = true;

-- Full-text search support for job names and descriptions
CREATE INDEX idx_job_definition_search ON job_definition
    USING gin(to_tsvector('english', job_name || ' ' || COALESCE(description, '')));
