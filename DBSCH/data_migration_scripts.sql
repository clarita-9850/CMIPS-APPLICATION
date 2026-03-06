-- ============================================================================
-- DATA MIGRATION SCRIPTS
-- Curam Legacy DB2 → Modern IHSS PostgreSQL
-- ============================================================================
-- These scripts migrate data from the legacy Curam schema to the new
-- modernized IHSS schema structure
--
-- IMPORTANT: Run these scripts IN ORDER after both databases are available
-- ============================================================================

-- ============================================================================
-- PREREQUISITES
-- ============================================================================

-- Ensure both databases are accessible
-- Legacy DB (DB2): Connected as source
-- Modern DB (PostgreSQL): Connected as target

-- If using Foreign Data Wrapper (recommended for gradual migration):
/*
CREATE EXTENSION postgres_fdw;

CREATE SERVER legacy_curam
  FOREIGN DATA WRAPPER postgres_fdw
  OPTIONS (host 'legacy-host', dbname 'curam', port '50000');

CREATE USER MAPPING FOR current_user
  SERVER legacy_curam
  OPTIONS (user 'curam_user', password 'password');

-- Import foreign schema
IMPORT FOREIGN SCHEMA public
  LIMIT TO (ORGANISATIONUNIT, POSITION, USERS, WORKQUEUE, ORGANISATION)
  FROM SERVER legacy_curam
  INTO public;
*/

-- ============================================================================
-- MIGRATION 1: PROGRAM UNITS (ORGANISATIONUNIT → program_unit)
-- ============================================================================

INSERT INTO ihss_org.program_unit (
    program_unit_id,
    parent_unit_id,
    unit_code,
    unit_name,
    unit_type,
    status,
    business_type_code,
    description,
    comments,
    web_address,
    location_id,
    effective_start,
    effective_end,
    read_sid,
    maintain_sid,
    create_unit_sid,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version_number,
    legacy_org_unit_id
)
SELECT 
    -- Generate new UUIDs
    gen_random_uuid(),
    
    -- Parent relationship (will be updated in second pass)
    NULL,  -- Updated later after all units are inserted
    
    -- Business key - use ORGANISATIONUNITID if no better code exists
    COALESCE(CAST(ou.ORGANISATIONUNITID AS VARCHAR(50)), 'ORG-' || ou.ORGANISATIONUNITID),
    
    -- Name
    COALESCE(ou.NAME, 'Unnamed Unit'),
    
    -- Type - infer from name or default to FIELD_OFFICE
    CASE 
        WHEN ou.NAME LIKE '%STATE%' THEN 'STATE'::ihss_org.unit_type_enum
        WHEN ou.NAME LIKE '%COUNTY%' THEN 'COUNTY'::ihss_org.unit_type_enum
        WHEN ou.NAME LIKE '%DISTRICT%' THEN 'DISTRICT'::ihss_org.unit_type_enum
        WHEN ou.NAME LIKE '%TEAM%' THEN 'TEAM'::ihss_org.unit_type_enum
        ELSE 'FIELD_OFFICE'::ihss_org.unit_type_enum
    END,
    
    -- Status
    CASE ou.STATUSCODE
        WHEN 'NORMAL' THEN 'ACTIVE'::ihss_org.unit_status_enum
        WHEN 'SUSPENDED' THEN 'INACTIVE'::ihss_org.unit_status_enum
        WHEN 'OBSOLETE' THEN 'CLOSED'::ihss_org.unit_status_enum
        ELSE 'ACTIVE'::ihss_org.unit_status_enum
    END,
    
    -- Business type code
    ou.BUSINESSTYPECODE,
    
    -- Description (combine if needed)
    ou.COMMENTS,
    
    -- Comments
    ou.COMMENTS,
    
    -- Web address
    ou.WEBADDRESS,
    
    -- Location ID - will need separate migration
    NULL,  -- TODO: Map to location table
    
    -- Dates
    COALESCE(ou.CREATIONDATE, CURRENT_DATE),
    NULL, -- effective_end
    
    -- Security SIDs
    ou.READSID,
    ou.MAINTAINSID,
    ou.CREATEUNITSID,
    
    -- Audit
    COALESCE(ou.CREATEDBY, 'MIGRATION'),
    COALESCE(ou.CREATEDON, NOW()),
    COALESCE(ou.LASTUPDATEDBY, 'MIGRATION'),
    COALESCE(ou.LASTUPDATEDON, NOW()),
    COALESCE(ou.VERSIONNO, 1),
    
    -- Legacy mapping
    ou.ORGANISATIONUNITID
    
