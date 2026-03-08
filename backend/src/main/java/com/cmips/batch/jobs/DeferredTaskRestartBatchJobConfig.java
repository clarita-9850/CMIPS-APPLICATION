package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.scheduler.DeferredTaskRestartJob;
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
 * Wraps DeferredTaskRestartJob.restartDeferredTasks() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeferredTaskRestartBatchJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final DeferredTaskRestartJob deferredTaskRestartJob;

    @Bean(name = "deferredTaskRestartBatchJob")
    public Job deferredTaskRestartBatchJob() {
        return new JobBuilder("DEFERRED_TASK_RESTART_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(deferredTaskRestartStep())
                .build();
    }

    @Bean
    public Step deferredTaskRestartStep() {
        return new StepBuilder("DEFERRED_TASK_RESTART_STEP", jobRepository)
                .tasklet(deferredTaskRestartTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet deferredTaskRestartTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[DEFERRED_TASK_RESTART_JOB] Starting deferred task restart...");
            deferredTaskRestartJob.restartDeferredTasks();
            log.info("[DEFERRED_TASK_RESTART_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
