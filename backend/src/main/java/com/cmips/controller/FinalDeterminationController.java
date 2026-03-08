package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.*;
import com.cmips.service.FinalDeterminationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Final Determination REST Controller — DSD Section 22
 *
 * Endpoints for:
 *   - Creating IHSS Authorization from approved assessment
 *   - Assigning Modes of Service (IP/CC/HM split)
 *   - SOC Spend Down calculation
 *   - Case Service Month queries
 *   - Full determination workflow
 */
@RestController
@RequestMapping("/api/determination")
@CrossOrigin(origins = "*")
public class FinalDeterminationController {

    private static final Logger log = LoggerFactory.getLogger(FinalDeterminationController.class);

    private final FinalDeterminationService determinationService;

    public FinalDeterminationController(FinalDeterminationService determinationService) {
        this.determinationService = determinationService;
    }

    // ==================== AUTHORIZATION ====================

    /**
     * Create IHSS Authorization from an approved assessment.
     * POST body: { "assessmentId": 123 }
     */
    @PostMapping("/authorize")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "approve")
    public ResponseEntity<IHSSAuthorizationEntity> createAuthorization(
            @RequestBody AuthorizeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        IHSSAuthorizationEntity auth = determinationService.createAuthorization(
                request.getAssessmentId(), userId);
        return ResponseEntity.ok(auth);
    }

    /**
     * Get active authorization for a case.
     */
    @GetMapping("/cases/{caseId}/authorization")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<?> getActiveAuthorization(@PathVariable Long caseId) {
        Optional<IHSSAuthorizationEntity> auth = determinationService.getActiveAuthorization(caseId);
        if (auth.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No active authorization found", "caseId", caseId));
        }
        return ResponseEntity.ok(auth.get());
    }

    /**
     * Get authorization history for a case.
     */
    @GetMapping("/cases/{caseId}/authorizations")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<IHSSAuthorizationEntity>> getAuthorizationHistory(@PathVariable Long caseId) {
        return ResponseEntity.ok(determinationService.getAuthorizationHistory(caseId));
    }

    /**
     * Get authorized services for an authorization.
     */
    @GetMapping("/authorizations/{authId}/services")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<IHSSAuthorizedServiceEntity>> getAuthorizedServices(@PathVariable Long authId) {
        return ResponseEntity.ok(determinationService.getAuthorizedServices(authId));
    }

    // ==================== MODES OF SERVICE ====================

    /**
     * Assign modes of service for an authorization.
     * POST body: { "authorizationId": 1, "ipMinutes": 6000, "ccMinutes": 0, "hmMinutes": 0,
     *              "startDate": "2026-04-01", "endDate": "2027-03-31" }
     */
    @PostMapping("/modes-of-service")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ModeOfServiceEntity> assignModesOfService(
            @RequestBody AssignMOSRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ModeOfServiceEntity mos = determinationService.assignModesOfService(
                request.getAuthorizationId(),
                request.getIpMinutes(),
                request.getCcMinutes(),
                request.getHmMinutes(),
                request.getStartDate(),
                request.getEndDate(),
                userId);
        return ResponseEntity.ok(mos);
    }

    /**
     * Get modes of service for a case.
     */
    @GetMapping("/cases/{caseId}/modes-of-service")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<ModeOfServiceEntity>> getModesOfService(@PathVariable Long caseId) {
        return ResponseEntity.ok(determinationService.getModesOfService(caseId));
    }

    /**
     * Get active mode of service for a case.
     */
    @GetMapping("/cases/{caseId}/modes-of-service/active")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<?> getActiveModeOfService(@PathVariable Long caseId) {
        Optional<ModeOfServiceEntity> mos = determinationService.getActiveModeOfService(caseId);
        if (mos.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No active mode of service found", "caseId", caseId));
        }
        return ResponseEntity.ok(mos.get());
    }

    /**
     * Get mode of service snapshot history.
     */
    @GetMapping("/modes-of-service/{mosId}/history")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<ModeOfServiceSnapshotEntity>> getMOSHistory(@PathVariable Long mosId) {
        return ResponseEntity.ok(determinationService.getModeOfServiceHistory(mosId));
    }

    // ==================== SOC SPEND DOWN ====================

    /**
     * Calculate SOC spend down for a case and service month.
     * POST body: { "caseId": 1, "serviceMonth": "2026-04-01" }
     */
    @PostMapping("/soc-spend-down")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<?> calculateSOCSpendDown(
            @RequestBody SOCSpendDownRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        SOCSpendDownTriggerEntity trigger = determinationService.calculateSOCSpendDown(
                request.getCaseId(), request.getServiceMonth(), userId);
        if (trigger == null) {
            return ResponseEntity.ok(Map.of("message", "No SOC obligation — spend down not required",
                    "caseId", request.getCaseId()));
        }
        return ResponseEntity.ok(trigger);
    }

    /**
     * Recalculate SOC spend down for all open months of a case.
     */
    @PostMapping("/cases/{caseId}/soc-spend-down/recalculate")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<List<SOCSpendDownTriggerEntity>> recalculateSOCSpendDown(
            @PathVariable Long caseId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        List<SOCSpendDownTriggerEntity> triggers = determinationService.recalculateSOCSpendDown(caseId, userId);
        return ResponseEntity.ok(triggers);
    }

    /**
     * Get SOC hours for a case.
     */
    @GetMapping("/cases/{caseId}/soc-hours")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<CaseSOCHoursEntity>> getSOCHours(@PathVariable Long caseId) {
        return ResponseEntity.ok(determinationService.getSOCHours(caseId));
    }

    /**
     * Get spend down trigger history for a case.
     */
    @GetMapping("/cases/{caseId}/spend-down-triggers")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<SOCSpendDownTriggerEntity>> getSpendDownTriggers(@PathVariable Long caseId) {
        return ResponseEntity.ok(determinationService.getSpendDownTriggers(caseId));
    }

    // ==================== CASE SERVICE MONTHS ====================

    /**
     * Get all case service months for a case.
     */
    @GetMapping("/cases/{caseId}/service-months")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<CaseServiceMonthEntity>> getCaseServiceMonths(@PathVariable Long caseId) {
        return ResponseEntity.ok(determinationService.getCaseServiceMonths(caseId));
    }

    /**
     * Get case service month for a specific month.
     */
    @GetMapping("/cases/{caseId}/service-months/{serviceMonth}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<?> getCaseServiceMonth(
            @PathVariable Long caseId, @PathVariable String serviceMonth) {
        LocalDate month = LocalDate.parse(serviceMonth);
        Optional<CaseServiceMonthEntity> csm = determinationService.getCaseServiceMonth(caseId, month);
        if (csm.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No service month record found",
                    "caseId", caseId, "serviceMonth", serviceMonth));
        }
        return ResponseEntity.ok(csm.get());
    }

    // ==================== FULL WORKFLOW ====================

    /**
     * Execute the full final determination workflow.
     * Creates authorization, assigns default MOS (100% IP), calculates SOC spend down, generates NOA.
     * POST body: { "assessmentId": 123 }
     */
    @PostMapping("/execute")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "approve")
    public ResponseEntity<Map<String, Object>> executeFinalDetermination(
            @RequestBody AuthorizeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        Map<String, Object> result = determinationService.executeFinalDetermination(
                request.getAssessmentId(), userId);
        return ResponseEntity.ok(result);
    }

    // ==================== REQUEST DTOs ====================

    public static class AuthorizeRequest {
        private Long assessmentId;
        public Long getAssessmentId() { return assessmentId; }
        public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
    }

    public static class AssignMOSRequest {
        private Long authorizationId;
        private int ipMinutes;
        private int ccMinutes;
        private int hmMinutes;
        private LocalDate startDate;
        private LocalDate endDate;

        public Long getAuthorizationId() { return authorizationId; }
        public void setAuthorizationId(Long v) { this.authorizationId = v; }
        public int getIpMinutes() { return ipMinutes; }
        public void setIpMinutes(int v) { this.ipMinutes = v; }
        public int getCcMinutes() { return ccMinutes; }
        public void setCcMinutes(int v) { this.ccMinutes = v; }
        public int getHmMinutes() { return hmMinutes; }
        public void setHmMinutes(int v) { this.hmMinutes = v; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate v) { this.startDate = v; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate v) { this.endDate = v; }
    }

    public static class SOCSpendDownRequest {
        private Long caseId;
        private LocalDate serviceMonth;

        public Long getCaseId() { return caseId; }
        public void setCaseId(Long v) { this.caseId = v; }
        public LocalDate getServiceMonth() { return serviceMonth; }
        public void setServiceMonth(LocalDate v) { this.serviceMonth = v; }
    }
}
