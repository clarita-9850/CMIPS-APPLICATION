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
 * Spring Batch Job Configuration for SAWS_S2_BATCH (SAWS S2 Interface).
 *
 * DSD Section 20: Nightly (runs after SCI daily update) - SAWS S2 batch interface.
 * Exchanges eligibility data with the Statewide Automated Welfare System (SAWS).
 * Sends IHSS eligibility updates and processes SAWS responses for Medi-Cal
 * eligibility confirmations.
 *
 * Data Flow:
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 * |  Our Database   |--->|  This Job       |--->|  Integration    |--->|  SAWS           |
 * |  (Eligibility   |    |  (Collect &     |    |  Hub (SFTP)     |    |  (Statewide     |
 * |   Updates)      |    |   Exchange)     |    |                 |    |   Welfare)      |
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 *
 * Job Flow:
 * Step 1: Collect eligibility updates
 * Step 2: Generate SAWS S2 file (mock)
 * Step 3: Process SAWS responses (mock)
 * Step 4: Summary
 *
 * Legacy Reference: SAWS S2 nightly batch interface
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SAWSS2BatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean(name = "sawsS2BatchJob")
    public Job sawsS2BatchJob() {
        return new JobBuilder("SAWS_S2_BATCH_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(sawsCollectEligibilityUpdatesStep())
                .next(sawsGenerateS2FileStep())
                .next(sawsProcessResponsesStep())
                .next(sawsS2SummaryStep())
                .build();
    }

    // ==========================================
    // STEP 1: Collect Eligibility Updates
    // ==========================================

    @Bean
    public Step sawsCollectEligibilityUpdatesStep() {
        return new StepBuilder("SAWS_COLLECT_ELIGIBILITY_UPDATES", jobRepository)
                .tasklet(sawsCollectEligibilityUpdatesTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet sawsCollectEligibilityUpdatesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SAWS_S2_BATCH] Step 1 - Collecting eligibility updates for SAWS S2...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();
            executionContext.put("totalSteps", 4);

            LocalDate today = LocalDate.now();
            AtomicInteger newEligibility = new AtomicInteger(0);
            AtomicInteger changedEligibility = new AtomicInteger(0);
            AtomicInteger terminatedEligibility = new AtomicInteger(0);

            // In production: query eligibility changes since last SAWS S2 run
            // - New IHSS eligibility determinations
            // - Changes to authorized hours, share of cost, waiver programs
            // - Terminated eligibility (case closures, denials)
            // Mock counts for demonstration
            newEligibility.set(45);
            changedEligibility.set(120);
            terminatedEligibility.set(18);

            int totalUpdates = newEligibility.get() + changedEligibility.get() + terminatedEligibility.get();

            log.info("[SAWS_S2_BATCH] Date: {}", today);
            log.info("[SAWS_S2_BATCH] New eligibility records: {}", newEligibility.get());
            log.info("[SAWS_S2_BATCH] Changed eligibility records: {}", changedEligibility.get());
            log.info("[SAWS_S2_BATCH] Terminated eligibility records: {}", terminatedEligibility.get());
            log.info("[SAWS_S2_BATCH] Total updates to send: {}", totalUpdates);

            executionContext.put("newEligibilityCount", newEligibility.get());
            executionContext.put("changedEligibilityCount", changedEligibility.get());
            executionContext.put("terminatedEligibilityCount", terminatedEligibility.get());
            executionContext.put("totalUpdateCount", totalUpdates);
            executionContext.put("reportDate", today.toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate SAWS S2 File
    // ==========================================

    @Bean
    public Step sawsGenerateS2FileStep() {
        return new StepBuilder("SAWS_GENERATE_S2_FILE", jobRepository)
                .tasklet(sawsGenerateS2FileTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet sawsGenerateS2FileTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SAWS_S2_BATCH] Step 2 - Generating SAWS S2 file...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            int totalUpdates = (int) executionContext.get("totalUpdateCount");
            String reportDate = (String) executionContext.get("reportDate");

            // In production: Integration Hub generates SAWS S2-format file
            // File contains: CIN, recipient name, county, eligibility status,
            // authorized hours, share of cost, effective dates, Medi-Cal aid code
            String mockFileName = String.format("SAWS_S2_SEND_%s.dat",
                    reportDate.replace("-", ""));

            log.info("[SAWS_S2_BATCH] In production: Integration Hub would generate SAWS S2-format file");
            log.info("[SAWS_S2_BATCH] Generated file: {} with {} eligibility update records",
                    mockFileName, totalUpdates);

            // In production: Integration Hub SftpClient sends to SAWS endpoint
            // bawFileService.sendOutboundFile("SAWS", "S2_ELIGIBILITY", records);
            log.info("[SAWS_S2_BATCH] In production: Integration Hub SFTP would send file to SAWS");

            executionContext.put("s2SendFileName", mockFileName);
            executionContext.put("s2SendRecordCount", totalUpdates);
            executionContext.put("sendTimestamp", LocalDateTime.now().toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Process SAWS Responses
    // ==========================================

    @Bean
    public Step sawsProcessResponsesStep() {
        return new StepBuilder("SAWS_PROCESS_RESPONSES", jobRepository)
                .tasklet(sawsProcessResponsesTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet sawsProcessResponsesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SAWS_S2_BATCH] Step 3 - Processing SAWS S2 responses...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            AtomicInteger confirmedCount = new AtomicInteger(0);
            AtomicInteger rejectedCount = new AtomicInteger(0);
            AtomicInteger pendingCount = new AtomicInteger(0);

            // In production: fetch SAWS response file from previous day's submission
            // Integration Hub SftpClient receives from SAWS endpoint
            // Response contains: CIN, confirmation status, Medi-Cal eligibility date,
            // aid code confirmation, any error codes
            String mockResponseFile = String.format("SAWS_S2_RESP_%s.dat",
                    LocalDate.now().minusDays(1).toString().replace("-", ""));

            // Mock response processing
            confirmedCount.set(150);
            rejectedCount.set(8);
            pendingCount.set(12);

            log.info("[SAWS_S2_BATCH] In production: Integration Hub SFTP would fetch response from SAWS");
            log.info("[SAWS_S2_BATCH] Response file: {}", mockResponseFile);
            log.info("[SAWS_S2_BATCH] Confirmed: {}, Rejected: {}, Pending: {}",
                    confirmedCount.get(), rejectedCount.get(), pendingCount.get());

            if (rejectedCount.get() > 0) {
                log.warn("[SAWS_S2_BATCH] {} eligibility records rejected by SAWS - review required",
                        rejectedCount.get());
            }

            executionContext.put("responseFileName", mockResponseFile);
            executionContext.put("confirmedCount", confirmedCount.get());
            executionContext.put("rejectedCount", rejectedCount.get());
            executionContext.put("pendingCount", pendingCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Summary
    // ==========================================

    @Bean
    public Step sawsS2SummaryStep() {
        return new StepBuilder("SAWS_S2_SUMMARY", jobRepository)
                .tasklet(sawsS2SummaryTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet sawsS2SummaryTasklet() {
        return (contribution, chunkContext) -> {
            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            String reportDate = (String) executionContext.get("reportDate");
            int newCount = (int) executionContext.get("newEligibilityCount");
            int changedCount = (int) executionContext.get("changedEligibilityCount");
            int terminatedCount = (int) executionContext.get("terminatedEligibilityCount");
            int totalSent = (int) executionContext.get("totalUpdateCount");
            String sendFile = (String) executionContext.get("s2SendFileName");
            String responseFile = (String) executionContext.get("responseFileName");
            int confirmed = (int) executionContext.get("confirmedCount");
            int rejected = (int) executionContext.get("rejectedCount");
            int pending = (int) executionContext.get("pendingCount");

            log.info("================================================");
            log.info("  SAWS_S2_BATCH_JOB COMPLETED");
            log.info("  DSD Section 20 - Nightly SAWS S2 Interface");
            log.info("================================================");
            log.info("  Report Date: {}", reportDate);
            log.info("------------------------------------------------");
            log.info("  Outbound (Send to SAWS):");
            log.info("    File: {}", sendFile);
            log.info("    New Eligibility:        {}", newCount);
            log.info("    Changed Eligibility:    {}", changedCount);
            log.info("    Terminated Eligibility: {}", terminatedCount);
            log.info("    Total Records Sent:     {}", totalSent);
            log.info("------------------------------------------------");
            log.info("  Inbound (SAWS Responses):");
            log.info("    File: {}", responseFile);
            log.info("    Confirmed: {}", confirmed);
            log.info("    Rejected:  {}", rejected);
            log.info("    Pending:   {}", pending);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
