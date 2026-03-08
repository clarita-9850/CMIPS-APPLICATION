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
 * Wraps ScheduledReportService.generateMonthlyReports() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ScheduledReportMonthlyJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ScheduledReportService scheduledReportService;

    @Bean(name = "scheduledReportMonthlyJob")
    public Job scheduledReportMonthlyJob() {
        return new JobBuilder("SCHEDULED_REPORT_MONTHLY_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(scheduledReportMonthlyStep())
                .build();
    }

    @Bean
    public Step scheduledReportMonthlyStep() {
        return new StepBuilder("SCHEDULED_REPORT_MONTHLY_STEP", jobRepository)
                .tasklet(scheduledReportMonthlyTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet scheduledReportMonthlyTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SCHEDULED_REPORT_MONTHLY_JOB] Starting monthly report generation...");
            scheduledReportService.generateMonthlyReports();
            log.info("[SCHEDULED_REPORT_MONTHLY_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
