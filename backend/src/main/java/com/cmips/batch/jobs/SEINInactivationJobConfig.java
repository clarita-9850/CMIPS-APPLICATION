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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for SEIN Inactivation.
 *
 * DSD Section 18: Quarterly inactivation of subject employer accounts
 * and handling of delinquent quarterly return forms. Employer accounts
 * that have had no payroll activity for a specified period must be
 * reported to EDD for SEIN inactivation. Also generates delinquency
 * notices for accounts with missing quarterly filings.
 *
 * Data Flow:
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │  Our Database   │───▶│  This Job       │───▶│  Integration    │───▶│      EDD        │
 * │  (Inactive      │    │  (Identify &    │    │  Hub (Format &  │    │  (Inactivate    │
 * │   Employers)    │    │   Process)      │    │   Send SFTP)    │    │   SEIN)         │
 * └─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
 *
 * Schedule: Quarterly (replaces AutoSys schedule)
 * Legacy Reference: SEIN Inactivation and Delinquency Processing
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SEINInactivationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ProviderRepository providerRepository;

    private static final String DESTINATION_SYSTEM = "EDD";
    private static final String FILE_TYPE = "SEIN_INACTIVATION";

    // Inactivity threshold: no payroll for 4 consecutive quarters (1 year)
    private static final int INACTIVITY_THRESHOLD_DAYS = 365;
    // Delinquency threshold: no quarterly filing for 2 consecutive quarters
    private static final int DELINQUENCY_QUARTERS = 2;

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean
    public Job seinInactivationJob() {
        return new JobBuilder("SEIN_INACTIVATION_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(findInactiveEmployersStep())
            .next(markSeinInactiveStep())
            .next(generateDelinquencyNoticesStep())
            .next(seinInactivationLogSummaryStep())
            .build();
    }

    // ==========================================
    // STEP 1: Find Inactive Employers
    // ==========================================

    @Bean
    public Step findInactiveEmployersStep() {
        return new StepBuilder("findInactiveEmployersStep", jobRepository)
            .listener(stepListener)
            .tasklet(findInactiveEmployersTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet findInactiveEmployersTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 1: Finding Inactive Employers ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            executionContext.put("totalSteps", 4);

            LocalDate inactivityCutoff = LocalDate.now().minusDays(INACTIVITY_THRESHOLD_DAYS);

            // Find providers who:
            // 1. Have a SEIN (taxpayerId) assigned
            // 2. Are TERMINATED or ON_LEAVE
            // 3. Have been inactive for longer than the threshold
            List<ProviderEntity> allProviders = providerRepository.findAll();
            List<Long> inactiveIds = new ArrayList<>();
            List<Long> delinquentIds = new ArrayList<>();

            for (ProviderEntity provider : allProviders) {
                if (provider.getTaxpayerId() == null || provider.getTaxpayerId().isBlank()) {
                    continue; // No SEIN to inactivate
                }

                // Check for terminated/inactive providers past the threshold
                if (provider.getProviderStatus() == ProviderEntity.ProviderStatus.TERMINATED
                        && provider.getLeaveTerminationEffectiveDate() != null
                        && provider.getLeaveTerminationEffectiveDate().isBefore(inactivityCutoff)) {
                    inactiveIds.add(provider.getId());
                }

                // Check for on-leave providers past the threshold
                if (provider.getProviderStatus() == ProviderEntity.ProviderStatus.ON_LEAVE
                        && provider.getLeaveTerminationEffectiveDate() != null
                        && provider.getLeaveTerminationEffectiveDate().isBefore(inactivityCutoff)) {
                    inactiveIds.add(provider.getId());
                }

                // Check for active providers with no recent payroll activity
                // (using updatedAt as proxy for last payroll activity)
                if (provider.getProviderStatus() == ProviderEntity.ProviderStatus.ACTIVE
                        && provider.getUpdatedAt() != null
                        && provider.getUpdatedAt().toLocalDate().isBefore(inactivityCutoff)) {
                    delinquentIds.add(provider.getId());
                }
            }

            if (inactiveIds.isEmpty() && delinquentIds.isEmpty()) {
                log.info("No inactive employers or delinquent accounts found");
                executionContext.put("skipProcessing", true);
                executionContext.put("skipReason", "No inactive or delinquent accounts found");
                return RepeatStatus.FINISHED;
            }

            log.info("Found {} inactive employers for SEIN inactivation and {} delinquent accounts",
                inactiveIds.size(), delinquentIds.size());

            executionContext.put("skipProcessing", false);
            executionContext.put("inactiveIds", new ArrayList<>(inactiveIds));
            executionContext.put("delinquentIds", new ArrayList<>(delinquentIds));
            executionContext.put("inactiveCount", inactiveIds.size());
            executionContext.put("delinquentCount", delinquentIds.size());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Mark SEIN Inactive
    // ==========================================

    @Bean
    public Step markSeinInactiveStep() {
        return new StepBuilder("markSeinInactiveStep", jobRepository)
            .listener(stepListener)
            .tasklet(markSeinInactiveTasklet(), transactionManager)
            .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public Tasklet markSeinInactiveTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 2: Marking SEIN Accounts Inactive ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping SEIN inactivation");
                return RepeatStatus.FINISHED;
            }

            List<Long> inactiveIds = (List<Long>) executionContext.get("inactiveIds");
            AtomicInteger inactivatedCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            StringBuilder inactivationContent = new StringBuilder();
            inactivationContent.append(String.format("HDR|SEIN_INACT|%s%n",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));

            for (Long providerId : inactiveIds) {
                try {
                    ProviderEntity provider = providerRepository.findById(providerId).orElse(null);
                    if (provider == null || provider.getTaxpayerId() == null) continue;

                    // In production: would use Integration Hub to send inactivation request to EDD
                    // Record for EDD: SEIN, provider info, termination date, reason
                    inactivationContent.append(String.format("INACT|%s|%s|%s|%s|%s%n",
                        provider.getTaxpayerId(),
                        provider.getProviderNumber(),
                        provider.getLeaveTerminationEffectiveDate() != null
                            ? provider.getLeaveTerminationEffectiveDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                            : "",
                        provider.getProviderStatus(),
                        provider.getTerminationReason() != null ? provider.getTerminationReason() : "INACTIVE"));

                    inactivatedCount.incrementAndGet();
                    log.debug("Marked SEIN {} inactive for provider {}",
                        provider.getTaxpayerId(), provider.getProviderNumber());

                } catch (Exception e) {
                    log.error("Error marking SEIN inactive for provider {}: {}", providerId, e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            inactivationContent.append(String.format("TRL|%d%n", inactivatedCount.get()));

            // In production: would use Integration Hub SFTP to send to EDD
            // bawFileService.sendOutboundFile("EDD", "SEIN_INACTIVATION", records);
            String fileReference = "SEIN-INACT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            log.info("MOCK: Sending SEIN inactivation file {} with {} records to EDD",
                fileReference, inactivatedCount.get());

            executionContext.put("inactivationFileReference", fileReference);
            executionContext.put("inactivatedCount", inactivatedCount.get());
            executionContext.put("inactivationErrorCount", errorCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Generate Delinquency Notices (Mock)
    // ==========================================

    @Bean
    public Step generateDelinquencyNoticesStep() {
        return new StepBuilder("generateDelinquencyNoticesStep", jobRepository)
            .listener(stepListener)
            .tasklet(generateDelinquencyNoticesTasklet(), transactionManager)
            .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public Tasklet generateDelinquencyNoticesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 3: Generating Delinquency Notices ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping delinquency notices");
                return RepeatStatus.FINISHED;
            }

            List<Long> delinquentIds = (List<Long>) executionContext.get("delinquentIds");
            AtomicInteger noticesGenerated = new AtomicInteger(0);
            AtomicInteger noticeErrors = new AtomicInteger(0);

            for (Long providerId : delinquentIds) {
                try {
                    ProviderEntity provider = providerRepository.findById(providerId).orElse(null);
                    if (provider == null) continue;

                    // In production: would generate a delinquency notice document
                    // and queue for mailing or electronic delivery
                    // Notice includes: SEIN, last filing date, delinquent quarters,
                    // penalty amounts, deadline for filing
                    log.debug("Generated delinquency notice for provider {} (SEIN: {})",
                        provider.getProviderNumber(), provider.getTaxpayerId());

                    noticesGenerated.incrementAndGet();
                } catch (Exception e) {
                    log.error("Error generating delinquency notice for provider {}: {}", providerId, e.getMessage());
                    noticeErrors.incrementAndGet();
                }
            }

            log.info("MOCK: Generated {} delinquency notices ({} errors)",
                noticesGenerated.get(), noticeErrors.get());

            executionContext.put("noticesGenerated", noticesGenerated.get());
            executionContext.put("noticeErrors", noticeErrors.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Log Summary
    // ==========================================

    @Bean
    public Step seinInactivationLogSummaryStep() {
        return new StepBuilder("seinInactivationLogSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(seinInactivationLogSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet seinInactivationLogSummaryTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 4: SEIN Inactivation Job Summary ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                String skipReason = (String) executionContext.get("skipReason");
                log.info("================================================");
                log.info("  SEIN_INACTIVATION JOB SKIPPED");
                log.info("  Reason: {}", skipReason);
                log.info("================================================");
                return RepeatStatus.FINISHED;
            }

            String inactivationFileReference = (String) executionContext.get("inactivationFileReference");
            int inactiveCount = (int) executionContext.get("inactiveCount");
            int delinquentCount = (int) executionContext.get("delinquentCount");
            int inactivatedCount = (int) executionContext.get("inactivatedCount");
            int inactivationErrorCount = (int) executionContext.get("inactivationErrorCount");
            int noticesGenerated = (int) executionContext.get("noticesGenerated");
            int noticeErrors = (int) executionContext.get("noticeErrors");

            log.info("================================================");
            log.info("  SEIN_INACTIVATION JOB COMPLETED");
            log.info("================================================");
            log.info("  Inactivation File: {}", inactivationFileReference);
            log.info("  Destination: {} ({})", DESTINATION_SYSTEM, FILE_TYPE);
            log.info("  Inactivity Threshold: {} days", INACTIVITY_THRESHOLD_DAYS);
            log.info("------------------------------------------------");
            log.info("  SEIN Inactivation:");
            log.info("    Inactive Employers Found: {}", inactiveCount);
            log.info("    SEINs Inactivated: {}", inactivatedCount);
            log.info("    Inactivation Errors: {}", inactivationErrorCount);
            log.info("------------------------------------------------");
            log.info("  Delinquency Processing:");
            log.info("    Delinquent Accounts Found: {}", delinquentCount);
            log.info("    Delinquency Notices Generated: {}", noticesGenerated);
            log.info("    Notice Errors: {}", noticeErrors);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
