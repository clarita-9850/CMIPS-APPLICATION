package com.ihss.scheduler.entity;

/**
 * How a job execution was triggered.
 */
public enum TriggerType {
    /**
     * Triggered by cron schedule via Quartz.
     */
    SCHEDULED,

    /**
     * Manually triggered by a user.
     */
    MANUAL,

    /**
     * Triggered because a parent job completed.
     */
    DEPENDENCY,

    /**
     * Automatic retry after failure.
     */
    RETRY,

    /**
     * Triggered via external API call.
     */
    API
}
