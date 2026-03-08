package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.dto.LiveInCertEntryResponse;
import com.cmips.dto.LiveInCertLookupRequest;
import com.cmips.dto.LiveInCertSaveRequest;
import com.cmips.entity.*;
import com.cmips.entity.ProviderEntity.ProviderStatus;
import com.cmips.repository.ProviderRepository;
import com.cmips.repository.ProviderAssignmentRepository;
import com.cmips.repository.OvertimeViolationRepository;
import com.cmips.repository.OvertimeExemptionRepository;
import com.cmips.repository.WorkweekAgreementRepository;
import com.cmips.repository.TravelTimeRepository;
import com.cmips.repository.ProviderBenefitRepository;
import com.cmips.repository.ProviderAttachmentRepository;
import com.cmips.repository.BackupProviderHoursRepository;
import com.cmips.repository.ProviderSickLeaveAccrualRepository;
import com.cmips.repository.ProviderNotificationPreferenceRepository;
import com.cmips.repository.RecipientWaiverRepository;
import com.cmips.repository.SickLeaveClaimRepository;
import com.cmips.service.LiveInSelfCertificationService;
import com.cmips.service.ProviderManagementService;
import com.cmips.service.FieldLevelAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Provider Management REST Controller
 * All endpoints are protected by configurable Keycloak permissions
 */
@RestController
@RequestMapping("/api/providers")
@CrossOrigin(origins = "*")
public class ProviderManagementController {

    private static final Logger log = LoggerFactory.getLogger(ProviderManagementController.class);

    private final ProviderManagementService providerService;
    private final ProviderRepository providerRepository;
    private final ProviderAssignmentRepository assignmentRepository;
    private final OvertimeViolationRepository violationRepository;
    private final OvertimeExemptionRepository exemptionRepository;
    private final WorkweekAgreementRepository workweekAgreementRepository;
    private final TravelTimeRepository travelTimeRepository;
    private final ProviderBenefitRepository benefitRepository;
    private final ProviderAttachmentRepository attachmentRepository;
    private final BackupProviderHoursRepository backupHoursRepository;
    private final FieldLevelAuthorizationService fieldAuthService;
    private final LiveInSelfCertificationService liveInCertService;
    private final ProviderSickLeaveAccrualRepository sickLeaveAccrualRepository;
    private final ProviderNotificationPreferenceRepository notificationPrefRepository;
    private final RecipientWaiverRepository recipientWaiverRepository;
    private final SickLeaveClaimRepository sickLeaveClaimRepository;

    public ProviderManagementController(ProviderManagementService providerService,
                                        ProviderRepository providerRepository,
                                        ProviderAssignmentRepository assignmentRepository,
                                        OvertimeViolationRepository violationRepository,
                                        OvertimeExemptionRepository exemptionRepository,
                                        WorkweekAgreementRepository workweekAgreementRepository,
                                        TravelTimeRepository travelTimeRepository,
                                        ProviderBenefitRepository benefitRepository,
                                        ProviderAttachmentRepository attachmentRepository,
                                        BackupProviderHoursRepository backupHoursRepository,
                                        FieldLevelAuthorizationService fieldAuthService,
                                        LiveInSelfCertificationService liveInCertService,
                                        ProviderSickLeaveAccrualRepository sickLeaveAccrualRepository,
                                        ProviderNotificationPreferenceRepository notificationPrefRepository,
                                        RecipientWaiverRepository recipientWaiverRepository,
                                        SickLeaveClaimRepository sickLeaveClaimRepository) {
        this.providerService = providerService;
        this.providerRepository = providerRepository;
        this.assignmentRepository = assignmentRepository;
        this.violationRepository = violationRepository;
        this.exemptionRepository = exemptionRepository;
        this.workweekAgreementRepository = workweekAgreementRepository;
        this.travelTimeRepository = travelTimeRepository;
        this.benefitRepository = benefitRepository;
        this.attachmentRepository = attachmentRepository;
        this.backupHoursRepository = backupHoursRepository;
        this.fieldAuthService = fieldAuthService;
        this.liveInCertService = liveInCertService;
        this.sickLeaveAccrualRepository = sickLeaveAccrualRepository;
        this.notificationPrefRepository = notificationPrefRepository;
        this.recipientWaiverRepository = recipientWaiverRepository;
        this.sickLeaveClaimRepository = sickLeaveClaimRepository;
    }

    // ==================== PROVIDER CRUD ====================

