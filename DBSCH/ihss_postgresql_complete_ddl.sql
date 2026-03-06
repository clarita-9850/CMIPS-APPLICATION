-- ============================================================================
-- IHSS CMIPS MODERNIZATION - COMPLETE POSTGRESQL DDL
-- ============================================================================
-- This DDL represents the complete modernized IHSS database schema
-- migrating from legacy Curam DB2 to modern PostgreSQL
--
-- Author: Database Migration Team
-- Date: 2024
-- Target DB: PostgreSQL 15+
-- ============================================================================

-- ============================================================================
-- SCHEMA CREATION
-- ============================================================================

-- Drop schemas if they exist (for clean installation)
DROP SCHEMA IF EXISTS ihss_org CASCADE;
DROP SCHEMA IF EXISTS cmips_ref CASCADE;
DROP SCHEMA IF EXISTS cmips_batch CASCADE;
DROP SCHEMA IF EXISTS cmips_case CASCADE;
DROP SCHEMA IF EXISTS cmips_provider CASCADE;
DROP SCHEMA IF EXISTS cmips_payroll CASCADE;
DROP SCHEMA IF EXISTS cmips_security CASCADE;
DROP SCHEMA IF EXISTS cmips_audit CASCADE;

-- Create schemas
CREATE SCHEMA ihss_org;        -- Organizational structure
CREATE SCHEMA cmips_ref;       -- Reference/code tables
CREATE SCHEMA cmips_batch;     -- Batch processing
CREATE SCHEMA cmips_case;      -- Case management
CREATE SCHEMA cmips_provider;  -- Provider management
CREATE SCHEMA cmips_payroll;   -- Payroll processing
CREATE SCHEMA cmips_security;  -- Security and authentication
CREATE SCHEMA cmips_audit;     -- Audit trail

COMMENT ON SCHEMA ihss_org IS 'IHSS organizational structure and staffing';
COMMENT ON SCHEMA cmips_ref IS 'Reference and code table data';
COMMENT ON SCHEMA cmips_batch IS 'Batch processing definitions and execution';
COMMENT ON SCHEMA cmips_case IS 'Case and eligibility management';
COMMENT ON SCHEMA cmips_provider IS 'Provider and service delivery';
COMMENT ON SCHEMA cmips_payroll IS 'Payroll and financial processing';
COMMENT ON SCHEMA cmips_security IS 'Security, roles, and permissions';
COMMENT ON SCHEMA cmips_audit IS 'Audit trail and change tracking';

-- ============================================================================
-- EXTENSIONS
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";      -- UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";       -- Encryption
CREATE EXTENSION IF NOT EXISTS "pg_trgm";        -- Text search
CREATE EXTENSION IF NOT EXISTS "btree_gin";      -- GIN indexes for multiple types

-- ============================================================================
-- ENUM TYPES
-- ============================================================================

-- Organizational enums
CREATE TYPE ihss_org.unit_type_enum AS ENUM (
    'STATE', 'COUNTY', 'DISTRICT', 'FIELD_OFFICE', 'TEAM'
);

CREATE TYPE ihss_org.unit_status_enum AS ENUM (
    'ACTIVE', 'INACTIVE', 'PENDING', 'CLOSED'
);

CREATE TYPE ihss_org.employment_status_enum AS ENUM (
    'ACTIVE', 'ON_LEAVE', 'SEPARATED', 'RETIRED', 'TERMINATED'
);

CREATE TYPE ihss_org.assignment_type_enum AS ENUM (
    'PRIMARY', 'ACTING', 'TEMPORARY', 'BACKUP'
);

CREATE TYPE ihss_org.position_status_enum AS ENUM (
    'ACTIVE', 'VACANT', 'FROZEN', 'ELIMINATED'
);

-- Case management enums
CREATE TYPE cmips_case.case_status_enum AS ENUM (
    'OPEN', 'IN_PROGRESS', 'PENDING_REVIEW', 'APPROVED', 
    'DENIED', 'CLOSED', 'CANCELLED', 'SUSPENDED'
);

CREATE TYPE cmips_case.case_type_enum AS ENUM (
    'ELIGIBILITY', 'ANNUAL_REVIEW', 'CHANGE_REQUEST', 
    'APPEAL', 'INVESTIGATION', 'COMPLIANCE'
);

CREATE TYPE cmips_case.priority_enum AS ENUM (
    'LOW', 'NORMAL', 'HIGH', 'CRITICAL'
);