FROM ORGANISATIONUNIT ou
WHERE ou.RECORDSTATUS != 'OBSOLETE' OR ou.RECORDSTATUS IS NULL;

-- Create mapping table for parent relationships
CREATE TEMP TABLE org_unit_id_map AS
SELECT 
    ou.ORGANISATIONUNITID as legacy_id,
    pu.program_unit_id as new_id
FROM ORGANISATIONUNIT ou
JOIN ihss_org.program_unit pu ON pu.legacy_org_unit_id = ou.ORGANISATIONUNITID;

-- Update parent relationships using org structure
UPDATE ihss_org.program_unit pu
SET parent_unit_id = map.new_id
FROM ORGANISATIONSTRUCTURE os
JOIN org_unit_id_map map ON os.PARENTORGANISATIONUNITID = map.legacy_id
WHERE pu.legacy_org_unit_id = os.ORGANISATIONUNITID;

-- Log migration results
DO $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM ihss_org.program_unit;
    RAISE NOTICE 'Migrated % program units', v_count;
END $$;

-- ============================================================================
-- MIGRATION 2: JOB CLASSIFICATIONS (implicit from POSITION.JOBID)
-- ============================================================================

-- First, extract unique job IDs and create classifications
INSERT INTO ihss_org.job_classification (
    job_classification_id,
    classification_code,
    title,
    classification_family,
    grade_level,
    description,
    created_by,
    created_at,
    version_number,
    legacy_job_id
)
SELECT DISTINCT
    gen_random_uuid(),
    'JOB-' || j.JOBID,
    COALESCE(j.NAME, 'Job ' || j.JOBID),
    j.JOBFAMILY,
    j.GRADELEVEL,
    j.DESCRIPTION,
    COALESCE(j.CREATEDBY, 'MIGRATION'),
    COALESCE(j.CREATEDON, NOW()),
    1,
    j.JOBID
FROM JOB j
WHERE j.RECORDSTATUS != 'OBSOLETE' OR j.RECORDSTATUS IS NULL;

-- Create job ID mapping
CREATE TEMP TABLE job_id_map AS
SELECT 
    j.JOBID as legacy_job_id,
    jc.job_classification_id as new_job_id
FROM JOB j
JOIN ihss_org.job_classification jc ON jc.legacy_job_id = j.JOBID;

DO $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM ihss_org.job_classification;
    RAISE NOTICE 'Migrated % job classifications', v_count;
END $$;

-- ============================================================================
-- MIGRATION 3: STAFFING POSITIONS (POSITION → staffing_position)
-- ============================================================================

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    comments,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
)
SELECT 
    gen_random_uuid(),
    
    -- Position code
    'POS-' || p.POSITIONID,
    
    -- Name
    COALESCE(p.NAME, 'Position ' || p.POSITIONID),
    
    -- Program unit (from position location link)
    ou_map.new_id,
    
    -- Job classification
    job_map.new_job_id,
    
    -- Supervisory flag
    FALSE, -- TODO: Derive from job or position data
    
    -- Lead position
    CASE WHEN p.LEADPOSITIONIND = '1' THEN TRUE ELSE FALSE END,
    
    -- Status
    CASE p.RECORDSTATUS
        WHEN 'NORMAL' THEN 'ACTIVE'::ihss_org.position_status_enum
        WHEN 'SUSPENDED' THEN 'FROZEN'::ihss_org.position_status_enum
        WHEN 'OBSOLETE' THEN 'ELIMINATED'::ihss_org.position_status_enum
        ELSE 'ACTIVE'::ihss_org.position_status_enum
    END,
    
    -- Comments
    p.COMMENTS,
    
    -- Dates
    COALESCE(p.FROMDATE, CURRENT_DATE),
    p.TODATE,
    
    -- Audit
    'MIGRATION',
    NOW(),
    COALESCE(p.VERSIONNO, 1),
    
    -- Legacy mapping
    p.POSITIONID
    