    @GetMapping
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<java.util.Map<String, Object>> getAllProviders(
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<ProviderEntity> providerPage;

        if (countyCode != null && !countyCode.isEmpty()) {
            providerPage = providerRepository.findByDojCountyCode(countyCode, pageable);
        } else if (status != null && !status.isEmpty()) {
            providerPage = providerRepository.findByProviderStatus(ProviderStatus.valueOf(status), pageable);
        } else {
            providerPage = providerRepository.findAll(pageable);
        }

        // Return paginated response with metadata
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", providerPage.getContent());
        response.put("totalElements", providerPage.getTotalElements());
        response.put("totalPages", providerPage.getTotalPages());
        response.put("currentPage", providerPage.getNumber());
        response.put("pageSize", providerPage.getSize());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getProviderById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {

        ProviderEntity provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        Map<String, Object> filteredProvider = fieldAuthService.filterFieldsForRole(provider, roles, "Provider Resource");
        // Add computed properties not captured by field reflection
        filteredProvider.put("fullName", provider.getFullName());
        filteredProvider.put("eligibleToServe", provider.isEligibleToServe());
        return ResponseEntity.ok(filteredProvider);
    }

    @GetMapping("/search")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<List<Map<String, Object>>> searchProviders(
            @RequestParam(required = false) String providerNumber,
            @RequestParam(required = false) String ssn,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String countyCode,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {

        List<ProviderEntity> providers = providerRepository.searchProviders(
                providerNumber, ssn, lastName, firstName, countyCode);

        List<Map<String, Object>> filteredProviders = providers.stream()
                .map(p -> {
                    Map<String, Object> m = fieldAuthService.filterFieldsForRole(p, roles, "Provider Resource");
                    m.put("fullName", p.getFullName());
                    m.put("eligibleToServe", p.isEligibleToServe());
                    return m;
                })
                .toList();

        return ResponseEntity.ok(filteredProviders);
    }

    @PostMapping
    @RequirePermission(resource = "Provider Resource", scope = "create")
    public ResponseEntity<ProviderEntity> createProvider(
            @RequestBody ProviderEntity provider,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderEntity created = providerService.createProvider(provider, userId);
        return ResponseEntity.ok(created);
    }

    // ==================== ENROLLMENT MANAGEMENT ====================

    @PutMapping("/{id}/approve-enrollment")
    @RequirePermission(resource = "Provider Resource", scope = "approve")
    public ResponseEntity<ProviderEntity> approveEnrollment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderEntity provider = providerService.approveEnrollment(id, userId);
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/{id}/set-ineligible")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<ProviderEntity> setProviderIneligible(
            @PathVariable Long id,
            @RequestBody IneligibleRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderEntity provider = providerService.setProviderIneligible(id, request.getReason(), userId);
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/{id}/reinstate")
    @RequirePermission(resource = "Provider Resource", scope = "reinstate")
    public ResponseEntity<ProviderEntity> reinstateProvider(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderEntity provider = providerService.reinstateProvider(id, userId);
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/{id}/re-enroll")
    @RequirePermission(resource = "Provider Resource", scope = "enroll")
    public ResponseEntity<ProviderEntity> reEnrollProvider(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderEntity provider = providerService.reEnrollProvider(id, userId);
        return ResponseEntity.ok(provider);
    }

    // ==================== PROVIDER ASSIGNMENTS ====================

    @GetMapping("/{id}/assignments")
    @RequirePermission(resource = "Provider Assignment Resource", scope = "view")
    public ResponseEntity<List<ProviderAssignmentEntity>> getProviderAssignments(@PathVariable Long id) {
        List<ProviderAssignmentEntity> assignments = assignmentRepository.findByProviderId(id);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/assignments")
    @RequirePermission(resource = "Provider Assignment Resource", scope = "create")
    public ResponseEntity<ProviderAssignmentEntity> assignProviderToCase(
            @RequestBody AssignmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderAssignmentEntity assignment = providerService.assignProviderToCase(
                request.getProviderId(),
                request.getCaseId(),
                request.getProviderType(),
                request.getRelationship(),
                request.getAssignedHours(),
                userId);

        return ResponseEntity.ok(assignment);
    }

    @PutMapping("/assignments/{id}/terminate")
    @RequirePermission(resource = "Provider Assignment Resource", scope = "terminate")
    public ResponseEntity<ProviderAssignmentEntity> terminateAssignment(
            @PathVariable Long id,
            @RequestBody TerminationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderAssignmentEntity assignment = providerService.terminateProviderAssignment(id, request.getReason(), userId);
        return ResponseEntity.ok(assignment);
    }

    @PutMapping("/assignments/{id}/leave")
    @RequirePermission(resource = "Provider Assignment Resource", scope = "edit")
    public ResponseEntity<ProviderAssignmentEntity> placeOnLeave(
            @PathVariable Long id,
            @RequestBody LeaveRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderAssignmentEntity assignment = providerService.placeProviderOnLeave(id, request.getReason(), userId);
        return ResponseEntity.ok(assignment);
    }

    // ==================== RECIPIENT WAIVER ====================

    @PostMapping("/assignments/{id}/recipient-waiver")
    @RequirePermission(resource = "Provider Assignment Resource", scope = "edit")
    public ResponseEntity<ProviderAssignmentEntity> addRecipientWaiver(
            @PathVariable Long id,
            @RequestBody RecipientWaiverRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderAssignmentEntity assignment = providerService.addRecipientWaiver(
                id, request.getCoriId(), request.getEndDate(), userId);
        return ResponseEntity.ok(assignment);
    }

    // ==================== CORI MANAGEMENT ====================

    @GetMapping("/{id}/cori")
    @RequirePermission(resource = "Provider CORI Resource", scope = "view")
    public ResponseEntity<List<ProviderCoriEntity>> getProviderCori(@PathVariable Long id) {
        List<ProviderCoriEntity> coriRecords = providerService.getProviderCoriRecords(id);
        return ResponseEntity.ok(coriRecords);
    }

    @PostMapping("/{id}/cori")
    @RequirePermission(resource = "Provider CORI Resource", scope = "create")
    public ResponseEntity<ProviderCoriEntity> createCori(
            @PathVariable Long id,
            @RequestBody ProviderCoriEntity cori,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderCoriEntity created = providerService.createCori(id, cori, userId);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/cori/{coriId}/general-exception")
    @RequirePermission(resource = "Provider CORI Resource", scope = "edit")
    public ResponseEntity<ProviderCoriEntity> addGeneralException(
            @PathVariable Long coriId,
            @RequestBody GeneralExceptionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderCoriEntity cori = providerService.addGeneralException(
                coriId, request.getBeginDate(), request.getEndDate(), request.getNotes(), userId);
        return ResponseEntity.ok(cori);
    }

    // ==================== OVERTIME VIOLATIONS ====================

    @GetMapping("/{id}/violations")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "view")
    public ResponseEntity<List<OvertimeViolationEntity>> getProviderViolations(@PathVariable Long id) {
        List<OvertimeViolationEntity> violations = violationRepository.findByProviderId(id);
        return ResponseEntity.ok(violations);
    }

    @PostMapping("/{id}/violations")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "create")
    public ResponseEntity<OvertimeViolationEntity> createViolation(
            @PathVariable Long id,
            @RequestBody ViolationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        OvertimeViolationEntity violation = providerService.createOvertimeViolation(
                id, request.getViolationType(), request.getMonth(), request.getYear(),
                request.getHoursClaimed(), request.getMaximumAllowed(), userId);

        return ResponseEntity.ok(violation);
    }

    @PutMapping("/violations/{violationId}/county-review")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "review")
    public ResponseEntity<OvertimeViolationEntity> countyReview(
            @PathVariable Long violationId,
            @RequestBody CountyReviewRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String reviewerId) {

        OvertimeViolationEntity violation = providerService.countyReviewViolation(
                violationId, request.getOutcome(), request.getComments(), reviewerId);
        return ResponseEntity.ok(violation);
    }

    @PutMapping("/violations/{violationId}/supervisor-review")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "supervisor-review")
    public ResponseEntity<OvertimeViolationEntity> supervisorReview(
            @PathVariable Long violationId,
            @RequestBody SupervisorReviewRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String reviewerId) {

        OvertimeViolationEntity violation = providerService.supervisorReviewViolation(
                violationId, request.getOutcome(), request.getComments(), reviewerId);
        return ResponseEntity.ok(violation);
    }

    @PutMapping("/violations/{violationId}/training-completed")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "edit")
    public ResponseEntity<OvertimeViolationEntity> recordTrainingCompletion(
            @PathVariable Long violationId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        OvertimeViolationEntity violation = providerService.recordTrainingCompletion(violationId, userId);
        return ResponseEntity.ok(violation);
    }