-- Audit enums
CREATE TYPE cmips_audit.audit_action_enum AS ENUM (
    'CREATE', 'UPDATE', 'DELETE', 'VIEW', 'EXPORT', 'APPROVE', 'REJECT'
);

-- Record status enum (legacy compatibility)
CREATE TYPE cmips_ref.record_status_enum AS ENUM (
    'NORMAL', 'OBSOLETE', 'SUSPENDED', 'ARCHIVED'
);

-- ============================================================================
-- ORGANIZATIONAL STRUCTURE (ihss_org schema)
-- ============================================================================

-- 1. Program Unit (replaces ORGANISATIONUNIT)
CREATE TABLE ihss_org.program_unit (
    -- Primary key
    program_unit_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Hierarchy
    parent_unit_id      UUID REFERENCES ihss_org.program_unit(program_unit_id),
    
    -- Core attributes
    unit_code           VARCHAR(50) UNIQUE NOT NULL,
    unit_name           VARCHAR(255) NOT NULL,
    unit_type           ihss_org.unit_type_enum NOT NULL,
    status              ihss_org.unit_status_enum NOT NULL DEFAULT 'ACTIVE',
    
    -- Descriptive info
    business_type_code  VARCHAR(40),
    description         TEXT,
    comments            TEXT,
    web_address         VARCHAR(200),
    
    -- Location reference
    location_id         UUID,
    
    -- Temporal
    effective_start     DATE NOT NULL,
    effective_end       DATE,
    
    -- Security
    read_sid            VARCHAR(400),
    maintain_sid        VARCHAR(400),
    create_unit_sid     VARCHAR(400),
    
    -- Audit
    created_by          VARCHAR(255) NOT NULL,
    created_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at          TIMESTAMPTZ,
    version_number      INTEGER DEFAULT 1 NOT NULL,
    
    -- Legacy mapping
    legacy_org_unit_id  BIGINT,
    
    -- Constraints
    CONSTRAINT ck_unit_dates CHECK (effective_end IS NULL OR effective_end >= effective_start),
    CONSTRAINT ck_unit_hierarchy CHECK (parent_unit_id != program_unit_id)
);

