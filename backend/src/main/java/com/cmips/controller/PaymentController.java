package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.CashedWarrantCopyRequestEntity;
import com.cmips.entity.ForgedEndorsementAffidavitEntity;
import com.cmips.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Payment Controller — DSD Section 27
 *
 * Endpoints:
 *   GET  /api/payments/search/by-person   — Payment Search by Person
 *   GET  /api/payments/search/by-case     — Payment Search by Case
 *   GET  /api/payments/{warrantId}        — View Payment Details
 *
 * Void/Stop/Reissue:
 *   POST /api/payments/{warrantId}/void-reissue
 *
 * Cashed Warrant Copy:
 *   POST /api/payments/{warrantId}/cashed-copies
 *   PUT  /api/payments/cashed-copies/{id}/cancel
 *
 * Forged Endorsement Affidavit:
 *   POST /api/payments/{warrantId}/forged-affidavits
 *   PUT  /api/payments/forged-affidavits/{id}
 *   PUT  /api/payments/forged-affidavits/{id}/cancel
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ─── Payment Search ────────────────────────────────────────────────────────

    @GetMapping("/search/by-person")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> searchByPerson(
            @RequestParam(required = false) String payeeId,
            @RequestParam(required = false) String servicePeriodFrom,
            @RequestParam(required = false) String servicePeriodTo,
            @RequestParam(required = false) String issueFrom,
            @RequestParam(required = false) String issueTo,
            @RequestParam(required = false) String warrantNumber) {
        return ResponseEntity.ok(paymentService.searchByPerson(
                payeeId, servicePeriodFrom, servicePeriodTo, issueFrom, issueTo, warrantNumber));
    }

    @GetMapping("/search/by-case")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> searchByCase(
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) String servicePeriodFrom,
            @RequestParam(required = false) String servicePeriodTo,
            @RequestParam(required = false) String issueFrom,
            @RequestParam(required = false) String issueTo,
            @RequestParam(required = false) String payeeName,
            @RequestParam(required = false) String warrantNumber) {
        return ResponseEntity.ok(paymentService.searchByCase(
                caseNumber, servicePeriodFrom, servicePeriodTo, issueFrom, issueTo, payeeName, warrantNumber));
    }

    // ─── View Payment Details ──────────────────────────────────────────────────

    @GetMapping("/{warrantId}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getPaymentDetails(@PathVariable Long warrantId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentDetails(warrantId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─── Void / Stop / Reissue / Replacement ──────────────────────────────────

    @PostMapping("/{warrantId}/void-reissue")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> requestVoidOrReissue(
            @PathVariable Long warrantId,
            @RequestBody Map<String, String> body) {
        try {
            String requestType = body.get("requestType");
            String voidReason = body.get("voidReason");
            String notes = body.getOrDefault("notes", "");
            String userId = body.getOrDefault("userId", "system");
            return ResponseEntity.ok(paymentService.requestVoidOrReissue(
                    warrantId, requestType, voidReason, notes, userId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Cashed Warrant Copy Request ──────────────────────────────────────────

    @PostMapping("/{warrantId}/cashed-copies")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> requestCashedCopy(
            @PathVariable Long warrantId,
            @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "");
            String userId = body.getOrDefault("userId", "system");
            CashedWarrantCopyRequestEntity saved = paymentService.requestCashedCopy(warrantId, reason, userId);
            return ResponseEntity.ok(Map.of("id", saved.getId(), "message", "Cashed warrant copy request submitted."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/cashed-copies/{id}/cancel")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> cancelCashedCopy(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String userId = body != null ? body.getOrDefault("userId", "system") : "system";
            return ResponseEntity.ok(paymentService.cancelCashedCopy(id, userId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Forged Endorsement Affidavit ─────────────────────────────────────────

    @PostMapping("/{warrantId}/forged-affidavits")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> createAffidavit(
            @PathVariable Long warrantId,
            @RequestBody ForgedEndorsementAffidavitEntity affidavit,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(paymentService.createAffidavit(warrantId, affidavit, userId != null ? userId : "system"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/forged-affidavits/{id}")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> updateAffidavit(
            @PathVariable Long id,
            @RequestBody ForgedEndorsementAffidavitEntity updates,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(paymentService.updateAffidavit(id, updates, userId != null ? userId : "system"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/forged-affidavits/{id}/cancel")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> cancelAffidavit(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(paymentService.cancelAffidavit(id, userId != null ? userId : "system"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
