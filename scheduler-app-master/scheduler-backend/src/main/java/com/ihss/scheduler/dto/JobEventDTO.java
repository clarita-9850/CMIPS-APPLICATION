package com.ihss.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ihss.scheduler.entity.ExecutionStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for job events received from CMIPS backend via Redis pub/sub.
 */
public record JobEventDTO(
    String eventType,
    String triggerId,
    @JsonProperty("executionId") Long cmipsExecutionId,
    Long springBatchExecutionId,
    String jobName,
    ExecutionStatus status,
    Integer progressPercentage,
    String progressMessage,
    String errorMessage,
    LocalDateTime timestamp,
    Map<String, Object> metadata
) {
    public static final String EVENT_STARTED = "JOB_STARTED";
    public static final String EVENT_PROGRESS = "JOB_PROGRESS";
    public static final String EVENT_COMPLETED = "JOB_COMPLETED";
    public static final String EVENT_FAILED = "JOB_FAILED";
}
