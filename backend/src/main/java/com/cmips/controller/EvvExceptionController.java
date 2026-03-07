package com.cmips.controller;

import com.cmips.entity.EvvExceptionEntity;
import com.cmips.annotation.RequirePermission;
import com.cmips.service.EvvExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * DSD Section 24 — EVV Exception Approval Controller
 */
@RestController
@RequestMapping("/api/evv-exceptions")
public class EvvExceptionController {

    @Autowired private EvvExceptionService evvService;

    @GetMapping("/pending")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<EvvExceptionEntity>> listPending(
            @RequestParam(required = false) String countyCode) {
        if (countyCode != null) {
            return ResponseEntity.ok(evvService.listPendingByCounty(countyCode));
        }
        return ResponseEntity.ok(evvService.listAllPending());
    }

    @GetMapping("/timesheet/{timesheetId}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<EvvExceptionEntity>> listByTimesheet(@PathVariable Long timesheetId) {
        return ResponseEntity.ok(evvService.listByTimesheet(timesheetId));
    }

    @GetMapping("/provider/{providerId}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<EvvExceptionEntity>> listByProvider(@PathVariable Long providerId) {
        return ResponseEntity.ok(evvService.listByProvider(providerId));
    }

    @GetMapping("/case/{caseId}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<EvvExceptionEntity>> listByCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(evvService.listByCase(caseId));
    }

    @GetMapping("/{exceptionNumber}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<?> getByNumber(@PathVariable String exceptionNumber) {
        return evvService.getByExceptionNumber(exceptionNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<EvvExceptionEntity> submit(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(evvService.submitException(request));
    }

    @PutMapping("/{id}/approve")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<EvvExceptionEntity> approve(@PathVariable Long id,
            @RequestParam String reviewedBy, @RequestParam(required = false) String reviewNotes) {
        return ResponseEntity.ok(evvService.approveException(id, reviewedBy, reviewNotes));
    }

    @PutMapping("/{id}/deny")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<EvvExceptionEntity> deny(@PathVariable Long id,
            @RequestParam String reviewedBy, @RequestParam String denialReason) {
        return ResponseEntity.ok(evvService.denyException(id, reviewedBy, denialReason));
    }
}