FROM POSITION p
-- Join to get the organization unit through position-org relationships
LEFT JOIN POSITIONORGANISATIONUNITLINK poul ON p.POSITIONID = poul.POSITIONID
LEFT JOIN org_unit_id_map ou_map ON poul.ORGANISATIONUNITID = ou_map.legacy_id
-- Join to get job classification
JOIN job_id_map job_map ON p.JOBID = job_map.legacy_job_id
WHERE p.RECORDSTATUS != 'OBSOLETE' OR p.RECORDSTATUS IS NULL;

-- Create position ID mapping
CREATE TEMP TABLE position_id_map AS
SELECT 
    p.POSITIONID as legacy_position_id,
    sp.staffing_position_id as new_position_id
FROM POSITION p
JOIN ihss_org.staffing_position sp ON sp.legacy_position_id = p.POSITIONID;

DO $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM ihss_org.staffing_position;
    RAISE NOTICE 'Migrated % staffing positions', v_count;
END $$;

-- ============================================================================
-- MIGRATION 4: STAFF ACCOUNTS (USERS → staff_account)
-- ============================================================================

INSERT INTO ihss_org.staff_account (
    staff_id,
    staff_identifier,
    username,
    first_name,
    surname,
    full_name,
    title,
    business_email_id,
    business_phone_id,
    personal_email_id,
    personal_phone_id,
    mobile_phone_id,
    fax_id,
    pager_id,
    location_id,
    employment_status,
    account_enabled,
    status_code,
    role_name,
    sensitivity,
    login_restrictions,
    login_time_from,
    login_time_to,
    login_day_mon,
    login_day_tue,
    login_day_wed,
    login_day_thu,
    login_day_fri,
    login_day_sat,
    login_day_sun,
    last_success_login,
    login_failures,
    creation_date,
    created_by,
    created_at,
    version_number,
    legacy_username
)
SELECT 
    gen_random_uuid(),
    
    -- Staff identifier (username or generate)
    COALESCE(u.USERNAME, 'USER-' || u.USERNAME),
    
    -- Username
    u.USERNAME,
    
    -- Personal info
    u.FIRSTNAME,
    u.SURNAME,
    COALESCE(u.FULLNAME, u.FIRSTNAME || ' ' || u.SURNAME),
    u.TITLE,
    
    -- Contact IDs (will need separate migration)
    NULL, -- business_email_id
    NULL, -- business_phone_id  
    NULL, -- personal_email_id
    NULL, -- personal_phone_id
    NULL, -- mobile_phone_id
    NULL, -- fax_id
    NULL, -- pager_id
    
    -- Location
    NULL, -- location_id - TODO: Map from USERS.LOCATIONID
    
    -- Status
    CASE u.STATUSCODE
        WHEN 'ACTIVE' THEN 'ACTIVE'::ihss_org.employment_status_enum
        WHEN 'INACTIVE' THEN 'SEPARATED'::ihss_org.employment_status_enum
        WHEN 'SUSPENDED' THEN 'ON_LEAVE'::ihss_org.employment_status_enum
        ELSE 'ACTIVE'::ihss_org.employment_status_enum
    END,
    
    CASE WHEN u.ACCOUNTENABLED = '1' THEN TRUE ELSE FALSE END,
    u.STATUSCODE,
    u.ROLENAME,
    u.SENSITIVITY,
    
    -- Login settings
    CASE WHEN u.LOGINRESTRICTIONS = '1' THEN TRUE ELSE FALSE END,
    u.LOGINTIMEFROM::TIME,
    u.LOGINTIMETO::TIME,
    CASE WHEN u.LOGINDAYMON = '1' THEN TRUE ELSE FALSE END,
    CASE WHEN u.LOGINDAYTUES = '1' THEN TRUE ELSE FALSE END,
    CASE WHEN u.LOGINDAYWED = '1' THEN TRUE ELSE FALSE END,
    CASE WHEN u.LOGINDAYTHURS = '1' THEN TRUE ELSE FALSE END,
    CASE WHEN u.LOGINDAYFRI = '1' THEN TRUE ELSE FALSE END,
    CASE WHEN u.LOGINDAYSAT = '1' THEN TRUE ELSE FALSE END,
    CASE WHEN u.LOGINDAYSUN = '1' THEN TRUE ELSE FALSE END,
    
    u.LASTSUCCESSLOGIN,
    u.LOGINFAILURES,
    u.CREATIONDATE,
    
    -- Audit
    'MIGRATION',
    NOW(),
    COALESCE(u.VERSIONNO, 1),
    
    -- Legacy mapping
    u.USERNAME
    
