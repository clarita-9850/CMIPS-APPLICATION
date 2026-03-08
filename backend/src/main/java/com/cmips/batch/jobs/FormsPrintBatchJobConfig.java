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
 * Spring Batch Job Configuration for FORMS_PRINT_BATCH.
 *
 * DSD Section 25: Nightly (runs after reports generation) - print forms queued
 * during the day. Workers queue forms (NOAs, SOC 873/874, enrollment forms, etc.)
 * throughout the day. This batch job collects all PENDING forms, generates PDFs,
 * sends them to the print queue, and marks them as PRINTED.
 *
 * Data Flow:
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 * |  ElectronicForm |    |  This Job       |    |  PDF Generation |    |  Print Queue    |
 * |  Entity         |--->|  (Query PENDING |--->|  (Mock)         |--->|  (County        |
 * |  (PENDING)      |    |   & Process)    |    |                 |    |   Printers)     |
 * +-----------------+    +-----------------+    +-----------------+    +-----------------+
 *
 * Job Flow:
 * Step 1: Query PENDING forms from ElectronicFormEntity (or mock)
 * Step 2: Generate PDF files (mock)
 * Step 3: Mark as PRINTED, update print date
 * Step 4: Summary with form counts
 *
 * Legacy Reference: DSD 25.x Nightly forms print batch
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FormsPrintBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean(name = "formsPrintBatchJob")
    public Job formsPrintBatchJob() {
        return new JobBuilder("FORMS_PRINT_BATCH_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(formsQueryPendingStep())
                .next(formsGeneratePdfStep())
                .next(formsMarkPrintedStep())
                .next(formsPrintSummaryStep())
                .build();
    }

    // ==========================================
    // STEP 1: Query PENDING Forms
    // ==========================================

    @Bean
    public Step formsQueryPendingStep() {
        return new StepBuilder("FORMS_QUERY_PENDING", jobRepository)
                .tasklet(formsQueryPendingTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet formsQueryPendingTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[FORMS_PRINT_BATCH] Step 1 - Querying PENDING forms for print...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();
            executionContext.put("totalSteps", 4);

            AtomicInteger noaCount = new AtomicInteger(0);
            AtomicInteger socCount = new AtomicInteger(0);
            AtomicInteger enrollmentCount = new AtomicInteger(0);
            AtomicInteger otherCount = new AtomicInteger(0);

            // In production: query ElectronicFormEntity or NoticeOfActionEntity
            // SELECT * FROM electronic_forms WHERE status = 'PENDING' AND print_requested = true
            // SELECT * FROM notice_of_action WHERE status = 'PENDING'
            // Mock form counts by type
            noaCount.set(85);          // NOA forms (NA-1250 through NA-1257)
            socCount.set(42);          // SOC 873/874 Health Care Certification forms
            enrollmentCount.set(28);   // Provider enrollment/agreement forms
            otherCount.set(15);        // Miscellaneous forms

            int totalPending = noaCount.get() + socCount.get() + enrollmentCount.get() + otherCount.get();

            log.info("[FORMS_PRINT_BATCH] PENDING forms found:");
            log.info("[FORMS_PRINT_BATCH]   NOA forms (NA-1250..1257): {}", noaCount.get());
            log.info("[FORMS_PRINT_BATCH]   SOC 873/874 forms:        {}", socCount.get());
            log.info("[FORMS_PRINT_BATCH]   Enrollment forms:          {}", enrollmentCount.get());
            log.info("[FORMS_PRINT_BATCH]   Other forms:               {}", otherCount.get());
            log.info("[FORMS_PRINT_BATCH]   Total PENDING:             {}", totalPending);

            executionContext.put("noaFormCount", noaCount.get());
            executionContext.put("socFormCount", socCount.get());
            executionContext.put("enrollmentFormCount", enrollmentCount.get());
            executionContext.put("otherFormCount", otherCount.get());
            executionContext.put("totalPendingCount", totalPending);

            if (totalPending == 0) {
                log.info("[FORMS_PRINT_BATCH] No PENDING forms to print tonight");
                executionContext.put("skipProcessing", true);
            } else {
                executionContext.put("skipProcessing", false);
            }

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate PDF Files
    // ==========================================

    @Bean
    public Step formsGeneratePdfStep() {
        return new StepBuilder("FORMS_GENERATE_PDF", jobRepository)
                .tasklet(formsGeneratePdfTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet formsGeneratePdfTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[FORMS_PRINT_BATCH] Step 2 - Generating PDF files...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("[FORMS_PRINT_BATCH] Skipping PDF generation - no PENDING forms");
                return RepeatStatus.FINISHED;
            }

            int totalPending = (int) executionContext.get("totalPendingCount");
            AtomicInteger generatedCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            // In production: for each PENDING form:
            // 1. Load form template (NOA, SOC, enrollment, etc.)
            // 2. Populate with case/recipient/provider data
            // 3. Generate PDF using template engine
            // 4. Store PDF in staging directory for print queue
            for (int i = 0; i < totalPending; i++) {
                try {
                    String formType = getFormType(i, executionContext);
                    String mockPdf = String.format("FORM_%s_%s_%04d.pdf",
                            formType,
                            LocalDate.now().toString().replace("-", ""),
                            i + 1);
                    log.debug("[FORMS_PRINT_BATCH] Generated: {}", mockPdf);
                    generatedCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("[FORMS_PRINT_BATCH] Error generating PDF {}: {}", i, e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            log.info("[FORMS_PRINT_BATCH] In production: PDF template engine would generate actual form PDFs");
            log.info("[FORMS_PRINT_BATCH] Generated {} PDFs, {} errors",
                    generatedCount.get(), errorCount.get());

            executionContext.put("generatedPdfCount", generatedCount.get());
            executionContext.put("pdfGenerationErrors", errorCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    private String getFormType(int index, org.springframework.batch.item.ExecutionContext ctx) {
        int noaCount = (int) ctx.get("noaFormCount");
        int socCount = (int) ctx.get("socFormCount");
        int enrollCount = (int) ctx.get("enrollmentFormCount");

        if (index < noaCount) return "NOA";
        if (index < noaCount + socCount) return "SOC";
        if (index < noaCount + socCount + enrollCount) return "ENROLL";
        return "OTHER";
    }

    // ==========================================
    // STEP 3: Mark as PRINTED
    // ==========================================

    @Bean
    public Step formsMarkPrintedStep() {
        return new StepBuilder("FORMS_MARK_PRINTED", jobRepository)
                .tasklet(formsMarkPrintedTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet formsMarkPrintedTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[FORMS_PRINT_BATCH] Step 3 - Marking forms as PRINTED and updating print date...");

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("[FORMS_PRINT_BATCH] Skipping - no forms to mark");
                return RepeatStatus.FINISHED;
            }

            int generatedCount = (int) executionContext.get("generatedPdfCount");
            AtomicInteger printedCount = new AtomicInteger(0);
            AtomicInteger printErrors = new AtomicInteger(0);
            LocalDateTime printTimestamp = LocalDateTime.now();

            // In production: for each successfully generated PDF:
            // UPDATE electronic_forms SET status = 'PRINTED', print_date = :now WHERE id = :formId
            // UPDATE notice_of_action SET status = 'PRINTED', print_date = :now WHERE id = :noaId
            // Send to county print queue via print service
            for (int i = 0; i < generatedCount; i++) {
                try {
                    // Mock: mark as printed
                    printedCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("[FORMS_PRINT_BATCH] Error marking form as printed: {}", e.getMessage());
                    printErrors.incrementAndGet();
                }
            }

            log.info("[FORMS_PRINT_BATCH] Marked {} forms as PRINTED at {}", printedCount.get(), printTimestamp);
            log.info("[FORMS_PRINT_BATCH] Sent to county print queues");
            if (printErrors.get() > 0) {
                log.warn("[FORMS_PRINT_BATCH] {} forms failed to update status", printErrors.get());
            }

            executionContext.put("printedCount", printedCount.get());
            executionContext.put("printErrors", printErrors.get());
            executionContext.put("printTimestamp", printTimestamp.toString());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Summary
    // ==========================================

    @Bean
    public Step formsPrintSummaryStep() {
        return new StepBuilder("FORMS_PRINT_SUMMARY", jobRepository)
                .tasklet(formsPrintSummaryTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet formsPrintSummaryTasklet() {
        return (contribution, chunkContext) -> {
            var executionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipped = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipped)) {
                log.info("================================================");
                log.info("  FORMS_PRINT_BATCH_JOB COMPLETED");
                log.info("  DSD Section 25 - Nightly Forms Print");
                log.info("================================================");
                log.info("  Status: SKIPPED (no PENDING forms)");
                log.info("================================================");
                return RepeatStatus.FINISHED;
            }

            int noaCount = (int) executionContext.get("noaFormCount");
            int socCount = (int) executionContext.get("socFormCount");
            int enrollCount = (int) executionContext.get("enrollmentFormCount");
            int otherCount = (int) executionContext.get("otherFormCount");
            int totalPending = (int) executionContext.get("totalPendingCount");
            int generatedCount = (int) executionContext.get("generatedPdfCount");
            int genErrors = (int) executionContext.get("pdfGenerationErrors");
            int printedCount = (int) executionContext.get("printedCount");
            int printErrors = (int) executionContext.get("printErrors");
            String printTimestamp = (String) executionContext.get("printTimestamp");

            log.info("================================================");
            log.info("  FORMS_PRINT_BATCH_JOB COMPLETED");
            log.info("  DSD Section 25 - Nightly Forms Print");
            log.info("================================================");
            log.info("  Print Timestamp: {}", printTimestamp);
            log.info("------------------------------------------------");
            log.info("  PENDING Forms by Type:");
            log.info("    NOA (NA-1250..1257):    {}", noaCount);
            log.info("    SOC 873/874:            {}", socCount);
            log.info("    Enrollment Forms:       {}", enrollCount);
            log.info("    Other Forms:            {}", otherCount);
            log.info("    Total PENDING:          {}", totalPending);
            log.info("------------------------------------------------");
            log.info("  Processing Statistics:");
            log.info("    PDFs Generated:   {}", generatedCount);
            log.info("    Generation Errors: {}", genErrors);
            log.info("    Forms Printed:     {}", printedCount);
            log.info("    Print Errors:      {}", printErrors);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
