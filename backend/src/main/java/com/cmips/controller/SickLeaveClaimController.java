package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.dto.SickLeaveLookupRequest;
import com.cmips.dto.SickLeaveLookupResponse;
import com.cmips.dto.SickLeaveSaveRequest;
import com.cmips.entity.SickLeaveClaimEntity;
import com.cmips.service.SickLeaveClaimService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sick Leave Claim REST Controller — DSD Section 32, CI-790531/790532/794527-794530
 */
@RestController
@RequestMapping("/api/sick-leave-claims")
@CrossOrigin(origins = "*")
public class SickLeaveClaimController {

    private static final Logger log = LoggerFactory.getLogger(SickLeaveClaimController.class);

    private final SickLeaveClaimService sickLeaveService;

    public SickLeaveClaimController(SickLeaveClaimService sickLeaveService) {
        this.sickLeaveService = sickLeaveService;
    }

    /**
     * CI-790531: Manual Entry → Continue — validate and return provider/case details.
     */
    @PostMapping("/lookup")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> lookupForEntry(@RequestBody SickLeaveLookupRequest request) {
        try {
            SickLeaveLookupResponse resp = sickLeaveService.lookupForEntry(request);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * CI-790532: Time Entries → Save — create new sick leave claim.
     */
    @PostMapping
    @RequirePermission(resource = "Provider Resource", scope = "create")
    public ResponseEntity<?> saveClaim(@RequestBody SickLeaveSaveRequest request) {
        try {
            SickLeaveClaimEntity saved = sickLeaveService.saveClaim(request);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * CI-794527: List claims for a provider.
     */
    @GetMapping("/provider/{providerId}")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> listByProvider(@PathVariable Long providerId) {
        try {
            List<SickLeaveClaimEntity> claims = sickLeaveService.listClaimsByProvider(providerId);
            return ResponseEntity.ok(claims);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * CI-794528: Modify Time Entries → Save — update existing claim (same-day, manual only).
     */
    @PutMapping("/{claimNumber}")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> updateClaim(@PathVariable String claimNumber,
                                          @RequestBody SickLeaveSaveRequest request) {
        try {
            SickLeaveClaimEntity updated = sickLeaveService.updateClaim(claimNumber, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * CI-794530: Cancel claim (same-day, manual only).
     */
    @DeleteMapping("/{claimNumber}")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> cancelClaim(@PathVariable String claimNumber) {
        try {
            SickLeaveClaimEntity cancelled = sickLeaveService.cancelClaim(claimNumber);
            return ResponseEntity.ok(cancelled);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * CI-794529: Get claim by number (for view/edit).
     */
    @GetMapping("/{claimNumber}")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getByClaimNumber(@PathVariable String claimNumber) {
        try {
            SickLeaveClaimEntity claim = sickLeaveService.getClaimByNumber(claimNumber);
            return ResponseEntity.ok(claim);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * List claims by case ID.
     */
    @GetMapping("/case/{caseId}")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<List<SickLeaveClaimEntity>> listByCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(sickLeaveService.listClaimsByCase(caseId));
    }

    /**
     * DSD Section 24/32 — Validate claim against all PMEC rules.
     */
    @PostMapping("/{claimNumber}/validate")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> validateClaim(@PathVariable String claimNumber) {
        try {
            SickLeaveClaimEntity claim = sickLeaveService.getClaimByNumber(claimNumber);
            List<String> errors = sickLeaveService.validateClaimPMEC(claim);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("claimNumber", claimNumber);
            result.put("valid", errors.isEmpty());
            result.put("errors", errors);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * DSD: Send sick leave claim to payroll (PRDS108A).
     * Applies cutback if needed, deducts from accrual, generates payroll record.
     */
    @PostMapping("/{claimNumber}/send-to-payroll")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> sendToPayroll(@PathVariable String claimNumber) {
        try {
            SickLeaveClaimEntity claim = sickLeaveService.getClaimByNumber(claimNumber);
            // Validate first
            List<String> errors = sickLeaveService.validateClaimPMEC(claim);
            // Filter blocking errors (skip PMEC015 cutback warning)
            List<String> blocking = errors.stream()
                    .filter(e -> !e.startsWith("PMEC015"))
                    .collect(java.util.stream.Collectors.toList());
            if (!blocking.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", blocking));
            }
            // Cutback if needed (DSD Rule 23)
            claim = sickLeaveService.cutbackIfNeeded(claim);
            // Deduct from accrual (DSD Rule 24)
            sickLeaveService.deductFromAccrual(claim);
            // Generate payroll record
            String payrollRecord = sickLeaveService.generatePayrollRecord(claim);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("claimNumber", claim.getClaimNumber());
            result.put("status", "SENT_TO_PAYROLL");
            result.put("payrollRecord", payrollRecord);
            result.put("claimedHoursAfterCutback", claim.getClaimedHours());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
