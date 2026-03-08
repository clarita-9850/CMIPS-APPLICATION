-- ============================================================================
-- V8: Seed remaining job definitions that were previously @Scheduled in-process
-- Completes the 100% AutoSys replacement — ALL scheduling now in scheduler app
-- ============================================================================

-- IDs 30-43 (continuing from V7 which ended at 29)

INSERT INTO job_definition (id, job_name, job_type, description, cron_expression, timezone, status, enabled, priority, max_retries, timeout_seconds, job_parameters, target_roles, target_counties, created_at, updated_at, created_by, updated_by)
VALUES

-- --------------------------------------------------------------------------
-- Case Maintenance (DSD Section 30/31) — formerly CaseMaintenanceBatchJob
-- --------------------------------------------------------------------------

(30, 'CASE_MAINTENANCE_DAILY_JOB', 'MAINTENANCE',
 'Case Maintenance Daily - CM-007/008/013/015/016/026/027/029/046/053/071/075 triggers',
 '0 0 6 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 7, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(31, 'CASE_MAINTENANCE_MONTHLY_JOB', 'MAINTENANCE',
 'Case Maintenance Monthly - CM-014 (age 18) and CM-020 (age 14) triggers',
 '0 0 5 1 * ?', 'America/Los_Angeles', 'ACTIVE', true, 6, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(32, 'NIGHTLY_BATCH_PRINT_JOB', 'REPORT',
 'Nightly Batch Print - DSD Section 31: print pending NOAs and electronic forms',
 '0 30 2 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 7, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

-- --------------------------------------------------------------------------
-- Data Retention — formerly DataRetentionService @Scheduled methods
-- --------------------------------------------------------------------------

(33, 'DATA_RETENTION_DAILY_JOB', 'MAINTENANCE',
 'Data Retention Daily - purge read notifications older than 90 days',
 '0 0 2 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 4, 3, 1800,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(34, 'DATA_RETENTION_WEEKLY_JOB', 'MAINTENANCE',
 'Data Retention Weekly - purge cancelled case notes (5yr), old timesheets (7yr), inactive person notes (5yr)',
 '0 10 2 * * SUN', 'America/Los_Angeles', 'ACTIVE', true, 5, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(35, 'DATA_RETENTION_MONTHLY_AUDIT_JOB', 'MAINTENANCE',
 'Data Retention Monthly Audit - flag closed cases exceeding 7yr retention for archival',
 '0 30 2 1 * ?', 'America/Los_Angeles', 'ACTIVE', true, 5, 3, 1800,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

-- --------------------------------------------------------------------------
-- Scheduled Reports — formerly ScheduledReportService @Scheduled methods
-- --------------------------------------------------------------------------

(36, 'SCHEDULED_REPORT_DAILY_JOB', 'REPORT',
 'Scheduled Report Daily - generate daily reports for all roles',
 '0 30 5 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 5, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(37, 'SCHEDULED_REPORT_WEEKLY_JOB', 'REPORT',
 'Scheduled Report Weekly - generate weekly summary reports (Monday)',
 '0 30 5 * * MON', 'America/Los_Angeles', 'ACTIVE', true, 5, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(38, 'SCHEDULED_REPORT_MONTHLY_JOB', 'REPORT',
 'Scheduled Report Monthly - generate monthly reports for all roles (1st of month)',
 '0 30 5 1 * ?', 'America/Los_Angeles', 'ACTIVE', true, 5, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(39, 'SCHEDULED_REPORT_QUARTERLY_JOB', 'REPORT',
 'Scheduled Report Quarterly - generate quarterly summary reports (Jan/Apr/Jul/Oct)',
 '0 30 5 1 1,4,7,10 ?', 'America/Los_Angeles', 'ACTIVE', true, 5, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(40, 'SCHEDULED_REPORT_YEARLY_JOB', 'REPORT',
 'Scheduled Report Yearly - generate annual reports (January 1)',
 '0 30 5 1 1 ?', 'America/Los_Angeles', 'ACTIVE', true, 5, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(41, 'COUNTY_REPORT_SCHEDULER_JOB', 'REPORT',
 'County Report Scheduler - generate county-specific batch reports (every 5 minutes during business hours)',
 '0 */5 * * * ?', 'America/Los_Angeles', 'ACTIVE', true, 3, 3, 600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

-- --------------------------------------------------------------------------
-- Task Management — formerly DeferredTaskRestartJob + TaskEscalationJob
-- --------------------------------------------------------------------------

(42, 'DEFERRED_TASK_RESTART_JOB', 'MAINTENANCE',
 'Deferred Task Restart - restart tasks that have reached their restart date (hourly)',
 '0 0 * * * ?', 'America/Los_Angeles', 'ACTIVE', true, 4, 3, 600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(43, 'TASK_ESCALATION_JOB', 'MAINTENANCE',
 'Task Escalation - check overdue tasks and escalate/auto-close (every 15 minutes)',
 '0 */15 * * * ?', 'America/Los_Angeles', 'ACTIVE', true, 4, 3, 600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

-- Advance the sequence past our explicit IDs
SELECT setval('job_definition_id_seq', 43, true);


-- ============================================================================
-- Additional dependencies
-- ============================================================================

INSERT INTO job_dependency (job_id, depends_on_job_id, dependency_type, is_active, created_at, updated_at, created_by)
VALUES
-- Nightly Batch Print runs after the Forms Print Batch job
(32, 25, 'SUCCESS', true, NOW(), NOW(), 'SYSTEM'),

-- Data Retention Weekly runs after Data Retention Daily (same night)
(34, 33, 'COMPLETION', true, NOW(), NOW(), 'SYSTEM'),

-- Data Retention Monthly Audit runs after Data Retention Weekly on 1st-of-month Sundays
(35, 34, 'COMPLETION', true, NOW(), NOW(), 'SYSTEM');
