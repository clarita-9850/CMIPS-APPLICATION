package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.dto.ValidationError;
import com.cmips.entity.*;
import com.cmips.entity.ProviderEntity.ProviderStatus;
import com.cmips.integration.SsaVerificationService;
import com.cmips.integration.DojBackgroundCheckService;
import com.cmips.integration.MediCalSuspendedListService;
import com.cmips.repository.ProviderRepository;
import com.cmips.repository.ProviderAssignmentRepository;
import com.cmips.repository.OvertimeViolationRepository;
import com.cmips.service.ProviderManagementService;
import com.cmips.service.ProviderManagementService.ProviderValidationException;
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
    private final FieldLevelAuthorizationService fieldAuthService;
    private final SsaVerificationService ssaService;
    private final DojBackgroundCheckService dojService;
    private final MediCalSuspendedListService mediCalService;

    public ProviderManagementController(ProviderManagementService providerService,
                                        ProviderRepository providerRepository,
                                        ProviderAssignmentRepository assignmentRepository,
                                        OvertimeViolationRepository violationRepository,
                                        FieldLevelAuthorizationService fieldAuthService,
                                        SsaVerificationService ssaService,
                                        DojBackgroundCheckService dojService,
                                        MediCalSuspendedListService mediCalService) {
        this.providerService = providerService;
        this.providerRepository = providerRepository;
        this.assignmentRepository = assignmentRepository;
        this.violationRepository = violationRepository;
        this.fieldAuthService = fieldAuthService;
        this.ssaService = ssaService;
        this.dojService = dojService;
        this.mediCalService = mediCalService;
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
            providerPage = providerRepository.findByCountyCode(countyCode, pageable);
        } else if (status != null && !status.isEmpty()) {
            providerPage = providerRepository.findByStatus(ProviderStatus.valueOf(status), pageable);
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
            @RequestParam(required = false) String dateOfBirth,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {

        // Parse dateOfBirth string to LocalDate if present
        LocalDate dob = null;
        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            try {
                dob = LocalDate.parse(dateOfBirth);
            } catch (Exception e) {
                log.warn("Invalid dateOfBirth format: {}", dateOfBirth);
            }
        }

        List<ProviderEntity> providers = providerRepository.searchProviders(
                providerNumber,
                ssn != null && !ssn.isBlank() ? ssn : null,
                lastName != null && !lastName.isBlank() ? lastName : null,
                firstName != null && !firstName.isBlank() ? firstName : null,
                countyCode != null && !countyCode.isBlank() ? countyCode : null,
                dob,
                gender != null && !gender.isBlank() ? gender : null,
                city != null && !city.isBlank() ? city : null,
                phone != null && !phone.isBlank() ? phone : null,
                email != null && !email.isBlank() ? email : null);

        List<Map<String, Object>> filteredProviders = providers.stream()
                .map(p -> fieldAuthService.filterFieldsForRole(p, roles, "Provider Resource"))
                .toList();

        return ResponseEntity.ok(filteredProviders);
    }

    @PostMapping
    @RequirePermission(resource = "Provider Resource", scope = "create")
    public ResponseEntity<?> createProvider(
            @RequestBody ProviderEntity provider,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            ProviderEntity created = providerService.createProvider(provider, userId);
            return ResponseEntity.ok(created);
        } catch (ProviderValidationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "validationErrors", e.getErrors()
            ));
        }
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

    @PutMapping("/assignments/{id}/inactivate")
    @RequirePermission(resource = "Provider Assignment Resource", scope = "edit")
    public ResponseEntity<ProviderAssignmentEntity> inactivateProviderOnCase(
            @PathVariable Long id,
            @RequestBody(required = false) TerminationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        String reason = request != null ? request.getReason() : "Provider Inactivated";
        ProviderAssignmentEntity assignment = providerService.inactivateProviderOnCase(id, reason, userId);
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

    // ==================== ENROLLMENT MANAGEMENT (Phase 2) ====================

    @PutMapping("/{id}/modify-enrollment")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> modifyEnrollment(
            @PathVariable Long id,
            @RequestBody ProviderEntity enrollmentData,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            ProviderEntity provider = providerService.modifyEnrollment(id, enrollmentData, userId);
            return ResponseEntity.ok(provider);
        } catch (ProviderValidationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "validationErrors", e.getErrors()
            ));
        }
    }

    @PutMapping("/{id}/reject-enrollment")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<ProviderEntity> rejectEnrollment(
            @PathVariable Long id,
            @RequestBody RejectRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderEntity provider = providerService.rejectEnrollment(id, request.getComments(), userId);
        return ResponseEntity.ok(provider);
    }

    // ==================== ENROLLMENT HISTORY (Phase 3) ====================

    @GetMapping("/{id}/enrollment-history")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<List<ProviderEnrollmentHistory>> getEnrollmentHistory(@PathVariable Long id) {
        List<ProviderEnrollmentHistory> history = providerService.getEnrollmentHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/pre-ineligibility-data")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<ProviderEnrollmentHistory> getPreIneligibilityData(@PathVariable Long id) {
        ProviderEnrollmentHistory data = providerService.getPreIneligibilityData(id);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    // ==================== ENROLLMENT REQUIREMENTS & VERIFICATIONS ====================

    /**
     * Update enrollment requirements and auto-check eligibility.
     * When all requirements met + verifications passed, auto-sets eligible=YES.
     */
    @PutMapping("/{id}/update-enrollment-requirements")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<?> updateEnrollmentRequirements(
            @PathVariable Long id,
            @RequestBody ProviderManagementService.EnrollmentRequirementsUpdate update,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            ProviderEntity provider = providerService.updateEnrollmentRequirements(id, update, userId);
            return ResponseEntity.ok(provider);
        } catch (ProviderValidationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "validationErrors", e.getErrors()
            ));
        }
    }

    /**
     * Run all external verifications (SSN, DOJ, Medi-Cal) for a provider.
     * Calls mock integration services with provider data from DB,
     * updates the provider entity with results, and auto-checks eligibility.
     */
    @PostMapping("/{id}/run-verifications")
    @RequirePermission(resource = "Provider Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> runVerifications(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ProviderEntity provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        Map<String, Object> results = new java.util.HashMap<>();
        String ssn = provider.getSsn() != null ? provider.getSsn().replaceAll("-", "") : "";

        // 1. SSA SSN Verification
        try {
            SsaVerificationService.SsnVerificationRequest ssaReq = ssaService.buildVerificationRequest(
                    ssn, provider.getLastName(), provider.getFirstName(), provider.getMiddleName(),
                    provider.getDateOfBirth(), provider.getGender(),
                    provider.getProviderNumber() != null ? provider.getProviderNumber() : String.valueOf(id));
            SsaVerificationService.SsnVerificationResponse ssaResp = ssaService.verifySSN(ssaReq);
            boolean ssaPassed = "1".equals(ssaResp.getVerificationCode());
            results.put("ssaVerification", Map.of(
                    "passed", ssaPassed,
                    "code", ssaResp.getVerificationCode(),
                    "status", ssaResp.getVerificationCodeDesc()));
        } catch (Exception e) {
            results.put("ssaVerification", Map.of("passed", false, "error", e.getMessage()));
        }

        // 2. DOJ Background Check
        try {
            DojBackgroundCheckService.BackgroundCheckRequest dojReq = dojService.buildBackgroundCheckRequest(
                    provider.getProviderNumber() != null ? provider.getProviderNumber() : String.valueOf(id),
                    ssn, provider.getLastName(), provider.getFirstName(), provider.getMiddleName(),
                    provider.getDateOfBirth(), provider.getGender(),
                    provider.getStreetAddress(), provider.getCity(), provider.getState(), provider.getZipCode());
            DojBackgroundCheckService.BackgroundCheckResponse dojResp = dojService.submitBackgroundCheck(dojReq);
            results.put("dojBackgroundCheck", Map.of(
                    "passed", dojResp.isProviderEligible(),
                    "coriTier", dojResp.getCoriTier() != null ? dojResp.getCoriTier() : "NO_RECORD",
                    "waiverAvailable", dojResp.isWaiverAvailable()));
        } catch (Exception e) {
            results.put("dojBackgroundCheck", Map.of("passed", false, "error", e.getMessage()));
        }

        // 3. Medi-Cal Suspended List Check
        try {
            MediCalSuspendedListService.MediCalLookupRequest mcReq = mediCalService.buildLookupRequest(
                    provider.getProviderNumber() != null ? provider.getProviderNumber() : String.valueOf(id),
                    ssn, provider.getLastName(), provider.getFirstName(), provider.getDateOfBirth());
            MediCalSuspendedListService.MediCalLookupResponse mcResp = mediCalService.checkSuspendedList(mcReq);
            results.put("mediCalCheck", Map.of(
                    "passed", !mcResp.isMatchFound(),
                    "onSuspendedList", mcResp.isMatchFound(),
                    "status", mcResp.getStatus() != null ? mcResp.getStatus() : "ACTIVE"));
        } catch (Exception e) {
            results.put("mediCalCheck", Map.of("passed", false, "error", e.getMessage()));
        }

        // Update provider entity with verification results
        @SuppressWarnings("unchecked")
        Map<String, Object> ssaResult = (Map<String, Object>) results.get("ssaVerification");
        @SuppressWarnings("unchecked")
        Map<String, Object> dojResult = (Map<String, Object>) results.get("dojBackgroundCheck");
        @SuppressWarnings("unchecked")
        Map<String, Object> mcResult = (Map<String, Object>) results.get("mediCalCheck");

        boolean ssaPassed = ssaResult != null && Boolean.TRUE.equals(ssaResult.get("passed"));
        boolean dojPassed = dojResult != null && Boolean.TRUE.equals(dojResult.get("passed"));
        boolean mcPassed = mcResult != null && Boolean.TRUE.equals(mcResult.get("passed"));

        String ssnStatus;
        if (ssaPassed) {
            ssnStatus = "VERIFIED";
        } else {
            // Map the SSA verification code to a CMIPS status
            String code = ssaResult != null ? String.valueOf(ssaResult.get("code")) : "";
            switch (code) {
                case "2": ssnStatus = "NAME_MISMATCH"; break;
                case "3": ssnStatus = "DOB_MISMATCH"; break;
                case "4": ssnStatus = "NAME_DOB_MISMATCH"; break;
                case "5": ssnStatus = "SSN_NOT_FOUND"; break;
                case "6": ssnStatus = "DECEASED"; break;
                case "7": ssnStatus = "SSN_INVALID_FORMAT"; break;
                case "8": ssnStatus = "SSN_NOT_ISSUED"; break;
                default:  ssnStatus = "NOT_YET_VERIFIED"; break;
            }
        }

        String bgStatus = "NO_RECORD";
        if (dojResult != null && dojResult.containsKey("coriTier")) {
            bgStatus = String.valueOf(dojResult.get("coriTier"));
        }

        ProviderManagementService.EnrollmentRequirementsUpdate update =
                ProviderManagementService.EnrollmentRequirementsUpdate.builder()
                        .ssnVerificationStatus(ssnStatus)
                        .backgroundCheckCompleted(true)
                        .backgroundCheckStatus(bgStatus)
                        .mediCalSuspended(!mcPassed)
                        .build();

        ProviderEntity updated = providerService.updateEnrollmentRequirements(id, update, userId);

        boolean allPassed = ssaPassed && dojPassed && mcPassed;
        results.put("overallResult", Map.of(
                "allVerificationsPassed", allPassed,
                "providerEligible", "YES".equals(updated.getEligible()),
                "updatedEligibleStatus", updated.getEligible()));
        results.put("provider", updated);

        return ResponseEntity.ok(results);
    }

    // ==================== CORI MODIFICATION (Phase 4) ====================

    @PutMapping("/cori/{coriId}/modify")
    @RequirePermission(resource = "Provider CORI Resource", scope = "edit")
    public ResponseEntity<?> modifyCori(
            @PathVariable Long coriId,
            @RequestBody ProviderCoriEntity coriData,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            ProviderCoriEntity cori = providerService.modifyCori(coriId, coriData, userId);
            return ResponseEntity.ok(cori);
        } catch (ProviderValidationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "validationErrors", e.getErrors()
            ));
        }
    }

    // ==================== STUB ENDPOINTS (Phase 5 - Payroll Integration) ====================

    @GetMapping("/{id}/tax-info")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getProviderTaxInfo(@PathVariable Long id) {
        return ResponseEntity.ok(providerService.getProviderTaxInfo(id));
    }

    @GetMapping("/{id}/health-benefits")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getProviderHealthBenefits(@PathVariable Long id) {
        return ResponseEntity.ok(providerService.getProviderHealthBenefits(id));
    }

    @GetMapping("/{id}/direct-deposit-status")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getProviderDirectDepositStatus(@PathVariable Long id) {
        return ResponseEntity.ok(providerService.getProviderDirectDepositStatus(id));
    }

    // ==================== EXCEPTION HANDLER ====================

    @ExceptionHandler(ProviderValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ProviderValidationException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "validationErrors", e.getErrors()
        ));
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

    @lombok.Data
    public static class RejectRequest {
        private String comments;
    }
}
