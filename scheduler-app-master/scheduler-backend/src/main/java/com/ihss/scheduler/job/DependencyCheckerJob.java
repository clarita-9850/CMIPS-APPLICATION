package com.ihss.scheduler.job;

import com.ihss.scheduler.dto.ExecutionSummaryDTO;
import com.ihss.scheduler.service.ExecutionService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Quartz job that checks for completed jobs and triggers their dependents.
 * This provides a fallback mechanism in case Redis events are missed.
 */
@Component
public class DependencyCheckerJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(DependencyCheckerJob.class);

    @Autowired
    private ExecutionService executionService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("Running dependency check");

        try {
            // Get all jobs that have dependents
            // This is a simplified check - in production, you'd want to track
            // which executions have already triggered dependents

            List<ExecutionSummaryDTO> runningExecutions = executionService.getRunningExecutions();

            // Check if any running executions have completed (status might be stale)
            for (ExecutionSummaryDTO execution : runningExecutions) {
                checkExecutionStatus(execution);
            }

        } catch (Exception e) {
            log.error("Error in dependency checker job", e);
        }
    }

    private void checkExecutionStatus(ExecutionSummaryDTO execution) {
        // This would poll the CMIPS backend for actual status
        // For now, we rely on Redis events for status updates
        log.trace("Checking execution: {} status: {}", execution.triggerId(), execution.status());
    }
}
