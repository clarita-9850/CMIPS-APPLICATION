package com.ihss.scheduler.entity;

/**
 * How a calendar affects job scheduling.
 */
public enum AssignmentType {
    /**
     * Skip job execution on calendar dates.
     */
    EXCLUDE,

    /**
     * Only run job on calendar dates.
     */
    INCLUDE_ONLY
}