FROM USERS u;

-- Create staff ID mapping
CREATE TEMP TABLE staff_id_map AS
SELECT 
    u.USERNAME as legacy_username,
    sa.staff_id as new_staff_id
FROM USERS u
JOIN ihss_org.staff_account sa ON sa.legacy_username = u.USERNAME;

DO $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM ihss_org.staff_account;
    RAISE NOTICE 'Migrated % staff accounts', v_count;
END $$;

-- ============================================================================
-- MIGRATION 5: STAFFING ASSIGNMENTS (POSITIONHOLDERLINK → staffing_assignment)
-- ============================================================================

INSERT INTO ihss_org.staffing_assignment (
    staffing_assignment_id,
    staffing_position_id,
    staff_id,
    assignment_start,
    assignment_end,
    assignment_type,
    created_by,
    created_at,
    version_number
)
SELECT 
    gen_random_uuid(),
    
    -- Position
    pos_map.new_position_id,
    
    -- Staff
    staff_map.new_staff_id,
    
    -- Dates
    COALESCE(phl.FROMDATE, CURRENT_DATE),
    phl.TODATE,
    
    -- Type - assume PRIMARY unless acting indicator present
    CASE 
        WHEN phl.ACTINGIND = '1' THEN 'ACTING'::ihss_org.assignment_type_enum
        ELSE 'PRIMARY'::ihss_org.assignment_type_enum
    END,
    
    -- Audit
    'MIGRATION',
    NOW(),
    1
    
FROM POSITIONHOLDERLINK phl
JOIN position_id_map pos_map ON phl.POSITIONID = pos_map.legacy_position_id
JOIN staff_id_map staff_map ON phl.USERNAME = staff_map.legacy_username
WHERE phl.RECORDSTATUS != 'OBSOLETE' OR phl.RECORDSTATUS IS NULL;

DO $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM ihss_org.staffing_assignment;
    RAISE NOTICE 'Migrated % staffing assignments', v_count;
END $$;

-- ============================================================================
-- MIGRATION 6: CASE QUEUES (WORKQUEUE → case_queue)
-- ============================================================================

INSERT INTO ihss_org.case_queue (
    case_queue_id,
    queue_code,
    queue_name,
    program_unit_id,
    administrator_staff_id,
    allow_user_subscription,
    sensitivity,
    comments,
    is_active,
    created_by,
    created_at,
    version_number,
    legacy_workqueue_id
)
SELECT 
    gen_random_uuid(),
    
    -- Queue code
    'QUEUE-' || wq.WORKQUEUEID,
    
    -- Name
    COALESCE(wq.NAME, 'Queue ' || wq.WORKQUEUEID),
    
    -- Program unit - try to derive from administrator
    NULL, -- TODO: Derive from user's org unit
    
    -- Administrator
    admin_map.new_staff_id,
    
    -- Settings
    CASE WHEN wq.ALLOWUSERSUBSCRIPTIONIND = '1' THEN TRUE ELSE FALSE END,
    wq.SENSITIVITY,
    wq.COMMENTS,
    TRUE, -- Assume active
    
    -- Audit
    'MIGRATION',
    NOW(),
    COALESCE(wq.VERSIONNO, 1),
    
    -- Legacy mapping
    wq.WORKQUEUEID
    
