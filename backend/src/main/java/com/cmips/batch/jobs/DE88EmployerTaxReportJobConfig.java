package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.entity.PayrollBatchRunEntity;
import com.cmips.repository.PayrollBatchRunRepository;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for DE-88 Employer Tax Report.
 *
 * DSD Section 18: Quarterly SDI, UI, and ETT tax payments to EDD.
 * California employers must file quarterly contribution returns (DE-88)
 * reporting State Disability Insurance (SDI), Unemployment Insurance (UI),
 * and Employment Training Tax (ETT) amounts.
 *
 * Data Flow:
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │  Our Database   │───▶│  This Job       │───▶│  Integration    │───▶│      EDD        │
 * │  (PayrollBatch  │    │  (Calculate &   │    │  Hub (Format &  │    │  (Process       │
 * │   Runs)         │    │   Generate)     │    │   Send SFTP)    │    │   Tax Return)   │
 * └─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
 *
 * Schedule: Quarterly (replaces AutoSys schedule)
 * Legacy Reference: EDD DE-88 Quarterly Contribution Return
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DE88EmployerTaxReportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final PayrollBatchRunRepository payrollBatchRunRepository;

    private static final String DESTINATION_SYSTEM = "EDD";
    private static final String FILE_TYPE = "DE88_TAX_RETURN";
    private static final String EMPLOYER_SEIN = "999-9999-9";

    // 2024 California tax rates (mock)
    private static final BigDecimal SDI_RATE = new BigDecimal("0.009");       // 0.9%
    private static final BigDecimal UI_RATE = new BigDecimal("0.034");        // 3.4% (new employer rate)
    private static final BigDecimal ETT_RATE = new BigDecimal("0.001");       // 0.1%
    private static final BigDecimal SDI_WAGE_LIMIT = new BigDecimal("153164"); // 2024 limit

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean
    public Job de88EmployerTaxReportJob() {
        return new JobBuilder("DE88_EMPLOYER_TAX_REPORT_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(calculateQuarterlyTaxTotalsStep())
            .next(generateDE88FileStep())
            .next(sendDE88ToEddStep())
            .next(de88LogSummaryStep())
            .build();
    }

    // ==========================================
    // STEP 1: Calculate Quarterly Tax Totals
    // ==========================================

    @Bean
    public Step calculateQuarterlyTaxTotalsStep() {
        return new StepBuilder("calculateQuarterlyTaxTotalsStep", jobRepository)
            .listener(stepListener)
            .tasklet(calculateQuarterlyTaxTotalsTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet calculateQuarterlyTaxTotalsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 1: Calculating Quarterly Tax Totals ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            executionContext.put("totalSteps", 4);

            // Determine the quarter to report (previous quarter)
            LocalDate today = LocalDate.now();
            int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
            int reportQuarter = currentQuarter == 1 ? 4 : currentQuarter - 1;
            int reportYear = currentQuarter == 1 ? today.getYear() - 1 : today.getYear();

            LocalDate quarterStart = LocalDate.of(reportYear, (reportQuarter - 1) * 3 + 1, 1);
            LocalDate quarterEnd = quarterStart.plusMonths(3).minusDays(1);

            log.info("Reporting quarter: Q{} {} ({} to {})", reportQuarter, reportYear, quarterStart, quarterEnd);

            // Query all completed payroll batch runs for the quarter
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

            // Aggregate totals across all batch runs in the quarter
            BigDecimal totalGrossWages = BigDecimal.ZERO;
            int totalEmployees = 0;
            AtomicInteger batchCount = new AtomicInteger(0);

            for (PayrollBatchRunEntity run : batchRuns) {
                BigDecimal grossPay = run.getTotalGrossPay() != null ? run.getTotalGrossPay() : BigDecimal.ZERO;
                totalGrossWages = totalGrossWages.add(grossPay);
                totalEmployees += run.getTotalTimesheets() != null ? run.getTotalTimesheets() : 0;
                batchCount.incrementAndGet();
            }

            // Calculate tax amounts
            BigDecimal sdiAmount = totalGrossWages.min(SDI_WAGE_LIMIT)
                .multiply(SDI_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal uiAmount = totalGrossWages.multiply(UI_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal ettAmount = totalGrossWages.multiply(ETT_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalTaxDue = sdiAmount.add(uiAmount).add(ettAmount);

            log.info("Quarter totals: {} batch runs, {} employees, ${} gross wages",
                batchCount.get(), totalEmployees, totalGrossWages);
            log.info("Tax calculations: SDI=${}, UI=${}, ETT=${}, Total=${}",
                sdiAmount, uiAmount, ettAmount, totalTaxDue);

            executionContext.put("skipProcessing", false);
            executionContext.put("reportQuarter", reportQuarter);
            executionContext.put("reportYear", reportYear);
            executionContext.put("batchRunCount", batchCount.get());
            executionContext.put("totalEmployees", totalEmployees);
            executionContext.put("totalGrossWages", totalGrossWages.toString());
            executionContext.put("sdiAmount", sdiAmount.toString());
            executionContext.put("uiAmount", uiAmount.toString());
            executionContext.put("ettAmount", ettAmount.toString());
            executionContext.put("totalTaxDue", totalTaxDue.toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate DE-88 File
    // ==========================================

    @Bean
    public Step generateDE88FileStep() {
        return new StepBuilder("generateDE88FileStep", jobRepository)
            .listener(stepListener)
            .tasklet(generateDE88FileTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet generateDE88FileTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 2: Generating DE-88 File ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping DE-88 generation - no payroll data");
                return RepeatStatus.FINISHED;
            }

            int reportQuarter = (int) executionContext.get("reportQuarter");
            int reportYear = (int) executionContext.get("reportYear");
            String totalGrossWages = (String) executionContext.get("totalGrossWages");
            String sdiAmount = (String) executionContext.get("sdiAmount");
            String uiAmount = (String) executionContext.get("uiAmount");
            String ettAmount = (String) executionContext.get("ettAmount");
            String totalTaxDue = (String) executionContext.get("totalTaxDue");
            int totalEmployees = (int) executionContext.get("totalEmployees");

            // In production: would use Integration Hub to format as EDD DE-88 electronic filing
            // Mock DE-88 content generation
            StringBuilder de88Content = new StringBuilder();
            de88Content.append(String.format("DE88|%s|Q%d|%d%n", EMPLOYER_SEIN, reportQuarter, reportYear));
            de88Content.append(String.format("WAGES|%s|%d%n", totalGrossWages, totalEmployees));
            de88Content.append(String.format("SDI|%s|%s%n", SDI_RATE, sdiAmount));
            de88Content.append(String.format("UI|%s|%s%n", UI_RATE, uiAmount));
            de88Content.append(String.format("ETT|%s|%s%n", ETT_RATE, ettAmount));
            de88Content.append(String.format("TOTAL|%s%n", totalTaxDue));

            String fileReference = String.format("DE88-Q%d%d-%s",
                reportQuarter, reportYear,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            log.info("Generated DE-88 file: {}", fileReference);

            executionContext.put("fileReference", fileReference);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Send DE-88 to EDD (Mock)
    // ==========================================

    @Bean
    public Step sendDE88ToEddStep() {
        return new StepBuilder("sendDE88ToEddStep", jobRepository)
            .listener(stepListener)
            .tasklet(sendDE88ToEddTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet sendDE88ToEddTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 3: Sending DE-88 to EDD ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping send - no file to transmit");
                return RepeatStatus.FINISHED;
            }

            String fileReference = (String) executionContext.get("fileReference");

            // In production: would use Integration Hub SFTP to send to EDD
            // bawFileService.sendOutboundFile("EDD", "DE88_TAX_RETURN", records);
            log.info("MOCK: Sending DE-88 file {} to EDD SFTP endpoint", fileReference);
            log.info("MOCK: EDD SFTP transfer complete");

            executionContext.put("sentToEdd", true);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Log Summary
    // ==========================================

    @Bean
    public Step de88LogSummaryStep() {
        return new StepBuilder("de88LogSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(de88LogSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet de88LogSummaryTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 4: DE-88 Job Summary ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                String skipReason = (String) executionContext.get("skipReason");
                log.info("================================================");
                log.info("  DE88_EMPLOYER_TAX_REPORT JOB SKIPPED");
                log.info("  Reason: {}", skipReason);
                log.info("================================================");
                return RepeatStatus.FINISHED;
            }

            String fileReference = (String) executionContext.get("fileReference");
            int reportQuarter = (int) executionContext.get("reportQuarter");
            int reportYear = (int) executionContext.get("reportYear");
            int batchRunCount = (int) executionContext.get("batchRunCount");
            int totalEmployees = (int) executionContext.get("totalEmployees");
            String totalGrossWages = (String) executionContext.get("totalGrossWages");
            String sdiAmount = (String) executionContext.get("sdiAmount");
            String uiAmount = (String) executionContext.get("uiAmount");
            String ettAmount = (String) executionContext.get("ettAmount");
            String totalTaxDue = (String) executionContext.get("totalTaxDue");

            log.info("================================================");
            log.info("  DE88_EMPLOYER_TAX_REPORT JOB COMPLETED");
            log.info("================================================");
            log.info("  File Reference: {}", fileReference);
            log.info("  Destination: {} ({})", DESTINATION_SYSTEM, FILE_TYPE);
            log.info("  Report Period: Q{} {}", reportQuarter, reportYear);
            log.info("------------------------------------------------");
            log.info("  Payroll Summary:");
            log.info("    Batch Runs Processed: {}", batchRunCount);
            log.info("    Total Employees: {}", totalEmployees);
            log.info("    Total Gross Wages: ${}", totalGrossWages);
            log.info("------------------------------------------------");
            log.info("  Tax Amounts:");
            log.info("    SDI ({}%): ${}", SDI_RATE.multiply(new BigDecimal("100")), sdiAmount);
            log.info("    UI  ({}%): ${}", UI_RATE.multiply(new BigDecimal("100")), uiAmount);
            log.info("    ETT ({}%): ${}", ETT_RATE.multiply(new BigDecimal("100")), ettAmount);
            log.info("    Total Tax Due: ${}", totalTaxDue);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
