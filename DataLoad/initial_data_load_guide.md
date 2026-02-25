# Initial Data Load Guide for IHSS Modernized Database

## Overview

This document describes the initial data load process for the IHSS modernized PostgreSQL database, converting data from Curam DMX files to the new schema.

---

## Files Included

1. **initial_data_inserts.sql** (450KB, 27,000+ lines)
   - Contains INSERT statements for initial organizational data
   - Auto-generated from DMX files
   - Includes temporary mapping tables for legacy ID tracking

---

## Data Summary

### Successfully Loaded

| Table | Records | Source File | Status |
|-------|---------|-------------|---------|
| ihss_org.program_unit | 80 | ORGANISATIONUNIT.dmx | ✅ Complete |
| ihss_org.job_classification | 25 | JOB.dmx | ✅ Complete |
| ihss_org.staff_account | 149 | USERS.dmx | ✅ Complete |
| ihss_org.case_queue | 706 | WORKQUEUE.dmx | ✅ Complete |

### Sample Data Breakdown

**Program Units (80 records)**
- California IHSS Counties: 50+ counties
- District Offices: 20+ districts
- Field Offices: Various

**Job Classifications (25 records)**
- Social Worker levels (I, II, III)
- Program Supervisors
- Eligibility Specialists
- Administrative positions

**Staff Accounts (149 users)**
- Active staff members across all counties
- Includes login preferences and authentication settings
- Mapped to organizational units

**Case Queues (706 queues)**
- County-specific intake queues
- Review queues
- Backlog queues
- Specialized processing queues

---

## Data Conversion Logic

### 1. Program Units (ORGANISATIONUNIT → program_unit)

**Mapping Rules:**
```
Legacy Status → Modern Status
├─ OUSC1, RST1 → ACTIVE
├─ SUSPENDED → INACTIVE
└─ OBSOLETE → CLOSED (excluded)

Business Type → Unit Type
├─ OUBTC100 → COUNTY
├─ OUBTC102 → DISTRICT
├─ Name contains "FIELD" → FIELD_OFFICE
├─ Name contains "TEAM" → TEAM
└─ Default → FIELD_OFFICE
```

**Key Fields:**
- `unit_code`: Generated as 'ORG-{legacy_id}'
- `legacy_org_unit_id`: Preserved for reference
- `effective_start`: From creation_date
- UUID generated for new primary key

### 2. Job Classifications (JOB → job_classification)

**Mapping Rules:**
```
Fields Mapped:
├─ jobID → legacy_job_id
├─ name → title
├─ jobFamily → classification_family
├─ gradeLevel → grade_level
└─ description → description
```

**Key Fields:**
- `classification_code`: Generated as 'JOB-{legacy_id}'
- UUID generated for new primary key

### 3. Staff Accounts (USERS → staff_account)

**Mapping Rules:**
```
Status Code → Employment Status
├─ ACTIVE, USR1 → ACTIVE
├─ SUSPENDED → ON_LEAVE
└─ Other → SEPARATED

Account Enabled:
├─ '1' → TRUE
└─ '0' → FALSE
```

**Key Fields:**
- `staff_identifier`: Same as username
- `username`: Preserved from legacy
- `legacy_username`: Preserved for reference
- Full login preferences migrated

### 4. Case Queues (WORKQUEUE → case_queue)

**Mapping Rules:**
```
Fields Mapped:
├─ workQueueID → legacy_workqueue_id
├─ name → queue_name
├─ allowUserSubscriptionInd → allow_user_subscription
├─ sensitivity → sensitivity
└─ comments → comments
```

**Key Fields:**
- `queue_code`: Generated as 'QUEUE-{legacy_id}'
- All queues marked as `is_active = TRUE`

---

## Installation Instructions

### Prerequisites

1. PostgreSQL 15+ installed and running
2. IHSS schema created (run `ihss_postgresql_complete_ddl.sql` first)
3. Sufficient database permissions
4. At least 500MB free disk space

### Step-by-Step Installation

#### Step 1: Prepare Database
```bash
# Connect to PostgreSQL
psql -h localhost -U postgres -d ihss_cmips

# Verify schemas exist
\dn
```