    @GetMapping("/violations/pending-review")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "view")
    public ResponseEntity<List<OvertimeViolationEntity>> getPendingReviewViolations() {
        List<OvertimeViolationEntity> violations = violationRepository.findPendingReviewViolations();
        return ResponseEntity.ok(violations);
    }

    @GetMapping("/violations/pending-supervisor-review")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "view")
    public ResponseEntity<List<OvertimeViolationEntity>> getPendingSupervisorReviewViolations() {
        List<OvertimeViolationEntity> violations = violationRepository.findViolationsWithPendingSupervisorReview();
        return ResponseEntity.ok(violations);
    }

    // ==================== SICK LEAVE ====================

    @PutMapping("/{id}/check-sick-leave-accrual")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<Void> checkSickLeaveAccrual(@PathVariable Long id) {
        providerService.checkAndAccrueSickLeave(id);
        return ResponseEntity.ok().build();
    }

    // ==================== ELIGIBLE PROVIDERS ====================

    @GetMapping("/eligible")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<List<ProviderEntity>> getEligibleProviders(
            @RequestParam(required = false) String countyCode) {

        List<ProviderEntity> providers;
        if (countyCode != null) {
            providers = providerRepository.findEligibleProvidersByCounty(countyCode);
        } else {
            providers = providerRepository.findByEligible("YES");
        }
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/pending-reinstatement")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<List<ProviderEntity>> getProvidersEligibleForReinstatement() {
        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        List<ProviderEntity> providers = providerRepository.findProvidersEligibleForReinstatement(cutoffDate);
        return ResponseEntity.ok(providers);
    }

    // ==================== REQUEST DTOs ====================

    @lombok.Data
    public static class IneligibleRequest {
        private String reason;
    }

    @lombok.Data
    public static class AssignmentRequest {
        private Long providerId;
        private Long caseId;
        private String providerType;
        private String relationship;
        private Double assignedHours;
    }

    @lombok.Data
    public static class TerminationRequest {
        private String reason;
    }

    @lombok.Data
    public static class LeaveRequest {
        private String reason;
    }

    @lombok.Data
    public static class RecipientWaiverRequest {
        private Long coriId;
        private LocalDate endDate;
    }

    @lombok.Data
    public static class GeneralExceptionRequest {
        private LocalDate beginDate;
        private LocalDate endDate;
        private String notes;
    }

    @lombok.Data
    public static class ViolationRequest {
        private String violationType;
        private Integer month;
        private Integer year;
        private Double hoursClaimed;
        private Double maximumAllowed;
    }

    @lombok.Data
    public static class CountyReviewRequest {
        private String outcome;
        private String comments;
    }

    @lombok.Data
    public static class SupervisorReviewRequest {
        private String outcome;
        private String comments;
    }

    // ========================================
    // IRS Live-In Provider Self-Certification (DSD Section 32, CI-718023/718024)
    // ========================================

    @PostMapping("/live-in-cert/lookup")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> lookupLiveInCert(@RequestBody LiveInCertLookupRequest request) {
        try {
            LiveInCertEntryResponse detail = liveInCertService.lookupProviderCase(request);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/live-in-cert/save")
    @RequirePermission(resource = "Provider Resource", scope = "create")
    public ResponseEntity<?> saveLiveInCert(@RequestBody LiveInCertSaveRequest request) {
        try {
            LiveInSelfCertificationEntity saved = liveInCertService.saveCertification(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Self-certification saved successfully.",
                    "certificationStatus", saved.getCertificationStatus(),
                    "statusDate", saved.getStatusDate().toString(),
                    "id", saved.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==================== COUNTY DISPUTE (DSD Section 23 - OT Violation) ====================

    /**
     * File a county dispute for an overtime violation.
     * DSD: County can dispute a violation within specified timeframe.
     * Triggers task to County Dispute work queue.
     */
    @PutMapping("/violations/{violationId}/county-dispute")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "review")
    public ResponseEntity<?> fileCountyDispute(@PathVariable Long violationId,
                                               @RequestBody CountyDisputeRequest req,
                                               @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            OvertimeViolationEntity updated = providerService.fileCountyDispute(violationId, req.comments, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error filing county dispute for violation {}", violationId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Resolve a county dispute.
     * Outcome: UPHELD (provider remains ineligible) or OVERRIDE (refer to supervisor).
     */
    @PutMapping("/violations/{violationId}/county-dispute/resolve")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "supervisor-review")
    public ResponseEntity<?> resolveCountyDispute(@PathVariable Long violationId,
                                                  @RequestBody DisputeResolutionRequest req,
                                                  @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            OvertimeViolationEntity updated = providerService.resolveCountyDispute(violationId, req.outcome, req.comments, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error resolving county dispute for violation {}", violationId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Request CDSS State Administrative Review (SAR).
     * Provider can request SAR after county dispute upheld.
     */
    @PutMapping("/violations/{violationId}/cdss-review")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "review")
    public ResponseEntity<?> requestCdssReview(@PathVariable Long violationId,
                                               @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            OvertimeViolationEntity updated = providerService.requestCdssReview(violationId, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error requesting CDSS review for violation {}", violationId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Record CDSS final review outcome.
     * CDSS provides final determination on SAR.
     */
    @PutMapping("/violations/{violationId}/cdss-review/outcome")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "approve")
    public ResponseEntity<?> recordCdssOutcome(@PathVariable Long violationId,
                                               @RequestBody CdssReviewRequest req,
                                               @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            OvertimeViolationEntity updated = providerService.recordCdssReviewOutcome(violationId, req.outcome, req.comments, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error recording CDSS outcome for violation {}", violationId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== OVERTIME EXEMPTIONS (CI-668111) ====================

    /** GET all exemptions for a provider */
    @GetMapping("/{id}/exemptions")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "view")
    public ResponseEntity<?> getExemptions(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(exemptionRepository.findExemptionHistoryForProvider(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** GET active exemption for a provider */
    @GetMapping("/{id}/exemptions/active")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "view")
    public ResponseEntity<?> getActiveExemption(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(
                    exemptionRepository.findActiveExemptionForProvider(id, LocalDate.now())
                            .orElse(null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** POST create a new overtime exemption (CI-668111 / CI-790066) */
    @PostMapping("/{id}/exemptions")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "create")
    public ResponseEntity<?> createExemption(@PathVariable Long id,
                                             @RequestBody OvertimeExemptionEntity exemption,
                                             @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            OvertimeExemptionEntity created = providerService.createOvertimeExemption(id, exemption, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating exemption for provider {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT modify an exemption (CI-668118) */
    @PutMapping("/exemptions/{exemptionId}")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "edit")
    public ResponseEntity<?> modifyExemption(@PathVariable Long exemptionId,
                                             @RequestBody OvertimeExemptionEntity updates,
                                             @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            OvertimeExemptionEntity updated = providerService.modifyOvertimeExemption(exemptionId, updates, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error modifying exemption {}", exemptionId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT inactivate an exemption (CI-668113) */
    @PutMapping("/exemptions/{exemptionId}/inactivate")
    @RequirePermission(resource = "Overtime Violation Resource", scope = "edit")
    public ResponseEntity<?> inactivateExemption(@PathVariable Long exemptionId,
                                                 @RequestBody InactivateRequest req,
                                                 @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            OvertimeExemptionEntity updated = providerService.inactivateOvertimeExemption(exemptionId, req.reason, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error inactivating exemption {}", exemptionId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== WORKWEEK AGREEMENTS (CI-480910) ====================

    /** GET workweek agreements for a provider */
    @GetMapping("/{id}/workweek-agreements")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getWorkweekAgreements(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(workweekAgreementRepository.findAgreementHistoryForProvider(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** POST create a workweek agreement */
    @PostMapping("/{id}/workweek-agreements")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> createWorkweekAgreement(@PathVariable Long id,
                                                     @RequestBody WorkweekAgreementEntity agreement,
                                                     @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            WorkweekAgreementEntity created = providerService.createWorkweekAgreement(id, agreement, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating workweek agreement for provider {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT modify a workweek agreement */
    @PutMapping("/workweek-agreements/{agreementId}")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> modifyWorkweekAgreement(@PathVariable Long agreementId,
                                                     @RequestBody WorkweekAgreementEntity updates,
                                                     @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            WorkweekAgreementEntity updated = providerService.modifyWorkweekAgreement(agreementId, updates, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error modifying workweek agreement {}", agreementId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT inactivate a workweek agreement */
    @PutMapping("/workweek-agreements/{agreementId}/inactivate")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> inactivateWorkweekAgreement(@PathVariable Long agreementId,
                                                         @RequestBody InactivateRequest req,
                                                         @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            WorkweekAgreementEntity updated = providerService.inactivateWorkweekAgreement(agreementId, req.reason, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error inactivating workweek agreement {}", agreementId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== TRAVEL TIME (CI-480867) ====================

    /** GET travel times for a provider */
    @GetMapping("/{id}/travel-times")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getTravelTimes(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(travelTimeRepository.findHistoryForProvider(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** POST create a travel time record */
    @PostMapping("/{id}/travel-times")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> createTravelTime(@PathVariable Long id,
                                              @RequestBody TravelTimeEntity travelTime,
                                              @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            TravelTimeEntity created = providerService.createTravelTime(id, travelTime, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating travel time for provider {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT modify a travel time record */
    @PutMapping("/travel-times/{travelTimeId}")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> modifyTravelTime(@PathVariable Long travelTimeId,
                                              @RequestBody TravelTimeEntity updates,
                                              @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            TravelTimeEntity updated = providerService.modifyTravelTime(travelTimeId, updates, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error modifying travel time {}", travelTimeId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT inactivate a travel time record */
    @PutMapping("/travel-times/{travelTimeId}/inactivate")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> inactivateTravelTime(@PathVariable Long travelTimeId,
                                                  @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            TravelTimeEntity updated = providerService.inactivateTravelTime(travelTimeId, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error inactivating travel time {}", travelTimeId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== PROVIDER BENEFITS / DEDUCTIONS (CI-117534) ====================

    /** GET benefits for a provider */
    @GetMapping("/{id}/benefits")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getBenefits(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(benefitRepository.findHistoryForProvider(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** POST create a benefit/deduction */
    @PostMapping("/{id}/benefits")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> createBenefit(@PathVariable Long id,
                                           @RequestBody ProviderBenefitEntity benefit,
                                           @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            ProviderBenefitEntity created = providerService.createProviderBenefit(id, benefit, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating benefit for provider {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT modify a benefit */
    @PutMapping("/benefits/{benefitId}")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> modifyBenefit(@PathVariable Long benefitId,
                                           @RequestBody ProviderBenefitEntity updates,
                                           @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            ProviderBenefitEntity updated = providerService.modifyProviderBenefit(benefitId, updates, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error modifying benefit {}", benefitId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT terminate a benefit */
    @PutMapping("/benefits/{benefitId}/terminate")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> terminateBenefit(@PathVariable Long benefitId,
                                              @RequestBody InactivateRequest req,
                                              @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            ProviderBenefitEntity updated = providerService.terminateProviderBenefit(benefitId, req.reason, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error terminating benefit {}", benefitId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CORI MODIFY / INACTIVATE (CI-117566/117567) ====================

    /** PUT modify a CORI record — CI-117567 */
    @PutMapping("/cori/{coriId}")
    @RequirePermission(resource = "Provider CORI Resource", scope = "edit")
    public ResponseEntity<?> modifyCori(@PathVariable Long coriId,
                                        @RequestBody ProviderCoriEntity updates,
                                        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(providerService.modifyCori(coriId, updates, userId));
        } catch (Exception e) {
            log.error("Error modifying CORI {}", coriId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT inactivate a CORI record — CI-117566 */
    @PutMapping("/cori/{coriId}/inactivate")
    @RequirePermission(resource = "Provider CORI Resource", scope = "edit")
    public ResponseEntity<?> inactivateCori(@PathVariable Long coriId,
                                            @RequestBody InactivateRequest req,
                                            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(providerService.inactivateCori(coriId, req.reason, userId));
        } catch (Exception e) {
            log.error("Error inactivating CORI {}", coriId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== PROVIDER ATTACHMENTS (CI-117642-117650) ====================

    /** GET list attachments for a provider — CI-117642 */
    @GetMapping("/{id}/attachments")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getAttachments(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(attachmentRepository.findByProviderIdOrderByUploadDateDesc(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** POST upload an attachment — CI-117643 */
    @PostMapping("/{id}/attachments")
    @RequirePermission(resource = "Provider Resource", scope = "create")
    public ResponseEntity<?> uploadAttachment(@PathVariable Long id,
                                              @RequestBody ProviderAttachmentEntity attachment,
                                              @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            ProviderAttachmentEntity created = providerService.uploadAttachment(id, attachment, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading attachment for provider {}", id, e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT update attachment description — CI-117649 */
    @PutMapping("/attachments/{attachmentId}/description")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> updateAttachmentDescription(@PathVariable Long attachmentId,
                                                          @RequestBody DescriptionRequest req,
                                                          @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(providerService.updateAttachmentDescription(attachmentId, req.description, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT archive an attachment — CI-117644 */
    @PutMapping("/attachments/{attachmentId}/archive")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> archiveAttachment(@PathVariable Long attachmentId,
                                               @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(providerService.archiveAttachment(attachmentId, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT restore an archived attachment — CI-117645 (same-day only) */
    @PutMapping("/attachments/{attachmentId}/restore")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> restoreAttachment(@PathVariable Long attachmentId,
                                               @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(providerService.restoreAttachment(attachmentId, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== BACKUP PROVIDER HOURS (CI-117646/117647) ====================

    /** GET backup provider hours for a provider — CI-117646 */
    @GetMapping("/{id}/backup-hours")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getBackupHours(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(backupHoursRepository.findHistoryForProvider(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** POST create backup provider hours — CI-117647 */
    @PostMapping("/{id}/backup-hours")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> createBackupHours(@PathVariable Long id,
                                               @RequestBody BackupProviderHoursEntity hours,
                                               @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            BackupProviderHoursEntity created = providerService.createBackupProviderHours(id, hours, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating backup hours for provider {}", id, e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT modify backup provider hours — CI-117647 */
    @PutMapping("/backup-hours/{hoursId}")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> modifyBackupHours(@PathVariable Long hoursId,
                                               @RequestBody BackupProviderHoursEntity updates,
                                               @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(providerService.modifyBackupProviderHours(hoursId, updates, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT terminate backup provider hours */
    @PutMapping("/backup-hours/{hoursId}/terminate")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> terminateBackupHours(@PathVariable Long hoursId,
                                                  @RequestBody InactivateRequest req,
                                                  @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(providerService.terminateBackupProviderHours(hoursId, req.reason, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== MONTHLY PAID HOURS ====================

    /** GET monthly paid hours summary for a provider */
    @GetMapping("/{id}/monthly-paid-hours")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getMonthlyPaidHours(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(providerService.getMonthlyPaidHours(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== SSN VERIFICATION ====================

    /** PUT trigger SSN verification for a provider (sends to CMRS701E batch queue) */
    @PutMapping("/{id}/verify-ssn")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> triggerSsnVerification(@PathVariable Long id,
                                                     @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(providerService.triggerSsnVerification(id, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT update SSN verification result (called by CMRR701D batch receive) */
    @PutMapping("/{id}/ssn-verification-result")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> updateSsnVerificationResult(@PathVariable Long id,
                                                          @RequestBody SsnResultRequest req,
                                                          @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.ok(providerService.updateSsnVerificationResult(id, req.verificationStatus, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== PROVIDER TRAINING (CI-117545 / CI-67804) ====================

    /** GET all training records for a provider */
    @GetMapping("/{id}/training")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getProviderTraining(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(providerService.getProviderTraining(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST record a training completion for a provider */
    @PostMapping("/{id}/training")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> recordTraining(@PathVariable Long id,
                                            @RequestBody com.cmips.entity.ProviderTrainingEntity training,
                                            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                    .body(providerService.recordTraining(id, training, userId));
        } catch (Exception e) {
            log.error("Error recording training for provider {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET provider qualification summary — all enrollment requirements + CORI + training status */
    @GetMapping("/{id}/qualification-summary")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getQualificationSummary(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(providerService.getQualificationSummary(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== SICK LEAVE ACCRUAL (DSD Section 23.9) ====================

    /** GET sick leave accrual records for a provider */
    @GetMapping("/{id}/sick-leave-accruals")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getSickLeaveAccruals(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(sickLeaveAccrualRepository.findByProviderIdOrderByAccrualYearDesc(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET sick leave accrual for current year */
    @GetMapping("/{id}/sick-leave-accruals/current")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getCurrentSickLeaveAccrual(@PathVariable Long id) {
        try {
            int currentYear = LocalDate.now().getYear();
            var accrual = sickLeaveAccrualRepository.findByProviderIdAndAccrualYear(id, currentYear);
            if (accrual.isPresent()) {
                var a = accrual.get();
                int available = (a.getHoursAccrued() + a.getHoursCarriedOver()) - a.getHoursUsed();
                return ResponseEntity.ok(Map.of(
                    "accrual", a,
                    "hoursAvailableMinutes", available,
                    "hoursAvailableFormatted", String.format("%d:%02d", available / 60, available % 60)
                ));
            }
            return ResponseEntity.ok(Map.of("message", "No sick leave accrual record for current year"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST create or update sick leave accrual for a year */
    @PostMapping("/{id}/sick-leave-accruals")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> createOrUpdateSickLeaveAccrual(@PathVariable Long id,
            @RequestBody ProviderSickLeaveAccrualEntity accrual,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            accrual.setProviderId(id);
            accrual.setCreatedBy(userId);
            accrual.setUpdatedBy(userId);
            var existing = sickLeaveAccrualRepository.findByProviderIdAndAccrualYear(id, accrual.getAccrualYear());
            if (existing.isPresent()) {
                var e = existing.get();
                e.setHoursAccrued(accrual.getHoursAccrued());
                e.setHoursUsed(accrual.getHoursUsed());
                e.setYtdHoursUsed(accrual.getYtdHoursUsed());
                e.setTotalHoursWorked(accrual.getTotalHoursWorked());
                e.setLastAccrualDate(accrual.getLastAccrualDate());
                e.setUpdatedBy(userId);
                return ResponseEntity.ok(sickLeaveAccrualRepository.save(e));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(sickLeaveAccrualRepository.save(accrual));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET sick leave claims for a provider */
    @GetMapping("/{id}/sick-leave-claims")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getSickLeaveClaims(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(sickLeaveClaimRepository.findByProviderIdOrderByPayPeriodBeginDateDesc(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== NOTIFICATION PREFERENCES (DSD Section 23.10) ====================

    /** GET notification preferences for a provider */
    @GetMapping("/{id}/notification-preferences")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getNotificationPreferences(@PathVariable Long id) {
        try {
            var prefs = notificationPrefRepository.findByProviderId(id);
            if (prefs.isPresent()) {
                return ResponseEntity.ok(prefs.get());
            }
            return ResponseEntity.ok(Map.of("message", "No notification preferences configured"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST/PUT notification preferences */
    @PutMapping("/{id}/notification-preferences")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> updateNotificationPreferences(@PathVariable Long id,
            @RequestBody ProviderNotificationPreferenceEntity prefs,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = notificationPrefRepository.findByProviderId(id);
            if (existing.isPresent()) {
                var e = existing.get();
                e.setPreferredContactMethod(prefs.getPreferredContactMethod());
                e.setEmailNotificationsEnabled(prefs.getEmailNotificationsEnabled());
                e.setSmsNotificationsEnabled(prefs.getSmsNotificationsEnabled());
                e.setPhoneNotificationsEnabled(prefs.getPhoneNotificationsEnabled());
                e.setMailNotificationsEnabled(prefs.getMailNotificationsEnabled());
                e.setTimesheetReminders(prefs.getTimesheetReminders());
                e.setPaymentConfirmations(prefs.getPaymentConfirmations());
                e.setCaseAssignmentChanges(prefs.getCaseAssignmentChanges());
                e.setPolicyUpdates(prefs.getPolicyUpdates());
                e.setTrainingOpportunities(prefs.getTrainingOpportunities());
                e.setTimesheetMethod(prefs.getTimesheetMethod());
                e.setETimesheetIndicator(prefs.getETimesheetIndicator());
                e.setUpdatedBy(userId);
                return ResponseEntity.ok(notificationPrefRepository.save(e));
            }
            prefs.setProviderId(id);
            prefs.setCreatedBy(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(notificationPrefRepository.save(prefs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT verify cell phone */
    @PutMapping("/{id}/verify-cell-phone")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> verifyCellPhone(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var prefs = notificationPrefRepository.findByProviderId(id);
            if (prefs.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No notification preferences found"));
            }
            var p = prefs.get();
            p.setCellPhoneVerified(true);
            p.setCellPhoneVerifiedDate(java.time.LocalDateTime.now());
            p.setUpdatedBy(userId);
            return ResponseEntity.ok(notificationPrefRepository.save(p));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT stop e-timesheet — set indicator to NO for provider */
    @PutMapping("/{id}/stop-e-timesheet")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> stopETimesheet(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var prefs = notificationPrefRepository.findByProviderId(id);
            if (prefs.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No notification preferences found"));
            }
            var p = prefs.get();
            p.setETimesheetIndicator(false);
            p.setTimesheetMethod("PAPER");
            p.setUpdatedBy(userId);
            return ResponseEntity.ok(notificationPrefRepository.save(p));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== RECIPIENT WAIVER CRUD (DSD Section 23.3) ====================

    /** GET all waivers for a provider */
    @GetMapping("/{id}/waivers")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getProviderWaivers(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(recipientWaiverRepository.findByProviderIdOrderByCreatedAtDesc(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET single waiver */
    @GetMapping("/waivers/{waiverId}")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<?> getWaiver(@PathVariable String waiverId) {
        try {
            return recipientWaiverRepository.findById(waiverId)
                .map(w -> ResponseEntity.ok((Object) w))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT modify waiver — update notes, justification */
    @PutMapping("/waivers/{waiverId}")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> modifyWaiver(@PathVariable String waiverId,
            @RequestBody Map<String, Object> updates,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var waiver = recipientWaiverRepository.findById(waiverId);
            if (waiver.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            var w = waiver.get();
            if (updates.containsKey("notes")) w.setNotes((String) updates.get("notes"));
            if (updates.containsKey("recipientJustification")) w.setRecipientJustification((String) updates.get("recipientJustification"));
            w.setUpdatedBy(userId);
            return ResponseEntity.ok(recipientWaiverRepository.save(w));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT inactivate waiver — revoke */
    @PutMapping("/waivers/{waiverId}/inactivate")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> inactivateWaiver(@PathVariable String waiverId,
            @RequestBody InactivateRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var waiver = recipientWaiverRepository.findById(waiverId);
            if (waiver.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            var w = waiver.get();
            w.setRevoked(true);
            w.setRevocationDate(LocalDate.now());
            w.setRevocationReason(request.reason);
            w.setRevokedBy(userId);
            w.setStatus(RecipientWaiverEntity.WaiverStatus.REVOKED);
            w.setUpdatedBy(userId);
            return ResponseEntity.ok(recipientWaiverRepository.save(w));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== VIOLATION STATE REVIEW (DSD Section 23.6) ====================

    /** PUT state review — state office reviews county's determination */
    @PutMapping("/violations/{violationId}/state-review")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> stateReview(@PathVariable Long violationId,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var violation = violationRepository.findById(violationId);
            if (violation.isEmpty()) return ResponseEntity.notFound().build();
            var v = violation.get();
            v.setStateReviewOutcome(body.get("outcome"));
            v.setStateReviewComments(body.get("comments"));
            v.setStateReviewedBy(userId);
            v.setStateReviewDate(LocalDate.now());
            return ResponseEntity.ok(violationRepository.save(v));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT state supervisor review */
    @PutMapping("/violations/{violationId}/state-supervisor-review")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> stateSupervisorReview(@PathVariable Long violationId,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var violation = violationRepository.findById(violationId);
            if (violation.isEmpty()) return ResponseEntity.notFound().build();
            var v = violation.get();
            v.setStateSupervisorOutcome(body.get("outcome"));
            v.setStateSupervisorComments(body.get("comments"));
            v.setStateSupervisorId(userId);
            v.setStateSupervisorReviewDate(LocalDate.now());
            return ResponseEntity.ok(violationRepository.save(v));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== INNER REQUEST CLASSES ====================

    static class CountyDisputeRequest { public String comments; }
    static class DisputeResolutionRequest { public String outcome; public String comments; }
    static class CdssReviewRequest { public String outcome; public String comments; }
    static class InactivateRequest { public String reason; }
    static class DescriptionRequest { public String description; }
    static class SsnResultRequest { public String verificationStatus; }
}
