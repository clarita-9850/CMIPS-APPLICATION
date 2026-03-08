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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for UI Rate Request/Response Processing.
 *
 * DSD Section 18: Quarterly UI (Unemployment Insurance) tax rate requests
 * and responses with EDD. Each employer account is assigned a UI rate based
 * on claims history. This job requests updated rates and processes responses.
 *
 * Data Flow (Outbound - Rate Request):
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │  Our Database   │───▶│  This Job       │───▶│  Integration    │───▶│      EDD        │
 * │  (Accounts      │    │  (Identify &    │    │  Hub (Format &  │    │  (Rate          │
 * │   needing rate) │    │   Generate req) │    │   Send SFTP)    │    │   Lookup)       │
 * └─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
 *
 * Data Flow (Inbound - Rate Response):
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │      EDD        │───▶│  Integration    │───▶│  This Job       │
 * │  (Assigned rate)│    │  Hub (Fetch)    │    │  (Update rates) │
 * └─────────────────┘    └─────────────────┘    └─────────────────┘
 *
 * Schedule: Quarterly (replaces AutoSys schedule)
 * Legacy Reference: EDD UI Rate Request/Response
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class UIRateRequestJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ProviderRepository providerRepository;

    private static final String DESTINATION_SYSTEM = "EDD";
    private static final String FILE_TYPE = "UI_RATE_REQUEST";

    // California UI rate range (2024)
    private static final BigDecimal UI_RATE_MIN = new BigDecimal("0.015");   // 1.5%
    private static final BigDecimal UI_RATE_MAX = new BigDecimal("0.062");   // 6.2%
    private static final BigDecimal UI_RATE_NEW_EMPLOYER = new BigDecimal("0.034"); // 3.4%

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean
    public Job uiRateRequestJob() {
        return new JobBuilder("UI_RATE_REQUEST_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(identifyAccountsNeedingRateStep())
            .next(generateRateRequestFileStep())
            .next(processRateResponseStep())
            .next(uiRateLogSummaryStep())
            .build();
    }

    // ==========================================
    // STEP 1: Identify Accounts Needing Rate Updates
    // ==========================================

    @Bean
    public Step identifyAccountsNeedingRateStep() {
        return new StepBuilder("identifyAccountsNeedingRateStep", jobRepository)
            .listener(stepListener)
            .tasklet(identifyAccountsNeedingRateTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet identifyAccountsNeedingRateTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 1: Identifying Accounts Needing UI Rate Updates ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            executionContext.put("totalSteps", 4);

            // Determine the rate year (rates are set annually, effective Jan 1)
            int rateYear = LocalDate.now().getYear();

            // Find all active provider accounts with SEIN (taxpayerId) that need
            // UI rate updates. Accounts need updates if:
            // 1. New accounts (registered in last quarter)
            // 2. Accounts with rate changes due to claims experience
            // 3. Annual rate refresh
            List<ProviderEntity> allProviders = providerRepository.findAll();
            List<Long> accountIds = new ArrayList<>();
            AtomicInteger newAccountCount = new AtomicInteger(0);
            AtomicInteger existingAccountCount = new AtomicInteger(0);

            for (ProviderEntity provider : allProviders) {
                if (provider.getProviderStatus() == ProviderEntity.ProviderStatus.ACTIVE
                        && provider.getTaxpayerId() != null
                        && !provider.getTaxpayerId().isBlank()) {
                    accountIds.add(provider.getId());

                    // Check if this is a new account (registered in last 90 days)
                    if (provider.getEffectiveDate() != null
                            && provider.getEffectiveDate().isAfter(LocalDate.now().minusDays(90))) {
                        newAccountCount.incrementAndGet();
                    } else {
                        existingAccountCount.incrementAndGet();
                    }
                }
            }

            if (accountIds.isEmpty()) {
                log.info("No accounts found requiring UI rate updates for {}", rateYear);
                executionContext.put("skipProcessing", true);
                executionContext.put("skipReason", "No accounts with SEIN found for rate year " + rateYear);
                return RepeatStatus.FINISHED;
            }

            log.info("Found {} accounts needing rate updates ({} new, {} existing) for rate year {}",
                accountIds.size(), newAccountCount.get(), existingAccountCount.get(), rateYear);

            executionContext.put("skipProcessing", false);
            executionContext.put("rateYear", rateYear);
            executionContext.put("accountIds", new ArrayList<>(accountIds));
            executionContext.put("newAccountCount", newAccountCount.get());
            executionContext.put("existingAccountCount", existingAccountCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate Rate Request File
    // ==========================================

    @Bean
    public Step generateRateRequestFileStep() {
        return new StepBuilder("generateRateRequestFileStep", jobRepository)
            .listener(stepListener)
            .tasklet(generateRateRequestFileTasklet(), transactionManager)
            .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public Tasklet generateRateRequestFileTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 2: Generating UI Rate Request File ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping rate request generation");
                return RepeatStatus.FINISHED;
            }

            List<Long> accountIds = (List<Long>) executionContext.get("accountIds");
            int rateYear = (int) executionContext.get("rateYear");

            AtomicInteger requestsGenerated = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            StringBuilder requestContent = new StringBuilder();
            requestContent.append(String.format("HDR|UI_RATE_REQ|%d|%s%n",
                rateYear, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));

            for (Long accountId : accountIds) {
                try {
                    ProviderEntity provider = providerRepository.findById(accountId).orElse(null);
                    if (provider == null || provider.getTaxpayerId() == null) continue;

                    // In production: would use Integration Hub to format as EDD rate request
                    requestContent.append(String.format("REQ|%s|%s|%d%n",
                        provider.getTaxpayerId(),
                        provider.getDojCountyCode() != null ? provider.getDojCountyCode() : "00",
                        rateYear));

                    requestsGenerated.incrementAndGet();
                } catch (Exception e) {
                    log.error("Error generating rate request for account {}: {}", accountId, e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            requestContent.append(String.format("TRL|%d%n", requestsGenerated.get()));

            // In production: would use Integration Hub SFTP to send to EDD
            // bawFileService.sendOutboundFile("EDD", "UI_RATE_REQUEST", requestRecords);
            String fileReference = String.format("UIRATE-REQ-%d-%s",
                rateYear, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            log.info("MOCK: Sending UI rate request file {} with {} requests to EDD",
                fileReference, requestsGenerated.get());

            executionContext.put("requestFileReference", fileReference);
            executionContext.put("requestsGenerated", requestsGenerated.get());
            executionContext.put("requestErrorCount", errorCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Process Rate Response (Mock)
    // ==========================================

    @Bean
    public Step processRateResponseStep() {
        return new StepBuilder("processRateResponseStep", jobRepository)
            .listener(stepListener)
            .tasklet(processRateResponseTasklet(), transactionManager)
            .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public Tasklet processRateResponseTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 3: Processing UI Rate Response (Mock) ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping rate response processing");
                return RepeatStatus.FINISHED;
            }

            // In production: would use Integration Hub to fetch rate response from EDD
            // bawFileService.fetchInboundFile("EDD", "UI_RATE_RESPONSE", UIRateResponse.class);

            List<Long> accountIds = (List<Long>) executionContext.get("accountIds");
            int newAccountCount = (int) executionContext.get("newAccountCount");

            AtomicInteger ratesUpdated = new AtomicInteger(0);
            AtomicInteger ratesUnchanged = new AtomicInteger(0);
            AtomicInteger rateErrors = new AtomicInteger(0);

            // Mock: simulate rate response processing
            // New employers get the standard new employer rate (3.4%)
            // Existing employers get experience-rated rate based on claims history
            for (Long accountId : accountIds) {
                try {
                    ProviderEntity provider = providerRepository.findById(accountId).orElse(null);
                    if (provider == null) continue;

                    // Mock rate assignment based on account age
                    // In production: parse actual EDD rate response
                    boolean isNewAccount = provider.getEffectiveDate() != null
                        && provider.getEffectiveDate().isAfter(LocalDate.now().minusDays(90));

                    BigDecimal assignedRate;
                    if (isNewAccount) {
                        assignedRate = UI_RATE_NEW_EMPLOYER;
                    } else {
                        // Mock experience rating: use a rate between min and max
                        // based on provider ID for deterministic mock
                        double fraction = (accountId % 10) / 10.0;
                        assignedRate = UI_RATE_MIN.add(
                            UI_RATE_MAX.subtract(UI_RATE_MIN)
                                .multiply(BigDecimal.valueOf(fraction)))
                            .setScale(3, RoundingMode.HALF_UP);
                    }

                    log.debug("UI rate for account {} (SEIN {}): {}%",
                        accountId, provider.getTaxpayerId(),
                        assignedRate.multiply(new BigDecimal("100")));

                    ratesUpdated.incrementAndGet();
                } catch (Exception e) {
                    log.error("Error processing rate for account {}: {}", accountId, e.getMessage());
                    rateErrors.incrementAndGet();
                }
            }

            log.info("Rate response processing: {} updated, {} unchanged, {} errors",
                ratesUpdated.get(), ratesUnchanged.get(), rateErrors.get());

            executionContext.put("ratesUpdated", ratesUpdated.get());
            executionContext.put("ratesUnchanged", ratesUnchanged.get());
            executionContext.put("rateErrors", rateErrors.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Log Summary
    // ==========================================

    @Bean
    public Step uiRateLogSummaryStep() {
        return new StepBuilder("uiRateLogSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(uiRateLogSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet uiRateLogSummaryTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 4: UI Rate Request Job Summary ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                String skipReason = (String) executionContext.get("skipReason");
                log.info("================================================");
                log.info("  UI_RATE_REQUEST JOB SKIPPED");
                log.info("  Reason: {}", skipReason);
                log.info("================================================");
                return RepeatStatus.FINISHED;
            }

            String requestFileReference = (String) executionContext.get("requestFileReference");
            int rateYear = (int) executionContext.get("rateYear");
            int newAccountCount = (int) executionContext.get("newAccountCount");
            int existingAccountCount = (int) executionContext.get("existingAccountCount");
            int requestsGenerated = (int) executionContext.get("requestsGenerated");
            int requestErrorCount = (int) executionContext.get("requestErrorCount");
            int ratesUpdated = (int) executionContext.get("ratesUpdated");
            int ratesUnchanged = (int) executionContext.get("ratesUnchanged");
            int rateErrors = (int) executionContext.get("rateErrors");

            log.info("================================================");
            log.info("  UI_RATE_REQUEST JOB COMPLETED");
            log.info("================================================");
            log.info("  Request File: {}", requestFileReference);
            log.info("  Destination: {} ({})", DESTINATION_SYSTEM, FILE_TYPE);
            log.info("  Rate Year: {}", rateYear);
            log.info("------------------------------------------------");
            log.info("  Account Summary:");
            log.info("    New Accounts: {}", newAccountCount);
            log.info("    Existing Accounts: {}", existingAccountCount);
            log.info("    Rate Requests Generated: {}", requestsGenerated);
            log.info("    Request Errors: {}", requestErrorCount);
            log.info("------------------------------------------------");
            log.info("  Rate Response:");
            log.info("    Rates Updated: {}", ratesUpdated);
            log.info("    Rates Unchanged: {}", ratesUnchanged);
            log.info("    Rate Errors: {}", rateErrors);
            log.info("  Rate Range: {}% - {}%",
                UI_RATE_MIN.multiply(new BigDecimal("100")),
                UI_RATE_MAX.multiply(new BigDecimal("100")));
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
