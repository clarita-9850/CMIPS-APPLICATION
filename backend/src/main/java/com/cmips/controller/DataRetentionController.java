package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.service.DataRetentionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for data retention operations.
 * All endpoints require "admin" scope — CMIPS System Admin role only.
 *
 * GET  /api/admin/data-retention/status   — describe next scheduled runs
 * POST /api/admin/data-retention/run-now  — trigger all retention jobs immediately
 */
@RestController
@RequestMapping("/api/admin/data-retention")
@CrossOrigin(origins = "*")
public class DataRetentionController {

    private static final Logger log = LoggerFactory.getLogger(DataRetentionController.class);

    private final DataRetentionService retentionService;

    public DataRetentionController(DataRetentionService retentionService) {
        this.retentionService = retentionService;
    }

    /**
     * Returns the configured retention policy thresholds.
     */
    @GetMapping("/status")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getRetentionStatus() {
        return ResponseEntity.ok(java.util.Map.of(
            "policy", java.util.Map.of(
                "notifications_retention_days",  90,
                "case_notes_retention_years",     5,
                "timesheets_retention_years",     7,
                "closed_case_archival_years",     7
            ),
            "schedules", java.util.Map.of(
                "purgeOldNotifications",      "daily at 02:00",
                "purgeCancelledCaseNotes",    "every Sunday at 02:10",
                "purgeOldTimesheets",         "every Sunday at 02:20",
                "purgeInactivePersonNotes",   "every Sunday at 02:40",
                "flagClosedCasesForArchival", "1st of month at 02:30"
            )
        ));
    }

    /**
     * Manually triggers all data retention jobs immediately.
     * Useful for testing or one-off cleanup after bulk data loads.
     */
    @PostMapping("/run-now")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> runRetentionNow() {
        log.info("[DataRetentionController] Manual retention trigger received");
        DataRetentionService.RetentionResult result = retentionService.runAllJobsNow();
        return ResponseEntity.ok(result);
    }
}
