package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.scheduler.TaskEscalationJob;
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
 * Wraps TaskEscalationJob.checkOverdueTasks() for external scheduler invocation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TaskEscalationBatchJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final TaskEscalationJob taskEscalationJob;

    @Bean(name = "taskEscalationBatchJob")
    public Job taskEscalationBatchJob() {
        return new JobBuilder("TASK_ESCALATION_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(taskEscalationStep())
                .build();
    }

    @Bean
    public Step taskEscalationStep() {
        return new StepBuilder("TASK_ESCALATION_STEP", jobRepository)
                .tasklet(taskEscalationTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet taskEscalationTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[TASK_ESCALATION_JOB] Starting overdue task check...");
            taskEscalationJob.checkOverdueTasks();
            log.info("[TASK_ESCALATION_JOB] Completed");
            return RepeatStatus.FINISHED;
        };
    }
}
