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
 * Wraps ScheduledReportService.generateYearlyReports() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ScheduledReportYearlyJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ScheduledReportService scheduledReportService;

    @Bean(name = "scheduledReportYearlyJob")
    public Job scheduledReportYearlyJob() {
        return new JobBuilder("SCHEDULED_REPORT_YEARLY_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(scheduledReportYearlyStep())
                .build();
    }

    @Bean
    public Step scheduledReportYearlyStep() {
        return new StepBuilder("SCHEDULED_REPORT_YEARLY_STEP", jobRepository)
                .tasklet(scheduledReportYearlyTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet scheduledReportYearlyTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SCHEDULED_REPORT_YEARLY_JOB] Starting yearly report generation...");
            scheduledReportService.generateYearlyReports();
            log.info("[SCHEDULED_REPORT_YEARLY_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
