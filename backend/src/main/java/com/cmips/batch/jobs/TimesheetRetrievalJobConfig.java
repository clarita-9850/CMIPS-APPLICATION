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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for TIMESHEET_RETRIEVAL_JOB.
 *
 * DSD Section 10-13: Runs every 4 hours to retrieve timesheet files from
 * the Third Party Processor (TPF) and process transactions into case management.
 *
 * Job Flow:
 * ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
 * │  Check TPF   │───▶│ Fetch &      │───▶│  Process     │───▶│   Summary    │
 * │  for Files   │    │ Validate     │    │ Transactions │    │    Step      │
 * └──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class TimesheetRetrievalJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;

    /**
     * Define the TIMESHEET_RETRIEVAL_JOB.
     * The bean name "timesheetRetrievalJob" is used by the trigger service.
     */
    @Bean
    public Job timesheetRetrievalJob() {
        return new JobBuilder("TIMESHEET_RETRIEVAL_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(checkTpfForFilesStep())
            .next(fetchAndValidateFilesStep())
            .next(processTimesheetTransactionsStep())
            .next(timesheetRetrievalSummaryStep())
            .build();
    }

    // ── Step 1: Check TPF for available files ──────────────────────────────

    @Bean
    public Step checkTpfForFilesStep() {
        return new StepBuilder("checkTpfForFilesStep", jobRepository)
            .listener(stepListener)
            .tasklet(checkTpfForFilesTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet checkTpfForFilesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Checking TPF for available timesheet files on thread: {}", Thread.currentThread());
            log.info("Is Virtual Thread: {}", Thread.currentThread().isVirtual());

            String retrievalTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("retrievalTime", retrievalTime);

            // Mock: simulate checking TPF SFTP endpoint for available files
            int availableFiles = 3; // Mock: 3 files found on TPF
            log.info("TPF check complete. Found {} files available for retrieval", availableFiles);

            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("availableFileCount", availableFiles);

            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("totalSteps", 4);

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 2: Fetch and validate files ───────────────────────────────────

    @Bean
    public Step fetchAndValidateFilesStep() {
        return new StepBuilder("fetchAndValidateFilesStep", jobRepository)
            .listener(stepListener)
            .tasklet(fetchAndValidateFilesTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet fetchAndValidateFilesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Fetching and validating timesheet files from TPF");

            Integer availableFiles = (Integer) chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("availableFileCount");

            // Mock: simulate SFTP download and file validation
            AtomicInteger validFiles = new AtomicInteger(0);
            AtomicInteger invalidFiles = new AtomicInteger(0);

            for (int i = 0; i < availableFiles; i++) {
                String filename = String.format("TPF_TIMESHEET_%s_%03d.dat",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), i + 1);

                // Mock: validate file structure (header, record count, checksum)
                boolean isValid = (i < availableFiles - 1); // Mock: last file fails validation
                if (isValid) {
                    validFiles.incrementAndGet();
                    log.info("File {} validated successfully", filename);
                } else {
                    invalidFiles.incrementAndGet();
                    log.warn("File {} failed validation - skipping", filename);
                }
            }

            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("validFileCount", validFiles.get());

            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("invalidFileCount", invalidFiles.get());

            log.info("Fetch & validate complete. Valid: {}, Invalid: {}", validFiles.get(), invalidFiles.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 3: Process timesheet transactions into case management ────────

    @Bean
    public Step processTimesheetTransactionsStep() {
        return new StepBuilder("processTimesheetTransactionsStep", jobRepository)
            .listener(stepListener)
            .tasklet(processTimesheetTransactionsTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet processTimesheetTransactionsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Processing timesheet transactions into case management");

            Integer validFiles = (Integer) chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("validFileCount");

            // Mock: process each valid file's timesheet records
            AtomicInteger totalRecords = new AtomicInteger(0);
            AtomicInteger processedRecords = new AtomicInteger(0);
            AtomicInteger errorRecords = new AtomicInteger(0);

            for (int f = 0; f < validFiles; f++) {
                int recordsInFile = 150 + (f * 75); // Mock: variable records per file
                totalRecords.addAndGet(recordsInFile);

                for (int r = 0; r < recordsInFile; r++) {
                    // Mock: match timesheet to case, validate hours, post to case
                    boolean success = (r % 50 != 0); // Mock: every 50th record has an error
                    if (success) {
                        processedRecords.incrementAndGet();
                    } else {
                        errorRecords.incrementAndGet();
                    }
                }

                log.info("Processed file {}/{}: {} records", f + 1, validFiles, recordsInFile);
            }

            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("totalRecords", totalRecords.get());

            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("processedRecords", processedRecords.get());

            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("errorRecords", errorRecords.get());

            log.info("Transaction processing complete. Total: {}, Processed: {}, Errors: {}",
                totalRecords.get(), processedRecords.get(), errorRecords.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 4: Summary ────────────────────────────────────────────────────

    @Bean
    public Step timesheetRetrievalSummaryStep() {
        return new StepBuilder("timesheetRetrievalSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(timesheetRetrievalSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet timesheetRetrievalSummaryTasklet() {
        return (contribution, chunkContext) -> {
            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            String retrievalTime = (String) execContext.get("retrievalTime");
            Integer availableFiles = (Integer) execContext.get("availableFileCount");
            Integer validFiles = (Integer) execContext.get("validFileCount");
            Integer invalidFiles = (Integer) execContext.get("invalidFileCount");
            Integer totalRecords = (Integer) execContext.get("totalRecords");
            Integer processedRecords = (Integer) execContext.get("processedRecords");
            Integer errorRecords = (Integer) execContext.get("errorRecords");

            log.info("=================================================");
            log.info("  TIMESHEET_RETRIEVAL_JOB COMPLETED");
            log.info("  Retrieval Time: {}", retrievalTime);
            log.info("  Files Available on TPF: {}", availableFiles);
            log.info("  Files Valid: {}", validFiles);
            log.info("  Files Invalid (skipped): {}", invalidFiles);
            log.info("  Total Records: {}", totalRecords);
            log.info("  Records Processed: {}", processedRecords);
            log.info("  Records with Errors: {}", errorRecords);
            log.info("  Executed on Virtual Thread: {}", Thread.currentThread().isVirtual());
            log.info("=================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
