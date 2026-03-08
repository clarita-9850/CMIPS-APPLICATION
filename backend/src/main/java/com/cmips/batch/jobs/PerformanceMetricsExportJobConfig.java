package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for PERFORMANCE_METRICS_EXPORT_JOB.
 *
 * DSD Section 5: Runs nightly to export batch job performance metrics to a reporting CSV.
 * This is the key missing AutoSys monitoring feature - queries Spring Batch metadata tables
 * (BATCH_JOB_EXECUTION) for today's runs and produces a CSV report.
 *
 * Job Flow:
 * ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
 * │ Query Job    │───▶│  Build CSV   │───▶│  Write CSV   │───▶│   Summary    │
 * │ Executions   │    │   Content    │    │  to Output   │    │    Step      │
 * └──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class PerformanceMetricsExportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final JobExplorer jobExplorer;

    private static final String METRICS_OUTPUT_DIR = "./reports/performance-metrics";

    /**
     * Define the PERFORMANCE_METRICS_EXPORT_JOB.
     * The bean name "performanceMetricsExportJob" is used by the trigger service.
     */
    @Bean
    public Job performanceMetricsExportJob() {
        return new JobBuilder("PERFORMANCE_METRICS_EXPORT_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(queryJobExecutionsStep())
            .next(buildMetricsCsvStep())
            .next(writeMetricsCsvStep())
            .next(performanceMetricsSummaryStep())
            .build();
    }

    // ── Step 1: Query today's job executions from JobExplorer ──────────────

    @Bean
    public Step queryJobExecutionsStep() {
        return new StepBuilder("queryJobExecutionsStep", jobRepository)
            .listener(stepListener)
            .tasklet(queryJobExecutionsTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet queryJobExecutionsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Querying today's job executions from Spring Batch metadata");
            log.info("Is Virtual Thread: {}", Thread.currentThread().isVirtual());

            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            execContext.put("totalSteps", 4);

            // Query all job names known to the JobExplorer
            List<String> jobNames = jobExplorer.getJobNames();
            log.info("Found {} registered job names in Spring Batch metadata", jobNames.size());

            AtomicInteger totalExecutions = new AtomicInteger(0);
            List<String> executionRows = new ArrayList<>();

            String serverName;
            try {
                serverName = InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                serverName = "CMIPS-BATCH-SERVER";
            }

            LocalDate today = LocalDate.now();

            for (String jobName : jobNames) {
                // Get recent instances for each job
                var instances = jobExplorer.getJobInstances(jobName, 0, 100);

                for (var instance : instances) {
                    List<JobExecution> executions = jobExplorer.getJobExecutions(instance);

                    for (JobExecution execution : executions) {
                        // Filter to today's executions
                        if (execution.getStartTime() != null
                            && execution.getStartTime().toLocalDate().equals(today)) {

                            totalExecutions.incrementAndGet();

                            String startTime = execution.getStartTime()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            String endTime = execution.getEndTime() != null
                                ? execution.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                : "RUNNING";

                            String runTime;
                            if (execution.getEndTime() != null) {
                                Duration duration = Duration.between(execution.getStartTime(), execution.getEndTime());
                                runTime = String.format("%02d:%02d:%02d",
                                    duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
                            } else {
                                runTime = "IN_PROGRESS";
                            }

                            String row = String.format("%s,%s,%s,%s,%s,%s",
                                serverName,
                                jobName,
                                startTime,
                                endTime,
                                runTime,
                                execution.getStatus().name());

                            executionRows.add(row);
                        }
                    }
                }
            }

            // Store for next steps
            execContext.put("serverName", serverName);
            execContext.put("executionCount", totalExecutions.get());
            execContext.putString("reportDate", today.format(DateTimeFormatter.ISO_DATE));

            // Store rows as a joined string (ExecutionContext only supports simple types)
            execContext.putString("executionRowData", String.join("\n", executionRows));

            log.info("Found {} job executions for today ({})", totalExecutions.get(), today);

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 2: Build CSV content ──────────────────────────────────────────

    @Bean
    public Step buildMetricsCsvStep() {
        return new StepBuilder("buildMetricsCsvStep", jobRepository)
            .listener(stepListener)
            .tasklet(buildMetricsCsvTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet buildMetricsCsvTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Building performance metrics CSV content");

            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            String rowData = execContext.getString("executionRowData");

            // Build the full CSV with header
            StringBuilder csv = new StringBuilder();
            csv.append("Server Name,Job Name,Start Date/Time,Finish Date/Time,Run Time,Status\n");

            if (rowData != null && !rowData.isEmpty()) {
                csv.append(rowData).append("\n");
            }

            execContext.putString("csvContent", csv.toString());

            log.info("CSV content built: {} bytes", csv.length());

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 3: Write CSV to output directory ──────────────────────────────

    @Bean
    public Step writeMetricsCsvStep() {
        return new StepBuilder("writeMetricsCsvStep", jobRepository)
            .listener(stepListener)
            .tasklet(writeMetricsCsvTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet writeMetricsCsvTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Writing performance metrics CSV to output directory");

            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            String csvContent = execContext.getString("csvContent");
            String reportDate = execContext.getString("reportDate");

            // Create output directory if needed
            Path outputDir = Paths.get(METRICS_OUTPUT_DIR);
            Files.createDirectories(outputDir);

            // Generate filename with date
            String filename = String.format("batch_performance_metrics_%s.csv", reportDate);
            Path outputPath = outputDir.resolve(filename);

            // Write CSV file
            Files.writeString(outputPath, csvContent);

            execContext.putString("outputFilePath", outputPath.toString());

            log.info("Performance metrics CSV written to: {}", outputPath);

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 4: Summary ────────────────────────────────────────────────────

    @Bean
    public Step performanceMetricsSummaryStep() {
        return new StepBuilder("performanceMetricsSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(performanceMetricsSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet performanceMetricsSummaryTasklet() {
        return (contribution, chunkContext) -> {
            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            String serverName = (String) execContext.get("serverName");
            Integer executionCount = (Integer) execContext.get("executionCount");
            String reportDate = execContext.getString("reportDate");
            String outputFilePath = execContext.getString("outputFilePath");

            log.info("=================================================");
            log.info("  PERFORMANCE_METRICS_EXPORT_JOB COMPLETED");
            log.info("  Server: {}", serverName);
            log.info("  Report Date: {}", reportDate);
            log.info("  Job Executions Found: {}", executionCount);
            log.info("  Output File: {}", outputFilePath);
            log.info("  Executed on Virtual Thread: {}", Thread.currentThread().isVirtual());
            log.info("=================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
