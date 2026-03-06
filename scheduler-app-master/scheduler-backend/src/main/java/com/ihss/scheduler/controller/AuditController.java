package com.ihss.scheduler.controller;

import com.ihss.scheduler.annotation.RequirePermission;
import com.ihss.scheduler.entity.AuditAction;
import com.ihss.scheduler.entity.AuditLog;
import com.ihss.scheduler.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/scheduler/audit")
@Tag(name = "Audit", description = "APIs for viewing audit logs")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit history for entity", description = "Get audit log entries for a specific entity")
    public ResponseEntity<Page<AuditLog>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PageableDefault(size = 20, sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditService.getAuditHistory(entityType, entityId, pageable));
    }

    @GetMapping("/user/{username}")
    @RequirePermission(resource = "Scheduler Audit Resource", scope = "search", message = "You don't have permission to search audit logs by user")
    @Operation(summary = "Get audit by user", description = "Get audit log entries for a specific user")
    public ResponseEntity<Page<AuditLog>> getUserAudit(
            @PathVariable String username,
            @PageableDefault(size = 20, sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditService.getAuditByUser(username, pageable));
    }

    @GetMapping("/search")
    @RequirePermission(resource = "Scheduler Audit Resource", scope = "search", message = "You don't have permission to search audit logs")
    @Operation(summary = "Search audit logs", description = "Search audit logs with filters")
    public ResponseEntity<Page<AuditLog>> searchAudit(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @PageableDefault(size = 20, sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        LocalDateTime sinceTime = since != null ? since : LocalDateTime.now().minusDays(30);
        return ResponseEntity.ok(auditService.searchAudit(entityType, action, performedBy, sinceTime, pageable));
    }

    @GetMapping("/timerange")
    @RequirePermission(resource = "Scheduler Audit Resource", scope = "search", message = "You don't have permission to search audit logs")
    @Operation(summary = "Get audit by time range", description = "Get audit logs within a time range")
    public ResponseEntity<Page<AuditLog>> getByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 20, sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditService.getAuditByTimeRange(start, end, pageable));
    }

    @GetMapping("/job/{jobId}/full")
    @Operation(summary = "Get full job history", description = "Get complete audit history for a job")
    public ResponseEntity<List<AuditLog>> getFullJobHistory(@PathVariable Long jobId) {
        return ResponseEntity.ok(auditService.getEntityHistory("JOB_DEFINITION", jobId));
    }

    @GetMapping("/operations/recent")
    @Operation(summary = "Get recent operations", description = "Get recent job operations (trigger, stop, hold, resume)")
    public ResponseEntity<Page<AuditLog>> getRecentOperations(
            @RequestParam(defaultValue = "24") int hours,
            @PageableDefault(size = 50, sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return ResponseEntity.ok(auditService.getRecentJobOperations(since, pageable));
    }
}
