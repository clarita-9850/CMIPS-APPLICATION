-- ============================================================================
-- V7: Seed all AutoSys job definitions, dependencies, and CA holiday calendar
-- ============================================================================

-- ============================================================================
-- 1. JOB DEFINITIONS (29 total)
-- ============================================================================

-- --------------------------------------------------------------------------
-- Existing CMIPS jobs (IDs 1-9)
-- --------------------------------------------------------------------------

INSERT INTO job_definition (id, job_name, job_type, description, cron_expression, timezone, status, enabled, priority, max_retries, timeout_seconds, job_parameters, target_roles, target_counties, created_at, updated_at, created_by, updated_by)
VALUES
(1, 'paymentFileGenerationJob', 'PAYROLL',
 'Payment File Generation - bi-weekly payroll file creation',
 '0 0 2 1,16 * ?', 'America/Los_Angeles', 'ACTIVE', true, 8, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(2, 'sickLeaveAccrualJob', 'PAYROLL',
 'Sick Leave Accrual - annual accrual reset on July 1',
 '0 0 3 1 7 ?', 'America/Los_Angeles', 'ACTIVE', true, 6, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(3, 'timesheetSummaryReportJob', 'REPORT',
 'Timesheet Summary Report - nightly timesheet aggregation',
 '0 0 6 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 5, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(4, 'SSN_VERIFICATION_JOB', 'SYNC',
 'SSN Verification - weekly SSA number verification batch',
 '0 0 0 ? * MON', 'America/Los_Angeles', 'ACTIVE', true, 7, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(5, 'countyDailyReportJob', 'REPORT',
 'County Daily Report - nightly county-level summary report',
 '0 0 7 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 5, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(6, 'largeFileProcessingJob', 'ETL',
 'Large File Processing - manual trigger for bulk file imports',
 NULL, 'America/Los_Angeles', 'ACTIVE', false, 4, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(7, 'providerAttachmentArchivalJob', 'MAINTENANCE',
 'Provider Attachment Archival - monthly archive of old provider documents',
 '0 0 4 1 * ?', 'America/Los_Angeles', 'ACTIVE', true, 3, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(8, 'warrantStatusUpdateJob', 'PAYROLL',
 'Warrant Status Update - daily warrant reconciliation with SCO',
 '0 0 5 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 8, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(9, 'computeIntensiveFileJob', 'ETL',
 'Compute Intensive File Job - manual trigger for heavy computation tasks',
 NULL, 'America/Los_Angeles', 'ACTIVE', false, 4, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

-- --------------------------------------------------------------------------
-- Tax & EDD jobs (IDs 10-17)
-- --------------------------------------------------------------------------

(10, 'DE34_NEW_HIRE_REPORT_JOB', 'TAX',
 'DE-34 New Hire Report to EDD - bi-weekly report of newly hired/rehired providers',
 '0 0 2 1,16 * ?', 'America/Los_Angeles', 'ACTIVE', true, 8, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(11, 'DE88_EMPLOYER_TAX_REPORT_JOB', 'TAX',
 'DE-88 Employer Tax Report - quarterly SDI, UI, ETT to EDD',
 '0 0 3 1 1,4,7,10 ?', 'America/Los_Angeles', 'ACTIVE', true, 8, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(12, 'DE1_REGISTRATION_JOB', 'TAX',
 'DE-1 Provider EDD Registration - weekly request + response processing',
 '0 0 4 ? * FRI', 'America/Los_Angeles', 'ACTIVE', true, 7, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(13, 'DE9_WAGE_REPORT_JOB', 'TAX',
 'DE-9/DE-9C Quarterly Wage Reports to EDD',
 '0 0 3 15 1,4,7,10 ?', 'America/Los_Angeles', 'ACTIVE', true, 8, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(14, 'EDD_STATEMENT_PROCESS_JOB', 'TAX',
 'EDD Statement Processing - transform positional to CSV',
 '0 0 2 1 * ?', 'America/Los_Angeles', 'ACTIVE', true, 6, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(15, 'W2C_YEAR_END_JOB', 'TAX',
 'W-2C Year-End Processing - annual wage correction filing',
 '0 0 2 15 1 ?', 'America/Los_Angeles', 'ACTIVE', true, 9, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(16, 'UI_RATE_REQUEST_JOB', 'TAX',
 'UI Rate Request/Response - quarterly tax rate updates',
 '0 0 4 1 1,4,7,10 ?', 'America/Los_Angeles', 'ACTIVE', true, 7, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(17, 'SEIN_INACTIVATION_JOB', 'TAX',
 'SEIN Inactivation - quarterly employer account cleanup',
 '0 0 5 1 1,4,7,10 ?', 'America/Los_Angeles', 'ACTIVE', true, 6, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

-- --------------------------------------------------------------------------
-- Health Benefits (IDs 18-19)
-- --------------------------------------------------------------------------

(18, 'HEALTH_BENEFITS_SEND_JOB', 'HEALTH',
 'Health Benefits Send - monthly provider data to HBMs (200K-400K records)',
 '0 0 1 13 * ?', 'America/Los_Angeles', 'ACTIVE', true, 8, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(19, 'HEALTH_BENEFITS_RECEIVE_JOB', 'HEALTH',
 'Health Benefits Receive - load HBM deduction files (~100K records)',
 '0 0 2 20 * ?', 'America/Los_Angeles', 'ACTIVE', true, 8, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

-- --------------------------------------------------------------------------
-- Data Retention (IDs 20-21)
-- --------------------------------------------------------------------------

(20, 'DATA_RETENTION_LOGICAL_DELETE_JOB', 'MAINTENANCE',
 'Data Retention Logical Delete - mark expired data (5.5yr) for deletion',
 '0 0 3 1 * ?', 'America/Los_Angeles', 'ACTIVE', true, 6, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(21, 'DATA_RETENTION_PURGE_JOB', 'MAINTENANCE',
 'Data Retention Purge - permanently remove data past 7.5yr threshold',
 '0 0 4 1 * ?', 'America/Los_Angeles', 'ACTIVE', true, 6, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

-- --------------------------------------------------------------------------
-- Nightly Cycle (IDs 22-25)
-- --------------------------------------------------------------------------

(22, 'SCI_DAILY_UPDATE_JOB', 'SYNC',
 'SCI Daily Update - nightly case/recipient changes to CDSS',
 '0 0 0 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 7, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(23, 'SAWS_S2_BATCH_JOB', 'SYNC',
 'SAWS S2 Batch - nightly eligibility interface',
 '0 30 0 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 7, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(24, 'NIGHTLY_REPORTS_GENERATION_JOB', 'REPORT',
 'Nightly Reports Generation - standard reports to county printers/BO',
 '0 0 1 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 6, 3, 7200,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(25, 'FORMS_PRINT_BATCH_JOB', 'REPORT',
 'Forms Print Batch - print queued forms from day',
 '0 0 2 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 6, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

-- --------------------------------------------------------------------------
-- Remaining (IDs 26-29)
-- --------------------------------------------------------------------------

(26, 'TIMESHEET_RETRIEVAL_JOB', 'ETL',
 'Timesheet Retrieval from TPF - process timesheet files throughout the day',
 '0 0 */4 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 8, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(27, 'PERFORMANCE_METRICS_EXPORT_JOB', 'MONITORING',
 'Performance Metrics Export - daily CSV of batch job statistics to reporting DB',
 '0 0 23 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 5, 3, 1800,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(28, 'CASE_COUNT_UPDATE_JOB', 'MAINTENANCE',
 'Case Count Update - nightly worker caseload statistics',
 '0 0 23 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 4, 3, 1800,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

(29, 'HOMEMAKER_TIMESHEET_PROCESS_JOB', 'PAYROLL',
 'Homemaker Timesheet Processing - nightly homemaker timesheet posting',
 '0 30 23 * * ?', 'America/Los_Angeles', 'ACTIVE', true, 6, 3, 3600,
 '{}', '{}', '{}', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

-- Advance the job_definition sequence past our explicit IDs
SELECT setval('job_definition_id_seq', 29, true);


-- ============================================================================
-- 2. JOB DEPENDENCIES
-- ============================================================================

INSERT INTO job_dependency (job_id, depends_on_job_id, dependency_type, is_active, created_at, updated_at, created_by)
VALUES
-- DE-9 Wage Report depends on DE-88 Employer Tax Report completing successfully
(13, 11, 'SUCCESS',    true, NOW(), NOW(), 'SYSTEM'),

-- SAWS S2 Batch depends on SCI Daily Update completing successfully
(23, 22, 'SUCCESS',    true, NOW(), NOW(), 'SYSTEM'),

-- Nightly Reports depends on SAWS S2 Batch completing successfully
(24, 23, 'SUCCESS',    true, NOW(), NOW(), 'SYSTEM'),

-- Forms Print Batch depends on Nightly Reports completing successfully
(25, 24, 'SUCCESS',    true, NOW(), NOW(), 'SYSTEM'),

-- Data Retention Purge depends on Logical Delete completing successfully
(21, 20, 'SUCCESS',    true, NOW(), NOW(), 'SYSTEM'),

-- Timesheet Summary Report depends on Timesheet Retrieval completing (any outcome)
(3,  26, 'COMPLETION', true, NOW(), NOW(), 'SYSTEM'),

-- County Daily Report depends on SCI Daily Update completing (any outcome)
(5,  22, 'COMPLETION', true, NOW(), NOW(), 'SYSTEM'),

-- SEIN Inactivation depends on DE-88 Employer Tax Report completing (any outcome)
(17, 11, 'COMPLETION', true, NOW(), NOW(), 'SYSTEM');


-- ============================================================================
-- 3. CALIFORNIA STATE HOLIDAY CALENDAR
-- ============================================================================

INSERT INTO job_calendar (calendar_name, calendar_type, description, is_active, created_at, created_by)
VALUES
('CA_STATE_HOLIDAYS', 'HOLIDAY', 'California State Holidays - jobs may be deferred', true, NOW(), 'SYSTEM');

-- 2026 California state holidays
INSERT INTO job_calendar_date (calendar_id, calendar_date, description, created_at)
VALUES
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-01-01', 'New Year''s Day',            NOW()),
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-01-19', 'Martin Luther King Jr. Day', NOW()),
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-02-16', 'Presidents'' Day',           NOW()),
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-03-31', 'Cesar Chavez Day',           NOW()),
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-05-25', 'Memorial Day',               NOW()),
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-07-03', 'Independence Day (observed)', NOW()),
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-09-07', 'Labor Day',                  NOW()),
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-11-11', 'Veterans Day',               NOW()),
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-11-26', 'Thanksgiving Day',           NOW()),
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-11-27', 'Day After Thanksgiving',     NOW()),
((SELECT id FROM job_calendar WHERE calendar_name = 'CA_STATE_HOLIDAYS'), '2026-12-25', 'Christmas Day',              NOW());
