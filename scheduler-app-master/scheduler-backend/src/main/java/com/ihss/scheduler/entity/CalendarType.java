package com.ihss.scheduler.entity;

/**
 * Type of calendar for scheduling purposes.
 */
public enum CalendarType {
    /**
     * General blackout dates when jobs should not run.
     */
    BLACKOUT,

    /**
     * Public holidays.
     */
    HOLIDAY,

    /**
     * Scheduled maintenance windows.
     */
    MAINTENANCE,

    /**
     * Custom calendar for specific purposes.
     */
    CUSTOM
}
