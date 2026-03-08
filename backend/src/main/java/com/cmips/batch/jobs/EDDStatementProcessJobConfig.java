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
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for EDD Statement Processing.
 *
 * DSD Section 18: Monthly transformation of EDD statement files from
 * fixed-width positional format to CSV for internal consumption.
 * EDD sends monthly statements containing tax account balances,
 * payment confirmations, and assessment notices.
 *
 * Data Flow:
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────────────────────────────┐
 * │      EDD        │───▶│  Integration    │───▶│  This Job (EDD Statement Process)      │
 * │  (SFTP)         │    │  Hub (Fetch)    │    │  - Fetch inbound file                  │
 * │                 │    │                 │    │  - Parse fixed-width records            │
 * │  Fixed-width    │    │  Delivers raw   │    │  - Transform to CSV                    │
 * │  statement      │    │  file           │    │  - Store/archive                       │
 * └─────────────────┘    └─────────────────┘    └─────────────────────────────────────────┘
 *
 * Schedule: Monthly (replaces AutoSys schedule)
 * Legacy Reference: EDD Statement File Processing
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class EDDStatementProcessJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;

    private static final String SOURCE_SYSTEM = "EDD";
    private static final String FILE_TYPE = "EDD_STATEMENT";

    // Fixed-width field positions for EDD statement records (mock layout)
    private static final int RECORD_TYPE_START = 0;
    private static final int RECORD_TYPE_END = 2;
    private static final int SEIN_START = 2;
    private static final int SEIN_END = 14;
    private static final int AMOUNT_START = 14;
    private static final int AMOUNT_END = 26;
    private static final int DATE_START = 26;
    private static final int DATE_END = 34;
    private static final int DESCRIPTION_START = 34;
    private static final int DESCRIPTION_END = 84;

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean
    public Job eddStatementProcessJob() {
        return new JobBuilder("EDD_STATEMENT_PROCESS_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(fetchInboundEddFileStep())
            .next(parseFixedWidthRecordsStep())
            .next(transformToCsvStep())
            .next(storeAndArchiveStep())
            .next(eddStatementLogSummaryStep())
            .build();
    }

    // ==========================================
    // STEP 1: Fetch Inbound EDD File (Mock)
    // ==========================================

    @Bean
    public Step fetchInboundEddFileStep() {
        return new StepBuilder("fetchInboundEddFileStep", jobRepository)
            .listener(stepListener)
            .tasklet(fetchInboundEddFileTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet fetchInboundEddFileTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 1: Fetching Inbound EDD Statement File ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            executionContext.put("totalSteps", 5);

            // In production: would use Integration Hub to fetch inbound file from EDD SFTP
            // bawFileService.isFileAvailable("EDD", "EDD_STATEMENT");
            // bawFileService.fetchInboundFile("EDD", "EDD_STATEMENT", byte[].class);

            // Mock: simulate receiving a fixed-width statement file
            // In production, check if file is available first
            boolean fileAvailable = true; // Mock - always available for demo

            if (!fileAvailable) {
                log.info("No EDD statement file available this month");
                executionContext.put("skipProcessing", true);
                executionContext.put("skipReason", "No EDD statement file available");
                return RepeatStatus.FINISHED;
            }

            // Mock fixed-width records (each line is 84 characters)
            String[] mockFixedWidthRecords = {
                "HD999-9999-9  000000000000202603011EDD MONTHLY STATEMENT - MARCH 2026              ",
                "PY999-9999-9  000012345670202602151SDI PAYMENT RECEIVED                            ",
                "PY999-9999-9  000003456780202602151UI PAYMENT RECEIVED                             ",
                "PY999-9999-9  000000123450202602151ETT PAYMENT RECEIVED                            ",
                "BA999-9999-9  000015925900202603011ACCOUNT BALANCE                                 ",
                "AS999-9999-9  000000000000202603011NO ASSESSMENTS PENDING                          ",
                "TL999-9999-9  000000060001                                                        "
            };

            String fileReference = "EDDSTMT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

            log.info("Fetched EDD statement file: {} with {} raw records", fileReference, mockFixedWidthRecords.length);

            executionContext.put("skipProcessing", false);
            executionContext.put("fileReference", fileReference);
            executionContext.put("rawRecordCount", mockFixedWidthRecords.length);
            // In production: store raw file content; here we use mock record count
            executionContext.put("mockRecordCount", mockFixedWidthRecords.length);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Parse Fixed-Width Records
    // ==========================================

    @Bean
    public Step parseFixedWidthRecordsStep() {
        return new StepBuilder("parseFixedWidthRecordsStep", jobRepository)
            .listener(stepListener)
            .tasklet(parseFixedWidthRecordsTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet parseFixedWidthRecordsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 2: Parsing Fixed-Width Records ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping parse - no file to process");
                return RepeatStatus.FINISHED;
            }

            int rawRecordCount = (int) executionContext.get("rawRecordCount");

            // Parse fixed-width records according to EDD layout spec
            // Record types: HD=Header, PY=Payment, BA=Balance, AS=Assessment, TL=Trailer
            AtomicInteger headerCount = new AtomicInteger(0);
            AtomicInteger paymentCount = new AtomicInteger(0);
            AtomicInteger balanceCount = new AtomicInteger(0);
            AtomicInteger assessmentCount = new AtomicInteger(0);
            AtomicInteger trailerCount = new AtomicInteger(0);
            AtomicInteger invalidCount = new AtomicInteger(0);

            // Mock parsing - in production, iterate through actual file lines
            // Each record: positions 0-1=type, 2-13=SEIN, 14-25=amount, 26-33=date, 34-83=description
            log.info("Parsing {} fixed-width records (layout: {}+{}+{}+{}+{} = 84 chars)",
                rawRecordCount, "type[2]", "SEIN[12]", "amount[12]", "date[8]", "desc[50]");

            // Mock counts from our test data
            headerCount.set(1);
            paymentCount.set(3);
            balanceCount.set(1);
            assessmentCount.set(1);
            trailerCount.set(1);

            int totalParsed = headerCount.get() + paymentCount.get() + balanceCount.get()
                + assessmentCount.get() + trailerCount.get();

            log.info("Parsed {} records: {} headers, {} payments, {} balances, {} assessments, {} trailers, {} invalid",
                totalParsed, headerCount.get(), paymentCount.get(), balanceCount.get(),
                assessmentCount.get(), trailerCount.get(), invalidCount.get());

            executionContext.put("headerCount", headerCount.get());
            executionContext.put("paymentCount", paymentCount.get());
            executionContext.put("balanceCount", balanceCount.get());
            executionContext.put("assessmentCount", assessmentCount.get());
            executionContext.put("trailerCount", trailerCount.get());
            executionContext.put("invalidCount", invalidCount.get());
            executionContext.put("totalParsed", totalParsed);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Transform to CSV
    // ==========================================

    @Bean
    public Step transformToCsvStep() {
        return new StepBuilder("transformToCsvStep", jobRepository)
            .listener(stepListener)
            .tasklet(transformToCsvTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet transformToCsvTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 3: Transforming to CSV ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping CSV transform");
                return RepeatStatus.FINISHED;
            }

            int totalParsed = (int) executionContext.get("totalParsed");

            // Transform parsed records to CSV format
            // CSV columns: record_type, sein, amount, date, description
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("record_type,sein,amount,date,description\n");

            // Mock CSV generation from parsed records
            // In production: iterate through parsed record objects
            csvContent.append("PY,999-9999-9,123456.70,2026-02-15,SDI PAYMENT RECEIVED\n");
            csvContent.append("PY,999-9999-9,34567.80,2026-02-15,UI PAYMENT RECEIVED\n");
            csvContent.append("PY,999-9999-9,1234.50,2026-02-15,ETT PAYMENT RECEIVED\n");
            csvContent.append("BA,999-9999-9,159259.00,2026-03-01,ACCOUNT BALANCE\n");
            csvContent.append("AS,999-9999-9,0.00,2026-03-01,NO ASSESSMENTS PENDING\n");

            int csvRecordCount = 5; // Data records only (excluding header/trailer)
            String csvFileName = "edd_statement_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + ".csv";

            log.info("Generated CSV file: {} with {} data records", csvFileName, csvRecordCount);

            executionContext.put("csvFileName", csvFileName);
            executionContext.put("csvRecordCount", csvRecordCount);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Store and Archive
    // ==========================================

    @Bean
    public Step storeAndArchiveStep() {
        return new StepBuilder("storeAndArchiveStep", jobRepository)
            .listener(stepListener)
            .tasklet(storeAndArchiveTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet storeAndArchiveTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 4: Storing and Archiving ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping store/archive");
                return RepeatStatus.FINISHED;
            }

            String fileReference = (String) executionContext.get("fileReference");
            String csvFileName = (String) executionContext.get("csvFileName");

            // In production:
            // 1. Store CSV in designated output directory for downstream consumers
            // 2. Archive original fixed-width file to long-term storage
            // 3. Update file processing audit log
            // 4. Acknowledge file processed with Integration Hub
            // bawFileService.acknowledgeFileProcessed("EDD", "EDD_STATEMENT", fileReference);

            log.info("MOCK: Stored CSV file {} to output directory", csvFileName);
            log.info("MOCK: Archived original file {} to long-term storage", fileReference);
            log.info("MOCK: Updated file processing audit log");

            executionContext.put("archived", true);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 5: Log Summary
    // ==========================================

    @Bean
    public Step eddStatementLogSummaryStep() {
        return new StepBuilder("eddStatementLogSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(eddStatementLogSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet eddStatementLogSummaryTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 5: EDD Statement Process Summary ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                String skipReason = (String) executionContext.get("skipReason");
                log.info("================================================");
                log.info("  EDD_STATEMENT_PROCESS JOB SKIPPED");
                log.info("  Reason: {}", skipReason);
                log.info("================================================");
                return RepeatStatus.FINISHED;
            }

            String fileReference = (String) executionContext.get("fileReference");
            String csvFileName = (String) executionContext.get("csvFileName");
            int rawRecordCount = (int) executionContext.get("rawRecordCount");
            int totalParsed = (int) executionContext.get("totalParsed");
            int paymentCount = (int) executionContext.get("paymentCount");
            int balanceCount = (int) executionContext.get("balanceCount");
            int assessmentCount = (int) executionContext.get("assessmentCount");
            int invalidCount = (int) executionContext.get("invalidCount");
            int csvRecordCount = (int) executionContext.get("csvRecordCount");

            log.info("================================================");
            log.info("  EDD_STATEMENT_PROCESS JOB COMPLETED");
            log.info("================================================");
            log.info("  Source: {} ({})", SOURCE_SYSTEM, FILE_TYPE);
            log.info("  Input File: {}", fileReference);
            log.info("  Output File: {}", csvFileName);
            log.info("------------------------------------------------");
            log.info("  Parsing Statistics:");
            log.info("    Raw Records Read: {}", rawRecordCount);
            log.info("    Records Parsed: {}", totalParsed);
            log.info("    Invalid Records: {}", invalidCount);
            log.info("------------------------------------------------");
            log.info("  Record Breakdown:");
            log.info("    Payment Records: {}", paymentCount);
            log.info("    Balance Records: {}", balanceCount);
            log.info("    Assessment Records: {}", assessmentCount);
            log.info("------------------------------------------------");
            log.info("  Output:");
            log.info("    CSV Records Written: {}", csvRecordCount);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
