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
 * DSD Section 30: Monthly age-based triggers (CM-014, CM-020).
 * Wraps CaseMaintenanceBatchJob.monthlyBatch() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CaseMaintenanceMonthlyJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final CaseMaintenanceBatchJob caseMaintenanceBatchJob;

    @Bean(name = "caseMaintenanceMonthlyJob")
    public Job caseMaintenanceMonthlyJob() {
        return new JobBuilder("CASE_MAINTENANCE_MONTHLY_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(caseMaintenanceMonthlyStep())
                .build();
    }

    @Bean
    public Step caseMaintenanceMonthlyStep() {
        return new StepBuilder("CASE_MAINTENANCE_MONTHLY_STEP", jobRepository)
                .tasklet(caseMaintenanceMonthlyTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet caseMaintenanceMonthlyTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[CASE_MAINTENANCE_MONTHLY_JOB] Starting monthly case maintenance batch...");
            caseMaintenanceBatchJob.monthlyBatch();
            log.info("[CASE_MAINTENANCE_MONTHLY_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
