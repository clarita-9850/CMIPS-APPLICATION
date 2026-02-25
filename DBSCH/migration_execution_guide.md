# IHSS CMIPS Database Migration - Execution Guide

## Overview

This document provides step-by-step instructions for migrating from legacy Curam DB2 to modern PostgreSQL using the IHSS-optimized schema design.

---

## 📋 Pre-Migration Checklist

### Environment Setup
- [ ] PostgreSQL 15+ server provisioned and accessible
- [ ] Legacy Curam DB2 database accessible (read-only recommended)
- [ ] Migration server with sufficient disk space (3x source database size)
- [ ] Network connectivity between legacy and target databases
- [ ] Backup strategy confirmed for both databases
- [ ] Rollback plan documented and tested

### Required Tools
- [ ] PostgreSQL client (`psql`) installed
- [ ] `pg_dump` and `pg_restore` available
- [ ] Data migration tool installed (`pgLoader` or custom scripts)
- [ ] Foreign Data Wrapper extension available (optional)
- [ ] Database monitoring tools configured

### Access & Permissions
- [ ] PostgreSQL superuser access for DDL operations
- [ ] Application role accounts created (`cmips_readonly`, `cmips_readwrite`, `cmips_admin`)
- [ ] Legacy database read access confirmed
- [ ] Network firewall rules configured

---

## 🚀 Migration Execution Steps

### Phase 1: Schema Creation (Est. 2-4 hours)

#### Step 1.1: Create PostgreSQL Database
```bash
# Connect to PostgreSQL server
psql -h postgres-host -U postgres

# Create database
CREATE DATABASE ihss_cmips
  WITH OWNER = postgres
  ENCODING = 'UTF8'
  LC_COLLATE = 'en_US.UTF-8'
  LC_CTYPE = 'en_US.UTF-8'
  TEMPLATE = template0;

# Connect to new database
\c ihss_cmips
```

#### Step 1.2: Execute DDL Script
```bash
# Run the complete DDL
psql -h postgres-host -U postgres -d ihss_cmips -f ihss_postgresql_complete_ddl.sql

# Verify schema creation
psql -h postgres-host -U postgres -d ihss_cmips -c "\dn"  # List schemas
psql -h postgres-host -U postgres -d ihss_cmips -c "\dt ihss_org.*"  # List org tables
```

#### Step 1.3: Validate Schema
```sql
-- Verify all schemas exist
SELECT schema_name 
FROM information_schema.schemata 
WHERE schema_name LIKE 'ihss_%' OR schema_name LIKE 'cmips_%';

-- Verify key tables exist
SELECT schemaname, tablename, tableowner
FROM pg_tables 
WHERE schemaname IN ('ihss_org', 'cmips_ref', 'cmips_audit')
ORDER BY schemaname, tablename;

-- Verify foreign keys
SELECT 
    tc.table_schema, 
    tc.table_name, 
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu 
  ON tc.constraint_name = ccu.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema IN ('ihss_org', 'cmips_ref')
ORDER BY tc.table_schema, tc.table_name;
```

---

### Phase 2: Data Extract (Est. 4-8 hours)

#### Step 2.1: Extract Legacy Data

**Option A: Direct Export (Small Databases < 10GB)**
```bash
# Export specific tables from DB2 to CSV
db2 "EXPORT TO organisationunit.csv OF DEL 
     SELECT * FROM ORGANISATIONUNIT WHERE RECORDSTATUS != 'OBSOLETE'"

db2 "EXPORT TO position.csv OF DEL 
     SELECT * FROM POSITION WHERE RECORDSTATUS != 'OBSOLETE'"

db2 "EXPORT TO users.csv OF DEL 
     SELECT * FROM USERS"

db2 "EXPORT TO workqueue.csv OF DEL 
     SELECT * FROM WORKQUEUE"

# Additional tables...
```

**Option B: Foreign Data Wrapper (Recommended for Gradual Migration)**
```sql
-- Setup FDW (run on PostgreSQL server)
CREATE EXTENSION postgres_fdw;

CREATE SERVER legacy_curam
  FOREIGN DATA WRAPPER postgres_fdw
  OPTIONS (host 'db2-host', dbname 'curam', port '50000');

CREATE USER MAPPING FOR postgres
  SERVER legacy_curam
  OPTIONS (user 'curam_user', password 'secure_password');

-- Import foreign schema
IMPORT FOREIGN SCHEMA public
  LIMIT TO (
    ORGANISATIONUNIT, ORGANISATIONSTRUCTURE,
    POSITION, POSITIONHOLDERLINK, POSITIONORGANISATIONUNITLINK,
    USERS, WORKQUEUE, WORKQUEUESUBSCRIPTION,
    JOB, CODETABLEHEADER, CODETABLEITEM
  )
  FROM SERVER legacy_curam
  INTO public;

-- Verify access
SELECT COUNT(*) FROM public.ORGANISATIONUNIT;
SELECT COUNT(*) FROM public.USERS;
```

