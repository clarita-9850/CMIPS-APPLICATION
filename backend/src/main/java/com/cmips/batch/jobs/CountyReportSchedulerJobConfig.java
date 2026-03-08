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
 * Wraps ScheduledReportService.generateCountyReportsForScheduler() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CountyReportSchedulerJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ScheduledReportService scheduledReportService;

    @Bean(name = "countyReportSchedulerJob")
    public Job countyReportSchedulerJob() {
        return new JobBuilder("COUNTY_REPORT_SCHEDULER_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(countyReportSchedulerStep())
                .build();
    }

    @Bean
    public Step countyReportSchedulerStep() {
        return new StepBuilder("COUNTY_REPORT_SCHEDULER_STEP", jobRepository)
                .tasklet(countyReportSchedulerTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet countyReportSchedulerTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[COUNTY_REPORT_SCHEDULER_JOB] Starting county report generation...");
            scheduledReportService.generateCountyReportsForScheduler();
            log.info("[COUNTY_REPORT_SCHEDULER_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
