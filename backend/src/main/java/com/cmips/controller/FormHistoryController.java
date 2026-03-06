package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.FormHistoryEntity;
import com.cmips.entity.FormHistoryEntity.EventType;
import com.cmips.repository.FormHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Form History Controller — DSD Section 31, CI-116615
 *
 * Provides a correspondence / audit trail for NOAs and Electronic Forms.
 * Staff can view all events (creation, print, mail, status changes) and add
 * free-text comments per document.
 *
 * Endpoints:
 *   GET  /api/cases/noas/{noaId}/history          — history for a specific NOA
 *   POST /api/cases/noas/{noaId}/history           — add comment to NOA
 *   GET  /api/cases/forms/{formId}/history         — history for a specific form
 *   POST /api/cases/forms/{formId}/history         — add comment to form
 *   GET  /api/cases/{caseId}/form-history          — all history entries for a case
 */
@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class FormHistoryController {

    private static final Logger log = LoggerFactory.getLogger(FormHistoryController.class);

    private final FormHistoryRepository historyRepo;

    public FormHistoryController(FormHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
    }

    // ── NOA history ──────────────────────────────────────────────────────────

    @GetMapping("/noas/{noaId}/history")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<FormHistoryEntity>> getNoaHistory(@PathVariable Long noaId) {
        return ResponseEntity.ok(historyRepo.findByNoaIdOrderByCreatedAtDesc(noaId));
    }

    @PostMapping("/noas/{noaId}/history")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> addNoaComment(
            @PathVariable Long noaId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {

        String comment = (String) request.get("comment");
        if (comment == null || comment.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comment text is required"));
        }
        Object caseIdObj = request.get("caseId");

        FormHistoryEntity entry = new FormHistoryEntity();
        entry.setNoaId(noaId);
        if (caseIdObj != null) entry.setCaseId(Long.parseLong(caseIdObj.toString()));
        entry.setEventType(EventType.COMMENT);
        entry.setEventSummary("Staff comment added");
        entry.setComment(comment.trim());
        entry.setCreatedBy(userId);

        FormHistoryEntity saved = historyRepo.save(entry);
        log.info("[FORM-HISTORY] Comment added to NOA {} by {}", noaId, userId);
        return ResponseEntity.ok(saved);
    }

    // ── Electronic Form history ───────────────────────────────────────────────

    @GetMapping("/forms/{formId}/history")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<FormHistoryEntity>> getFormHistory(@PathVariable Long formId) {
        return ResponseEntity.ok(historyRepo.findByFormIdOrderByCreatedAtDesc(formId));
    }

    @PostMapping("/forms/{formId}/history")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> addFormComment(
            @PathVariable Long formId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {

        String comment = (String) request.get("comment");
        if (comment == null || comment.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comment text is required"));
        }
        Object caseIdObj = request.get("caseId");

        FormHistoryEntity entry = new FormHistoryEntity();
        entry.setFormId(formId);
        if (caseIdObj != null) entry.setCaseId(Long.parseLong(caseIdObj.toString()));
        entry.setEventType(EventType.COMMENT);
        entry.setEventSummary("Staff comment added");
        entry.setComment(comment.trim());
        entry.setCreatedBy(userId);

        FormHistoryEntity saved = historyRepo.save(entry);
        log.info("[FORM-HISTORY] Comment added to form {} by {}", formId, userId);
        return ResponseEntity.ok(saved);
    }

    // ── Case-level history (all forms + NOAs) ────────────────────────────────

    @GetMapping("/{caseId}/form-history")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<FormHistoryEntity>> getCaseFormHistory(@PathVariable Long caseId) {
        return ResponseEntity.ok(historyRepo.findByCaseIdOrderByCreatedAtDesc(caseId));
    }
}