**Option C: pgLoader (Fast Bulk Migration)**
```bash
# Create pgLoader config file
cat > curam_to_postgres.load << 'EOF'
LOAD DATABASE
  FROM db2://curam_user:password@db2-host:50000/curam
  INTO postgresql://postgres:password@postgres-host:5432/ihss_cmips

WITH include drop, create tables, create indexes, reset sequences,
     workers = 8, concurrency = 4

SET PostgreSQL PARAMETERS
  maintenance_work_mem to '1GB',
  work_mem to '256MB'

CAST type int to integer drop typemod,
     type bigint to bigint drop typemod

BEFORE LOAD DO
  $$ CREATE SCHEMA IF NOT EXISTS legacy; $$

AFTER LOAD DO
  $$ SELECT 'Migration completed'; $$

INCLUDING ONLY TABLE NAMES MATCHING 
  'ORGANISATION%', 'POSITION%', 'USERS', 'WORKQUEUE%', 
  'JOB', 'CODETABLE%'
INTO SCHEMA 'legacy';

EOF

# Run pgLoader
pgloader curam_to_postgres.load
```

---

### Phase 3: Data Transformation & Loading (Est. 8-16 hours)

#### Step 3.1: Prepare for Migration
```sql
-- Disable triggers temporarily for faster loading
ALTER TABLE ihss_org.program_unit DISABLE TRIGGER ALL;
ALTER TABLE ihss_org.job_classification DISABLE TRIGGER ALL;
ALTER TABLE ihss_org.staffing_position DISABLE TRIGGER ALL;
ALTER TABLE ihss_org.staff_account DISABLE TRIGGER ALL;
ALTER TABLE ihss_org.staffing_assignment DISABLE TRIGGER ALL;
ALTER TABLE ihss_org.case_queue DISABLE TRIGGER ALL;

-- Set work_mem for better performance
SET work_mem = '256MB';
SET maintenance_work_mem = '1GB';
```

#### Step 3.2: Execute Migration Scripts
```bash
# Run the main migration script
psql -h postgres-host -U postgres -d ihss_cmips -f data_migration_scripts.sql

# Monitor progress
tail -f /var/log/postgresql/postgresql-*.log
```

#### Step 3.3: Re-enable Triggers
```sql
-- Re-enable triggers
ALTER TABLE ihss_org.program_unit ENABLE TRIGGER ALL;
ALTER TABLE ihss_org.job_classification ENABLE TRIGGER ALL;
ALTER TABLE ihss_org.staffing_position ENABLE TRIGGER ALL;
ALTER TABLE ihss_org.staff_account ENABLE TRIGGER ALL;
ALTER TABLE ihss_org.staffing_assignment ENABLE TRIGGER ALL;
ALTER TABLE ihss_org.case_queue ENABLE TRIGGER ALL;
```

---

### Phase 4: Data Validation (Est. 4-6 hours)

#### Step 4.1: Row Count Validation
```sql
-- Create validation report
CREATE TEMP TABLE migration_validation AS
SELECT 
    'program_unit' as table_name,
    (SELECT COUNT(*) FROM ORGANISATIONUNIT WHERE RECORDSTATUS != 'OBSOLETE') as source_count,
    (SELECT COUNT(*) FROM ihss_org.program_unit WHERE deleted_at IS NULL) as target_count
UNION ALL
SELECT 
    'staffing_position',
    (SELECT COUNT(*) FROM POSITION WHERE RECORDSTATUS != 'OBSOLETE'),
    (SELECT COUNT(*) FROM ihss_org.staffing_position WHERE deleted_at IS NULL)
UNION ALL
SELECT 
    'staff_account',
    (SELECT COUNT(*) FROM USERS),
    (SELECT COUNT(*) FROM ihss_org.staff_account WHERE deleted_at IS NULL)
UNION ALL
SELECT 
    'case_queue',
    (SELECT COUNT(*) FROM WORKQUEUE),
    (SELECT COUNT(*) FROM ihss_org.case_queue WHERE deleted_at IS NULL);

-- View results
SELECT 
    table_name,
    source_count,
    target_count,
    target_count - source_count as difference,
    ROUND(target_count::numeric / NULLIF(source_count, 0) * 100, 2) as match_percentage
FROM migration_validation
ORDER BY table_name;
```

