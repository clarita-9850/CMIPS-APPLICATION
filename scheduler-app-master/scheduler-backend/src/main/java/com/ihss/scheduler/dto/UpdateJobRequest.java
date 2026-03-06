package com.ihss.scheduler.dto;

import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateJobRequest(
    @Size(max = 100, message = "Job type must not exceed 100 characters")
    String jobType,

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description,

    @Size(max = 100, message = "Cron expression must not exceed 100 characters")
    String cronExpression,

    String timezone,

    Boolean enabled,

    Integer priority,

    Integer maxRetries,

    Integer timeoutSeconds,

    Map<String, Object> jobParameters,

    String[] targetRoles,

    String[] targetCounties
) {}
