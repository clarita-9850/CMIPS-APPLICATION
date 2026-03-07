package com.cmips.controller;

import com.cmips.entity.TimesheetIssuanceEntity;
import com.cmips.annotation.RequirePermission;
import com.cmips.service.TimesheetIssuanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * DSD Section 24 — Timesheet Issuance Controller
 */
@RestController
@RequestMapping("/api/timesheet-issuances")
public class TimesheetIssuanceController {

    @Autowired private TimesheetIssuanceService issuanceService;

    @GetMapping("/case/{caseId}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<TimesheetIssuanceEntity>> listByCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(issuanceService.listByCase(caseId));
    }

    @GetMapping("/provider/{providerId}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<TimesheetIssuanceEntity>> listByProvider(@PathVariable Long providerId) {
        return ResponseEntity.ok(issuanceService.listByProvider(providerId));
    }

    @GetMapping("/pending-generation")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<TimesheetIssuanceEntity>> listPendingGeneration() {
        return ResponseEntity.ok(issuanceService.listPendingGeneration());
    }

    @GetMapping("/pending-print")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<TimesheetIssuanceEntity>> listPendingPrint() {
        return ResponseEntity.ok(issuanceService.listPendingBatchPrint());
    }

    @GetMapping("/{issuanceNumber}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<?> getByNumber(@PathVariable String issuanceNumber) {
        return issuanceService.getByIssuanceNumber(issuanceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<TimesheetIssuanceEntity> create(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(issuanceService.createIssuance(request));
    }

    @PutMapping("/{id}/generate")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<TimesheetIssuanceEntity> generate(@PathVariable Long id) {
        return ResponseEntity.ok(issuanceService.generateTimesheet(id));
    }

    @PutMapping("/{id}/mail")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<TimesheetIssuanceEntity> markMailed(@PathVariable Long id) {
        return ResponseEntity.ok(issuanceService.markMailed(id));
    }

    @PutMapping("/{id}/deliver-electronic")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<TimesheetIssuanceEntity> deliverElectronic(@PathVariable Long id) {
        return ResponseEntity.ok(issuanceService.markDeliveredElectronic(id));
    }

    @PutMapping("/{id}/cancel")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<TimesheetIssuanceEntity> cancel(@PathVariable Long id,
            @RequestParam String reason) {
        return ResponseEntity.ok(issuanceService.cancelIssuance(id, reason));
    }

    @PostMapping("/{id}/reissue")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<TimesheetIssuanceEntity> reissue(@PathVariable Long id,
            @RequestParam String reason) {
        return ResponseEntity.ok(issuanceService.reissue(id, reason));
    }

    @PostMapping("/batch-generate")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> batchGenerate() {
        int count = issuanceService.batchGenerate();
        return ResponseEntity.ok(Map.of("generatedCount", count));
    }
}
