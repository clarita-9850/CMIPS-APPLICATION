package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
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
 * Spring Batch Job Configuration for HEALTH_BENEFITS_RECEIVE (HBM Inbound).
 *
 * DSD Section 15: Monthly - load HBM deduction files containing ~100K records.
 * Processes health benefit deduction data returned from Health Benefits Managers,
 * archives prior month records, loads new deductions, and purges old data.
 *
 * Data Flow:
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 * |  HBM Systems    |--->|  Integration    |--->|  This Job       |--->|  Our Database   |
 * |  (Deduction     |    |  Hub (SFTP)     |    |  (Parse &       |    |  (HBM Deduction |
 * |   Files)        |    |                 |    |   Load)         |    |   Records)      |
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 *
 * Job Flow:
 * Step 1: Fetch inbound HBM file (mock)
 * Step 2: Archive prior month records to history
 * Step 3: Load new deduction records
 * Step 4: Purge records older than 1 year
 * Step 5: Summary
 *
 * Legacy Reference: HBM monthly inbound deduction file
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HealthBenefitsReceiveJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean(name = "healthBenefitsReceiveJob")
    public Job healthBenefitsReceiveJob() {
        return new JobBuilder("HEALTH_BENEFITS_RECEIVE_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(hbmFetchInboundFileStep())
                .next(hbmArchivePriorMonthStep())
                .next(hbmLoadNewDeductionsStep())
                .next(hbmPurgeOldRecordsStep())
                .next(hbmReceiveSummaryStep())
                .build();
    }

    // ==========================================
    // STEP 1: Fetch Inbound HBM File
    // ==========================================

    @Bean
    public Step hbmFetchInboundFileStep() {
        return new StepBuilder("HBM_FETCH_INBOUND_FILE", jobRepository)
                .tasklet(hbmFetchInboundFileTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet hbmFetchInboundFileTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[HEALTH_BENEFITS_RECEIVE] Step 1 - Fetching inbound HBM deduction file...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();
            executionContext.put("totalSteps", 5);

            // In production: Integration Hub SftpClient fetches file from HBM endpoint
            // List<DeductionRecord> records = bawFileService.receiveInboundFile("HBM", "DEDUCTIONS");
            String mockFileName = String.format("HBM_DEDUCTIONS_%s.dat",
                    LocalDate.now().toString().replace("-", ""));
            int mockRecordCount = 98500; // ~100K records per DSD

            log.info("[HEALTH_BENEFITS_RECEIVE] In production: Integration Hub SFTP would fetch file from HBM");
            log.info("[HEALTH_BENEFITS_RECEIVE] Mock file received: {} with {} deduction records",
                    mockFileName, mockRecordCount);

            executionContext.put("inboundFileName", mockFileName);
            executionContext.put("inboundRecordCount", mockRecordCount);
            executionContext.put("fetchTimestamp", LocalDateTime.now().toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Archive Prior Month Records
    // ==========================================

    @Bean
    public Step hbmArchivePriorMonthStep() {
        return new StepBuilder("HBM_ARCHIVE_PRIOR_MONTH", jobRepository)
                .tasklet(hbmArchivePriorMonthTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet hbmArchivePriorMonthTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[HEALTH_BENEFITS_RECEIVE] Step 2 - Archiving prior month deduction records to history...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            LocalDate priorMonth = LocalDate.now().minusMonths(1);
            AtomicInteger archivedCount = new AtomicInteger(0);

            // In production: move current deduction records to history table
            // UPDATE hbm_deductions SET archived = true, archive_date = NOW()
            //   WHERE deduction_month = :priorMonth AND archived = false;
            int mockArchivedCount = 95200;
            archivedCount.set(mockArchivedCount);

            log.info("[HEALTH_BENEFITS_RECEIVE] Archived {} records from {} to history table",
                    archivedCount.get(), priorMonth);

            executionContext.put("archivedCount", archivedCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Load New Deduction Records
    // ==========================================

    @Bean
    public Step hbmLoadNewDeductionsStep() {
        return new StepBuilder("HBM_LOAD_NEW_DEDUCTIONS", jobRepository)
                .tasklet(hbmLoadNewDeductionsTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet hbmLoadNewDeductionsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[HEALTH_BENEFITS_RECEIVE] Step 3 - Loading new deduction records...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            int inboundRecordCount = (int) executionContext.get("inboundRecordCount");
            AtomicInteger loadedCount = new AtomicInteger(0);
            AtomicInteger rejectedCount = new AtomicInteger(0);

            // In production: parse HBM file and insert deduction records
            // Each record contains: provider ID, deduction type, amount, effective date, HBM code
            // Validate provider exists, deduction type is valid, amount is reasonable
            int mockLoaded = inboundRecordCount - 150; // Small number of rejects
            int mockRejected = 150;
            loadedCount.set(mockLoaded);
            rejectedCount.set(mockRejected);

            log.info("[HEALTH_BENEFITS_RECEIVE] Loaded {} new deduction records, rejected {}",
                    loadedCount.get(), rejectedCount.get());
            if (rejectedCount.get() > 0) {
                log.warn("[HEALTH_BENEFITS_RECEIVE] {} records rejected - provider not found or invalid deduction type",
                        rejectedCount.get());
            }

            executionContext.put("loadedCount", loadedCount.get());
            executionContext.put("rejectedCount", rejectedCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Purge Records Older Than 1 Year
    // ==========================================

    @Bean
    public Step hbmPurgeOldRecordsStep() {
        return new StepBuilder("HBM_PURGE_OLD_RECORDS", jobRepository)
                .tasklet(hbmPurgeOldRecordsTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet hbmPurgeOldRecordsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[HEALTH_BENEFITS_RECEIVE] Step 4 - Purging deduction records older than 1 year...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            LocalDate oneYearAgo = LocalDate.now().minusYears(1);
            AtomicInteger purgedCount = new AtomicInteger(0);

            // In production: DELETE FROM hbm_deductions_history
            //   WHERE archive_date < :oneYearAgo;
            int mockPurgedCount = 87000;
            purgedCount.set(mockPurgedCount);

            log.info("[HEALTH_BENEFITS_RECEIVE] Purged {} archived records older than {}",
                    purgedCount.get(), oneYearAgo);

            executionContext.put("purgedCount", purgedCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 5: Summary
    // ==========================================

    @Bean
    public Step hbmReceiveSummaryStep() {
        return new StepBuilder("HBM_RECEIVE_SUMMARY", jobRepository)
                .tasklet(hbmReceiveSummaryTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet hbmReceiveSummaryTasklet() {
        return (contribution, chunkContext) -> {
            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            String fileName = (String) executionContext.get("inboundFileName");
            int inboundCount = (int) executionContext.get("inboundRecordCount");
            int archivedCount = (int) executionContext.get("archivedCount");
            int loadedCount = (int) executionContext.get("loadedCount");
            int rejectedCount = (int) executionContext.get("rejectedCount");
            int purgedCount = (int) executionContext.get("purgedCount");

            log.info("================================================");
            log.info("  HEALTH_BENEFITS_RECEIVE_JOB COMPLETED");
            log.info("  DSD Section 15 - Monthly HBM Inbound");
            log.info("================================================");
            log.info("  Inbound File: {}", fileName);
            log.info("  Inbound Records: {}", inboundCount);
            log.info("------------------------------------------------");
            log.info("  Processing Statistics:");
            log.info("    Prior Month Archived: {}", archivedCount);
            log.info("    New Records Loaded:   {}", loadedCount);
            log.info("    Records Rejected:     {}", rejectedCount);
            log.info("    Old Records Purged:   {}", purgedCount);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
