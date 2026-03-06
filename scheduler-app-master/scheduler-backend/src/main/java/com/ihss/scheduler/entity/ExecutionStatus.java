package com.ihss.scheduler.entity;

/**
 * Status of a job execution.
 */
public enum ExecutionStatus {
    /**
     * Job has been triggered by Scheduler, REST call sent to CMIPS backend.
     */
    TRIGGERED,

    /**
     * Job is queued in CMIPS backend waiting to start.
     */
    QUEUED,

    /**
     * Job is starting up in CMIPS backend.
     */
    STARTING,

    /**
     * Job is actively running.
     */
    RUNNING,

    /**
     * Job completed successfully.
     */
    COMPLETED,

    /**
     * Job failed.
     */
    FAILED,

    /**
     * Job was manually stopped.
     */
    STOPPED,

    /**
     * Job was abandoned (e.g., CMIPS backend crashed).
     */
    ABANDONED,

    /**
     * Job status is unknown (communication failure).
     */
    UNKNOWN
}