#### Step 4.2: Data Integrity Validation
```sql
-- Check for orphaned records
SELECT 'Orphaned Positions' as issue, COUNT(*) as count
FROM ihss_org.staffing_position sp
WHERE NOT EXISTS (
    SELECT 1 FROM ihss_org.program_unit pu 
    WHERE pu.program_unit_id = sp.program_unit_id
)
UNION ALL
SELECT 'Orphaned Assignments', COUNT(*)
FROM ihss_org.staffing_assignment sa
WHERE NOT EXISTS (
    SELECT 1 FROM ihss_org.staffing_position sp 
    WHERE sp.staffing_position_id = sa.staffing_position_id
)
UNION ALL
SELECT 'Orphaned Queue Participants', COUNT(*)
FROM ihss_org.case_queue_participant cqp
WHERE cqp.staff_id IS NOT NULL 
  AND NOT EXISTS (
    SELECT 1 FROM ihss_org.staff_account sa 
    WHERE sa.staff_id = cqp.staff_id
  );

-- Check for NULL required fields
SELECT 'Program Units with NULL unit_code' as issue, COUNT(*) as count
FROM ihss_org.program_unit
WHERE unit_code IS NULL
UNION ALL
SELECT 'Staff Accounts with NULL username', COUNT(*)
FROM ihss_org.staff_account
WHERE username IS NULL
UNION ALL
SELECT 'Positions with NULL job_classification', COUNT(*)
FROM ihss_org.staffing_position
WHERE job_classification_id IS NULL;
```

#### Step 4.3: Sample Data Verification
```sql
-- Compare sample records
SELECT 
    'Source' as db,
    ou.ORGANISATIONUNITID as id,
    ou.NAME as name,
    ou.STATUSCODE as status
FROM ORGANISATIONUNIT ou
WHERE ou.ORGANISATIONUNITID IN (
    SELECT ORGANISATIONUNITID FROM ORGANISATIONUNIT LIMIT 5
)
UNION ALL
SELECT 
    'Target' as db,
    pu.legacy_org_unit_id as id,
    pu.unit_name as name,
    pu.status::text as status
FROM ihss_org.program_unit pu
WHERE pu.legacy_org_unit_id IN (
    SELECT ORGANISATIONUNITID FROM ORGANISATIONUNIT LIMIT 5
)
ORDER BY id, db;
```

---

### Phase 5: Index & Statistics (Est. 2-4 hours)

#### Step 5.1: Analyze Tables
```sql
-- Update statistics for all tables
ANALYZE ihss_org.program_unit;
ANALYZE ihss_org.job_classification;
ANALYZE ihss_org.staffing_position;
ANALYZE ihss_org.staff_account;
ANALYZE ihss_org.staffing_assignment;
ANALYZE ihss_org.case_queue;
ANALYZE ihss_org.case_queue_participant;
ANALYZE ihss_org.case_routing_target;
ANALYZE cmips_ref.code_table_header;
ANALYZE cmips_ref.code_table_item;

-- Verify index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan as index_scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes
WHERE schemaname IN ('ihss_org', 'cmips_ref')
ORDER BY schemaname, tablename, indexname;
```

#### Step 5.2: Vacuum Full
```bash
# Perform vacuum full to reclaim space and optimize
vacuumdb -h postgres-host -U postgres -d ihss_cmips --full --analyze --verbose
```

---

### Phase 6: Application Testing (Est. 16-24 hours)

#### Step 6.1: Unit Testing
- Test organizational hierarchy queries
- Test staff assignment lookups
- Test queue participant queries
- Test code table retrievals

#### Step 6.2: Integration Testing
- Test case assignment workflows
- Test user authentication flows
- Test reporting queries
- Test batch processes

#### Step 6.3: Performance Testing
```sql
-- Test common queries
EXPLAIN ANALYZE
SELECT pu.unit_name, COUNT(sp.staffing_position_id) as position_count
FROM ihss_org.program_unit pu
LEFT JOIN ihss_org.staffing_position sp ON sp.program_unit_id = pu.program_unit_id
WHERE pu.deleted_at IS NULL AND pu.status = 'ACTIVE'
GROUP BY pu.unit_name;

EXPLAIN ANALYZE
SELECT sa.full_name, sp.position_name, sas.assignment_type
FROM ihss_org.staff_account sa
JOIN ihss_org.staffing_assignment sas ON sas.staff_id = sa.staff_id
JOIN ihss_org.staffing_position sp ON sp.staffing_position_id = sas.staffing_position_id
WHERE sa.employment_status = 'ACTIVE' 
  AND sa.deleted_at IS NULL
  AND (sas.assignment_end IS NULL OR sas.assignment_end > CURRENT_DATE);
```

