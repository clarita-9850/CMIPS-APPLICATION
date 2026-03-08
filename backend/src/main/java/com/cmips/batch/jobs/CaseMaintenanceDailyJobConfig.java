package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.scheduler.CaseMaintenanceBatchJob;
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

/**
 * DSD Section 30: Daily case maintenance triggers (CM-007, CM-008, CM-013, CM-015, etc.)
 * Wraps CaseMaintenanceBatchJob.dailyBatch() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CaseMaintenanceDailyJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final CaseMaintenanceBatchJob caseMaintenanceBatchJob;

    @Bean(name = "caseMaintenanceDailyJob")
    public Job caseMaintenanceDailyJob() {
        return new JobBuilder("CASE_MAINTENANCE_DAILY_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(caseMaintenanceDailyStep())
                .build();
    }

    @Bean
    public Step caseMaintenanceDailyStep() {
        return new StepBuilder("CASE_MAINTENANCE_DAILY_STEP", jobRepository)
                .tasklet(caseMaintenanceDailyTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet caseMaintenanceDailyTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[CASE_MAINTENANCE_DAILY_JOB] Starting daily case maintenance batch...");
            caseMaintenanceBatchJob.dailyBatch();
            log.info("[CASE_MAINTENANCE_DAILY_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
