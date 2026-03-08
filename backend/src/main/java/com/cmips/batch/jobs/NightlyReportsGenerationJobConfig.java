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
 * Spring Batch Job Configuration for NIGHTLY_REPORTS_GENERATION.
 *
 * DSD Sections 8, 10-13: Nightly (runs after SAWS S2) - generate and distribute
 * standard reports. Reports are sent to county printers or BusinessObjects accounts
 * based on report type and county configuration.
 *
 * Report categories:
 * - Section 8: Management reports (caseload, financial, provider)
 * - Section 10: Payment reports (warrant, payroll, tax)
 * - Section 11: Eligibility reports (Medi-Cal, SOC, assessment)
 * - Section 12: Provider reports (enrollment, certification, hours)
 * - Section 13: County-specific reports (custom, ad-hoc, regulatory)
 *
 * Data Flow:
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 * |  Our Database   |--->|  This Job       |--->|  Report Files   |--->|  County Printers|
 * |  (Case, Payment,|    |  (Determine     |    |  (PDF/CSV)      |    |  or BO Accounts |
 * |   Provider Data)|    |   & Generate)   |    |                 |    |                 |
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 *
 * Job Flow:
 * Step 1: Determine which reports are due tonight
 * Step 2: Generate report files (mock)
 * Step 3: Distribute to county printers/BO accounts (mock)
 * Step 4: Summary with report counts
 *
 * Legacy Reference: DSD 8.x/10-13.x Nightly report generation and distribution
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class NightlyReportsGenerationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean(name = "nightlyReportsGenerationJob")
    public Job nightlyReportsGenerationJob() {
        return new JobBuilder("NIGHTLY_REPORTS_GENERATION_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(reportsDetermineScheduleStep())
                .next(reportsGenerateFilesStep())
                .next(reportsDistributeStep())
                .next(reportsGenerationSummaryStep())
                .build();
    }

    // ==========================================
    // STEP 1: Determine Report Schedule
    // ==========================================

    @Bean
    public Step reportsDetermineScheduleStep() {
        return new StepBuilder("REPORTS_DETERMINE_SCHEDULE", jobRepository)
                .tasklet(reportsDetermineScheduleTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet reportsDetermineScheduleTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[NIGHTLY_REPORTS] Step 1 - Determining which reports are due tonight...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();
            executionContext.put("totalSteps", 4);

            LocalDate today = LocalDate.now();
            int dayOfMonth = today.getDayOfMonth();
            int dayOfWeek = today.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
            boolean isMonthEnd = today.plusDays(1).getMonthValue() != today.getMonthValue();
            boolean isQuarterEnd = isMonthEnd && (today.getMonthValue() % 3 == 0);

            AtomicInteger dailyReports = new AtomicInteger(0);
            AtomicInteger weeklyReports = new AtomicInteger(0);
            AtomicInteger monthlyReports = new AtomicInteger(0);
            AtomicInteger quarterlyReports = new AtomicInteger(0);

            // Daily reports (always run)
            // - Daily caseload activity report
            // - Daily payment processing report
            // - Daily provider enrollment report
            dailyReports.set(3);

            // Weekly reports (Fridays)
            if (dayOfWeek == 5) {
                // - Weekly caseload summary
                // - Weekly payment summary
                // - Weekly eligibility status report
                weeklyReports.set(3);
            }

            // Monthly reports (last business day of month)
            if (isMonthEnd) {
                // - Monthly management caseload report (DSD 8)
                // - Monthly payment reconciliation (DSD 10)
                // - Monthly eligibility summary (DSD 11)
                // - Monthly provider hours report (DSD 12)
                // - County-specific monthly reports (DSD 13)
                monthlyReports.set(5);
            }

            // Quarterly reports (end of quarter)
            if (isQuarterEnd) {
                // - Quarterly financial summary
                // - Quarterly provider certification report
                // - Quarterly regulatory compliance report
                quarterlyReports.set(3);
            }

            int totalReports = dailyReports.get() + weeklyReports.get()
                    + monthlyReports.get() + quarterlyReports.get();

            log.info("[NIGHTLY_REPORTS] Date: {} (Day of week: {}, Day of month: {})",
                    today, dayOfWeek, dayOfMonth);
            log.info("[NIGHTLY_REPORTS] Daily reports: {}", dailyReports.get());
            log.info("[NIGHTLY_REPORTS] Weekly reports: {} {}", weeklyReports.get(),
                    dayOfWeek == 5 ? "(Friday)" : "(not Friday)");
            log.info("[NIGHTLY_REPORTS] Monthly reports: {} {}", monthlyReports.get(),
                    isMonthEnd ? "(month-end)" : "(not month-end)");
            log.info("[NIGHTLY_REPORTS] Quarterly reports: {} {}", quarterlyReports.get(),
                    isQuarterEnd ? "(quarter-end)" : "(not quarter-end)");
            log.info("[NIGHTLY_REPORTS] Total reports to generate: {}", totalReports);

            executionContext.put("dailyReportCount", dailyReports.get());
            executionContext.put("weeklyReportCount", weeklyReports.get());
            executionContext.put("monthlyReportCount", monthlyReports.get());
            executionContext.put("quarterlyReportCount", quarterlyReports.get());
            executionContext.put("totalReportCount", totalReports);
            executionContext.put("reportDate", today.toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate Report Files
    // ==========================================

    @Bean
    public Step reportsGenerateFilesStep() {
        return new StepBuilder("REPORTS_GENERATE_FILES", jobRepository)
                .tasklet(reportsGenerateFilesTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet reportsGenerateFilesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[NIGHTLY_REPORTS] Step 2 - Generating report files...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            int totalReports = (int) executionContext.get("totalReportCount");
            String reportDate = (String) executionContext.get("reportDate");
            AtomicInteger generatedCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            // In production: for each scheduled report:
            // 1. Query database for report data
            // 2. Apply report template (PDF for print, CSV for BO)
            // 3. Generate output file
            // Report files would be stored in a staging directory
            for (int i = 0; i < totalReports; i++) {
                try {
                    String reportType = getReportType(i);
                    String mockFile = String.format("RPT_%s_%s_%03d.pdf",
                            reportType, reportDate.replace("-", ""), i + 1);
                    log.debug("[NIGHTLY_REPORTS] Generated: {}", mockFile);
                    generatedCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("[NIGHTLY_REPORTS] Error generating report {}: {}", i, e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            log.info("[NIGHTLY_REPORTS] In production: report engine would generate actual PDF/CSV files");
            log.info("[NIGHTLY_REPORTS] Generated {} report files, {} errors",
                    generatedCount.get(), errorCount.get());

            executionContext.put("generatedReportCount", generatedCount.get());
            executionContext.put("reportGenerationErrors", errorCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    private String getReportType(int index) {
        return switch (index % 5) {
            case 0 -> "CASELOAD";
            case 1 -> "PAYMENT";
            case 2 -> "ELIGIBILITY";
            case 3 -> "PROVIDER";
            case 4 -> "COUNTY";
            default -> "OTHER";
        };
    }

    // ==========================================
    // STEP 3: Distribute Reports
    // ==========================================

    @Bean
    public Step reportsDistributeStep() {
        return new StepBuilder("REPORTS_DISTRIBUTE", jobRepository)
                .tasklet(reportsDistributeTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet reportsDistributeTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[NIGHTLY_REPORTS] Step 3 - Distributing reports to county printers/BO accounts...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            int generatedCount = (int) executionContext.get("generatedReportCount");
            AtomicInteger printedCount = new AtomicInteger(0);
            AtomicInteger boDeliveredCount = new AtomicInteger(0);
            AtomicInteger distributeErrors = new AtomicInteger(0);

            // In production: for each generated report:
            // - PDF reports: send to county printer queues via print service
            // - CSV/BO reports: upload to BusinessObjects accounts
            // - Email notifications sent to report subscribers
            // Mock distribution
            int toPrint = (int) (generatedCount * 0.6); // 60% go to printers
            int toBO = generatedCount - toPrint;           // 40% go to BO
            printedCount.set(toPrint);
            boDeliveredCount.set(toBO);

            log.info("[NIGHTLY_REPORTS] In production: reports would be sent to county printers and BO accounts");
            log.info("[NIGHTLY_REPORTS] Distributed to printers: {}", printedCount.get());
            log.info("[NIGHTLY_REPORTS] Delivered to BO accounts: {}", boDeliveredCount.get());

            executionContext.put("printedCount", printedCount.get());
            executionContext.put("boDeliveredCount", boDeliveredCount.get());
            executionContext.put("distributeErrors", distributeErrors.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Summary
    // ==========================================

    @Bean
    public Step reportsGenerationSummaryStep() {
        return new StepBuilder("REPORTS_GENERATION_SUMMARY", jobRepository)
                .tasklet(reportsGenerationSummaryTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet reportsGenerationSummaryTasklet() {
        return (contribution, chunkContext) -> {
            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            String reportDate = (String) executionContext.get("reportDate");
            int dailyCount = (int) executionContext.get("dailyReportCount");
            int weeklyCount = (int) executionContext.get("weeklyReportCount");
            int monthlyCount = (int) executionContext.get("monthlyReportCount");
            int quarterlyCount = (int) executionContext.get("quarterlyReportCount");
            int totalScheduled = (int) executionContext.get("totalReportCount");
            int generatedCount = (int) executionContext.get("generatedReportCount");
            int genErrors = (int) executionContext.get("reportGenerationErrors");
            int printedCount = (int) executionContext.get("printedCount");
            int boCount = (int) executionContext.get("boDeliveredCount");
            int distErrors = (int) executionContext.get("distributeErrors");

            log.info("================================================");
            log.info("  NIGHTLY_REPORTS_GENERATION_JOB COMPLETED");
            log.info("  DSD Sections 8, 10-13 - Nightly Reports");
            log.info("================================================");
            log.info("  Report Date: {}", reportDate);
            log.info("------------------------------------------------");
            log.info("  Scheduled Reports:");
            log.info("    Daily:     {}", dailyCount);
            log.info("    Weekly:    {}", weeklyCount);
            log.info("    Monthly:   {}", monthlyCount);
            log.info("    Quarterly: {}", quarterlyCount);
            log.info("    Total:     {}", totalScheduled);
            log.info("------------------------------------------------");
            log.info("  Generation Statistics:");
            log.info("    Reports Generated: {}", generatedCount);
            log.info("    Generation Errors: {}", genErrors);
            log.info("------------------------------------------------");
            log.info("  Distribution Statistics:");
            log.info("    Sent to Printers:    {}", printedCount);
            log.info("    Sent to BO Accounts: {}", boCount);
            log.info("    Distribution Errors: {}", distErrors);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