FROM WORKQUEUE wq
LEFT JOIN staff_id_map admin_map ON wq.ADMINISTRATORUSERNAME = admin_map.legacy_username;

-- Create queue ID mapping
CREATE TEMP TABLE queue_id_map AS
SELECT 
    wq.WORKQUEUEID as legacy_queue_id,
    cq.case_queue_id as new_queue_id
FROM WORKQUEUE wq
JOIN ihss_org.case_queue cq ON cq.legacy_workqueue_id = wq.WORKQUEUEID;

DO $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM ihss_org.case_queue;
    RAISE NOTICE 'Migrated % case queues', v_count;
END $$;

-- ============================================================================
-- MIGRATION 7: QUEUE PARTICIPANTS (WORKQUEUESUBSCRIPTION → case_queue_participant)
-- ============================================================================

INSERT INTO ihss_org.case_queue_participant (
    participant_id,
    case_queue_id,
    staff_id,
    joined_at,
    created_by,
    created_at
)
SELECT 
    gen_random_uuid(),
    
    -- Queue
    queue_map.new_queue_id,
    
    -- Staff
    staff_map.new_staff_id,
    
    -- Dates
    NOW(), -- Use current time as joined_at
    
    -- Audit
    'MIGRATION',
    NOW()
    
FROM WORKQUEUESUBSCRIPTION wqs
JOIN queue_id_map queue_map ON wqs.WORKQUEUEID = queue_map.legacy_queue_id
JOIN staff_id_map staff_map ON wqs.USERNAME = staff_map.legacy_username;

DO $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM ihss_org.case_queue_participant;
    RAISE NOTICE 'Migrated % queue participants', v_count;
END $$;

-- ============================================================================
-- MIGRATION 8: CODE TABLES (CODETABLEHEADER, CODETABLEITEM)
-- ============================================================================

-- Migrate code table headers
INSERT INTO cmips_ref.code_table_header (
    code_table_id,
    table_name,
    default_code,
    time_entered,
    created_by,
    created_at,
    version_number,
    legacy_table_name
)
SELECT 
    gen_random_uuid(),
    cth.TABLENAME,
    cth.DEFAULTCODE,
    cth.TIMEENTERED,
    'MIGRATION',
    NOW(),
    COALESCE(cth.VERSIONNO, 1),
    cth.TABLENAME
FROM CODETABLEHEADER cth;

-- Create code table mapping
CREATE TEMP TABLE code_table_map AS
SELECT 
    cth.TABLENAME as legacy_table_name,
    ct.code_table_id as new_table_id
FROM CODETABLEHEADER cth
JOIN cmips_ref.code_table_header ct ON ct.legacy_table_name = cth.TABLENAME;

-- Migrate code table items
INSERT INTO cmips_ref.code_table_item (
    code_item_id,
    code_table_id,
    code_value,
    enabled,
    start_date,
    end_date,
    record_status,
    display_name,
    description,
    display_order,
    created_by,
    created_at,
    version_number
)
SELECT 
    gen_random_uuid(),
    
    -- Table reference
    ct_map.new_table_id,
    
    -- Code value
    cti.CODE,
    
    -- Enabled
    CASE WHEN cti.ENABLED = '1' THEN TRUE ELSE FALSE END,
    
    -- Dates
    cti.STARTDATE,
    cti.ENDDATE,
    
    -- Status
    CASE cti.RECORDSTATUS
        WHEN 'NORMAL' THEN 'NORMAL'::cmips_ref.record_status_enum
        WHEN 'OBSOLETE' THEN 'OBSOLETE'::cmips_ref.record_status_enum
        WHEN 'SUSPENDED' THEN 'SUSPENDED'::cmips_ref.record_status_enum
        ELSE 'NORMAL'::cmips_ref.record_status_enum
    END,
    
    -- Display
    NULL, -- Will need to get from CTDISPLAYNAME
    NULL,
    cti.DISPLAYORDER,
    
    -- Audit
    'MIGRATION',
    NOW(),
    1
    
