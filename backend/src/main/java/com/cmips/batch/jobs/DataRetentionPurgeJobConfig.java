package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.repository.CaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for DATA_RETENTION_PURGE.
 *
 * DSD Section 7: Monthly - permanently remove data 7.5 years past expiration.
 * Records that were logically deleted (by DataRetentionLogicalDeleteJob) and have
 * reached the 7.5-year threshold are permanently purged from the database.
 * Audit extracts are created before purge. Files are removed after 9.5 years.
 *
 * Data Flow:
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 * |  Case Table     |--->|  This Job       |--->|  Audit Extract  |--->|  Permanent      |
 * |  (Logically     |    |  (Extract &     |    |  (Archive)      |    |  Removal from   |
 * |   Deleted)      |    |   Purge)        |    |                 |    |  Database)       |
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 *
 * Job Flow:
 * Step 1: Identify logically-deleted records past 7.5 year threshold
 * Step 2: Create audit extracts (mock)
 * Step 3: Purge from database
 * Step 4: Remove files after 9.5 years (mock)
 * Step 5: Summary
 *
 * Legacy Reference: DSD 7.x Data Retention - Physical Purge Phase
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataRetentionPurgeJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final CaseRepository caseRepository;

    private static final double PURGE_THRESHOLD_YEARS = 7.5;
    private static final double FILE_REMOVAL_YEARS = 9.5;

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean(name = "dataRetentionPurgeJob")
    public Job dataRetentionPurgeJob() {
        return new JobBuilder("DATA_RETENTION_PURGE_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(purgeIdentifyEligibleRecordsStep())
                .next(purgeCreateAuditExtractsStep())
                .next(purgeDatabaseRecordsStep())
                .next(purgeRemoveOldFilesStep())
                .next(purgeSummaryStep())
                .build();
    }

    // ==========================================
    // STEP 1: Identify Eligible Records
    // ==========================================

    @Bean
    public Step purgeIdentifyEligibleRecordsStep() {
        return new StepBuilder("PURGE_IDENTIFY_ELIGIBLE_RECORDS", jobRepository)
                .tasklet(purgeIdentifyEligibleRecordsTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet purgeIdentifyEligibleRecordsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[DATA_RETENTION_PURGE] Step 1 - Identifying logically-deleted records past 7.5-year threshold...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();
            executionContext.put("totalSteps", 5);

            // 7.5 years = 7 years + 6 months
            LocalDate purgeCutoff = LocalDate.now().minusYears(7).minusMonths(6);
            AtomicInteger eligibleCount = new AtomicInteger(0);

            // In production: SELECT * FROM cases
            //   WHERE logical_delete_flag = true
            //     AND logical_delete_date <= :purgeCutoff
            //     AND physical_purge_date IS NULL
            var allCases = caseRepository.findAll();
            allCases.forEach(caseEntity -> {
                // Mock: count terminated/denied cases as potential purge candidates
                if (caseEntity.getCaseStatus() != null
                        && ("TERMINATED".equalsIgnoreCase(caseEntity.getCaseStatus().name())
                        || "DENIED".equalsIgnoreCase(caseEntity.getCaseStatus().name()))) {
                    eligibleCount.incrementAndGet();
                }
            });

            log.info("[DATA_RETENTION_PURGE] Purge cutoff date: {} (7.5 years ago)", purgeCutoff);
            log.info("[DATA_RETENTION_PURGE] Found {} records eligible for permanent purge", eligibleCount.get());

            executionContext.put("purgeEligibleCount", eligibleCount.get());
            executionContext.put("purgeCutoffDate", purgeCutoff.toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Create Audit Extracts
    // ==========================================

    @Bean
    public Step purgeCreateAuditExtractsStep() {
        return new StepBuilder("PURGE_CREATE_AUDIT_EXTRACTS", jobRepository)
                .tasklet(purgeCreateAuditExtractsTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet purgeCreateAuditExtractsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[DATA_RETENTION_PURGE] Step 2 - Creating audit extracts before purge...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            int eligibleCount = (int) executionContext.get("purgeEligibleCount");
            AtomicInteger extractCount = new AtomicInteger(0);

            // In production: for each record being purged, create an audit extract containing:
            // - Case number, recipient ID, provider ID
            // - Key dates (application, termination, logical delete)
            // - Financial summary (total payments, share of cost)
            // - Reason for termination
            // Store extracts in audit_retention_extracts table and/or file system
            String mockExtractFile = String.format("PURGE_AUDIT_%s.dat",
                    LocalDate.now().toString().replace("-", ""));
            extractCount.set(eligibleCount);

            log.info("[DATA_RETENTION_PURGE] In production: audit extracts would be written to archive");
            log.info("[DATA_RETENTION_PURGE] Created audit extract: {} with {} records",
                    mockExtractFile, extractCount.get());

            executionContext.put("auditExtractFile", mockExtractFile);
            executionContext.put("auditExtractCount", extractCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Purge from Database
    // ==========================================

    @Bean
    public Step purgeDatabaseRecordsStep() {
        return new StepBuilder("PURGE_DATABASE_RECORDS", jobRepository)
                .tasklet(purgeDatabaseRecordsTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet purgeDatabaseRecordsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[DATA_RETENTION_PURGE] Step 3 - Purging records from database...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            int eligibleCount = (int) executionContext.get("purgeEligibleCount");
            AtomicInteger purgedCases = new AtomicInteger(0);
            AtomicInteger purgedRecipients = new AtomicInteger(0);
            AtomicInteger purgedProviders = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            // In production: cascading delete in correct order:
            // 1. Delete NOA records for case
            // 2. Delete timesheets for case
            // 3. Delete eligibility assessments
            // 4. Delete case-provider associations
            // 5. Delete case-recipient associations
            // 6. Delete case record
            // 7. If no other cases reference recipient/provider, delete those too
            purgedCases.set(eligibleCount);
            purgedRecipients.set((int) (eligibleCount * 0.95)); // Some recipients have multiple cases
            purgedProviders.set((int) (eligibleCount * 0.80));  // Providers often serve multiple cases

            log.info("[DATA_RETENTION_PURGE] Purged {} cases, {} recipients, {} providers",
                    purgedCases.get(), purgedRecipients.get(), purgedProviders.get());
            if (errorCount.get() > 0) {
                log.warn("[DATA_RETENTION_PURGE] {} records failed to purge", errorCount.get());
            }

            executionContext.put("purgedCases", purgedCases.get());
            executionContext.put("purgedRecipients", purgedRecipients.get());
            executionContext.put("purgedProviders", purgedProviders.get());
            executionContext.put("purgeErrorCount", errorCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Remove Files After 9.5 Years
    // ==========================================

    @Bean
    public Step purgeRemoveOldFilesStep() {
        return new StepBuilder("PURGE_REMOVE_OLD_FILES", jobRepository)
                .tasklet(purgeRemoveOldFilesTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet purgeRemoveOldFilesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[DATA_RETENTION_PURGE] Step 4 - Removing archived files older than 9.5 years...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            LocalDate fileRemovalCutoff = LocalDate.now().minusYears(9).minusMonths(6);
            AtomicInteger filesRemoved = new AtomicInteger(0);

            // In production: scan audit extract archive directory for files older than 9.5 years
            // Remove: audit extracts, HBM files, payment files, SCI files, etc.
            // Also clean up Integration Hub archived files
            int mockFilesRemoved = 45;
            filesRemoved.set(mockFilesRemoved);

            log.info("[DATA_RETENTION_PURGE] File removal cutoff: {} (9.5 years ago)", fileRemovalCutoff);
            log.info("[DATA_RETENTION_PURGE] In production: {} archived files would be permanently removed",
                    filesRemoved.get());

            executionContext.put("filesRemoved", filesRemoved.get());
            executionContext.put("fileRemovalCutoff", fileRemovalCutoff.toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 5: Summary
    // ==========================================

    @Bean
    public Step purgeSummaryStep() {
        return new StepBuilder("PURGE_SUMMARY", jobRepository)
                .tasklet(purgeSummaryTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet purgeSummaryTasklet() {
        return (contribution, chunkContext) -> {
            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            String purgeCutoff = (String) executionContext.get("purgeCutoffDate");
            int eligibleCount = (int) executionContext.get("purgeEligibleCount");
            String auditFile = (String) executionContext.get("auditExtractFile");
            int auditCount = (int) executionContext.get("auditExtractCount");
            int purgedCases = (int) executionContext.get("purgedCases");
            int purgedRecipients = (int) executionContext.get("purgedRecipients");
            int purgedProviders = (int) executionContext.get("purgedProviders");
            int errorCount = (int) executionContext.get("purgeErrorCount");
            int filesRemoved = (int) executionContext.get("filesRemoved");
            String fileRemovalCutoff = (String) executionContext.get("fileRemovalCutoff");

            log.info("================================================");
            log.info("  DATA_RETENTION_PURGE_JOB COMPLETED");
            log.info("  DSD Section 7 - Monthly Physical Purge");
            log.info("================================================");
            log.info("  Purge Threshold: {} years", PURGE_THRESHOLD_YEARS);
            log.info("  Purge Cutoff Date: {}", purgeCutoff);
            log.info("------------------------------------------------");
            log.info("  Audit Extracts:");
            log.info("    Extract File: {}", auditFile);
            log.info("    Records Extracted: {}", auditCount);
            log.info("------------------------------------------------");
            log.info("  Database Purge Statistics:");
            log.info("    Eligible Records:    {}", eligibleCount);
            log.info("    Cases Purged:        {}", purgedCases);
            log.info("    Recipients Purged:   {}", purgedRecipients);
            log.info("    Providers Purged:    {}", purgedProviders);
            log.info("    Purge Errors:        {}", errorCount);
            log.info("------------------------------------------------");
            log.info("  File Removal ({} year threshold):", FILE_REMOVAL_YEARS);
            log.info("    File Cutoff Date: {}", fileRemovalCutoff);
            log.info("    Files Removed:    {}", filesRemoved);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
