package com.ihss.scheduler.exception;

public class CalendarNotFoundException extends RuntimeException {
    public CalendarNotFoundException(String message) {
        super(message);
    }
}
