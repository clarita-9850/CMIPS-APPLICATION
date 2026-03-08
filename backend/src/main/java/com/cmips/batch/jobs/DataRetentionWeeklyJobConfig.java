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
 * Data retention: weekly Sunday purge cycle.
 * Three sequential steps wrapping DataRetentionService methods for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataRetentionWeeklyJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final DataRetentionService dataRetentionService;

    @Bean(name = "dataRetentionWeeklyJob")
    public Job dataRetentionWeeklyJob() {
        return new JobBuilder("DATA_RETENTION_WEEKLY_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(purgeCancelledCaseNotesStep())
                .next(purgeOldTimesheetsStep())
                .next(purgeInactivePersonNotesStep())
                .build();
    }

    @Bean
    public Step purgeCancelledCaseNotesStep() {
        return new StepBuilder("PURGE_CANCELLED_CASE_NOTES_STEP", jobRepository)
                .tasklet(purgeCancelledCaseNotesTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    @Bean
    public Step purgeOldTimesheetsStep() {
        return new StepBuilder("PURGE_OLD_TIMESHEETS_STEP", jobRepository)
                .tasklet(purgeOldTimesheetsTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    @Bean
    public Step purgeInactivePersonNotesStep() {
        return new StepBuilder("PURGE_INACTIVE_PERSON_NOTES_STEP", jobRepository)
                .tasklet(purgeInactivePersonNotesTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet purgeCancelledCaseNotesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[DATA_RETENTION_WEEKLY_JOB] Purging cancelled case notes...");
            dataRetentionService.purgeCancelledCaseNotes();
            log.info("[DATA_RETENTION_WEEKLY_JOB] Cancelled case notes purge completed");
            return RepeatStatus.FINISHED;
        };
    }

    private Tasklet purgeOldTimesheetsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[DATA_RETENTION_WEEKLY_JOB] Purging old timesheets...");
            dataRetentionService.purgeOldTimesheets();
            log.info("[DATA_RETENTION_WEEKLY_JOB] Old timesheets purge completed");
            return RepeatStatus.FINISHED;
        };
    }

    private Tasklet purgeInactivePersonNotesTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[DATA_RETENTION_WEEKLY_JOB] Purging inactive person notes...");
            dataRetentionService.purgeInactivePersonNotes();
            log.info("[DATA_RETENTION_WEEKLY_JOB] Inactive person notes purge completed");
            return RepeatStatus.FINISHED;
        };
    }
}
