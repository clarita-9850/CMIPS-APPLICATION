package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.AdvancePayEntity;
import com.cmips.repository.AdvancePayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Advance Pay Controller — DSD Section 14
 *
 * Manages advance payment requests for IHSS providers, including
 * issuance, cancellation, and recovery tracking.
 *
 * GET    /api/advance-pay/cases/{caseId}           — list by case
 * GET    /api/advance-pay/providers/{providerId}   — list by provider
 * GET    /api/advance-pay/{id}                     — get by id
 * POST   /api/advance-pay/cases/{caseId}           — create advance pay
 * PUT    /api/advance-pay/{id}/issue               — issue advance pay
 * PUT    /api/advance-pay/{id}/cancel              — cancel advance pay
 * PUT    /api/advance-pay/{id}/recover             — start recovery
 * GET    /api/advance-pay/pending                  — list pending advance pays
 */
@RestController
@RequestMapping("/api/advance-pay")
@CrossOrigin(origins = "*")
public class AdvancePayController {

    private static final Logger logger = LoggerFactory.getLogger(AdvancePayController.class);

    private final AdvancePayRepository advancePayRepository;

    public AdvancePayController(AdvancePayRepository advancePayRepository) {
        this.advancePayRepository = advancePayRepository;
    }

    // ─── List by Case ───────────────────────────────────────────────────────────

    @GetMapping("/cases/{caseId}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listByCaseId(@PathVariable Long caseId) {
        try {
            return ResponseEntity.ok(advancePayRepository.findByCaseIdOrderByCreatedAtDesc(caseId));
        } catch (Exception e) {
            logger.error("Error listing advance pays for case {}: {}", caseId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── List by Provider ───────────────────────────────────────────────────────

    @GetMapping("/providers/{providerId}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listByProviderId(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(advancePayRepository.findByProviderIdOrderByCreatedAtDesc(providerId));
        } catch (Exception e) {
            logger.error("Error listing advance pays for provider {}: {}", providerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Get by ID ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return advancePayRepository.findById(id)
                    .map(ap -> ResponseEntity.ok((Object) ap))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching advance pay {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Create ─────────────────────────────────────────────────────────────────

    @PostMapping("/cases/{caseId}")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> create(
            @PathVariable Long caseId,
            @RequestBody AdvancePayEntity advancePay,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            advancePay.setCaseId(caseId);
            advancePay.setStatus("PENDING");
            advancePay.setCreatedBy(userId);
            advancePay.setCreatedAt(LocalDateTime.now());
            AdvancePayEntity saved = advancePayRepository.save(advancePay);
            logger.info("Created advance pay {} for case {} by user {}", saved.getId(), caseId, userId);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            logger.error("Error creating advance pay for case {}: {}", caseId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Issue ──────────────────────────────────────────────────────────────────

    @PutMapping("/{id}/issue")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> issue(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            AdvancePayEntity ap = advancePayRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Advance pay not found: " + id));
            ap.setStatus("ISSUED");
            ap.setWarrantDate(LocalDate.now());
            ap.setUpdatedBy(userId);
            ap.setUpdatedAt(LocalDateTime.now());
            advancePayRepository.save(ap);
            logger.info("Issued advance pay {} by user {}", id, userId);
            return ResponseEntity.ok(ap);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error issuing advance pay {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Cancel ─────────────────────────────────────────────────────────────────

    @PutMapping("/{id}/cancel")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            AdvancePayEntity ap = advancePayRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Advance pay not found: " + id));
            ap.setStatus("CANCELLED");
            ap.setUpdatedBy(userId);
            ap.setUpdatedAt(LocalDateTime.now());
            advancePayRepository.save(ap);
            logger.info("Cancelled advance pay {} by user {}", id, userId);
            return ResponseEntity.ok(ap);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cancelling advance pay {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Start Recovery ─────────────────────────────────────────────────────────

    @PutMapping("/{id}/recover")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> recover(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            AdvancePayEntity ap = advancePayRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Advance pay not found: " + id));
            ap.setRecoveryStartDate(LocalDate.now());
            ap.setUpdatedBy(userId);
            ap.setUpdatedAt(LocalDateTime.now());
            advancePayRepository.save(ap);
            logger.info("Started recovery for advance pay {} by user {}", id, userId);
            return ResponseEntity.ok(ap);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error starting recovery for advance pay {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── List Pending ───────────────────────────────────────────────────────────

    @GetMapping("/pending")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listPending() {
        try {
            return ResponseEntity.ok(advancePayRepository.findByStatus("PENDING"));
        } catch (Exception e) {
            logger.error("Error listing pending advance pays: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
