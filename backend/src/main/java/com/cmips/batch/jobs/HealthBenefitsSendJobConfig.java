package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.entity.ProviderEntity;
import com.cmips.repository.ProviderRepository;
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
 * Spring Batch Job Configuration for HEALTH_BENEFITS_SEND (HBM Outbound).
 *
 * DSD Section 15: Monthly (~13th of each month) send provider data to
 * Health Benefits Managers (HBMs). Transmits 200,000-400,000 records containing
 * provider ID, demographics, and hours paid/authorized for the last 3-month period.
 *
 * Data Flow:
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 * |  Our Database   |--->|  This Job       |--->|  Integration    |--->|  HBM Systems    |
 * |  (Providers +   |    |  (Extract &     |    |  Hub (SFTP)     |    |  (Process       |
 * |   Auth Hours)   |    |   Generate)     |    |                 |    |   Benefits)     |
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 *
 * Job Flow:
 * Step 1: Query eligible providers with authorized hours
 * Step 2: Generate HBM send file (mock)
 * Step 3: Send to HBMs via SFTP (mock)
 * Step 4: Summary with record counts
 *
 * Legacy Reference: HBM monthly outbound provider file
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HealthBenefitsSendJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ProviderRepository providerRepository;

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean(name = "healthBenefitsSendJob")
    public Job healthBenefitsSendJob() {
        return new JobBuilder("HEALTH_BENEFITS_SEND_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(hbmQueryEligibleProvidersStep())
                .next(hbmGenerateSendFileStep())
                .next(hbmSendToHbmSftpStep())
                .next(hbmSendSummaryStep())
                .build();
    }

    // ==========================================
    // STEP 1: Query Eligible Providers
    // ==========================================

    @Bean
    public Step hbmQueryEligibleProvidersStep() {
        return new StepBuilder("HBM_QUERY_ELIGIBLE_PROVIDERS", jobRepository)
                .tasklet(hbmQueryEligibleProvidersTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet hbmQueryEligibleProvidersTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[HEALTH_BENEFITS_SEND] Step 1 - Querying eligible providers with authorized hours...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();
            executionContext.put("totalSteps", 4);

            LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
            AtomicInteger eligibleCount = new AtomicInteger(0);

            // Query all active providers - in production, filter by authorized hours in last 3 months
            var allProviders = providerRepository.findAll();
            allProviders.forEach(provider -> {
                // In production: check provider has authorized hours within last 3-month period
                if (provider.getProviderStatus() == ProviderEntity.ProviderStatus.ACTIVE) {
                    eligibleCount.incrementAndGet();
                }
            });

            // Mock: simulate 200K-400K records for DSD compliance
            int mockRecordCount = Math.max(eligibleCount.get(), 250000);
            log.info("[HEALTH_BENEFITS_SEND] Found {} active providers in DB, simulating {} HBM records",
                    eligibleCount.get(), mockRecordCount);
            log.info("[HEALTH_BENEFITS_SEND] Reporting period: {} to {}", threeMonthsAgo, LocalDate.now());

            executionContext.put("eligibleProviderCount", eligibleCount.get());
            executionContext.put("mockRecordCount", mockRecordCount);
            executionContext.put("reportPeriodStart", threeMonthsAgo.toString());
            executionContext.put("reportPeriodEnd", LocalDate.now().toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate HBM Send File
    // ==========================================

    @Bean
    public Step hbmGenerateSendFileStep() {
        return new StepBuilder("HBM_GENERATE_SEND_FILE", jobRepository)
                .tasklet(hbmGenerateSendFileTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet hbmGenerateSendFileTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[HEALTH_BENEFITS_SEND] Step 2 - Generating HBM send file...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            int mockRecordCount = (int) executionContext.get("mockRecordCount");
            String reportPeriodStart = (String) executionContext.get("reportPeriodStart");
            String reportPeriodEnd = (String) executionContext.get("reportPeriodEnd");

            // In production: Integration Hub would generate the HBM file with:
            // - Provider ID, name, address, SSN
            // - Hours paid last 3 months
            // - Hours authorized last 3 months
            // - County code, case number
            String mockFileName = String.format("HBM_SEND_%s_%s.dat",
                    LocalDate.now().toString().replace("-", ""),
                    LocalDateTime.now().getHour());

            log.info("[HEALTH_BENEFITS_SEND] Generated file: {} with {} records", mockFileName, mockRecordCount);
            log.info("[HEALTH_BENEFITS_SEND] File contains provider data for period {} to {}",
                    reportPeriodStart, reportPeriodEnd);

            executionContext.put("generatedFileName", mockFileName);
            executionContext.put("fileRecordCount", mockRecordCount);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Send to HBMs via SFTP
    // ==========================================

    @Bean
    public Step hbmSendToHbmSftpStep() {
        return new StepBuilder("HBM_SEND_TO_HBM_SFTP", jobRepository)
                .tasklet(hbmSendToHbmSftpTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet hbmSendToHbmSftpTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[HEALTH_BENEFITS_SEND] Step 3 - Sending file to HBMs via SFTP...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            String fileName = (String) executionContext.get("generatedFileName");
            int recordCount = (int) executionContext.get("fileRecordCount");

            // In production: Integration Hub SftpClient sends file to HBM endpoints
            // bawFileService.sendOutboundFile("HBM", "HEALTH_BENEFITS_SEND", records);
            log.info("[HEALTH_BENEFITS_SEND] Mock SFTP transfer: {} ({} records) -> HBM endpoint",
                    fileName, recordCount);
            log.info("[HEALTH_BENEFITS_SEND] In production: Integration Hub SFTP would handle file transfer");

            executionContext.put("sftpStatus", "SUCCESS");
            executionContext.put("transferTimestamp", LocalDateTime.now().toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Summary
    // ==========================================

    @Bean
    public Step hbmSendSummaryStep() {
        return new StepBuilder("HBM_SEND_SUMMARY", jobRepository)
                .tasklet(hbmSendSummaryTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet hbmSendSummaryTasklet() {
        return (contribution, chunkContext) -> {
            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            int eligibleCount = (int) executionContext.get("eligibleProviderCount");
            int mockRecordCount = (int) executionContext.get("mockRecordCount");
            String fileName = (String) executionContext.get("generatedFileName");
            String sftpStatus = (String) executionContext.get("sftpStatus");
            String reportPeriodStart = (String) executionContext.get("reportPeriodStart");
            String reportPeriodEnd = (String) executionContext.get("reportPeriodEnd");

            log.info("================================================");
            log.info("  HEALTH_BENEFITS_SEND_JOB COMPLETED");
            log.info("  DSD Section 15 - Monthly HBM Outbound");
            log.info("================================================");
            log.info("  Reporting Period: {} to {}", reportPeriodStart, reportPeriodEnd);
            log.info("  Eligible Providers (DB): {}", eligibleCount);
            log.info("  Total Records Sent: {}", mockRecordCount);
            log.info("  File: {}", fileName);
            log.info("  SFTP Status: {}", sftpStatus);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
