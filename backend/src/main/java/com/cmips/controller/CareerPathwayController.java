package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.CareerPathwayClaimEntity;
import com.cmips.service.CareerPathwayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Career Pathway Controller — DSD Section 27 CI-823458
 *
 * Career Pathways Program (California Senate Bill No. 172).
 * IHSS/WPCS providers submit claims via ESP; CDSS reviews and approves.
 *
 * GET    /api/career-pathway/claims                              — provider's claims (by providerId param)
 * POST   /api/career-pathway/claims                             — create new claim (from ESP submission)
 * GET    /api/career-pathway/claims/{id}                        — view claim
 * PUT    /api/career-pathway/claims/{id}/submit-for-approval    — initial reviewer submits
 * PUT    /api/career-pathway/claims/{id}/approve                — final CDSS approval
 * PUT    /api/career-pathway/claims/{id}/reject                 — reject claim
 * POST   /api/career-pathway/claims/{id}/reissue               — reissue voided claim
 * GET    /api/career-pathway/claims/pending-review              — initial review work queue
 * GET    /api/career-pathway/claims/pending-approval            — final approval work queue
 * GET    /api/career-pathway/cumulative-hours/{providerId}      — cumulative hours per pathway
 */
@RestController
@RequestMapping("/api/career-pathway")
@CrossOrigin(origins = "*")
public class CareerPathwayController {

    private final CareerPathwayService service;

    public CareerPathwayController(CareerPathwayService service) {
        this.service = service;
    }

    @GetMapping("/claims")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> listByProvider(@RequestParam String providerId) {
        return ResponseEntity.ok(service.getByProvider(providerId).stream()
                .map(service::toMap).collect(Collectors.toList()));
    }

    @PostMapping("/claims")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> create(
            @RequestBody CareerPathwayClaimEntity claim,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.status(201).body(service.toMap(
                    service.create(claim, userId != null ? userId : "system")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/claims/{id}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.toMap(service.getById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/claims/{id}/submit-for-approval")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> submitForApproval(
            @PathVariable Long id,
            @RequestBody(required = false) CareerPathwayClaimEntity updates,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            if (updates == null) updates = new CareerPathwayClaimEntity();
            return ResponseEntity.ok(service.toMap(
                    service.submitForApproval(id, updates, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/claims/{id}/approve")
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

    @PutMapping("/claims/{id}/reject")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            String reason = body.getOrDefault("rejectionReason", "OTHER");
            String notes = body.getOrDefault("notes", "");
            return ResponseEntity.ok(service.toMap(
                    service.reject(id, reason, notes, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/claims/{id}/reissue")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> reissue(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.status(201).body(service.toMap(
                    service.reissue(id, userId != null ? userId : "system")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/claims/pending-review")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getPendingReview() {
        return ResponseEntity.ok(service.getPendingReview().stream()
                .map(service::toMap).collect(Collectors.toList()));
    }

    @GetMapping("/claims/pending-approval")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getPendingApproval() {
        return ResponseEntity.ok(service.getPendingApproval().stream()
                .map(service::toMap).collect(Collectors.toList()));
    }

    @GetMapping("/cumulative-hours/{providerId}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getCumulativeHours(@PathVariable String providerId) {
        return ResponseEntity.ok(service.getCumulativeHours(providerId));
    }
}