Expected output should show:
- `ihss_org`
- `cmips_ref`
- `cmips_audit`
- etc.

#### Step 2: Load Initial Data
```bash
# Execute the initial data load script
psql -h localhost -U postgres -d ihss_cmips -f initial_data_inserts.sql
```

#### Step 3: Verify Data Load
```sql
-- Check record counts
SELECT 'program_unit' as table_name, COUNT(*) as records 
FROM ihss_org.program_unit WHERE deleted_at IS NULL
UNION ALL
SELECT 'job_classification', COUNT(*) 
FROM ihss_org.job_classification WHERE deleted_at IS NULL
UNION ALL
SELECT 'staff_account', COUNT(*) 
FROM ihss_org.staff_account WHERE deleted_at IS NULL
UNION ALL
SELECT 'case_queue', COUNT(*) 
FROM ihss_org.case_queue WHERE deleted_at IS NULL;
```

Expected output:
```
   table_name       | records
-------------------+---------
 program_unit      |      80
 job_classification|      25
 staff_account     |     149
 case_queue        |     706
```

#### Step 4: Verify Legacy Mappings
```sql
-- Check that legacy IDs are preserved
SELECT 
    COUNT(*) as total_units,
    COUNT(legacy_org_unit_id) as with_legacy_id
FROM ihss_org.program_unit;

-- Should show all records have legacy IDs
```

#### Step 5: Check Organizational Hierarchy
```sql
-- View sample organizational units
SELECT 
    unit_code,
    unit_name,
    unit_type,
    status,
    effective_start
FROM ihss_org.program_unit
WHERE unit_type = 'COUNTY'
ORDER BY unit_name
LIMIT 10;
```

---

## Post-Load Tasks

### 1. Update Parent Relationships

After loading program units, update the hierarchical parent relationships:

```sql
-- This requires ORGANISATIONSTRUCTURE.dmx data
-- To be implemented in Phase 2
UPDATE ihss_org.program_unit pu
SET parent_unit_id = (
    SELECT new_uuid 
    FROM temp_org_unit_mapping 
    WHERE legacy_id = -- parent_id from org_structure
)
WHERE legacy_org_unit_id = -- child_id from org_structure;
```

### 2. Load Additional Tables

The following tables still need to be loaded from DMX files:

**Phase 2 - Relationships:**
- [ ] POSITIONHOLDERLINK → staffing_assignment
- [ ] POSITIONLOCATIONLINK → (updates to staffing_position)
- [ ] WORKQUEUESUBSCRIPTION → case_queue_participant
- [ ] ORGANISATIONSTRUCTURE → (updates parent_unit_id)

**Phase 3 - Reference Data:**
- [ ] CODE TABLE data
- [ ] LOCATION data
- [ ] SECURITY data

**Phase 4 - Case Data:**
- [ ] Case/Client data (when ready)
- [ ] Provider data
- [ ] Evidence data

### 3. Create Indexes

After bulk loading, recreate indexes for performance:

```sql
-- Analyze tables
ANALYZE ihss_org.program_unit;
ANALYZE ihss_org.job_classification;
ANALYZE ihss_org.staff_account;
ANALYZE ihss_org.case_queue;

-- Check index usage
SELECT 
    schemaname,
    tablename,
    indexname
FROM pg_indexes
WHERE schemaname = 'ihss_org'
ORDER BY tablename, indexname;
```

---

## Validation Queries

### Count Verification
```sql
-- Compare with source DMX files
SELECT 
    'ORGANISATIONUNIT' as source_table,
    (SELECT COUNT(*) FROM ihss_org.program_unit) as loaded_count,
    80 as expected_count;
```

### Data Quality Checks
```sql
-- Check for NULL required fields
SELECT 'Units with NULL unit_code' as issue, COUNT(*) as count
FROM ihss_org.program_unit
WHERE unit_code IS NULL
UNION ALL
SELECT 'Staff with NULL username', COUNT(*)
FROM ihss_org.staff_account
WHERE username IS NULL
UNION ALL
SELECT 'Queues with NULL queue_code', COUNT(*)
FROM ihss_org.case_queue
WHERE queue_code IS NULL;

-- Should return 0 for all
```

