package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.service.DataRetentionService;
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
 * Data retention: monthly archival flagging.
 * Wraps DataRetentionService.flagClosedCasesForArchival() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataRetentionMonthlyAuditJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final DataRetentionService dataRetentionService;

    @Bean(name = "dataRetentionMonthlyAuditJob")
    public Job dataRetentionMonthlyAuditJob() {
        return new JobBuilder("DATA_RETENTION_MONTHLY_AUDIT_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(dataRetentionMonthlyAuditStep())
                .build();
    }

    @Bean
    public Step dataRetentionMonthlyAuditStep() {
        return new StepBuilder("DATA_RETENTION_MONTHLY_AUDIT_STEP", jobRepository)
                .tasklet(dataRetentionMonthlyAuditTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet dataRetentionMonthlyAuditTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[DATA_RETENTION_MONTHLY_AUDIT_JOB] Starting monthly archival flagging...");
            dataRetentionService.flagClosedCasesForArchival();
            log.info("[DATA_RETENTION_MONTHLY_AUDIT_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
