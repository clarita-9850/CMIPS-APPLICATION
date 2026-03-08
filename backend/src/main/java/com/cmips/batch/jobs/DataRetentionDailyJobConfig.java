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
 * Data retention: daily notification cleanup.
 * Wraps DataRetentionService.purgeOldNotifications() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataRetentionDailyJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final DataRetentionService dataRetentionService;

    @Bean(name = "dataRetentionDailyJob")
    public Job dataRetentionDailyJob() {
        return new JobBuilder("DATA_RETENTION_DAILY_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(dataRetentionDailyStep())
                .build();
    }

    @Bean
    public Step dataRetentionDailyStep() {
        return new StepBuilder("DATA_RETENTION_DAILY_STEP", jobRepository)
                .tasklet(dataRetentionDailyTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet dataRetentionDailyTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[DATA_RETENTION_DAILY_JOB] Starting daily notification purge...");
            dataRetentionService.purgeOldNotifications();
            log.info("[DATA_RETENTION_DAILY_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
