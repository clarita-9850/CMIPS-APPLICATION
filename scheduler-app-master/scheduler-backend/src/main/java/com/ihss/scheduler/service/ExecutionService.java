package com.ihss.scheduler.service;

import com.ihss.scheduler.dto.ExecutionSummaryDTO;
import com.ihss.scheduler.dto.JobEventDTO;
import com.ihss.scheduler.dto.TriggerJobRequest;
import com.ihss.scheduler.entity.*;
import com.ihss.scheduler.exception.JobNotFoundException;
import com.ihss.scheduler.exception.JobNotRunnableException;
import com.ihss.scheduler.repository.ExecutionMappingRepository;
import com.ihss.scheduler.repository.JobDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);

    private final ExecutionMappingRepository executionRepository;
    private final JobDefinitionRepository jobRepository;
    private final CmipsBackendClient cmipsBackendClient;
    private final AuditService auditService;
    private final DependencyService dependencyService;

    public ExecutionService(
            ExecutionMappingRepository executionRepository,
            JobDefinitionRepository jobRepository,
            CmipsBackendClient cmipsBackendClient,
            AuditService auditService,
            DependencyService dependencyService) {
        this.executionRepository = executionRepository;
        this.jobRepository = jobRepository;
        this.cmipsBackendClient = cmipsBackendClient;
        this.auditService = auditService;
        this.dependencyService = dependencyService;
    }

    public ExecutionSummaryDTO triggerJob(Long jobId, TriggerJobRequest request, String triggeredBy, TriggerType triggerType) {
        log.info("Triggering job ID: {} by user: {} with type: {}", jobId, triggeredBy, triggerType);

        JobDefinition job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
            .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

        // Check if job can run
        if (!job.canRun() && triggerType != TriggerType.MANUAL) {
            throw new JobNotRunnableException("Job '" + job.getJobName() + "' cannot run. Status: " + job.getStatus() + ", Enabled: " + job.getEnabled());
        }

        // Check for already running executions
        List<ExecutionMapping> running = executionRepository.findRunningExecutionsForJob(jobId);
        if (!running.isEmpty()) {
            throw new JobNotRunnableException("Job '" + job.getJobName() + "' is already running");
        }

        // Check dependencies (unless explicitly skipped for manual triggers)
        if (!Boolean.TRUE.equals(request.skipDependencyCheck()) && triggerType != TriggerType.MANUAL) {
            boolean dependenciesMet = dependencyService.areAllDependenciesSatisfied(
                jobId,
                new DependencyService.LocalDateTimeHolder(LocalDateTime.now())
            );
            if (!dependenciesMet) {
                throw new JobNotRunnableException("Dependencies not satisfied for job: " + job.getJobName());
            }
        }

        // Generate trigger ID
        String triggerId = UUID.randomUUID().toString();

        // Create execution mapping
        ExecutionMapping execution = new ExecutionMapping();
        execution.setJobDefinition(job);
        execution.setTriggerId(triggerId);
        execution.setStatus(ExecutionStatus.TRIGGERED);
        execution.setTriggerType(triggerType);
        execution.setTriggeredBy(triggeredBy);
        execution.setTriggeredAt(LocalDateTime.now());

        ExecutionMapping saved = executionRepository.save(execution);

        // Merge parameters
        Map<String, Object> params = job.getJobParameters() != null ?
            new java.util.HashMap<>(job.getJobParameters()) : new java.util.HashMap<>();
        if (request != null && request.parameters() != null) {
            params.putAll(request.parameters());
        }

        // Call CMIPS backend
        try {
            cmipsBackendClient.triggerJob(job.getJobName(), triggerId, params);
        } catch (Exception e) {
            log.error("Failed to trigger job in CMIPS backend", e);
            saved.setStatus(ExecutionStatus.FAILED);
            saved.setErrorMessage("Failed to trigger job: " + e.getMessage());
            saved.setCompletedAt(LocalDateTime.now());
            executionRepository.save(saved);
            throw e;
        }

        auditService.logAction(
            "JOB_DEFINITION",
            jobId,
            AuditAction.TRIGGER,
            triggeredBy,
            null,
            null,
            "Triggered job: " + job.getJobName() + " (trigger ID: " + triggerId + ")"
        );

        return ExecutionSummaryDTO.fromEntity(saved);
    }

    public void stopExecution(String triggerId, String stoppedBy) {
        log.info("Stopping execution: {} by user: {}", triggerId, stoppedBy);

        ExecutionMapping execution = executionRepository.findByTriggerId(triggerId)
            .orElseThrow(() -> new JobNotFoundException("Execution not found: " + triggerId));

        if (execution.isTerminal()) {
            throw new IllegalStateException("Execution already completed");
        }

        try {
            cmipsBackendClient.stopJob(triggerId);
        } catch (Exception e) {
            log.error("Failed to stop job in CMIPS backend", e);
        }

        execution.setStatus(ExecutionStatus.STOPPED);
        execution.setCompletedAt(LocalDateTime.now());
        executionRepository.save(execution);

        auditService.logAction(
            "JOB_DEFINITION",
            execution.getJobDefinition().getId(),
            AuditAction.STOP,
            stoppedBy,
            null,
            null,
            "Stopped execution: " + triggerId
        );
    }

    public void handleJobEvent(JobEventDTO event) {
        log.debug("Handling job event: {} for trigger: {}", event.eventType(), event.triggerId());

        Optional<ExecutionMapping> optExecution = executionRepository.findByTriggerId(event.triggerId());
        if (optExecution.isEmpty()) {
            log.warn("Received event for unknown trigger ID: {}", event.triggerId());
            return;
        }

        ExecutionMapping execution = optExecution.get();

        switch (event.eventType()) {
            case JobEventDTO.EVENT_STARTED -> {
                execution.setStatus(ExecutionStatus.RUNNING);
                execution.setStartedAt(event.timestamp());
                execution.setCmipsExecutionId(event.cmipsExecutionId());
                execution.setSpringBatchExecutionId(event.springBatchExecutionId());
            }
            case JobEventDTO.EVENT_PROGRESS -> {
                execution.setProgressPercentage(event.progressPercentage());
                execution.setProgressMessage(event.progressMessage());
            }
            case JobEventDTO.EVENT_COMPLETED -> {
                execution.setStatus(ExecutionStatus.COMPLETED);
                execution.setCompletedAt(event.timestamp());
                execution.setProgressPercentage(100);
                // Trigger dependent jobs
                triggerDependentJobs(execution);
            }
            case JobEventDTO.EVENT_FAILED -> {
                execution.setStatus(ExecutionStatus.FAILED);
                execution.setCompletedAt(event.timestamp());
                execution.setErrorMessage(event.errorMessage());
                // Handle retry logic
                handleRetry(execution);
            }
        }

        executionRepository.save(execution);
    }

    @Transactional(readOnly = true)
    public ExecutionSummaryDTO getExecution(String triggerId) {
        ExecutionMapping execution = executionRepository.findByTriggerId(triggerId)
            .orElseThrow(() -> new JobNotFoundException("Execution not found: " + triggerId));
        return ExecutionSummaryDTO.fromEntity(execution);
    }

    @Transactional(readOnly = true)
    public Page<ExecutionSummaryDTO> getExecutionsForJob(Long jobId, Pageable pageable) {
        return executionRepository.findByJobDefinitionId(jobId, pageable)
            .map(ExecutionSummaryDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ExecutionSummaryDTO> getRunningExecutions() {
        return executionRepository.findAllRunningExecutions()
            .stream()
            .map(ExecutionSummaryDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ExecutionSummaryDTO> getRecentExecutions(LocalDateTime since, Pageable pageable) {
        return executionRepository.findRecentWithJobDetails(since, pageable)
            .map(ExecutionSummaryDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<ExecutionSummaryDTO> getLastSuccessfulExecution(Long jobId) {
        return executionRepository.findLastSuccessfulExecutions(jobId, PageRequest.of(0, 1))
            .stream()
            .findFirst()
            .map(ExecutionSummaryDTO::fromEntity);
    }

    private void triggerDependentJobs(ExecutionMapping completedExecution) {
        log.info("Checking dependent jobs for: {}", completedExecution.getJobDefinition().getJobName());

        List<com.ihss.scheduler.dto.DependencyDTO> dependents =
            dependencyService.getDependents(completedExecution.getJobDefinition().getId());

        for (com.ihss.scheduler.dto.DependencyDTO dependent : dependents) {
            // Check dependency type
            if (dependent.dependencyType() == DependencyType.SUCCESS) {
                try {
                    triggerJob(
                        dependent.jobId(),
                        new TriggerJobRequest(null, false),
                        "SYSTEM",
                        TriggerType.DEPENDENCY
                    );
                } catch (Exception e) {
                    log.error("Failed to trigger dependent job: {}", dependent.jobName(), e);
                }
            }
        }
    }

    private void handleRetry(ExecutionMapping failedExecution) {
        JobDefinition job = failedExecution.getJobDefinition();

        if (failedExecution.getRetryCount() < job.getMaxRetries()) {
            log.info("Scheduling retry {} for job: {}",
                failedExecution.getRetryCount() + 1, job.getJobName());

            // In a real implementation, this would schedule a delayed retry
            // For now, we just increment the retry count
            failedExecution.setRetryCount(failedExecution.getRetryCount() + 1);
        }
    }
}
