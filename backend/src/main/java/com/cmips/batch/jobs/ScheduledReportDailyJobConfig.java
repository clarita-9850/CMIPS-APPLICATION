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
 * Wraps ScheduledReportService.generateDailyReports() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ScheduledReportDailyJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ScheduledReportService scheduledReportService;

    @Bean(name = "scheduledReportDailyJob")
    public Job scheduledReportDailyJob() {
        return new JobBuilder("SCHEDULED_REPORT_DAILY_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(scheduledReportDailyStep())
                .build();
    }

    @Bean
    public Step scheduledReportDailyStep() {
        return new StepBuilder("SCHEDULED_REPORT_DAILY_STEP", jobRepository)
                .tasklet(scheduledReportDailyTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet scheduledReportDailyTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SCHEDULED_REPORT_DAILY_JOB] Starting daily report generation...");
            scheduledReportService.generateDailyReports();
            log.info("[SCHEDULED_REPORT_DAILY_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
