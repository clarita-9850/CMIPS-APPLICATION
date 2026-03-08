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
 * Spring Batch Job Configuration for CASE_COUNT_UPDATE_JOB.
 *
 * DSD Section 20: Runs nightly to count Active cases per worker
 * and update field statistics for caseload management.
 *
 * Job Flow:
 * ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
 * │ Count Cases  │───▶│ Update Worker│───▶│   Summary    │
 * │ by Status &  │    │  Caseload    │    │    Step      │
 * │   Worker     │    │  Statistics  │    │              │
 * └──────────────┘    └──────────────┘    └──────────────┘
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class CaseCountUpdateJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;

    /**
     * Define the CASE_COUNT_UPDATE_JOB.
     * The bean name "caseCountUpdateJob" is used by the trigger service.
     */
    @Bean
    public Job caseCountUpdateJob() {
        return new JobBuilder("CASE_COUNT_UPDATE_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(countCasesByStatusAndWorkerStep())
            .next(updateWorkerCaseloadStatisticsStep())
            .next(caseCountUpdateSummaryStep())
            .build();
    }

    // ── Step 1: Count cases by status and worker ───────────────────────────

    @Bean
    public Step countCasesByStatusAndWorkerStep() {
        return new StepBuilder("countCasesByStatusAndWorkerStep", jobRepository)
            .listener(stepListener)
            .tasklet(countCasesByStatusAndWorkerTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet countCasesByStatusAndWorkerTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Counting cases by status and worker on thread: {}", Thread.currentThread());
            log.info("Is Virtual Thread: {}", Thread.currentThread().isVirtual());

            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            execContext.put("totalSteps", 3);
            execContext.putString("runDate",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            // Mock: simulate querying case counts by status
            // In production this would be: SELECT worker_id, status, COUNT(*) FROM cases GROUP BY worker_id, status
            int totalActiveCases = 4523;
            int totalPendingCases = 876;
            int totalClosedCases = 12045;
            int totalWorkers = 185;

            execContext.put("totalActiveCases", totalActiveCases);
            execContext.put("totalPendingCases", totalPendingCases);
            execContext.put("totalClosedCases", totalClosedCases);
            execContext.put("totalWorkers", totalWorkers);

            // Mock: per-worker active case counts
            AtomicInteger highCaseloadWorkers = new AtomicInteger(0);
            AtomicInteger normalCaseloadWorkers = new AtomicInteger(0);

            for (int w = 0; w < totalWorkers; w++) {
                // Mock: simulate varying caseloads per worker
                int workerActiveCases = 10 + (w % 40);  // Range: 10-49
                if (workerActiveCases > 35) {
                    highCaseloadWorkers.incrementAndGet(); // Over threshold
                } else {
                    normalCaseloadWorkers.incrementAndGet();
                }
            }

            execContext.put("highCaseloadWorkers", highCaseloadWorkers.get());
            execContext.put("normalCaseloadWorkers", normalCaseloadWorkers.get());

            log.info("Case counts computed. Active: {}, Pending: {}, Workers: {}, High caseload: {}",
                totalActiveCases, totalPendingCases, totalWorkers, highCaseloadWorkers.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 2: Update worker caseload statistics ──────────────────────────

    @Bean
    public Step updateWorkerCaseloadStatisticsStep() {
        return new StepBuilder("updateWorkerCaseloadStatisticsStep", jobRepository)
            .listener(stepListener)
            .tasklet(updateWorkerCaseloadStatisticsTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet updateWorkerCaseloadStatisticsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Updating worker caseload statistics");

            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            Integer totalWorkers = (Integer) execContext.get("totalWorkers");
            Integer totalActiveCases = (Integer) execContext.get("totalActiveCases");

            // Mock: update each worker's statistics record
            // In production: UPDATE worker_statistics SET active_case_count = ?, last_updated = NOW()
            AtomicInteger updatedWorkers = new AtomicInteger(0);

            for (int w = 0; w < totalWorkers; w++) {
                // Mock: update worker statistics table
                updatedWorkers.incrementAndGet();

                if (updatedWorkers.get() % 50 == 0) {
                    log.debug("Updated statistics for {} of {} workers", updatedWorkers.get(), totalWorkers);
                }
            }

            double avgCaseload = totalWorkers > 0 ? (double) totalActiveCases / totalWorkers : 0;

            execContext.put("updatedWorkers", updatedWorkers.get());
            execContext.put("avgCaseload", Math.round(avgCaseload * 100.0) / 100.0);

            log.info("Worker caseload statistics updated for {} workers. Average caseload: {}",
                updatedWorkers.get(), String.format("%.1f", avgCaseload));

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 3: Summary ────────────────────────────────────────────────────

    @Bean
    public Step caseCountUpdateSummaryStep() {
        return new StepBuilder("caseCountUpdateSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(caseCountUpdateSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet caseCountUpdateSummaryTasklet() {
        return (contribution, chunkContext) -> {
            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            String runDate = execContext.getString("runDate");
            Integer totalActiveCases = (Integer) execContext.get("totalActiveCases");
            Integer totalPendingCases = (Integer) execContext.get("totalPendingCases");
            Integer totalClosedCases = (Integer) execContext.get("totalClosedCases");
            Integer totalWorkers = (Integer) execContext.get("totalWorkers");
            Integer highCaseloadWorkers = (Integer) execContext.get("highCaseloadWorkers");
            Integer updatedWorkers = (Integer) execContext.get("updatedWorkers");
            Double avgCaseload = (Double) execContext.get("avgCaseload");

            log.info("=================================================");
            log.info("  CASE_COUNT_UPDATE_JOB COMPLETED");
            log.info("  Run Date: {}", runDate);
            log.info("  Active Cases: {}", totalActiveCases);
            log.info("  Pending Cases: {}", totalPendingCases);
            log.info("  Closed Cases: {}", totalClosedCases);
            log.info("  Total Workers: {}", totalWorkers);
            log.info("  High Caseload Workers (>35): {}", highCaseloadWorkers);
            log.info("  Average Caseload: {}", avgCaseload);
            log.info("  Workers Updated: {}", updatedWorkers);
            log.info("  Executed on Virtual Thread: {}", Thread.currentThread().isVirtual());
            log.info("=================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
