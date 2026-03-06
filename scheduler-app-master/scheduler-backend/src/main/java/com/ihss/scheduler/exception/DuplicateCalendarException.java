package com.ihss.scheduler.exception;

public class DuplicateCalendarException extends RuntimeException {
    public DuplicateCalendarException(String message) {
        super(message);
    }
}
