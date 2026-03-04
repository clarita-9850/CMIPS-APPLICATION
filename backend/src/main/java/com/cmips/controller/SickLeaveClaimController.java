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
}