-- Indexes
CREATE INDEX idx_program_unit_parent ON ihss_org.program_unit(parent_unit_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_program_unit_type ON ihss_org.program_unit(unit_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_program_unit_status ON ihss_org.program_unit(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_program_unit_code ON ihss_org.program_unit(unit_code) WHERE deleted_at IS NULL;
CREATE INDEX idx_program_unit_effective ON ihss_org.program_unit(effective_start, effective_end);
CREATE INDEX idx_program_unit_legacy ON ihss_org.program_unit(legacy_org_unit_id);

COMMENT ON TABLE ihss_org.program_unit IS 'IHSS organizational hierarchy: State, County, District, Field Office, Team';
COMMENT ON COLUMN ihss_org.program_unit.legacy_org_unit_id IS 'Maps to original ORGANISATIONUNIT.ORGANISATIONUNITID';

-- 2. Job Classification (replaces JOB)
CREATE TABLE ihss_org.job_classification (
    -- Primary key
    job_classification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Core attributes
    classification_code   VARCHAR(50) UNIQUE NOT NULL,
    title                 VARCHAR(255) NOT NULL,
    classification_family VARCHAR(100),
    grade_level           VARCHAR(50),
    
    -- Details
    description           TEXT,
    duties_description    TEXT,
    qualifications        TEXT,
    
    -- Metadata
    is_supervisory        BOOLEAN DEFAULT FALSE NOT NULL,
    salary_range_min      NUMERIC(19,4),
    salary_range_max      NUMERIC(19,4),
    
    -- Audit
    created_by            VARCHAR(255) NOT NULL,
    created_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_by            VARCHAR(255),
    updated_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at            TIMESTAMPTZ,
    version_number        INTEGER DEFAULT 1 NOT NULL,
    
    -- Legacy mapping
    legacy_job_id         BIGINT
);

CREATE INDEX idx_job_class_code ON ihss_org.job_classification(classification_code) WHERE deleted_at IS NULL;
CREATE INDEX idx_job_class_family ON ihss_org.job_classification(classification_family) WHERE deleted_at IS NULL;
CREATE INDEX idx_job_class_legacy ON ihss_org.job_classification(legacy_job_id);

COMMENT ON TABLE ihss_org.job_classification IS 'Civil service job classifications (not individual positions)';

-- 3. Staffing Position (replaces POSITION)
CREATE TABLE ihss_org.staffing_position (
    -- Primary key
    staffing_position_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Business key
    position_code         VARCHAR(50) UNIQUE NOT NULL,
    position_name         VARCHAR(400),
    
    -- Relationships
    program_unit_id       UUID NOT NULL REFERENCES ihss_org.program_unit(program_unit_id),
    job_classification_id UUID NOT NULL REFERENCES ihss_org.job_classification(job_classification_id),
    
    -- Attributes
    is_supervisory        BOOLEAN NOT NULL DEFAULT FALSE,
    is_lead_position      BOOLEAN NOT NULL DEFAULT FALSE,
    funding_source        VARCHAR(100),
    status                ihss_org.position_status_enum NOT NULL DEFAULT 'ACTIVE',
    
    -- Description
    comments              TEXT,
    
    -- Temporal
    effective_start       DATE NOT NULL,
    effective_end         DATE,
    
    -- Audit
    created_by            VARCHAR(255) NOT NULL,
    created_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_by            VARCHAR(255),
    updated_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at            TIMESTAMPTZ,
    version_number        INTEGER DEFAULT 1 NOT NULL,
    
    -- Legacy mapping
    legacy_position_id    BIGINT,
    
    -- Constraints
    CONSTRAINT ck_position_dates CHECK (effective_end IS NULL OR effective_end >= effective_start)
);

CREATE INDEX idx_staffing_pos_unit ON ihss_org.staffing_position(program_unit_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_staffing_pos_job ON ihss_org.staffing_position(job_classification_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_staffing_pos_status ON ihss_org.staffing_position(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_staffing_pos_code ON ihss_org.staffing_position(position_code) WHERE deleted_at IS NULL;
CREATE INDEX idx_staffing_pos_legacy ON ihss_org.staffing_position(legacy_position_id);

COMMENT ON TABLE ihss_org.staffing_position IS 'Budgeted staffing slots (can be vacant)';
COMMENT ON COLUMN ihss_org.staffing_position.legacy_position_id IS 'Maps to original POSITION.POSITIONID';

-- 4. Staff Account (replaces USERS)
CREATE TABLE ihss_org.staff_account (
    -- Primary key
    staff_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Authentication
    staff_identifier      VARCHAR(100) UNIQUE NOT NULL,  -- Employee number
    username              VARCHAR(150) UNIQUE NOT NULL,
    
    -- Personal info
    first_name            VARCHAR(100),
    surname               VARCHAR(120),
    full_name             VARCHAR(255),
    title                 VARCHAR(40),
    
    -- Contact
    business_email_id     UUID,
    business_phone_id     UUID,
    personal_email_id     UUID,
    personal_phone_id     UUID,
    mobile_phone_id       UUID,
    fax_id                UUID,
    pager_id              UUID,
    
    -- Location
    location_id           UUID NOT NULL,
    
    -- Status
    employment_status     ihss_org.employment_status_enum NOT NULL DEFAULT 'ACTIVE',
    account_enabled       BOOLEAN DEFAULT TRUE NOT NULL,
    status_code           VARCHAR(40),
    
    -- Security
    role_name             VARCHAR(200),
    sensitivity           VARCHAR(40),
    
    -- Login settings
    login_restrictions    BOOLEAN DEFAULT FALSE,
    login_time_from       TIME,
    login_time_to         TIME,
    login_day_mon         BOOLEAN DEFAULT TRUE,
    login_day_tue         BOOLEAN DEFAULT TRUE,
    login_day_wed         BOOLEAN DEFAULT TRUE,
    login_day_thu         BOOLEAN DEFAULT TRUE,
    login_day_fri         BOOLEAN DEFAULT TRUE,
    login_day_sat         BOOLEAN DEFAULT FALSE,
    login_day_sun         BOOLEAN DEFAULT FALSE,
    
    -- Password tracking (legacy - use external IAM)
    last_success_login    TIMESTAMPTZ,
    login_failures        INTEGER DEFAULT 0,
    
    -- Audit
    creation_date         DATE,
    created_by            VARCHAR(255) NOT NULL,
    created_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_by            VARCHAR(255),
    updated_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at            TIMESTAMPTZ,
    version_number        INTEGER DEFAULT 1 NOT NULL,
    
    -- Legacy mapping
    legacy_username       VARCHAR(120),
    
    -- Constraints
    CONSTRAINT ck_login_time CHECK (
        (login_time_from IS NULL AND login_time_to IS NULL) OR
        (login_time_from IS NOT NULL AND login_time_to IS NOT NULL)
    )
);

CREATE INDEX idx_staff_username ON ihss_org.staff_account(username) WHERE deleted_at IS NULL;
CREATE INDEX idx_staff_identifier ON ihss_org.staff_account(staff_identifier) WHERE deleted_at IS NULL;
CREATE INDEX idx_staff_status ON ihss_org.staff_account(employment_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_staff_location ON ihss_org.staff_account(location_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_staff_legacy ON ihss_org.staff_account(legacy_username);
CREATE INDEX idx_staff_name ON ihss_org.staff_account USING gin(
    to_tsvector('english', COALESCE(full_name, '') || ' ' || COALESCE(surname, ''))
);

COMMENT ON TABLE ihss_org.staff_account IS 'IHSS program staff/workers (decoupled from IAM)';
COMMENT ON COLUMN ihss_org.staff_account.legacy_username IS 'Maps to original USERS.USERNAME';

-- 5. Staffing Assignment (position holder)
CREATE TABLE ihss_org.staffing_assignment (
    -- Primary key
    staffing_assignment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Relationships
    staffing_position_id   UUID NOT NULL REFERENCES ihss_org.staffing_position(staffing_position_id),
    staff_id               UUID NOT NULL REFERENCES ihss_org.staff_account(staff_id),
    
    -- Assignment details
    assignment_start       DATE NOT NULL,
    assignment_end         DATE,
    assignment_type        ihss_org.assignment_type_enum NOT NULL DEFAULT 'PRIMARY',
    
    -- Audit
    created_by             VARCHAR(255) NOT NULL,
    created_at             TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_by             VARCHAR(255),
    updated_at             TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at             TIMESTAMPTZ,
    version_number         INTEGER DEFAULT 1 NOT NULL,
    
    -- Constraints
    CONSTRAINT uq_assignment UNIQUE (staffing_position_id, staff_id, assignment_start),
    CONSTRAINT ck_assignment_dates CHECK (assignment_end IS NULL OR assignment_end >= assignment_start)
);

CREATE INDEX idx_staffing_assign_position ON ihss_org.staffing_assignment(staffing_position_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_staffing_assign_staff ON ihss_org.staffing_assignment(staff_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_staffing_assign_dates ON ihss_org.staffing_assignment(assignment_start, assignment_end);
CREATE INDEX idx_staffing_assign_type ON ihss_org.staffing_assignment(assignment_type);

COMMENT ON TABLE ihss_org.staffing_assignment IS 'Who fills which position (supports acting/temporary assignments)';

-- 6. Case Queue (replaces WORKQUEUE)
CREATE TABLE ihss_org.case_queue (
    -- Primary key
    case_queue_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Business key
    queue_code            VARCHAR(50) UNIQUE NOT NULL,
    queue_name            VARCHAR(255) NOT NULL,
    
    -- Ownership
    program_unit_id       UUID REFERENCES ihss_org.program_unit(program_unit_id),
    administrator_staff_id UUID REFERENCES ihss_org.staff_account(staff_id),
    
    -- Settings
    allow_user_subscription BOOLEAN DEFAULT TRUE NOT NULL,
    sensitivity           VARCHAR(40),
    comments              TEXT,
    
    -- Status
    is_active             BOOLEAN DEFAULT TRUE,
    
    -- Audit
    created_by            VARCHAR(255) NOT NULL,
    created_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_by            VARCHAR(255),
    updated_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at            TIMESTAMPTZ,
    version_number        INTEGER DEFAULT 1 NOT NULL,
    
    -- Legacy mapping
    legacy_workqueue_id   BIGINT
);

CREATE INDEX idx_case_queue_code ON ihss_org.case_queue(queue_code) WHERE deleted_at IS NULL;
CREATE INDEX idx_case_queue_unit ON ihss_org.case_queue(program_unit_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_case_queue_admin ON ihss_org.case_queue(administrator_staff_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_case_queue_active ON ihss_org.case_queue(is_active) WHERE deleted_at IS NULL;
CREATE INDEX idx_case_queue_legacy ON ihss_org.case_queue(legacy_workqueue_id);

COMMENT ON TABLE ihss_org.case_queue IS 'IHSS case distribution queues';
COMMENT ON COLUMN ihss_org.case_queue.legacy_workqueue_id IS 'Maps to original WORKQUEUE.WORKQUEUEID';

-- 7. Queue Participation
CREATE TABLE ihss_org.case_queue_participant (
    -- Primary key
    participant_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Relationships (one must be present)
    case_queue_id         UUID NOT NULL REFERENCES ihss_org.case_queue(case_queue_id),
    staff_id              UUID REFERENCES ihss_org.staff_account(staff_id),
    staffing_position_id  UUID REFERENCES ihss_org.staffing_position(staffing_position_id),
    
    -- Metadata
    joined_at             TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    left_at               TIMESTAMPTZ,
    
    -- Audit
    created_by            VARCHAR(255) NOT NULL,
    created_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at            TIMESTAMPTZ,
    
    -- Constraints
    CONSTRAINT ck_participant_entity CHECK (
        staff_id IS NOT NULL OR staffing_position_id IS NOT NULL
    ),
    CONSTRAINT uq_queue_staff UNIQUE (case_queue_id, staff_id, joined_at),
    CONSTRAINT uq_queue_position UNIQUE (case_queue_id, staffing_position_id, joined_at)
);

CREATE INDEX idx_queue_part_queue ON ihss_org.case_queue_participant(case_queue_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_queue_part_staff ON ihss_org.case_queue_participant(staff_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_queue_part_position ON ihss_org.case_queue_participant(staffing_position_id) WHERE deleted_at IS NULL;

COMMENT ON TABLE ihss_org.case_queue_participant IS 'Who can pull cases from which queues';

-- 8. Case Routing Target (workflow abstraction)
CREATE TABLE ihss_org.case_routing_target (
    -- Primary key
    routing_target_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Polymorphic reference
    target_type           VARCHAR(30) NOT NULL,
    target_ref_id         UUID NOT NULL,
    
    -- Metadata
    target_name           VARCHAR(255),
    is_active             BOOLEAN DEFAULT TRUE,
    
    -- Audit
    created_by            VARCHAR(255) NOT NULL,
    created_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at            TIMESTAMPTZ,
    
    -- Constraints
    CONSTRAINT ck_target_type CHECK (
        target_type IN ('STAFF', 'STAFFING_POSITION', 'PROGRAM_UNIT', 'CASE_QUEUE')
    ),
    CONSTRAINT uq_routing_target UNIQUE (target_type, target_ref_id)
);

CREATE INDEX idx_routing_target_type ON ihss_org.case_routing_target(target_type, target_ref_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_routing_target_active ON ihss_org.case_routing_target(is_active) WHERE deleted_at IS NULL;

COMMENT ON TABLE ihss_org.case_routing_target IS 'Workflow routing abstraction layer';

-- ============================================================================
-- REFERENCE DATA (cmips_ref schema)
-- ============================================================================

-- Code Table Header (modernized)
CREATE TABLE cmips_ref.code_table_header (
    -- Primary key
    code_table_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Business key
    table_name            VARCHAR(100) UNIQUE NOT NULL,
    
    -- Attributes
    default_code          VARCHAR(40),
    parent_code_table_id  UUID REFERENCES cmips_ref.code_table_header(code_table_id),
    
    -- Temporal
    time_entered          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Audit
    created_by            VARCHAR(255) NOT NULL,
    created_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_by            VARCHAR(255),
    updated_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    version_number        INTEGER DEFAULT 1 NOT NULL,
    
    -- Legacy mapping
    legacy_table_name     VARCHAR(100)
);

CREATE INDEX idx_code_table_name ON cmips_ref.code_table_header(table_name);
CREATE INDEX idx_code_table_parent ON cmips_ref.code_table_header(parent_code_table_id);

-- Code Table Item
CREATE TABLE cmips_ref.code_table_item (
    -- Primary key
    code_item_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Relationships
    code_table_id         UUID NOT NULL REFERENCES cmips_ref.code_table_header(code_table_id),
    
    -- Business key
    code_value            VARCHAR(40) NOT NULL,
    
    -- Attributes
    enabled               BOOLEAN DEFAULT TRUE NOT NULL,
    start_date            DATE,
    end_date              DATE,
    record_status         cmips_ref.record_status_enum DEFAULT 'NORMAL' NOT NULL,
    
    -- Display
    display_name          VARCHAR(255),
    description           TEXT,
    display_order         INTEGER,
    
    -- Audit
    created_by            VARCHAR(255) NOT NULL,
    created_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_by            VARCHAR(255),
    updated_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    version_number        INTEGER DEFAULT 1 NOT NULL,
    
    -- Constraints
    CONSTRAINT uq_code_table_item UNIQUE (code_table_id, code_value),
    CONSTRAINT ck_code_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_code_item_table ON cmips_ref.code_table_item(code_table_id);
CREATE INDEX idx_code_item_enabled ON cmips_ref.code_table_item(enabled);
CREATE INDEX idx_code_item_status ON cmips_ref.code_table_item(record_status);

-- ============================================================================
-- AUDIT TRAIL (cmips_audit schema)
-- ============================================================================

-- Comprehensive audit trail (partitioned by month)
CREATE TABLE cmips_audit.audit_trail (
    -- Primary key
    audit_id              UUID DEFAULT gen_random_uuid(),
    
    -- What was changed
    table_schema          VARCHAR(50) NOT NULL,
    table_name            VARCHAR(255) NOT NULL,
    record_id             UUID NOT NULL,
    action                cmips_audit.audit_action_enum NOT NULL,
    
    -- Who made the change
    staff_id              UUID,
    username              VARCHAR(255) NOT NULL,
    session_id            UUID,
    ip_address            INET,
    user_agent            TEXT,
    
    -- When
    created_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    
    -- What changed
    old_values            JSONB,
    new_values            JSONB,
    changed_fields        TEXT[],
    
    -- Why
    reason                TEXT,
    
    -- Context
    request_id            UUID,
    correlation_id        UUID,
    
    -- Application context
    application_code      VARCHAR(40),
    transaction_id        BIGINT
) PARTITION BY RANGE (created_at);

-- Create initial partitions (last 3 months + next 3 months)
CREATE TABLE cmips_audit.audit_trail_2024_01 
    PARTITION OF cmips_audit.audit_trail
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE cmips_audit.audit_trail_2024_02 
    PARTITION OF cmips_audit.audit_trail
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- Indexes on partitions
CREATE INDEX idx_audit_2024_01_table_record 
    ON cmips_audit.audit_trail_2024_01(table_schema, table_name, record_id);
CREATE INDEX idx_audit_2024_01_staff 
    ON cmips_audit.audit_trail_2024_01(staff_id);
CREATE INDEX idx_audit_2024_01_action 
    ON cmips_audit.audit_trail_2024_01(action);

COMMENT ON TABLE cmips_audit.audit_trail IS 'Comprehensive audit trail for all data changes';

-- ============================================================================
-- SUPPORTING FUNCTIONS
-- ============================================================================

-- Function to auto-update timestamps
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    IF TG_OP = 'UPDATE' THEN
        NEW.version_number = OLD.version_number + 1;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply update trigger to all main tables
CREATE TRIGGER trg_program_unit_update
    BEFORE UPDATE ON ihss_org.program_unit
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_job_classification_update
    BEFORE UPDATE ON ihss_org.job_classification
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_staffing_position_update
    BEFORE UPDATE ON ihss_org.staffing_position
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_staff_account_update
    BEFORE UPDATE ON ihss_org.staff_account
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_staffing_assignment_update
    BEFORE UPDATE ON ihss_org.staffing_assignment
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_case_queue_update
    BEFORE UPDATE ON ihss_org.case_queue
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_code_table_header_update
    BEFORE UPDATE ON cmips_ref.code_table_header
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_code_table_item_update
    BEFORE UPDATE ON cmips_ref.code_table_item
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ============================================================================
-- VIEWS FOR LEGACY COMPATIBILITY
-- ============================================================================

-- View to simulate ORGANISATIONUNIT for legacy queries
CREATE VIEW ihss_org.v_organisationunit AS
SELECT 
    program_unit_id as organisationunitid,
    unit_code,
    unit_name as name,
    UPPER(unit_name) as uppername,
    business_type_code as businesstypecode,
    comments,
    status::text as statuscode,
    web_address as webaddress,
    read_sid as readsid,
    maintain_sid as maintainsid,
    create_unit_sid as createunitsid,
    location_id as locationid,
    effective_start as creationdate,
    created_by as createdby,
    created_at as createdon,
    updated_by as lastupdatedby,
    updated_at as lastupdatedon,
    'NORMAL' as recordstatus,
    version_number as versionno,
    updated_at as lastwritten
FROM ihss_org.program_unit
WHERE deleted_at IS NULL;

-- View to simulate POSITION for legacy queries
CREATE VIEW ihss_org.v_position AS
SELECT 
    staffing_position_id as positionid,
    position_code,
    position_name as name,
    UPPER(position_name) as uppername,
    is_lead_position as leadpositionind,
    job_classification_id as jobid,
    effective_start as fromdate,
    effective_end as todate,
    CASE status
        WHEN 'ACTIVE' THEN 'NORMAL'
        WHEN 'VACANT' THEN 'NORMAL'
        WHEN 'FROZEN' THEN 'SUSPENDED'
        WHEN 'ELIMINATED' THEN 'OBSOLETE'
    END as recordstatus,
    comments,
    version_number as versionno,
    updated_at as lastwritten
FROM ihss_org.staffing_position
WHERE deleted_at IS NULL;

-- View to simulate USERS for legacy queries  
CREATE VIEW ihss_org.v_users AS
SELECT 
    staff_id,
    username,
    staff_identifier,
    first_name as firstname,
    surname,
    full_name as fullname,
    title,
    account_enabled::CHAR(1) as accountenabled,
    employment_status::text as statuscode,
    location_id as locationid,
    business_email_id as businessemailid,
    business_phone_id as businessphoneid,
    personal_email_id as personalemailid,
    personal_phone_id as personalphonenumberid,
    mobile_phone_id as mobilephoneid,
    fax_id as faxid,
    pager_id as pagerid,
    role_name as rolename,
    sensitivity,
    login_restrictions::CHAR(1) as loginrestrictions,
    login_time_from as logintimefrom,
    login_time_to as logintimeto,
    login_day_mon::CHAR(1) as logindaymon,
    login_day_tue::CHAR(1) as logindaytues,
    login_day_wed::CHAR(1) as logindaywed,
    login_day_thu::CHAR(1) as logindaythurs,
    login_day_fri::CHAR(1) as logindayfri,
    login_day_sat::CHAR(1) as logindaysat,
    login_day_sun::CHAR(1) as logindaysun,
    last_success_login as lastsuccesslogin,
    login_failures as loginfailures,
    creation_date as creationdate,
    version_number as versionno,
    updated_at as lastwritten
FROM ihss_org.staff_account
WHERE deleted_at IS NULL;

-- View to simulate WORKQUEUE for legacy queries
CREATE VIEW ihss_org.v_workqueue AS
SELECT 
    case_queue_id as workqueueid,
    queue_name as name,
    UPPER(queue_name) as uppername,
    allow_user_subscription::CHAR(1) as allowusersubscriptionind,
    sensitivity,
    administrator_staff_id,
    comments,
    version_number as versionno,
    updated_at as lastwritten
FROM ihss_org.case_queue
WHERE deleted_at IS NULL;

-- ============================================================================
-- GRANT PERMISSIONS
-- ============================================================================

-- Create application roles
CREATE ROLE cmips_readonly;
CREATE ROLE cmips_readwrite;
CREATE ROLE cmips_admin;

-- Grant schema usage
GRANT USAGE ON SCHEMA ihss_org TO cmips_readonly, cmips_readwrite, cmips_admin;
GRANT USAGE ON SCHEMA cmips_ref TO cmips_readonly, cmips_readwrite, cmips_admin;
GRANT USAGE ON SCHEMA cmips_audit TO cmips_readonly, cmips_readwrite, cmips_admin;

-- Grant select to readonly
GRANT SELECT ON ALL TABLES IN SCHEMA ihss_org TO cmips_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA cmips_ref TO cmips_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA cmips_audit TO cmips_readonly;

-- Grant read/write to readwrite
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA ihss_org TO cmips_readwrite;
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA cmips_ref TO cmips_readwrite;
GRANT SELECT, INSERT ON ALL TABLES IN SCHEMA cmips_audit TO cmips_readwrite;

-- Grant all to admin
GRANT ALL ON ALL TABLES IN SCHEMA ihss_org TO cmips_admin;
GRANT ALL ON ALL TABLES IN SCHEMA cmips_ref TO cmips_admin;
GRANT ALL ON ALL TABLES IN SCHEMA cmips_audit TO cmips_admin;

-- ============================================================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================================================

COMMENT ON DATABASE current_database() IS 'IHSS CMIPS Modernization - PostgreSQL';

-- ============================================================================
-- END OF DDL
-- ============================================================================
