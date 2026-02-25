package com.ihss.scheduler.controller;

import com.ihss.scheduler.annotation.RequirePermission;
import com.ihss.scheduler.entity.AssignmentType;
import com.ihss.scheduler.entity.CalendarType;
import com.ihss.scheduler.entity.JobCalendar;
import com.ihss.scheduler.service.CalendarService;
import com.ihss.scheduler.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/scheduler/calendars")
@Tag(name = "Calendar Management", description = "APIs for managing scheduling calendars")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    @Operation(summary = "Get all calendars", description = "Get all active calendars")
    public ResponseEntity<List<JobCalendar>> getAllCalendars() {
        return ResponseEntity.ok(calendarService.getAllCalendars());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get calendar by ID", description = "Get a calendar by its ID")
    public ResponseEntity<JobCalendar> getCalendar(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.getCalendar(id));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get calendars by type", description = "Get calendars filtered by type")
    public ResponseEntity<List<JobCalendar>> getCalendarsByType(@PathVariable CalendarType type) {
        return ResponseEntity.ok(calendarService.getCalendarsByType(type));
    }

    @PostMapping
    @RequirePermission(resource = "Scheduler Calendar Resource", scope = "create", message = "You don't have permission to create calendars")
    @Operation(summary = "Create calendar", description = "Create a new calendar")
    public ResponseEntity<JobCalendar> createCalendar(
            @Valid @RequestBody CreateCalendarRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        JobCalendar calendar = calendarService.createCalendar(
            request.name(),
            request.description(),
            request.type(),
            username
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(calendar);
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "Scheduler Calendar Resource", scope = "edit", message = "You don't have permission to edit calendars")
    @Operation(summary = "Update calendar", description = "Update an existing calendar")
    public ResponseEntity<JobCalendar> updateCalendar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCalendarRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        return ResponseEntity.ok(calendarService.updateCalendar(
            id,
            request.description(),
            request.type(),
            request.isActive(),
            username
        ));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resource = "Scheduler Calendar Resource", scope = "delete", message = "You don't have permission to delete calendars")
    @Operation(summary = "Delete calendar", description = "Delete a calendar")
    public ResponseEntity<Void> deleteCalendar(@PathVariable Long id) {
        calendarService.deleteCalendar(id);
        return ResponseEntity.noContent().build();
    }

    // Date Management
    @GetMapping("/{id}/dates")
    @Operation(summary = "Get calendar dates", description = "Get dates in a calendar within a range")
    public ResponseEntity<List<LocalDate>> getDates(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(calendarService.getDatesInRange(id, start, end));
    }

    @PostMapping("/{id}/dates")
    @RequirePermission(resource = "Scheduler Calendar Resource", scope = "edit", message = "You don't have permission to manage calendar dates")
    @Operation(summary = "Add date to calendar", description = "Add a single date to a calendar")
    public ResponseEntity<Void> addDate(
            @PathVariable Long id,
            @Valid @RequestBody AddDateRequest request) {
        calendarService.addDate(id, request.date(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{id}/dates/bulk")
    @RequirePermission(resource = "Scheduler Calendar Resource", scope = "edit", message = "You don't have permission to manage calendar dates")
    @Operation(summary = "Add multiple dates", description = "Add multiple dates to a calendar")
    public ResponseEntity<Void> addDates(
            @PathVariable Long id,
            @RequestBody List<LocalDate> dates) {
        calendarService.addDates(id, dates);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/dates")
    @RequirePermission(resource = "Scheduler Calendar Resource", scope = "edit", message = "You don't have permission to manage calendar dates")
    @Operation(summary = "Remove date from calendar", description = "Remove a date from a calendar")
    public ResponseEntity<Void> removeDate(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        calendarService.removeDate(id, date);
        return ResponseEntity.noContent().build();
    }

    // Assignment Management
    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get calendars for job", description = "Get all calendars assigned to a job")
    public ResponseEntity<List<JobCalendar>> getCalendarsForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(calendarService.getCalendarsForJob(jobId));
    }

    @PostMapping("/{calendarId}/assign/{jobId}")
    @RequirePermission(resource = "Scheduler Calendar Resource", scope = "assign", message = "You don't have permission to assign calendars to jobs")
    @Operation(summary = "Assign calendar to job", description = "Assign a calendar to a job")
    public ResponseEntity<Void> assignToJob(
            @PathVariable Long calendarId,
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "EXCLUDE") AssignmentType type,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        calendarService.assignCalendarToJob(jobId, calendarId, type, username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{calendarId}/assign/{jobId}")
    @RequirePermission(resource = "Scheduler Calendar Resource", scope = "assign", message = "You don't have permission to remove calendar assignments")
    @Operation(summary = "Remove calendar from job", description = "Remove a calendar assignment from a job")
    public ResponseEntity<Void> removeFromJob(
            @PathVariable Long calendarId,
            @PathVariable Long jobId) {
        calendarService.removeCalendarFromJob(jobId, calendarId);
        return ResponseEntity.noContent().build();
    }

    // Request DTOs
    public record CreateCalendarRequest(
        @NotBlank String name,
        String description,
        @NotNull CalendarType type
    ) {}

    public record UpdateCalendarRequest(
        String description,
        CalendarType type,
        Boolean isActive
    ) {}

    public record AddDateRequest(
        @NotNull LocalDate date,
        String description
    ) {}
}
