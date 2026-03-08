package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.entity.PayrollBatchRunEntity;
import com.cmips.entity.ProviderEntity;
import com.cmips.repository.PayrollBatchRunRepository;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for DE-9/DE-9C Quarterly Wage Reports.
 *
 * DSD Section 18: Quarterly wage reports to EDD.
 * DE-9 is the employer's quarterly summary (total wages, number of employees).
 * DE-9C is the detail report listing each employee's wages for the quarter.
 * Both must be filed together within 30 days after the quarter ends.
 *
 * Data Flow:
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │  Our Database   │───▶│  This Job       │───▶│  Integration    │───▶│      EDD        │
 * │  (Timesheets &  │    │  (Aggregate &   │    │  Hub (Format &  │    │  (Process       │
 * │   Payroll Runs) │    │   Generate)     │    │   Send SFTP)    │    │   Wage Report)  │
 * └─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
 *
 * Schedule: Quarterly (replaces AutoSys schedule)
 * Legacy Reference: EDD DE-9 Quarterly Contribution Return and Report of Wages
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DE9WageReportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final PayrollBatchRunRepository payrollBatchRunRepository;
    private final ProviderRepository providerRepository;

    private static final String DESTINATION_SYSTEM = "EDD";
    private static final String FILE_TYPE = "DE9_WAGE_REPORT";
    private static final String EMPLOYER_SEIN = "999-9999-9";
    private static final String EMPLOYER_FEIN = "94-6000134";

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean
    public Job de9WageReportJob() {
        return new JobBuilder("DE9_WAGE_REPORT_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(aggregateQuarterlyWagesStep())
            .next(generateDE9SummaryStep())
            .next(generateDE9CDetailStep())
            .next(sendDE9ToEddStep())
            .next(de9LogSummaryStep())
            .build();
    }

    // ==========================================
    // STEP 1: Aggregate Provider Wages for Quarter
    // ==========================================

    @Bean
    public Step aggregateQuarterlyWagesStep() {
        return new StepBuilder("aggregateQuarterlyWagesStep", jobRepository)
            .listener(stepListener)
            .tasklet(aggregateQuarterlyWagesTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet aggregateQuarterlyWagesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 1: Aggregating Quarterly Wages ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            executionContext.put("totalSteps", 5);

            // Determine the quarter to report (previous quarter)
            LocalDate today = LocalDate.now();
            int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
            int reportQuarter = currentQuarter == 1 ? 4 : currentQuarter - 1;
            int reportYear = currentQuarter == 1 ? today.getYear() - 1 : today.getYear();

            LocalDate quarterStart = LocalDate.of(reportYear, (reportQuarter - 1) * 3 + 1, 1);
            LocalDate quarterEnd = quarterStart.plusMonths(3).minusDays(1);

            log.info("Aggregating wages for Q{} {} ({} to {})", reportQuarter, reportYear, quarterStart, quarterEnd);

            // Query completed payroll batch runs for the quarter
            List<PayrollBatchRunEntity> batchRuns = payrollBatchRunRepository.findAll().stream()
                .filter(run -> run.getPayPeriodBeginDate() != null
                    && !run.getPayPeriodBeginDate().isBefore(quarterStart)
                    && !run.getPayPeriodEndDate().isAfter(quarterEnd)
                    && "COMPLETED".equals(run.getStatus()))
                .toList();

            if (batchRuns.isEmpty()) {
                log.info("No completed payroll batch runs found for Q{} {}", reportQuarter, reportYear);
                executionContext.put("skipProcessing", true);
                executionContext.put("skipReason",
                    "No completed payroll runs for Q" + reportQuarter + " " + reportYear);
                return RepeatStatus.FINISHED;
            }

            // Aggregate totals
            BigDecimal totalGrossWages = BigDecimal.ZERO;
            BigDecimal totalDeductions = BigDecimal.ZERO;
            BigDecimal totalNetPay = BigDecimal.ZERO;
            int totalTimesheetCount = 0;

            for (PayrollBatchRunEntity run : batchRuns) {
                totalGrossWages = totalGrossWages.add(
                    run.getTotalGrossPay() != null ? run.getTotalGrossPay() : BigDecimal.ZERO);
                totalDeductions = totalDeductions.add(
                    run.getTotalDeductions() != null ? run.getTotalDeductions() : BigDecimal.ZERO);
                totalNetPay = totalNetPay.add(
                    run.getTotalNetPay() != null ? run.getTotalNetPay() : BigDecimal.ZERO);
                totalTimesheetCount += run.getTotalTimesheets() != null ? run.getTotalTimesheets() : 0;
            }

            // Count unique providers paid this quarter
            // In production: would query TimesheetRepository for distinct provider IDs
            List<ProviderEntity> activeProviders = providerRepository
                .findByProviderStatus(ProviderEntity.ProviderStatus.ACTIVE);
            int uniqueProvidersPaid = activeProviders.size();

            log.info("Quarter aggregation: {} batch runs, {} timesheets, {} unique providers, ${} gross",
                batchRuns.size(), totalTimesheetCount, uniqueProvidersPaid, totalGrossWages);

            executionContext.put("skipProcessing", false);
            executionContext.put("reportQuarter", reportQuarter);
            executionContext.put("reportYear", reportYear);
            executionContext.put("batchRunCount", batchRuns.size());
            executionContext.put("totalTimesheetCount", totalTimesheetCount);
            executionContext.put("uniqueProvidersPaid", uniqueProvidersPaid);
            executionContext.put("totalGrossWages", totalGrossWages.toString());
            executionContext.put("totalDeductions", totalDeductions.toString());
            executionContext.put("totalNetPay", totalNetPay.toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate DE-9 Summary
    // ==========================================

    @Bean
    public Step generateDE9SummaryStep() {
        return new StepBuilder("generateDE9SummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(generateDE9SummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet generateDE9SummaryTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 2: Generating DE-9 Summary ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping DE-9 summary generation");
                return RepeatStatus.FINISHED;
            }

            int reportQuarter = (int) executionContext.get("reportQuarter");
            int reportYear = (int) executionContext.get("reportYear");
            String totalGrossWages = (String) executionContext.get("totalGrossWages");
            int uniqueProvidersPaid = (int) executionContext.get("uniqueProvidersPaid");

            // In production: would use Integration Hub to format as EDD DE-9
            // DE-9 summary includes: employer SEIN/FEIN, quarter, total wages,
            // total employees, total subject wages, excess wages, tax amounts
            StringBuilder de9Content = new StringBuilder();
            de9Content.append(String.format("DE9|%s|%s|Q%d|%d%n", EMPLOYER_SEIN, EMPLOYER_FEIN,
                reportQuarter, reportYear));
            de9Content.append(String.format("SUMMARY|TOTAL_WAGES|%s%n", totalGrossWages));
            de9Content.append(String.format("SUMMARY|NUM_EMPLOYEES|%d%n", uniqueProvidersPaid));
            de9Content.append(String.format("SUMMARY|MONTH1_EMPLOYEES|%d%n", uniqueProvidersPaid));
            de9Content.append(String.format("SUMMARY|MONTH2_EMPLOYEES|%d%n", uniqueProvidersPaid));
            de9Content.append(String.format("SUMMARY|MONTH3_EMPLOYEES|%d%n", uniqueProvidersPaid));

            String de9FileRef = String.format("DE9-Q%d%d-%s",
                reportQuarter, reportYear,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            log.info("Generated DE-9 summary file: {}", de9FileRef);
            executionContext.put("de9FileReference", de9FileRef);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Generate DE-9C Detail Per Provider
    // ==========================================

    @Bean
    public Step generateDE9CDetailStep() {
        return new StepBuilder("generateDE9CDetailStep", jobRepository)
            .listener(stepListener)
            .tasklet(generateDE9CDetailTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet generateDE9CDetailTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 3: Generating DE-9C Detail Per Provider ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping DE-9C detail generation");
                return RepeatStatus.FINISHED;
            }

            int reportQuarter = (int) executionContext.get("reportQuarter");
            int reportYear = (int) executionContext.get("reportYear");

            // In production: would query TimesheetRepository for each provider's wages
            // Mock: generate DE-9C records from active providers
            List<ProviderEntity> activeProviders = providerRepository
                .findByProviderStatus(ProviderEntity.ProviderStatus.ACTIVE);

            AtomicInteger de9cRecords = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            StringBuilder de9cContent = new StringBuilder();
            de9cContent.append(String.format("DE9C|%s|Q%d|%d%n", EMPLOYER_SEIN, reportQuarter, reportYear));

            for (ProviderEntity provider : activeProviders) {
                try {
                    // In production: would use Integration Hub to format as EDD DE-9C
                    // DE-9C detail: SSN, last name, first name, quarterly wages by month
                    String ssn = provider.getSsn() != null ? provider.getSsn().replace("-", "") : "000000000";

                    // Mock wage calculation - in production, aggregate from timesheets
                    BigDecimal mockQuarterlyWage = new BigDecimal("4500.00");

                    de9cContent.append(String.format("DTL|%s|%s|%s|%s|%s|%s|%s%n",
                        ssn,
                        provider.getLastName(),
                        provider.getFirstName(),
                        mockQuarterlyWage.toString(),  // Month 1
                        mockQuarterlyWage.toString(),  // Month 2
                        mockQuarterlyWage.toString(),  // Month 3
                        mockQuarterlyWage.multiply(new BigDecimal("3")).toString())); // Quarter total

                    de9cRecords.incrementAndGet();
                } catch (Exception e) {
                    log.error("Error generating DE-9C for provider {}: {}", provider.getId(), e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            de9cContent.append(String.format("TRL|%d%n", de9cRecords.get()));

            String de9cFileRef = String.format("DE9C-Q%d%d-%s",
                reportQuarter, reportYear,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            log.info("Generated DE-9C detail file: {} with {} provider records ({} errors)",
                de9cFileRef, de9cRecords.get(), errorCount.get());

            executionContext.put("de9cFileReference", de9cFileRef);
            executionContext.put("de9cRecordCount", de9cRecords.get());
            executionContext.put("de9cErrorCount", errorCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Send to EDD (Mock)
    // ==========================================

    @Bean
    public Step sendDE9ToEddStep() {
        return new StepBuilder("sendDE9ToEddStep", jobRepository)
            .listener(stepListener)
            .tasklet(sendDE9ToEddTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet sendDE9ToEddTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 4: Sending DE-9/DE-9C to EDD ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping send - no files to transmit");
                return RepeatStatus.FINISHED;
            }

            String de9FileRef = (String) executionContext.get("de9FileReference");
            String de9cFileRef = (String) executionContext.get("de9cFileReference");

            // In production: would use Integration Hub SFTP to send both files to EDD
            // bawFileService.sendOutboundFile("EDD", "DE9_SUMMARY", de9Records);
            // bawFileService.sendOutboundFile("EDD", "DE9C_DETAIL", de9cRecords);
            log.info("MOCK: Sending DE-9 summary file {} to EDD SFTP", de9FileRef);
            log.info("MOCK: Sending DE-9C detail file {} to EDD SFTP", de9cFileRef);
            log.info("MOCK: EDD SFTP transfers complete");

            executionContext.put("sentToEdd", true);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 5: Log Summary
    // ==========================================

    @Bean
    public Step de9LogSummaryStep() {
        return new StepBuilder("de9LogSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(de9LogSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet de9LogSummaryTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 5: DE-9 Job Summary ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                String skipReason = (String) executionContext.get("skipReason");
                log.info("================================================");
                log.info("  DE9_WAGE_REPORT JOB SKIPPED");
                log.info("  Reason: {}", skipReason);
                log.info("================================================");
                return RepeatStatus.FINISHED;
            }

            String de9FileRef = (String) executionContext.get("de9FileReference");
            String de9cFileRef = (String) executionContext.get("de9cFileReference");
            int reportQuarter = (int) executionContext.get("reportQuarter");
            int reportYear = (int) executionContext.get("reportYear");
            int batchRunCount = (int) executionContext.get("batchRunCount");
            int totalTimesheetCount = (int) executionContext.get("totalTimesheetCount");
            int uniqueProvidersPaid = (int) executionContext.get("uniqueProvidersPaid");
            String totalGrossWages = (String) executionContext.get("totalGrossWages");
            int de9cRecordCount = (int) executionContext.get("de9cRecordCount");
            int de9cErrorCount = (int) executionContext.get("de9cErrorCount");

            log.info("================================================");
            log.info("  DE9_WAGE_REPORT JOB COMPLETED");
            log.info("================================================");
            log.info("  DE-9 Summary File: {}", de9FileRef);
            log.info("  DE-9C Detail File: {}", de9cFileRef);
            log.info("  Destination: {} ({})", DESTINATION_SYSTEM, FILE_TYPE);
            log.info("  Report Period: Q{} {}", reportQuarter, reportYear);
            log.info("------------------------------------------------");
            log.info("  Payroll Summary:");
            log.info("    Batch Runs Processed: {}", batchRunCount);
            log.info("    Total Timesheets: {}", totalTimesheetCount);
            log.info("    Unique Providers Paid: {}", uniqueProvidersPaid);
            log.info("    Total Gross Wages: ${}", totalGrossWages);
            log.info("------------------------------------------------");
            log.info("  DE-9C Detail:");
            log.info("    Provider Records Generated: {}", de9cRecordCount);
            log.info("    Generation Errors: {}", de9cErrorCount);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
