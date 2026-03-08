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
 * DSD Section 31: Nightly NOA + forms print pipeline.
 * Wraps CaseMaintenanceBatchJob.nightlyBatchPrint() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class NightlyBatchPrintJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final CaseMaintenanceBatchJob caseMaintenanceBatchJob;

    @Bean(name = "nightlyBatchPrintJob")
    public Job nightlyBatchPrintJob() {
        return new JobBuilder("NIGHTLY_BATCH_PRINT_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(nightlyBatchPrintStep())
                .build();
    }

    @Bean
    public Step nightlyBatchPrintStep() {
        return new StepBuilder("NIGHTLY_BATCH_PRINT_STEP", jobRepository)
                .tasklet(nightlyBatchPrintTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet nightlyBatchPrintTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[NIGHTLY_BATCH_PRINT_JOB] Starting nightly batch print...");
            caseMaintenanceBatchJob.nightlyBatchPrint();
            log.info("[NIGHTLY_BATCH_PRINT_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
