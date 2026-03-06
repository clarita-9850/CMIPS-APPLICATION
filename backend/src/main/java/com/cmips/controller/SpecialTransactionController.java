package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.SpecialTransactionEntity;
import com.cmips.service.SpecialTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Special Transaction Controller — DSD Section 27 CI-67322
 *
 * One-time payments and deductions (27+ pay types, 8 deduction types).
 * County/CDSS require Payroll Approver secondary approval.
 * Vendor travel claims and System transactions auto-approve.
 *
 * GET    /api/cases/{caseId}/special-transactions           — list by case
 * POST   /api/cases/{caseId}/special-transactions           — create
 * GET    /api/special-transactions/{id}                     — view single
 * PUT    /api/special-transactions/{id}                     — modify
 * POST   /api/special-transactions/{id}/submit              — submit for approval
 * PUT    /api/special-transactions/{id}/approve             — approve (Payroll Approver)
 * PUT    /api/special-transactions/{id}/reject              — reject (Payroll Approver)
 * PUT    /api/special-transactions/{id}/cancel              — cancel
 * GET    /api/special-transactions/pending-approval         — work queue
 */
@RestController
@CrossOrigin(origins = "*")
public class SpecialTransactionController {

    private final SpecialTransactionService service;

    public SpecialTransactionController(SpecialTransactionService service) {
        this.service = service;
    }

    @GetMapping("/api/cases/{caseId}/special-transactions")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> listByCase(@PathVariable Long caseId) {
        List<Map<String, Object>> result = service.getByCase(caseId)
                .stream().map(service::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/cases/{caseId}/special-transactions")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> create(
            @PathVariable Long caseId,
            @RequestBody SpecialTransactionEntity txn,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            txn.setCaseId(caseId);
            SpecialTransactionEntity saved = service.create(txn, userId != null ? userId : "system");
            return ResponseEntity.status(201).body(service.toMap(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/special-transactions/{id}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.toMap(service.getById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/api/special-transactions/{id}")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody SpecialTransactionEntity updates,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(service.toMap(service.update(id, updates, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/special-transactions/{id}/submit")
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

    @PutMapping("/api/special-transactions/{id}/approve")
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

    @PutMapping("/api/special-transactions/{id}/reject")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            String reason = body.getOrDefault("reason", "");
            return ResponseEntity.ok(service.toMap(service.reject(id, reason, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/special-transactions/{id}/cancel")
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

    @GetMapping("/api/special-transactions/pending-approval")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getPendingApproval() {
        return ResponseEntity.ok(service.getPendingApproval().stream()
                .map(service::toMap).collect(Collectors.toList()));
    }
}
