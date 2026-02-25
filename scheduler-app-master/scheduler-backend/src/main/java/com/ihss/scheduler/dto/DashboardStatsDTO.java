package com.ihss.scheduler.dto;

import java.util.Map;

public record DashboardStatsDTO(
    long totalJobs,
    long activeJobs,
    long runningExecutions,
    long completedToday,
    long failedToday,
    Map<String, Long> executionsByStatus,
    Map<String, Long> jobsByType,
    Map<String, Long> jobsByStatus
) {}
