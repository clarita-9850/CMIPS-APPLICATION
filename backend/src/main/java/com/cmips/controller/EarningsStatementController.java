package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.EarningsStatementEntity;
import com.cmips.repository.EarningsStatementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Earnings Statement Controller — DSD Section 17 (Pay Stubs)
 *
 * Manages provider earnings statements (pay stubs) including
 * creation and mailing status tracking.
 *
 * GET    /api/earnings-statements/providers/{providerId}   — list by provider
 * GET    /api/earnings-statements/{id}                     — get by id
 * GET    /api/earnings-statements/warrant/{warrantId}      — find by warrant
 * POST   /api/earnings-statements                          — create statement
 * PUT    /api/earnings-statements/{id}/mark-mailed         — mark as mailed
 */
@RestController
@RequestMapping("/api/earnings-statements")
@CrossOrigin(origins = "*")
public class EarningsStatementController {

    private static final Logger logger = LoggerFactory.getLogger(EarningsStatementController.class);

    private final EarningsStatementRepository earningsStatementRepository;

    public EarningsStatementController(EarningsStatementRepository earningsStatementRepository) {
        this.earningsStatementRepository = earningsStatementRepository;
    }

    // ─── List by Provider ───────────────────────────────────────────────────────

    @GetMapping("/providers/{providerId}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listByProviderId(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(
                    earningsStatementRepository.findByProviderIdOrderByPayPeriodBeginDateDesc(providerId));
        } catch (Exception e) {
            logger.error("Error listing earnings statements for provider {}: {}", providerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Get by ID ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return earningsStatementRepository.findById(id)
                    .map(stmt -> ResponseEntity.ok((Object) stmt))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching earnings statement {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Find by Warrant ────────────────────────────────────────────────────────

    @GetMapping("/warrant/{warrantId}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> findByWarrantId(@PathVariable Long warrantId) {
        try {
            return ResponseEntity.ok(earningsStatementRepository.findByWarrantId(warrantId));
        } catch (Exception e) {
            logger.error("Error fetching earnings statement for warrant {}: {}", warrantId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Create ─────────────────────────────────────────────────────────────────

    @PostMapping
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> create(
            @RequestBody EarningsStatementEntity statement,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            statement.setCreatedBy(userId);
            statement.setCreatedAt(LocalDateTime.now());
            EarningsStatementEntity saved = earningsStatementRepository.save(statement);
            logger.info("Created earnings statement {} for provider {} by user {}",
                    saved.getId(), saved.getProviderId(), userId);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            logger.error("Error creating earnings statement: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Mark as Mailed ─────────────────────────────────────────────────────────

    @PutMapping("/{id}/mark-mailed")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> markMailed(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            EarningsStatementEntity stmt = earningsStatementRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Earnings statement not found: " + id));
            stmt.setStatus("MAILED");
            stmt.setUpdatedBy(userId);
            stmt.setUpdatedAt(LocalDateTime.now());
            earningsStatementRepository.save(stmt);
            logger.info("Marked earnings statement {} as MAILED by user {}", id, userId);
            return ResponseEntity.ok(stmt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error marking earnings statement {} as mailed: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