### Sample Data Review
```sql
-- View a sample of each table
SELECT * FROM ihss_org.program_unit LIMIT 5;
SELECT * FROM ihss_org.job_classification LIMIT 5;
SELECT * FROM ihss_org.staff_account LIMIT 5;
SELECT * FROM ihss_org.case_queue LIMIT 5;
```

---

## Troubleshooting

### Issue: Duplicate Key Violations

**Symptom:** Error about duplicate key values

**Solution:**
```sql
-- Check for duplicates before loading
SELECT unit_code, COUNT(*) 
FROM ihss_org.program_unit 
GROUP BY unit_code 
HAVING COUNT(*) > 1;

-- If found, truncate and reload
TRUNCATE ihss_org.program_unit CASCADE;
-- Then re-run initial_data_inserts.sql
```

### Issue: Foreign Key Violations

**Symptom:** Foreign key constraint errors

**Solution:**
```sql
-- Disable triggers temporarily
ALTER TABLE ihss_org.staffing_position DISABLE TRIGGER ALL;
-- Load data
-- Re-enable triggers
ALTER TABLE ihss_org.staffing_position ENABLE TRIGGER ALL;
```

### Issue: Timestamp Format Errors

**Symptom:** Invalid timestamp format

**Check:**
```sql
-- Review timestamp conversion
SELECT 
    legacy_org_unit_id,
    created_at,
    updated_at
FROM ihss_org.program_unit
WHERE created_at IS NULL OR updated_at IS NULL;
```

---

## Performance Considerations

### Loading Time
- **Expected Duration:** 2-5 minutes for ~1,000 records
- **Bottlenecks:** Index updates, constraint checking
- **Optimization:** Disable triggers during bulk load

### Disk Space
- **Initial Data:** ~50MB
- **Indexes:** ~20MB
- **Total:** ~70MB for initial load

### Memory Requirements
- **Recommended work_mem:** 64MB
- **Recommended maintenance_work_mem:** 256MB

```sql
-- Set for session
SET work_mem = '64MB';
SET maintenance_work_mem = '256MB';
```

---

## Next Steps

1. ✅ Load initial organizational data (COMPLETE)
2. ⏳ Load POSITION data and create staffing_position records
3. ⏳ Load POSITIONHOLDERLINK and create staffing_assignment records
4. ⏳ Update parent_unit_id relationships from ORGANISATIONSTRUCTURE
5. ⏳ Load WORKQUEUESUBSCRIPTION and create case_queue_participant records
6. ⏳ Load reference/code table data
7. ⏳ Begin case and provider data migration

---

## DMX Files Reference

### Available DMX Files (120+ files)
The initial.zip contains DMX files for all Curam tables. Key files for next phases:

**Organizational:**
- ✅ ORGANISATIONUNIT.dmx (loaded)
- ⏳ ORGANISATIONSTRUCTURE.dmx (for parent relationships)
- ⏳ ORGUNITPOSITIONLINK.dmx

**Position/Staff:**
- ✅ JOB.dmx (loaded)
- ⏳ POSITION.dmx (ready to load)
- ⏳ POSITIONHOLDERLINK.dmx
- ⏳ POSITIONLOCATIONLINK.dmx
- ✅ USERS.dmx (loaded)

**Work Queues:**
- ✅ WORKQUEUE.dmx (loaded)
- ⏳ WORKQUEUESUBSCRIPTION.dmx
- ⏳ WORKQUEUEMAPPING.dmx

**Reference Data:**
- ⏳ CODE TABLE files
- ⏳ LOCATION.dmx
- ⏳ SECURITY files

---

## Support & Contact

For questions or issues with the initial data load:
- Review this guide
- Check validation queries
- Consult the main migration guide
- Review DMX parser script for data transformation logic

---

**Document Version:** 1.0  
**Generated:** February 11, 2026  
**Last Updated:** February 11, 2026
