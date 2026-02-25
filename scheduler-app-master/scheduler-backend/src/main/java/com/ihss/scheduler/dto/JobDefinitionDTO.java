package com.ihss.scheduler.dto;

import com.ihss.scheduler.entity.JobStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record JobDefinitionDTO(
    Long id,
    String jobName,
    String jobType,
    String description,
    String cronExpression,
    String timezone,
    JobStatus status,
    Boolean enabled,
    Integer priority,
    Integer maxRetries,
    Integer timeoutSeconds,
    Map<String, Object> jobParameters,
    String[] targetRoles,
    String[] targetCounties,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy,
    List<DependencyDTO> dependencies,
    List<DependencyDTO> dependents,
    String nextFireTime,
    ExecutionSummaryDTO lastExecution
) {
    public static JobDefinitionDTO fromEntity(com.ihss.scheduler.entity.JobDefinition entity) {
        return new JobDefinitionDTO(
            entity.getId(),
            entity.getJobName(),
            entity.getJobType(),
            entity.getDescription(),
            entity.getCronExpression(),
            entity.getTimezone(),
            entity.getStatus(),
            entity.getEnabled(),
            entity.getPriority(),
            entity.getMaxRetries(),
            entity.getTimeoutSeconds(),
            entity.getJobParameters(),
            entity.getTargetRoles(),
            entity.getTargetCounties(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy(),
            null,
            null,
            null,
            null
        );
    }

    public JobDefinitionDTO withDependencies(List<DependencyDTO> dependencies, List<DependencyDTO> dependents) {
        return new JobDefinitionDTO(
            id, jobName, jobType, description, cronExpression, timezone, status, enabled,
            priority, maxRetries, timeoutSeconds, jobParameters, targetRoles, targetCounties,
            createdAt, updatedAt, createdBy, updatedBy, dependencies, dependents, nextFireTime, lastExecution
        );
    }

    public JobDefinitionDTO withScheduleInfo(String nextFireTime, ExecutionSummaryDTO lastExecution) {
        return new JobDefinitionDTO(
            id, jobName, jobType, description, cronExpression, timezone, status, enabled,
            priority, maxRetries, timeoutSeconds, jobParameters, targetRoles, targetCounties,
            createdAt, updatedAt, createdBy, updatedBy, dependencies, dependents, nextFireTime, lastExecution
        );
    }
}
