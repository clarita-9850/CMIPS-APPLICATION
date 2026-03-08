package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.RecipientRepository;
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
 * Spring Batch Job Configuration for SCI_DAILY_UPDATE.
 *
 * DSD Section 20: Nightly - SCI (Statewide Client Index) daily update to CDSS.
 * Collects all case and recipient changes from the current day and transmits
 * them to CDSS for the statewide client index. This ensures CDSS has current
 * data for cross-county lookups and statewide reporting.
 *
 * Data Flow:
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 * |  Our Database   |--->|  This Job       |--->|  Integration    |--->|  CDSS           |
 * |  (Daily Case/   |    |  (Collect &     |    |  Hub (SFTP)     |    |  (Statewide     |
 * |   Recip Changes)|    |   Generate)     |    |                 |    |   Client Index) |
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 *
 * Job Flow:
 * Step 1: Collect daily case/recipient changes
 * Step 2: Generate SCI update file (mock)
 * Step 3: Send to CDSS (mock)
 * Step 4: Summary
 *
 * Legacy Reference: SCI daily update interface to CDSS
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SCIDailyUpdateJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final CaseRepository caseRepository;
    private final RecipientRepository recipientRepository;

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean(name = "sciDailyUpdateJob")
    public Job sciDailyUpdateJob() {
        return new JobBuilder("SCI_DAILY_UPDATE_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(sciCollectDailyChangesStep())
                .next(sciGenerateUpdateFileStep())
                .next(sciSendToCdssStep())
                .next(sciDailyUpdateSummaryStep())
                .build();
    }

    // ==========================================
    // STEP 1: Collect Daily Changes
    // ==========================================

    @Bean
    public Step sciCollectDailyChangesStep() {
        return new StepBuilder("SCI_COLLECT_DAILY_CHANGES", jobRepository)
                .tasklet(sciCollectDailyChangesTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet sciCollectDailyChangesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SCI_DAILY_UPDATE] Step 1 - Collecting daily case/recipient changes...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();
            executionContext.put("totalSteps", 4);

            LocalDate today = LocalDate.now();
            AtomicInteger caseChanges = new AtomicInteger(0);
            AtomicInteger recipientChanges = new AtomicInteger(0);

            // In production: query cases and recipients modified today
            // SELECT * FROM cases WHERE updated_at >= :todayStart AND updated_at < :tomorrowStart
            // SELECT * FROM recipients WHERE updated_at >= :todayStart AND updated_at < :tomorrowStart
            var allCases = caseRepository.findAll();
            allCases.forEach(c -> {
                // Mock: count all cases as if they were changed today
                // In production: filter by updatedAt date
                caseChanges.incrementAndGet();
            });

            var allRecipients = recipientRepository.findAll();
            allRecipients.forEach(r -> {
                // Mock: count all recipients
                recipientChanges.incrementAndGet();
            });

            log.info("[SCI_DAILY_UPDATE] Date: {}", today);
            log.info("[SCI_DAILY_UPDATE] Case changes today: {}", caseChanges.get());
            log.info("[SCI_DAILY_UPDATE] Recipient changes today: {}", recipientChanges.get());

            int totalChanges = caseChanges.get() + recipientChanges.get();
            executionContext.put("caseChangeCount", caseChanges.get());
            executionContext.put("recipientChangeCount", recipientChanges.get());
            executionContext.put("totalChangeCount", totalChanges);
            executionContext.put("reportDate", today.toString());

            if (totalChanges == 0) {
                log.info("[SCI_DAILY_UPDATE] No changes to report today");
                executionContext.put("skipProcessing", true);
            } else {
                executionContext.put("skipProcessing", false);
            }

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate SCI Update File
    // ==========================================

    @Bean
    public Step sciGenerateUpdateFileStep() {
        return new StepBuilder("SCI_GENERATE_UPDATE_FILE", jobRepository)
                .tasklet(sciGenerateUpdateFileTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet sciGenerateUpdateFileTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SCI_DAILY_UPDATE] Step 2 - Generating SCI update file...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("[SCI_DAILY_UPDATE] Skipping file generation - no changes");
                return RepeatStatus.FINISHED;
            }

            int totalChanges = (int) executionContext.get("totalChangeCount");
            String reportDate = (String) executionContext.get("reportDate");

            // In production: Integration Hub generates SCI-format file
            // File contains: recipient CIN, name, DOB, SSN, county, case status, address
            // Format follows CDSS SCI specification
            String mockFileName = String.format("SCI_UPDATE_%s.dat",
                    reportDate.replace("-", ""));

            log.info("[SCI_DAILY_UPDATE] In production: Integration Hub would generate SCI-format file");
            log.info("[SCI_DAILY_UPDATE] Generated file: {} with {} change records", mockFileName, totalChanges);

            executionContext.put("sciFileName", mockFileName);
            executionContext.put("fileRecordCount", totalChanges);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Send to CDSS
    // ==========================================

    @Bean
    public Step sciSendToCdssStep() {
        return new StepBuilder("SCI_SEND_TO_CDSS", jobRepository)
                .tasklet(sciSendToCdssTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet sciSendToCdssTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SCI_DAILY_UPDATE] Step 3 - Sending SCI update to CDSS...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("[SCI_DAILY_UPDATE] Skipping send - no changes");
                return RepeatStatus.FINISHED;
            }

            String fileName = (String) executionContext.get("sciFileName");
            int recordCount = (int) executionContext.get("fileRecordCount");

            // In production: Integration Hub SftpClient sends to CDSS endpoint
            // bawFileService.sendOutboundFile("CDSS", "SCI_DAILY_UPDATE", records);
            log.info("[SCI_DAILY_UPDATE] Mock SFTP transfer: {} ({} records) -> CDSS SCI endpoint",
                    fileName, recordCount);
            log.info("[SCI_DAILY_UPDATE] In production: Integration Hub SFTP would handle file transfer to CDSS");

            executionContext.put("sftpStatus", "SUCCESS");
            executionContext.put("transferTimestamp", LocalDateTime.now().toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Summary
    // ==========================================

    @Bean
    public Step sciDailyUpdateSummaryStep() {
        return new StepBuilder("SCI_DAILY_UPDATE_SUMMARY", jobRepository)
                .tasklet(sciDailyUpdateSummaryTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet sciDailyUpdateSummaryTasklet() {
        return (contribution, chunkContext) -> {
            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            String reportDate = (String) executionContext.get("reportDate");
            int caseChanges = (int) executionContext.get("caseChangeCount");
            int recipientChanges = (int) executionContext.get("recipientChangeCount");
            int totalChanges = (int) executionContext.get("totalChangeCount");
            Boolean skipped = (Boolean) executionContext.get("skipProcessing");

            log.info("================================================");
            log.info("  SCI_DAILY_UPDATE_JOB COMPLETED");
            log.info("  DSD Section 20 - Nightly SCI Update to CDSS");
            log.info("================================================");
            log.info("  Report Date: {}", reportDate);
            if (Boolean.TRUE.equals(skipped)) {
                log.info("  Status: SKIPPED (no changes)");
            } else {
                log.info("  Status: SENT");
                log.info("------------------------------------------------");
                log.info("  Change Statistics:");
                log.info("    Case Changes:      {}", caseChanges);
                log.info("    Recipient Changes: {}", recipientChanges);
                log.info("    Total Records:     {}", totalChanges);
                String sftpStatus = (String) executionContext.get("sftpStatus");
                log.info("  SFTP Status: {}", sftpStatus);
            }
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
