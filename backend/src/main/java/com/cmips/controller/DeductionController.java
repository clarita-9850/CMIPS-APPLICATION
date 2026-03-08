package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.DeductionSetupEntity;
import com.cmips.repository.DeductionSetupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Deduction Controller — DSD Section 15
 *
 * Manages provider payroll deductions (garnishments, voluntary deductions,
 * union dues, tax levies, etc.).
 *
 * GET    /api/deductions/providers/{providerId}          — list by provider
 * GET    /api/deductions/providers/{providerId}/active   — active deductions
 * GET    /api/deductions/{id}                            — get by id
 * POST   /api/deductions/providers/{providerId}          — create deduction
 * PUT    /api/deductions/{id}                            — update deduction
 * PUT    /api/deductions/{id}/suspend                    — suspend deduction
 * PUT    /api/deductions/{id}/reactivate                 — reactivate deduction
 * DELETE /api/deductions/{id}                            — soft delete (INACTIVE)
 */
@RestController
@RequestMapping("/api/deductions")
@CrossOrigin(origins = "*")
public class DeductionController {

    private static final Logger logger = LoggerFactory.getLogger(DeductionController.class);

    private final DeductionSetupRepository deductionSetupRepository;

    public DeductionController(DeductionSetupRepository deductionSetupRepository) {
        this.deductionSetupRepository = deductionSetupRepository;
    }

    // ─── List by Provider ───────────────────────────────────────────────────────

    @GetMapping("/providers/{providerId}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listByProviderId(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(deductionSetupRepository.findByProviderIdOrderByPriorityAsc(providerId));
        } catch (Exception e) {
            logger.error("Error listing deductions for provider {}: {}", providerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Active Deductions by Provider ──────────────────────────────────────────

    @GetMapping("/providers/{providerId}/active")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listActiveByProviderId(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(deductionSetupRepository.findByProviderIdAndStatus(providerId, "ACTIVE"));
        } catch (Exception e) {
            logger.error("Error listing active deductions for provider {}: {}", providerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Get by ID ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return deductionSetupRepository.findById(id)
                    .map(d -> ResponseEntity.ok((Object) d))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching deduction {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Create ─────────────────────────────────────────────────────────────────

    @PostMapping("/providers/{providerId}")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> create(
            @PathVariable Long providerId,
            @RequestBody DeductionSetupEntity deduction,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            deduction.setProviderId(providerId);
            deduction.setStatus("ACTIVE");
            deduction.setCreatedBy(userId);
            deduction.setCreatedAt(LocalDateTime.now());
            DeductionSetupEntity saved = deductionSetupRepository.save(deduction);
            logger.info("Created deduction {} for provider {} by user {}", saved.getId(), providerId, userId);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            logger.error("Error creating deduction for provider {}: {}", providerId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Update ─────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody DeductionSetupEntity updates,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            DeductionSetupEntity existing = deductionSetupRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Deduction not found: " + id));
            if (updates.getDeductionType() != null) existing.setDeductionType(updates.getDeductionType());
            if (updates.getAmount() != null) existing.setAmount(updates.getAmount());
            if (updates.getPriority() != null) existing.setPriority(updates.getPriority());
            if (updates.getEffectiveDate() != null) existing.setEffectiveDate(updates.getEffectiveDate());
            if (updates.getEndDate() != null) existing.setEndDate(updates.getEndDate());
            if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
            existing.setUpdatedBy(userId);
            existing.setUpdatedAt(LocalDateTime.now());
            deductionSetupRepository.save(existing);
            logger.info("Updated deduction {} by user {}", id, userId);
            return ResponseEntity.ok(existing);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating deduction {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Suspend ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}/suspend")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> suspend(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            DeductionSetupEntity deduction = deductionSetupRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Deduction not found: " + id));
            deduction.setStatus("SUSPENDED");
            deduction.setUpdatedBy(userId);
            deduction.setUpdatedAt(LocalDateTime.now());
            deductionSetupRepository.save(deduction);
            logger.info("Suspended deduction {} by user {}", id, userId);
            return ResponseEntity.ok(deduction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error suspending deduction {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Reactivate ─────────────────────────────────────────────────────────────

    @PutMapping("/{id}/reactivate")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> reactivate(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            DeductionSetupEntity deduction = deductionSetupRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Deduction not found: " + id));
            deduction.setStatus("ACTIVE");
            deduction.setUpdatedBy(userId);
            deduction.setUpdatedAt(LocalDateTime.now());
            deductionSetupRepository.save(deduction);
            logger.info("Reactivated deduction {} by user {}", id, userId);
            return ResponseEntity.ok(deduction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error reactivating deduction {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Soft Delete ────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> softDelete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            DeductionSetupEntity deduction = deductionSetupRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Deduction not found: " + id));
            deduction.setStatus("INACTIVE");
            deduction.setUpdatedBy(userId);
            deduction.setUpdatedAt(LocalDateTime.now());
            deductionSetupRepository.save(deduction);
            logger.info("Soft-deleted deduction {} by user {}", id, userId);
            return ResponseEntity.ok(Map.of("message", "Deduction inactivated", "id", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting deduction {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
