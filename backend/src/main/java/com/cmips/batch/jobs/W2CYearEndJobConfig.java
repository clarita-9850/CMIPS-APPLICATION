package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.entity.PaymentCorrectionEntity;
import com.cmips.repository.PaymentCorrectionRepository;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for W-2C Year-End Processing.
 *
 * DSD Section 18: Annual W-2C processing for corrections to previously
 * issued W-2 forms. When provider wages are corrected after W-2s have been
 * issued, a W-2C (Corrected Wage and Tax Statement) must be generated
 * and filed with both SSA and EDD.
 *
 * Data Flow:
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │  Our Database   │───▶│  This Job       │───▶│  Integration    │───▶│   SSA / EDD     │
 * │  (Payment       │    │  (Find corr. &  │    │  Hub (Format &  │    │  (Process       │
 * │   Corrections)  │    │   Generate W2C) │    │   Send)         │    │   W-2C filings) │
 * └─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
 *
 * Schedule: Annual (January-February for prior tax year; replaces AutoSys schedule)
 * Legacy Reference: W-2C Corrected Wage and Tax Statement
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class W2CYearEndJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final PaymentCorrectionRepository paymentCorrectionRepository;
    private final ProviderRepository providerRepository;

    private static final String DESTINATION_SYSTEM = "SSA_EDD";
    private static final String FILE_TYPE = "W2C_CORRECTION";
    private static final String EMPLOYER_FEIN = "94-6000134";
    private static final String EMPLOYER_NAME = "IHSS PROGRAM - STATE OF CALIFORNIA";

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean
    public Job w2cYearEndJob() {
        return new JobBuilder("W2C_YEAR_END_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(findWageCorrectionProvidersStep())
            .next(generateW2CRecordsStep())
            .next(generateW2CFilingStep())
            .next(w2cLogSummaryStep())
            .build();
    }

    // ==========================================
    // STEP 1: Find Providers with Wage Corrections
    // ==========================================

    @Bean
    public Step findWageCorrectionProvidersStep() {
        return new StepBuilder("findWageCorrectionProvidersStep", jobRepository)
            .listener(stepListener)
            .tasklet(findWageCorrectionProvidersTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet findWageCorrectionProvidersTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 1: Finding Providers with Wage Corrections ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            executionContext.put("totalSteps", 4);

            // Determine the tax year to process (prior year)
            int taxYear = LocalDate.now().getYear() - 1;

            log.info("Searching for wage corrections for tax year {}", taxYear);

            // Query payment corrections that affect the prior tax year
            // and have not yet been included in a W-2C filing
            List<PaymentCorrectionEntity> allCorrections = paymentCorrectionRepository.findAll();
            List<Long> correctionIds = new ArrayList<>();

            for (PaymentCorrectionEntity correction : allCorrections) {
                // Use payPeriodStart as the correction date reference
                // Only include PROCESSED corrections not yet filed as W-2C
                if (correction.getPayPeriodStart() != null
                        && correction.getPayPeriodStart().getYear() == taxYear
                        && correction.getStatus() == PaymentCorrectionEntity.CorrectionStatus.PROCESSED) {
                    correctionIds.add(correction.getId());
                }
            }

            if (correctionIds.isEmpty()) {
                log.info("No wage corrections found requiring W-2C for tax year {}", taxYear);
                executionContext.put("skipProcessing", true);
                executionContext.put("skipReason", "No wage corrections for tax year " + taxYear);
                return RepeatStatus.FINISHED;
            }

            log.info("Found {} wage corrections requiring W-2C for tax year {}", correctionIds.size(), taxYear);

            executionContext.put("skipProcessing", false);
            executionContext.put("taxYear", taxYear);
            executionContext.put("correctionIds", new ArrayList<>(correctionIds));
            executionContext.put("correctionCount", correctionIds.size());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate W-2C Records
    // ==========================================

    @Bean
    public Step generateW2CRecordsStep() {
        return new StepBuilder("generateW2CRecordsStep", jobRepository)
            .listener(stepListener)
            .tasklet(generateW2CRecordsTasklet(), transactionManager)
            .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public Tasklet generateW2CRecordsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 2: Generating W-2C Records ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping W-2C generation");
                return RepeatStatus.FINISHED;
            }

            List<Long> correctionIds = (List<Long>) executionContext.get("correctionIds");
            int taxYear = (int) executionContext.get("taxYear");

            AtomicInteger w2cRecordsGenerated = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            BigDecimal totalOriginalWages = BigDecimal.ZERO;
            BigDecimal totalCorrectedWages = BigDecimal.ZERO;

            for (Long correctionId : correctionIds) {
                try {
                    PaymentCorrectionEntity correction = paymentCorrectionRepository
                        .findById(correctionId).orElse(null);
                    if (correction == null) continue;

                    // In production: would look up the provider to get SSN, name, address
                    // and calculate the difference between original and corrected wages.
                    // PaymentCorrectionEntity stores hoursCorrectedMinutes; convert to wage amounts
                    // using a mock hourly rate for demonstration purposes.
                    BigDecimal hourlyRate = new BigDecimal("16.00"); // mock IHSS hourly rate
                    int correctedMinutes = correction.getHoursCorrectedMinutes() != null
                        ? correction.getHoursCorrectedMinutes() : 0;
                    // Original amount is zero (hours that should not have been paid or were missing)
                    BigDecimal originalAmount = BigDecimal.ZERO;
                    // Corrected amount is based on the hours correction
                    BigDecimal correctedAmount = hourlyRate
                        .multiply(BigDecimal.valueOf(correctedMinutes))
                        .divide(new BigDecimal("60"), 2, java.math.RoundingMode.HALF_UP);

                    totalOriginalWages = totalOriginalWages.add(originalAmount);
                    totalCorrectedWages = totalCorrectedWages.add(correctedAmount);

                    // W-2C record would contain:
                    // Box a: Employee SSN, Box b: Employer FEIN
                    // Box c: Corrected employer info, Box d: Employee name
                    // Previously Reported: wages, SS wages, Medicare wages, SS tips
                    // Corrected Information: wages, SS wages, Medicare wages, SS tips
                    log.debug("W-2C record for correction {}: original=${}, corrected=${}",
                        correctionId, originalAmount, correctedAmount);

                    w2cRecordsGenerated.incrementAndGet();
                } catch (Exception e) {
                    log.error("Error generating W-2C for correction {}: {}", correctionId, e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            BigDecimal totalAdjustment = totalCorrectedWages.subtract(totalOriginalWages);

            log.info("Generated {} W-2C records ({} errors)", w2cRecordsGenerated.get(), errorCount.get());
            log.info("Total wage adjustment: ${} (original: ${}, corrected: ${})",
                totalAdjustment, totalOriginalWages, totalCorrectedWages);

            executionContext.put("w2cRecordsGenerated", w2cRecordsGenerated.get());
            executionContext.put("w2cErrorCount", errorCount.get());
            executionContext.put("totalOriginalWages", totalOriginalWages.toString());
            executionContext.put("totalCorrectedWages", totalCorrectedWages.toString());
            executionContext.put("totalAdjustment", totalAdjustment.toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Generate W-2C Filing (Mock)
    // ==========================================

    @Bean
    public Step generateW2CFilingStep() {
        return new StepBuilder("generateW2CFilingStep", jobRepository)
            .listener(stepListener)
            .tasklet(generateW2CFilingTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet generateW2CFilingTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 3: Generating W-2C Filing ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping W-2C filing");
                return RepeatStatus.FINISHED;
            }

            int taxYear = (int) executionContext.get("taxYear");
            int w2cRecordsGenerated = (int) executionContext.get("w2cRecordsGenerated");

            // In production: would use Integration Hub to:
            // 1. Generate EFW2C file format (SSA electronic filing)
            // 2. Generate state-level W-2C for CA EDD
            // 3. Send via secure transmission to SSA and EDD
            // bawFileService.sendOutboundFile("SSA", "W2C_FEDERAL", w2cRecords);
            // bawFileService.sendOutboundFile("EDD", "W2C_STATE", w2cRecords);

            String fileReference = String.format("W2C-%d-%s",
                taxYear, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            log.info("MOCK: Generated EFW2C electronic filing: {}", fileReference);
            log.info("MOCK: {} W-2C records for tax year {} (Employer: {} / {})",
                w2cRecordsGenerated, taxYear, EMPLOYER_FEIN, EMPLOYER_NAME);
            log.info("MOCK: Sent W-2C filing to SSA and CA EDD");

            executionContext.put("fileReference", fileReference);
            executionContext.put("filingGenerated", true);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Log Summary
    // ==========================================

    @Bean
    public Step w2cLogSummaryStep() {
        return new StepBuilder("w2cLogSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(w2cLogSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet w2cLogSummaryTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 4: W-2C Job Summary ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                String skipReason = (String) executionContext.get("skipReason");
                log.info("================================================");
                log.info("  W2C_YEAR_END JOB SKIPPED");
                log.info("  Reason: {}", skipReason);
                log.info("================================================");
                return RepeatStatus.FINISHED;
            }

            String fileReference = (String) executionContext.get("fileReference");
            int taxYear = (int) executionContext.get("taxYear");
            int correctionCount = (int) executionContext.get("correctionCount");
            int w2cRecordsGenerated = (int) executionContext.get("w2cRecordsGenerated");
            int w2cErrorCount = (int) executionContext.get("w2cErrorCount");
            String totalOriginalWages = (String) executionContext.get("totalOriginalWages");
            String totalCorrectedWages = (String) executionContext.get("totalCorrectedWages");
            String totalAdjustment = (String) executionContext.get("totalAdjustment");

            log.info("================================================");
            log.info("  W2C_YEAR_END JOB COMPLETED");
            log.info("================================================");
            log.info("  File Reference: {}", fileReference);
            log.info("  Destination: {} ({})", DESTINATION_SYSTEM, FILE_TYPE);
            log.info("  Tax Year: {}", taxYear);
            log.info("  Employer: {} (FEIN: {})", EMPLOYER_NAME, EMPLOYER_FEIN);
            log.info("------------------------------------------------");
            log.info("  Statistics:");
            log.info("    Wage Corrections Found: {}", correctionCount);
            log.info("    W-2C Records Generated: {}", w2cRecordsGenerated);
            log.info("    Generation Errors: {}", w2cErrorCount);
            log.info("------------------------------------------------");
            log.info("  Financial Summary:");
            log.info("    Total Original Wages: ${}", totalOriginalWages);
            log.info("    Total Corrected Wages: ${}", totalCorrectedWages);
            log.info("    Net Wage Adjustment: ${}", totalAdjustment);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
