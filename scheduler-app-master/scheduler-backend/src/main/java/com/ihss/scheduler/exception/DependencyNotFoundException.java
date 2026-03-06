package com.ihss.scheduler.exception;

public class DependencyNotFoundException extends RuntimeException {
    public DependencyNotFoundException(String message) {
        super(message);
    }
}