FROM CODETABLEITEM cti
JOIN code_table_map ct_map ON cti.TABLENAME = ct_map.legacy_table_name;

DO $$
DECLARE
    v_header_count INTEGER;
    v_item_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_header_count FROM cmips_ref.code_table_header;
    SELECT COUNT(*) INTO v_item_count FROM cmips_ref.code_table_item;
    RAISE NOTICE 'Migrated % code table headers and % items', v_header_count, v_item_count;
END $$;

-- ============================================================================
-- POST-MIGRATION VALIDATION
-- ============================================================================

-- Validate migration counts
DO $$
DECLARE
    v_source_org_units INTEGER;
    v_target_org_units INTEGER;
    v_source_positions INTEGER;
    v_target_positions INTEGER;
    v_source_users INTEGER;
    v_target_users INTEGER;
    v_source_queues INTEGER;
    v_target_queues INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_source_org_units FROM ORGANISATIONUNIT WHERE RECORDSTATUS != 'OBSOLETE';
    SELECT COUNT(*) INTO v_target_org_units FROM ihss_org.program_unit WHERE deleted_at IS NULL;
    
    SELECT COUNT(*) INTO v_source_positions FROM POSITION WHERE RECORDSTATUS != 'OBSOLETE';
    SELECT COUNT(*) INTO v_target_positions FROM ihss_org.staffing_position WHERE deleted_at IS NULL;
    
    SELECT COUNT(*) INTO v_source_users FROM USERS;
    SELECT COUNT(*) INTO v_target_users FROM ihss_org.staff_account WHERE deleted_at IS NULL;
    
    SELECT COUNT(*) INTO v_source_queues FROM WORKQUEUE;
    SELECT COUNT(*) INTO v_target_queues FROM ihss_org.case_queue WHERE deleted_at IS NULL;
    
    RAISE NOTICE '=== MIGRATION VALIDATION ===';
    RAISE NOTICE 'Organizational Units: % (source) -> % (target)', v_source_org_units, v_target_org_units;
    RAISE NOTICE 'Positions: % (source) -> % (target)', v_source_positions, v_target_positions;
    RAISE NOTICE 'Users/Staff: % (source) -> % (target)', v_source_users, v_target_users;
    RAISE NOTICE 'Queues: % (source) -> % (target)', v_source_queues, v_target_queues;
    
    IF v_source_org_units != v_target_org_units THEN
        RAISE WARNING 'Organization unit count mismatch!';
    END IF;
    
    IF v_source_positions != v_target_positions THEN
        RAISE WARNING 'Position count mismatch!';
    END IF;
    
    IF v_source_users != v_target_users THEN
        RAISE WARNING 'User count mismatch!';
    END IF;
END $$;

-- ============================================================================
-- CLEANUP TEMPORARY TABLES
-- ============================================================================

DROP TABLE IF EXISTS org_unit_id_map;
DROP TABLE IF EXISTS job_id_map;
DROP TABLE IF EXISTS position_id_map;
DROP TABLE IF EXISTS staff_id_map;
DROP TABLE IF EXISTS queue_id_map;
DROP TABLE IF EXISTS code_table_map;

-- ============================================================================
-- END OF MIGRATION SCRIPTS
-- ============================================================================

-- Final success message
DO $$
BEGIN
    RAISE NOTICE '=================================================';
    RAISE NOTICE 'DATA MIGRATION COMPLETED SUCCESSFULLY';
    RAISE NOTICE '=================================================';
    RAISE NOTICE 'Please review validation warnings above';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Verify data integrity';
    RAISE NOTICE '2. Update foreign key relationships';
    RAISE NOTICE '3. Migrate remaining domain tables';
    RAISE NOTICE '4. Run application smoke tests';
    RAISE NOTICE '=================================================';
END $$;
