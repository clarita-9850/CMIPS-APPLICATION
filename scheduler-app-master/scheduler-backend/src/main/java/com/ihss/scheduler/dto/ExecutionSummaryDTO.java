package com.ihss.scheduler.dto;

import com.ihss.scheduler.entity.ExecutionStatus;
import com.ihss.scheduler.entity.TriggerType;

import java.time.LocalDateTime;

public record ExecutionSummaryDTO(
    Long id,
    String triggerId,
    Long jobDefinitionId,
    String jobName,
    ExecutionStatus status,
    TriggerType triggerType,
    LocalDateTime triggeredAt,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    Integer progressPercentage,
    String progressMessage,
    String errorMessage,
    Integer retryCount,
    String triggeredBy,
    Long durationSeconds
) {
    public static ExecutionSummaryDTO fromEntity(com.ihss.scheduler.entity.ExecutionMapping entity) {
        Long duration = null;
        if (entity.getStartedAt() != null && entity.getCompletedAt() != null) {
            duration = java.time.Duration.between(entity.getStartedAt(), entity.getCompletedAt()).getSeconds();
        } else if (entity.getStartedAt() != null) {
            duration = java.time.Duration.between(entity.getStartedAt(), LocalDateTime.now()).getSeconds();
        }

        return new ExecutionSummaryDTO(
            entity.getId(),
            entity.getTriggerId(),
            entity.getJobDefinition().getId(),
            entity.getJobDefinition().getJobName(),
            entity.getStatus(),
            entity.getTriggerType(),
            entity.getTriggeredAt(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getProgressPercentage(),
            entity.getProgressMessage(),
            entity.getErrorMessage(),
            entity.getRetryCount(),
            entity.getTriggeredBy(),
            duration
        );
    }
}
