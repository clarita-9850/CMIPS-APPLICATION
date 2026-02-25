package com.ihss.scheduler.job;

import com.ihss.scheduler.dto.TriggerJobRequest;
import com.ihss.scheduler.entity.JobDefinition;
import com.ihss.scheduler.entity.TriggerType;
import com.ihss.scheduler.repository.JobDefinitionRepository;
import com.ihss.scheduler.service.CalendarService;
import com.ihss.scheduler.service.ExecutionService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Quartz job that evaluates cron expressions and triggers jobs when due.
 * Runs periodically to check which jobs need to be executed.
 */
@Component
public class CronEvaluatorJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(CronEvaluatorJob.class);

    @Autowired
    private JobDefinitionRepository jobRepository;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private CalendarService calendarService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("Running cron evaluation");

        try {
            List<JobDefinition> schedulableJobs = jobRepository.findSchedulableJobs();

            for (JobDefinition job : schedulableJobs) {
                try {
                    evaluateAndTrigger(job);
                } catch (Exception e) {
                    log.error("Error evaluating job {}: {}", job.getJobName(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error in cron evaluation job", e);
            throw new JobExecutionException(e);
        }
    }

    private void evaluateAndTrigger(JobDefinition job) {
        String cronExpression = job.getCronExpression();
        if (cronExpression == null || cronExpression.isBlank()) {
            return;
        }

        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            ZoneId timezone = ZoneId.of(job.getTimezone());
            LocalDateTime now = LocalDateTime.now(timezone);

            // Check if job should run now (within the last minute)
            LocalDateTime lastMinute = now.minusMinutes(1);
            LocalDateTime nextRun = cron.next(lastMinute);

            if (nextRun != null && !nextRun.isAfter(now)) {
                // Check calendar exclusions
                LocalDate today = LocalDate.now(timezone);
                if (calendarService.shouldSkipDate(job.getId(), today)) {
                    log.info("Skipping job {} due to calendar exclusion on {}", job.getJobName(), today);
                    return;
                }

                log.info("Triggering scheduled job: {}", job.getJobName());

                executionService.triggerJob(
                    job.getId(),
                    new TriggerJobRequest(null, false),
                    "SCHEDULER",
                    TriggerType.SCHEDULED
                );
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid cron expression for job {}: {}", job.getJobName(), cronExpression);
        } catch (Exception e) {
            log.error("Failed to trigger job {}: {}", job.getJobName(), e.getMessage());
        }
    }
}
