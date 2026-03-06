package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.RecipientWaiverEntity;
import com.cmips.entity.RecipientWaiverEntity.*;
import com.cmips.service.RecipientWaiverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Recipient Waiver (SOC 2298) Management
 * Implements DSD Section 23 - CORI Tier 2 Waiver Processing
 */
@RestController
@RequestMapping("/api/waivers")
@CrossOrigin(origins = "*")
public class RecipientWaiverController {

    private static final Logger log = LoggerFactory.getLogger(RecipientWaiverController.class);

    private final RecipientWaiverService waiverService;

    public RecipientWaiverController(RecipientWaiverService waiverService) {
        this.waiverService = waiverService;
    }

    // ==================== INITIATE WAIVER ====================

    /**
     * Initiate waiver process after Tier 2 conviction identified
     */
    @PostMapping
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "create")
    public ResponseEntity<?> initiateWaiver(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Long recipientId = ((Number) request.get("recipientId")).longValue();
            Long providerId = ((Number) request.get("providerId")).longValue();
            String coriId = (String) request.get("coriId");
            String countyCode = (String) request.get("countyCode");
            String convictionDetails = (String) request.get("convictionDetails");
            LocalDate convictionDate = request.get("convictionDate") != null ?
                    LocalDate.parse((String) request.get("convictionDate")) : null;

            RecipientWaiverEntity waiver = waiverService.initiateWaiver(
                    recipientId, providerId, coriId, countyCode, convictionDetails, convictionDate, userId);

            log.info("Waiver initiated: {} by {}", waiver.getId(), userId);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error initiating waiver", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== DISCLOSURE ====================

    /**
     * Record disclosure to recipient
     */
    @PostMapping("/{waiverId}/disclose")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "edit")
    public ResponseEntity<?> recordDisclosure(@PathVariable String waiverId,
                                               @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            LocalDate disclosureDate = LocalDate.parse(request.get("disclosureDate"));
            String disclosureMethod = request.get("disclosureMethod");
            String witnessName = request.get("witnessName");

            RecipientWaiverEntity waiver = waiverService.recordDisclosure(
                    waiverId, disclosureDate, disclosureMethod, witnessName, userId);

            log.info("Disclosure recorded for waiver: {} by {}", waiverId, userId);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error recording disclosure", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== RECIPIENT DECISION ====================

    /**
     * Record recipient's decision
     */
    @PostMapping("/{waiverId}/recipient-decision")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "edit")
    public ResponseEntity<?> recordRecipientDecision(@PathVariable String waiverId,
                                                      @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            boolean waiverRequested = (Boolean) request.get("waiverRequested");
            String recipientComments = (String) request.get("comments");

            RecipientWaiverEntity waiver = waiverService.recordRecipientDecision(
                    waiverId, waiverRequested, recipientComments, userId);

            log.info("Recipient decision recorded for waiver: {} - Requested: {}", waiverId, waiverRequested);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error recording recipient decision", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== SOC 2298 FORM ====================

    /**
     * Sign SOC 2298 form
     */
    @PostMapping("/{waiverId}/soc-2298/sign")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "edit")
    public ResponseEntity<?> signSOC2298(@PathVariable String waiverId,
                                          @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            LocalDate signedDate = LocalDate.parse(request.get("signedDate"));
            String witnessName = request.get("witnessName");

            RecipientWaiverEntity waiver = waiverService.recordSOC2298Signing(
                    waiverId, signedDate, witnessName, userId);

            log.info("SOC 2298 signed for waiver: {}", waiverId);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error signing SOC 2298", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Submit for county review (after SOC 2298 is signed)
     */
    @PostMapping("/{waiverId}/submit-for-review")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "submit")
    public ResponseEntity<?> submitForReview(@PathVariable String waiverId) {
        try {
            String userId = getCurrentUserId();

            RecipientWaiverEntity waiver = waiverService.submitForCountyReview(waiverId, userId);

            log.info("Waiver submitted for county review: {}", waiverId);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error verifying signatures", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== COUNTY REVIEW ====================

    /**
     * Submit for county review
     */
    @PostMapping("/{waiverId}/submit-county-review")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "submit")
    public ResponseEntity<?> submitForCountyReview(@PathVariable String waiverId) {
        try {
            String userId = getCurrentUserId();
            RecipientWaiverEntity waiver = waiverService.submitForCountyReview(waiverId, userId);

            log.info("Waiver {} submitted for county review", waiverId);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error submitting for county review", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Assign county reviewer
     */
    @PostMapping("/{waiverId}/assign-reviewer")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "assign")
    public ResponseEntity<?> assignCountyReviewer(@PathVariable String waiverId,
                                                   @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String reviewerId = request.get("reviewerId");
            String reviewerName = request.get("reviewerName");

            RecipientWaiverEntity waiver = waiverService.assignCountyReviewer(
                    waiverId, reviewerId, reviewerName, userId);

            log.info("Reviewer {} assigned to waiver {}", reviewerName, waiverId);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error assigning reviewer", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Record county decision
     */
    @PostMapping("/{waiverId}/county-decision")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "approve")
    public ResponseEntity<?> recordCountyDecision(@PathVariable String waiverId,
                                                   @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            CountyDecision decision = CountyDecision.valueOf((String) request.get("decision"));
            String decisionReason = (String) request.get("reason");
            boolean requiresSupervisor = request.get("requiresSupervisorReview") != null ?
                    (Boolean) request.get("requiresSupervisorReview") : false;

            RecipientWaiverEntity waiver = waiverService.recordCountyDecision(
                    waiverId, decision, decisionReason, requiresSupervisor, userId);

            log.info("County decision recorded for waiver: {} - Decision: {}", waiverId, decision);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error recording county decision", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== SUPERVISOR REVIEW ====================

    /**
     * Assign supervisor
     */
    @PostMapping("/{waiverId}/assign-supervisor")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "assign")
    public ResponseEntity<?> assignSupervisor(@PathVariable String waiverId,
                                               @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String supervisorId = request.get("supervisorId");
            String supervisorName = request.get("supervisorName");

            RecipientWaiverEntity waiver = waiverService.assignSupervisor(
                    waiverId, supervisorId, supervisorName, userId);

            log.info("Supervisor {} assigned to waiver {}", supervisorName, waiverId);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error assigning supervisor", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Record supervisor decision
     */
    @PostMapping("/{waiverId}/supervisor-decision")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "approve")
    public ResponseEntity<?> recordSupervisorDecision(@PathVariable String waiverId,
                                                       @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            boolean approved = (Boolean) request.get("approved");
            String comments = (String) request.get("comments");

            RecipientWaiverEntity waiver = waiverService.recordSupervisorDecision(
                    waiverId, approved, comments, userId);

            log.info("Supervisor decision recorded for waiver: {} - Approved: {}", waiverId, approved);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error recording supervisor decision", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== REVOKE ====================

    /**
     * Revoke waiver
     */
    @PostMapping("/{waiverId}/revoke")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "revoke")
    public ResponseEntity<?> revokeWaiver(@PathVariable String waiverId,
                                           @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String reason = request.get("reason");

            RecipientWaiverEntity waiver = waiverService.revokeWaiver(waiverId, reason, userId);

            log.info("Waiver {} revoked by {} - Reason: {}", waiverId, userId, reason);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            log.error("Error revoking waiver", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== QUERY ====================

    /**
     * Get waiver by ID
     */
    @GetMapping("/{waiverId}")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> getWaiver(@PathVariable String waiverId) {
        try {
            RecipientWaiverEntity waiver = waiverService.getWaiverById(waiverId);
            return ResponseEntity.ok(waiver);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if provider has active waiver
     */
    @GetMapping("/provider/{providerId}/has-active")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> hasActiveWaiver(@PathVariable Long providerId) {
        try {
            boolean hasActive = waiverService.hasActiveWaiver(providerId);
            return ResponseEntity.ok(Map.of(
                    "providerId", providerId,
                    "hasActiveWaiver", hasActive
            ));
        } catch (Exception e) {
            log.error("Error checking active waiver", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get active waiver for recipient-provider pair
     */
    @GetMapping("/active")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> getActiveWaiver(@RequestParam Long recipientId,
                                              @RequestParam Long providerId) {
        try {
            return waiverService.getActiveWaiver(recipientId, providerId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting active waiver", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get waivers by recipient
     */
    @GetMapping("/recipient/{recipientId}")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> getWaiversByRecipient(@PathVariable Long recipientId) {
        try {
            List<RecipientWaiverEntity> waivers = waiverService.getWaiversByRecipient(recipientId);
            return ResponseEntity.ok(waivers);
        } catch (Exception e) {
            log.error("Error getting waivers by recipient", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get waivers by provider
     */
    @GetMapping("/provider/{providerId}")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> getWaiversByProvider(@PathVariable Long providerId) {
        try {
            List<RecipientWaiverEntity> waivers = waiverService.getWaiversByProvider(providerId);
            return ResponseEntity.ok(waivers);
        } catch (Exception e) {
            log.error("Error getting waivers by provider", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get pending waivers
     */
    @GetMapping("/pending")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> getPendingWaivers(@RequestParam(required = false) String countyCode) {
        try {
            List<RecipientWaiverEntity> waivers = countyCode != null ?
                    waiverService.getPendingWaiversByCounty(countyCode) :
                    waiverService.getPendingWaivers();
            return ResponseEntity.ok(waivers);
        } catch (Exception e) {
            log.error("Error getting pending waivers", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get waivers pending county review
     */
    @GetMapping("/pending-county-review")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> getPendingCountyReview(@RequestParam(required = false) String countyCode) {
        try {
            List<RecipientWaiverEntity> waivers = countyCode != null ?
                    waiverService.getWaiversPendingCountyReviewByCounty(countyCode) :
                    waiverService.getWaiversPendingCountyReview();
            return ResponseEntity.ok(waivers);
        } catch (Exception e) {
            log.error("Error getting pending county review", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get waivers pending supervisor review
     */
    @GetMapping("/pending-supervisor-review")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> getPendingSupervisorReview() {
        try {
            List<RecipientWaiverEntity> waivers = waiverService.getWaiversPendingSupervisorReview();
            return ResponseEntity.ok(waivers);
        } catch (Exception e) {
            log.error("Error getting pending supervisor review", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get expiring waivers
     */
    @GetMapping("/expiring")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> getExpiringWaivers(@RequestParam(defaultValue = "30") int daysUntilExpiration) {
        try {
            List<RecipientWaiverEntity> waivers = waiverService.getExpiringWaivers(daysUntilExpiration);
            return ResponseEntity.ok(waivers);
        } catch (Exception e) {
            log.error("Error getting expiring waivers", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search waivers
     */
    @GetMapping("/search")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> searchWaivers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) String countyDecision) {
        try {
            List<RecipientWaiverEntity> waivers = waiverService.searchWaivers(
                    status != null ? WaiverStatus.valueOf(status) : null,
                    countyCode,
                    recipientId,
                    providerId,
                    countyDecision != null ? CountyDecision.valueOf(countyDecision) : null
            );
            return ResponseEntity.ok(waivers);
        } catch (Exception e) {
            log.error("Error searching waivers", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Get waiver statistics by county
     */
    @GetMapping("/stats/{countyCode}")
    @RequirePermission(resource = "Recipient Waiver Resource", scope = "view")
    public ResponseEntity<?> getWaiverStats(@PathVariable String countyCode) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("countyCode", countyCode);
            stats.put("pendingCount", waiverService.countPendingByCounty(countyCode));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting waiver stats", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getName();
        }
        return "anonymous";
    }
}
