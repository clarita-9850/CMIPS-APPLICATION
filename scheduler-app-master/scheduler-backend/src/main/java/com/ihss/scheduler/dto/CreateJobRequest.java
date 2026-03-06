package com.ihss.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateJobRequest(
    @NotBlank(message = "Job name is required")
    @Size(max = 255, message = "Job name must not exceed 255 characters")
    String jobName,

    @NotBlank(message = "Job type is required")
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
) {
    public CreateJobRequest {
        if (timezone == null) timezone = "America/Los_Angeles";
        if (enabled == null) enabled = true;
        if (priority == null) priority = 5;
        if (maxRetries == null) maxRetries = 3;
        if (timeoutSeconds == null) timeoutSeconds = 3600;
    }
}
