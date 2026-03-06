package com.ihss.scheduler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleJobNotFound(JobNotFoundException ex) {
        log.warn("Job not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateJobException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateJob(DuplicateJobException ex) {
        log.warn("Duplicate job: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(JobNotRunnableException.class)
    public ResponseEntity<ErrorResponse> handleJobNotRunnable(JobNotRunnableException ex) {
        log.warn("Job not runnable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(CyclicDependencyException.class)
    public ResponseEntity<ErrorResponse> handleCyclicDependency(CyclicDependencyException ex) {
        log.warn("Cyclic dependency: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("CYCLIC_DEPENDENCY", ex.getMessage()));
    }

    @ExceptionHandler(DependencyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDependencyNotFound(DependencyNotFoundException ex) {
        log.warn("Dependency not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(CalendarNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCalendarNotFound(CalendarNotFoundException ex) {
        log.warn("Calendar not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateCalendarException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCalendar(DuplicateCalendarException ex) {
        log.warn("Duplicate calendar: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("FORBIDDEN", "Access denied"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ValidationErrorResponse("VALIDATION_ERROR", "Validation failed", errors));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp
    ) {
        public ErrorResponse(String code, String message) {
            this(code, message, LocalDateTime.now());
        }
    }

    public record ValidationErrorResponse(
        String code,
        String message,
        Map<String, String> errors,
        LocalDateTime timestamp
    ) {
        public ValidationErrorResponse(String code, String message, Map<String, String> errors) {
            this(code, message, errors, LocalDateTime.now());
        }
    }
}
