package com.ihss.scheduler.exception;

public class JobNotRunnableException extends RuntimeException {
    public JobNotRunnableException(String message) {
        super(message);
    }
}
