package com.ihss.scheduler.entity;

/**
 * Status of a job definition.
 */
public enum JobStatus {
    /**
     * Job runs normally according to its schedule.
     */
    ACTIVE,

    /**
     * Job is disabled and will not run.
     */
    INACTIVE,

    /**
     * Job is paused. Dependent jobs are also blocked.
     */
    ON_HOLD,

    /**
     * Job is skipped but dependent jobs can still run.
     * Useful for temporarily disabling a job without blocking the dependency chain.
     */
    ON_ICE
}
