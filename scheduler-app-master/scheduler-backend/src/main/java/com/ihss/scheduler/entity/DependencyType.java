package com.ihss.scheduler.entity;

/**
 * Type of dependency relationship between jobs.
 */
public enum DependencyType {
    /**
     * Parent job must complete successfully for child to run.
     */
    SUCCESS,

    /**
     * Parent job must complete (success or failure) for child to run.
     */
    COMPLETION,

    /**
     * Parent job must fail for child to run (useful for error handling jobs).
     */
    FAILURE
}
