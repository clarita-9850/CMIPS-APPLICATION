package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.service.ScheduledReportService;
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
 * Wraps ScheduledReportService.generateQuarterlyReports() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ScheduledReportQuarterlyJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ScheduledReportService scheduledReportService;

    @Bean(name = "scheduledReportQuarterlyJob")
    public Job scheduledReportQuarterlyJob() {
        return new JobBuilder("SCHEDULED_REPORT_QUARTERLY_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(scheduledReportQuarterlyStep())
                .build();
    }

    @Bean
    public Step scheduledReportQuarterlyStep() {
        return new StepBuilder("SCHEDULED_REPORT_QUARTERLY_STEP", jobRepository)
                .tasklet(scheduledReportQuarterlyTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet scheduledReportQuarterlyTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SCHEDULED_REPORT_QUARTERLY_JOB] Starting quarterly report generation...");
            scheduledReportService.generateQuarterlyReports();
            log.info("[SCHEDULED_REPORT_QUARTERLY_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
