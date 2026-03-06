package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.OverpaymentCollectionEntity;
import com.cmips.entity.OverpaymentEntity;
import com.cmips.service.OverpaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Overpayment Controller — DSD Section 27 CI-67319
 *
 * Manages overpayment recovery setup, pay period collection, and personal payments.
 * Always set up from the recipient case prospective.
 *
 * GET    /api/cases/{caseId}/overpayments                   — list (PENDING+ACTIVE by default)
 * GET    /api/cases/{caseId}/overpayments/all               — all overpayments including closed/cancelled
 * POST   /api/cases/{caseId}/overpayments                   — create occurrence
 * GET    /api/overpayments/{id}                             — view recovery
 * PUT    /api/overpayments/{id}                             — modify (PENDING or PENDING_PAYROLL only)
 * POST   /api/overpayments/{id}/submit                      — submit recovery (→ PENDING_PAYROLL or ACTIVE)
 * PUT    /api/overpayments/{id}/cancel                      — cancel recovery
 * PUT    /api/overpayments/{id}/stop                        — stop collection (ACTIVE only)
 *
 * Collections (personal payments):
 * GET    /api/overpayments/{id}/collections                 — list collections
 * POST   /api/overpayments/{id}/collections                 — add personal payment
 */
@RestController
@CrossOrigin(origins = "*")
public class OverpaymentController {

    private final OverpaymentService service;

    public OverpaymentController(OverpaymentService service) {
        this.service = service;
    }

    @GetMapping("/api/cases/{caseId}/overpayments")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> listActive(@PathVariable Long caseId) {
        return ResponseEntity.ok(service.getActiveByCaseId(caseId).stream()
                .map(service::toMap).collect(Collectors.toList()));
    }

    @GetMapping("/api/cases/{caseId}/overpayments/all")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> listAll(@PathVariable Long caseId) {
        return ResponseEntity.ok(service.getAllByCaseId(caseId).stream()
                .map(service::toMap).collect(Collectors.toList()));
    }

    @PostMapping("/api/cases/{caseId}/overpayments")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> create(
            @PathVariable Long caseId,
            @RequestBody OverpaymentEntity op,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            op.setCaseId(caseId);
            return ResponseEntity.status(201).body(service.toMap(
                    service.create(op, userId != null ? userId : "system")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/overpayments/{id}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.toMap(service.getById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/api/overpayments/{id}")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody OverpaymentEntity updates,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(service.toMap(
                    service.update(id, updates, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/overpayments/{id}/submit")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> submit(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(service.toMap(
                    service.submitRecovery(id, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/overpayments/{id}/cancel")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(service.toMap(
                    service.cancelRecovery(id, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/overpayments/{id}/stop")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> stop(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(service.toMap(
                    service.stopCollection(id, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Collections ──────────────────────────────────────────────────────────

    @GetMapping("/api/overpayments/{id}/collections")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getCollections(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCollections(id));
    }

    @PostMapping("/api/overpayments/{id}/collections")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> addCollection(
            @PathVariable Long id,
            @RequestBody OverpaymentCollectionEntity collection,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.status(201).body(
                    service.addCollection(id, collection, userId != null ? userId : "system"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
