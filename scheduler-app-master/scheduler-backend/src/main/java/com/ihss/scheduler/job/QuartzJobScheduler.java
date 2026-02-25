package com.ihss.scheduler.job;

import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Initializes and schedules the internal Quartz jobs for the scheduler.
 */
@Component
public class QuartzJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(QuartzJobScheduler.class);

    private final Scheduler scheduler;

    @Value("${scheduler.cron.evaluation-interval:60000}")
    private long cronEvaluationInterval;

    @Value("${scheduler.dependency.check-interval:5000}")
    private long dependencyCheckInterval;

    @Value("${scheduler.health.cmips-check-interval:30000}")
    private long healthCheckInterval;

    public QuartzJobScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostConstruct
    public void initialize() throws SchedulerException {
        log.info("Initializing Quartz job scheduler");

        scheduleCronEvaluator();
        scheduleDependencyChecker();
        scheduleStaleExecutionChecker();

        log.info("Quartz job scheduler initialized");
    }

    private void scheduleCronEvaluator() throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(CronEvaluatorJob.class)
            .withIdentity("cronEvaluatorJob", "scheduler")
            .withDescription("Evaluates job cron expressions and triggers jobs when due")
            .storeDurably()
            .build();

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("cronEvaluatorTrigger", "scheduler")
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(cronEvaluationInterval)
                .repeatForever())
            .build();

        scheduleJobIfNotExists(jobDetail, trigger);
        log.info("Scheduled cron evaluator job with interval: {}ms", cronEvaluationInterval);
    }

    private void scheduleDependencyChecker() throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(DependencyCheckerJob.class)
            .withIdentity("dependencyCheckerJob", "scheduler")
            .withDescription("Checks completed jobs and triggers dependents")
            .storeDurably()
            .build();

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("dependencyCheckerTrigger", "scheduler")
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(dependencyCheckInterval)
                .repeatForever())
            .build();

        scheduleJobIfNotExists(jobDetail, trigger);
        log.info("Scheduled dependency checker job with interval: {}ms", dependencyCheckInterval);
    }

    private void scheduleStaleExecutionChecker() throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(StaleExecutionCheckerJob.class)
            .withIdentity("staleExecutionCheckerJob", "scheduler")
            .withDescription("Checks for stale executions and updates their status")
            .storeDurably()
            .build();

        // Run every 5 minutes
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("staleExecutionCheckerTrigger", "scheduler")
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(5)
                .repeatForever())
            .build();

        scheduleJobIfNotExists(jobDetail, trigger);
        log.info("Scheduled stale execution checker job");
    }

    private void scheduleJobIfNotExists(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        if (!scheduler.checkExists(jobDetail.getKey())) {
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            scheduler.addJob(jobDetail, true);
            scheduler.rescheduleJob(trigger.getKey(), trigger);
        }
    }
}
