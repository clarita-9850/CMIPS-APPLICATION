package com.ihss.scheduler.job;

import com.ihss.scheduler.entity.ExecutionMapping;
import com.ihss.scheduler.entity.ExecutionStatus;
import com.ihss.scheduler.repository.ExecutionMappingRepository;
import com.ihss.scheduler.service.CmipsBackendClient;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Quartz job that checks for stale executions that might have lost their status updates.
 * Polls CMIPS backend for actual status of long-running or stuck executions.
 */
@Component
public class StaleExecutionCheckerJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(StaleExecutionCheckerJob.class);

    @Autowired
    private ExecutionMappingRepository executionRepository;

    @Autowired
    private CmipsBackendClient cmipsBackendClient;

    @Value("${scheduler.stale.threshold-minutes:30}")
    private int staleThresholdMinutes;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("Running stale execution check");

        try {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(staleThresholdMinutes);
            List<ExecutionMapping> staleExecutions = executionRepository.findStaleRunningExecutions(threshold);

            for (ExecutionMapping execution : staleExecutions) {
                try {
                    checkAndUpdateStatus(execution);
                } catch (Exception e) {
                    log.error("Error checking stale execution {}: {}",
                        execution.getTriggerId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in stale execution checker job", e);
        }
    }

    private void checkAndUpdateStatus(ExecutionMapping execution) {
        log.info("Checking stale execution: {} for job: {}",
            execution.getTriggerId(), execution.getJobDefinition().getJobName());

        try {
            // Poll CMIPS backend for actual status
            Map<String, Object> status = cmipsBackendClient.getJobStatus(execution.getTriggerId());

            if (status != null) {
                String statusStr = (String) status.get("status");
                if (statusStr != null) {
                    ExecutionStatus newStatus = ExecutionStatus.valueOf(statusStr);

                    if (newStatus != execution.getStatus()) {
                        log.info("Updating stale execution {} status from {} to {}",
                            execution.getTriggerId(), execution.getStatus(), newStatus);

                        execution.setStatus(newStatus);

                        if (newStatus == ExecutionStatus.COMPLETED || newStatus == ExecutionStatus.FAILED) {
                            execution.setCompletedAt(LocalDateTime.now());
                        }

                        executionRepository.save(execution);
                    }
                }
            }
        } catch (Exception e) {
            // If we can't reach CMIPS backend, mark as UNKNOWN after extended period
            if (execution.getTriggeredAt().isBefore(LocalDateTime.now().minusHours(2))) {
                log.warn("Marking execution {} as ABANDONED after extended stale period",
                    execution.getTriggerId());

                execution.setStatus(ExecutionStatus.ABANDONED);
                execution.setCompletedAt(LocalDateTime.now());
                execution.setErrorMessage("Execution abandoned - unable to determine status");
                executionRepository.save(execution);
            }
        }
    }
}
