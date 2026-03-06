package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.PaymentCorrectionEntity;
import com.cmips.service.PaymentCorrectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payment Correction Controller — DSD Section 27 CI-67321
 *
 * Corrects payments for over-reported hours, prior underpayments, timesheet exceptions,
 * and WPCS recipient-on-leave situations.
 *
 * All corrections require secondary Payroll Approver approval.
 *
 * GET    /api/cases/{caseId}/payment-corrections         — list by case
 * POST   /api/cases/{caseId}/payment-corrections         — create
 * GET    /api/payment-corrections/{id}                   — view single
 * PUT    /api/payment-corrections/{id}                   — modify
 * POST   /api/payment-corrections/{id}/submit            — submit for approval
 * PUT    /api/payment-corrections/{id}/approve           — approve
 * PUT    /api/payment-corrections/{id}/reject            — reject
 * PUT    /api/payment-corrections/{id}/cancel            — cancel
 * GET    /api/payment-corrections/pending-approval       — approver work queue
 */
@RestController
@CrossOrigin(origins = "*")
public class PaymentCorrectionController {

    private final PaymentCorrectionService service;

    public PaymentCorrectionController(PaymentCorrectionService service) {
        this.service = service;
    }

    @GetMapping("/api/cases/{caseId}/payment-corrections")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> listByCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(service.getByCase(caseId).stream()
                .map(service::toMap).collect(Collectors.toList()));
    }

    @PostMapping("/api/cases/{caseId}/payment-corrections")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> create(
            @PathVariable Long caseId,
            @RequestBody PaymentCorrectionEntity correction,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            correction.setCaseId(caseId);
            return ResponseEntity.status(201).body(service.toMap(
                    service.create(correction, userId != null ? userId : "system")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/payment-corrections/{id}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.toMap(service.getById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/api/payment-corrections/{id}")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody PaymentCorrectionEntity updates,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(service.toMap(
                    service.update(id, updates, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/payment-corrections/{id}/submit")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> submit(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(service.toMap(service.submit(id, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/payment-corrections/{id}/approve")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(service.toMap(service.approve(id, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/payment-corrections/{id}/reject")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            String reason = body.getOrDefault("reason", "");
            return ResponseEntity.ok(service.toMap(
                    service.reject(id, reason, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/payment-corrections/{id}/cancel")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(service.toMap(service.cancel(id, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/payment-corrections/pending-approval")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getPendingApproval() {
        return ResponseEntity.ok(service.getPendingApproval().stream()
                .map(service::toMap).collect(Collectors.toList()));
    }
}