---

### Phase 7: Cutover Preparation (Est. 4-8 hours)

#### Step 7.1: Final Data Sync
```bash
# If using incremental migration, run final sync
psql -h postgres-host -U postgres -d ihss_cmips -f data_migration_scripts.sql
```

#### Step 7.2: Application Configuration
- Update application connection strings
- Update ORM mappings if needed
- Update stored procedure calls
- Update report queries

#### Step 7.3: Monitoring Setup
```sql
-- Enable query logging for initial period
ALTER SYSTEM SET log_min_duration_statement = 1000; -- Log queries > 1s
ALTER SYSTEM SET log_statement = 'mod'; -- Log modifications
SELECT pg_reload_conf();
```

---

## 🔄 Rollback Procedure

### In Case of Critical Issues

```bash
# 1. Stop application
systemctl stop cmips-app

# 2. Restore from backup
pg_restore -h postgres-host -U postgres -d ihss_cmips -c backup.dump

# 3. Point application back to legacy DB
# Update connection string in app config

# 4. Restart application
systemctl start cmips-app

# 5. Document issues for analysis
```

---

## 📊 Post-Migration Monitoring

### First 24 Hours
- Monitor query performance (pg_stat_statements)
- Watch for connection pool saturation
- Track error logs
- Verify batch job execution
- Monitor disk I/O

### First Week
- Review slow query log
- Check for table bloat
- Validate backup completion
- Monitor replication lag (if applicable)
- Track application errors

### First Month
- Analyze index usage
- Review partition performance
- Optimize based on actual workload
- Plan for long-term capacity

---

## 📝 Migration Checklist Summary

### Pre-Migration
- [ ] Schema DDL executed successfully
- [ ] Legacy data accessible
- [ ] Migration scripts validated
- [ ] Rollback plan documented
- [ ] Team trained on new schema

### Migration
- [ ] Data extracted from legacy system
- [ ] Transformation scripts executed
- [ ] Data loaded to target database
- [ ] Row counts validated
- [ ] Data integrity checks passed
- [ ] Sample verification completed

### Post-Migration
- [ ] Indexes analyzed and optimized
- [ ] Statistics updated
- [ ] Application tested end-to-end
- [ ] Performance benchmarks met
- [ ] Monitoring configured
- [ ] Documentation updated
- [ ] Team handoff completed

---

## 🆘 Troubleshooting

### Common Issues

**Issue: Foreign key violations during migration**
```sql
-- Temporarily disable foreign keys
SET session_replication_role = replica;
-- Run migration
-- Re-enable
SET session_replication_role = DEFAULT;
```

**Issue: Slow INSERT performance**
```sql
-- Increase work memory
SET work_mem = '512MB';
-- Disable autovacuum during migration
ALTER TABLE ihss_org.program_unit SET (autovacuum_enabled = false);
-- Re-enable after migration
ALTER TABLE ihss_org.program_unit SET (autovacuum_enabled = true);
```

**Issue: Character encoding problems**
```bash
# Specify encoding when importing
psql -h postgres-host -U postgres -d ihss_cmips --set=client_encoding=UTF8 -f script.sql
```

---

## 📞 Support Contacts

- **Database Team Lead**: [Contact Info]
- **Application Team Lead**: [Contact Info]
- **Infrastructure Team**: [Contact Info]
- **Business Owner**: [Contact Info]

---

## 📚 Related Documents

- `ihss_postgresql_complete_ddl.sql` - Complete schema DDL
- `data_migration_scripts.sql` - Data transformation scripts
- `curam_to_postgresql_modernization_analysis.md` - Detailed analysis
- `migration_quick_reference.md` - Quick reference guide

---

**Migration Timeline Summary**
- Phase 1 (Schema): 2-4 hours
- Phase 2 (Extract): 4-8 hours
- Phase 3 (Transform/Load): 8-16 hours
- Phase 4 (Validation): 4-6 hours
- Phase 5 (Optimization): 2-4 hours
- Phase 6 (Testing): 16-24 hours
- Phase 7 (Cutover): 4-8 hours

**Total Estimated Time**: 40-70 hours (1-2 weeks with proper preparation)

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Next Review**: After migration completion
