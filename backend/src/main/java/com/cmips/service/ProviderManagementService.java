package com.cmips.service;

import com.cmips.dto.ValidationError;
import com.cmips.entity.*;
import com.cmips.entity.ProviderEntity.ProviderStatus;
import com.cmips.entity.ProviderAssignmentEntity.AssignmentStatus;
import com.cmips.entity.OvertimeViolationEntity.ViolationStatus;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Provider Management Service
 * Implements business rules from DSD Section 23
 */
@Service
public class ProviderManagementService {

    private static final Logger log = LoggerFactory.getLogger(ProviderManagementService.class);

    // FLSA cutoff date for SOC 846 dual-checkbox requirement
    private static final LocalDate SOC_846_CUTOFF_DATE = LocalDate.of(2016, 2, 1);

    private final ProviderRepository providerRepository;
    private final ProviderCoriRepository providerCoriRepository;
    private final ProviderAssignmentRepository providerAssignmentRepository;
    private final OvertimeViolationRepository overtimeViolationRepository;
    private final CaseRepository caseRepository;
    private final TaskService taskService;
    private final ProviderValidationService validationService;
    private final ProviderNotificationService providerNotificationService;
    private final ProviderEnrollmentHistoryRepository enrollmentHistoryRepository;

    public ProviderManagementService(
            ProviderRepository providerRepository,
            ProviderCoriRepository providerCoriRepository,
            ProviderAssignmentRepository providerAssignmentRepository,
            OvertimeViolationRepository overtimeViolationRepository,
            CaseRepository caseRepository,
            TaskService taskService,
            ProviderValidationService validationService,
            ProviderNotificationService providerNotificationService,
            ProviderEnrollmentHistoryRepository enrollmentHistoryRepository) {
        this.providerRepository = providerRepository;
        this.providerCoriRepository = providerCoriRepository;
        this.providerAssignmentRepository = providerAssignmentRepository;
        this.overtimeViolationRepository = overtimeViolationRepository;
        this.caseRepository = caseRepository;
        this.taskService = taskService;
        this.validationService = validationService;
        this.providerNotificationService = providerNotificationService;
        this.enrollmentHistoryRepository = enrollmentHistoryRepository;
    }

    // ==================== PROVIDER ENROLLMENT ====================

    /**
     * Create a new provider (per BR PVM 01-03)
     * Returns validation errors if any; throws ValidationException with error list.
     */
    @Transactional
    public ProviderEntity createProvider(ProviderEntity provider, String userId) {
        // Run all EM validations before saving
        List<ValidationError> errors = validationService.validateCreateProvider(provider);
        if (!errors.isEmpty()) {
            throw new ProviderValidationException("Validation failed", errors);
        }

        // Generate provider number
        provider.setProviderNumber(generateProviderNumber());

        // Set initial status
        provider.setEligible("PENDING");
        provider.setStatus(ProviderStatus.ACTIVE);

        // Per BR PVM 03 - Set SSN verification to Not Verified
        provider.setSsnVerificationStatus("NOT_YET_VERIFIED");

        // Set enrollment dates
        provider.setEnrollmentBeginDate(LocalDate.now());
        provider.setEnrollmentDueDate(LocalDate.now().plusDays(90));

        // BR-69: SOC 846 dual checkbox - if effective date on/after cutoff and one checked, auto-check the other
        applySoc846DualCheckbox(provider);

        provider.setCreatedBy(userId);

        // Names converted to uppercase in @PrePersist per BR PVM 20
        ProviderEntity saved = providerRepository.save(provider);

        // Record enrollment history (BR-87)
        recordEnrollmentHistory(saved, userId, "Provider created with enrollment status: PENDING");

        return saved;
    }

    /**
     * Approve provider enrollment
     */
    @Transactional
    public ProviderEntity approveEnrollment(Long providerId, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // EM-495: Validate enrollment due date not passed
        if (provider.getEnrollmentDueDate() != null &&
                provider.getEnrollmentDueDate().isBefore(LocalDate.now()) &&
                !Boolean.TRUE.equals(provider.getGoodCauseExtension())) {
            throw new RuntimeException("Enrollment due date has passed. A Good Cause Extension is required.");
        }

        // Validate enrollment requirements
        validateEnrollmentRequirements(provider);

        boolean wasReinstatement = "PENDING_REINSTATEMENT".equals(provider.getEligible());

        provider.setEligible("YES");
        provider.setEffectiveDate(LocalDate.now());
        // BR-17: Clear ineligibleReason
        provider.setIneligibleReason(null);
        provider.setOriginalHireDate(LocalDate.now());
        provider.setUpdatedBy(userId);

        // Per BR PVM 17 - Reset sick leave eligibility period
        provider.setSickLeaveEligibilityPeriodEnd(LocalDate.of(9999, 12, 31));

        ProviderEntity saved = providerRepository.save(provider);

        // BR-87: Record enrollment history
        recordEnrollmentHistory(saved, userId, "Enrollment approved - eligible set to YES");

        // PVM-11: If reinstatement approval, send notification
        if (wasReinstatement) {
            providerNotificationService.notifyReinstatementApproved(saved);
        }

        return saved;
    }

    /**
     * Update enrollment requirements and auto-check eligibility
     * When all requirements are met, system automatically sets provider as eligible
     */
    @Transactional
    public ProviderEntity updateEnrollmentRequirements(Long providerId, EnrollmentRequirementsUpdate update, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Update requirements based on what's provided
        if (update.getSoc426Completed() != null) {
            provider.setSoc426Completed(update.getSoc426Completed());
            if (update.getSoc426Completed()) {
                provider.setSoc426Date(LocalDate.now());
            }
        }
        if (update.getOrientationCompleted() != null) {
            provider.setOrientationCompleted(update.getOrientationCompleted());
            if (update.getOrientationCompleted()) {
                provider.setOrientationDate(LocalDate.now());
            }
        }
        if (update.getSoc846Completed() != null) {
            provider.setSoc846Completed(update.getSoc846Completed());
            if (update.getSoc846Completed()) {
                provider.setSoc846Date(LocalDate.now());
            }
        }
        if (update.getProviderAgreementSigned() != null) {
            provider.setProviderAgreementSigned(update.getProviderAgreementSigned());
        }
        if (update.getOvertimeAgreementSigned() != null) {
            provider.setOvertimeAgreementSigned(update.getOvertimeAgreementSigned());
        }
        if (update.getBackgroundCheckCompleted() != null) {
            provider.setBackgroundCheckCompleted(update.getBackgroundCheckCompleted());
            if (update.getBackgroundCheckCompleted()) {
                provider.setBackgroundCheckDate(LocalDate.now());
            }
        }
        if (update.getBackgroundCheckStatus() != null) {
            provider.setBackgroundCheckStatus(update.getBackgroundCheckStatus());
        }
        if (update.getSsnVerificationStatus() != null) {
            provider.setSsnVerificationStatus(update.getSsnVerificationStatus());
        }
        if (update.getMediCalSuspended() != null) {
            provider.setMediCalSuspended(update.getMediCalSuspended());
        }

        provider.setUpdatedBy(userId);
        provider = providerRepository.save(provider);

        // Auto-check eligibility after updating requirements
        checkAndSetEligibility(provider, userId);

        return providerRepository.findById(providerId).orElse(provider);
    }

    /**
     * Check if all enrollment requirements are met and auto-set eligibility
     * This is called automatically when requirements are updated
     */
    private void checkAndSetEligibility(ProviderEntity provider, String userId) {
        // Check all requirements
        boolean allRequirementsMet = true;
        StringBuilder missingRequirements = new StringBuilder();

        if (!Boolean.TRUE.equals(provider.getSoc426Completed())) {
            allRequirementsMet = false;
            missingRequirements.append("SOC 426 not completed. ");
        }
        if (!Boolean.TRUE.equals(provider.getOrientationCompleted())) {
            allRequirementsMet = false;
            missingRequirements.append("Orientation not completed. ");
        }
        if (!Boolean.TRUE.equals(provider.getSoc846Completed())) {
            allRequirementsMet = false;
            missingRequirements.append("SOC 846 not completed. ");
        }
        if (!Boolean.TRUE.equals(provider.getProviderAgreementSigned())) {
            allRequirementsMet = false;
            missingRequirements.append("Provider Agreement not signed. ");
        }
        if (!Boolean.TRUE.equals(provider.getOvertimeAgreementSigned())) {
            allRequirementsMet = false;
            missingRequirements.append("Overtime Agreement not signed. ");
        }
        if (!Boolean.TRUE.equals(provider.getBackgroundCheckCompleted())) {
            allRequirementsMet = false;
            missingRequirements.append("Background check not completed. ");
        }
        // Check background check didn't return Tier 1 (ineligible)
        if ("TIER_1".equals(provider.getBackgroundCheckStatus())) {
            allRequirementsMet = false;
            missingRequirements.append("Background check returned Tier 1 conviction (ineligible). ");
        }
        // Tier 2 requires waiver - check if there's an active general exception
        if ("TIER_2".equals(provider.getBackgroundCheckStatus())) {
            List<ProviderCoriEntity> geRecords = providerCoriRepository
                    .findCoriWithActiveGeneralException(provider.getId(), LocalDate.now());
            if (geRecords.isEmpty()) {
                allRequirementsMet = false;
                missingRequirements.append("Background check returned Tier 2 conviction without waiver. ");
            }
        }
        // SSN must be verified
        if (!"VERIFIED".equals(provider.getSsnVerificationStatus())) {
            allRequirementsMet = false;
            missingRequirements.append("SSN not verified. ");
        }
        // Cannot be on Medi-Cal suspended list
        if (Boolean.TRUE.equals(provider.getMediCalSuspended())) {
            allRequirementsMet = false;
            missingRequirements.append("Provider is on Medi-Cal suspended list. ");
        }

        // Auto-set eligibility based on requirements
        if (allRequirementsMet) {
            log.info("All enrollment requirements met for provider {}. Auto-setting to ELIGIBLE.", provider.getId());
            provider.setEligible("YES");
            provider.setEffectiveDate(LocalDate.now());
            provider.setIneligibleReason(null);
            if (provider.getOriginalHireDate() == null) {
                provider.setOriginalHireDate(LocalDate.now());
            }
            providerRepository.save(provider);
        } else {
            log.info("Provider {} missing requirements: {}", provider.getId(), missingRequirements);
            // Keep as PENDING if not all requirements met
            if (!"NO".equals(provider.getEligible())) {
                provider.setEligible("PENDING");
                providerRepository.save(provider);
            }
        }
    }

    /**
     * DTO for enrollment requirements update
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EnrollmentRequirementsUpdate {
        private Boolean soc426Completed;
        private Boolean orientationCompleted;
        private Boolean soc846Completed;
        private Boolean providerAgreementSigned;
        private Boolean overtimeAgreementSigned;
        private Boolean backgroundCheckCompleted;
        private String backgroundCheckStatus;  // NO_RECORD, TIER_1, TIER_2
        private String ssnVerificationStatus;  // VERIFIED, NOT_VERIFIED, etc.
        private Boolean mediCalSuspended;
    }

    /**
     * Validate enrollment requirements (SOC 426, orientation, SOC 846, background check)
     */
    private void validateEnrollmentRequirements(ProviderEntity provider) {
        StringBuilder errors = new StringBuilder();

        if (!Boolean.TRUE.equals(provider.getSoc426Completed())) {
            errors.append("SOC 426 (Provider Enrollment Form) not completed. ");
        }
        if (!Boolean.TRUE.equals(provider.getOrientationCompleted())) {
            errors.append("Provider Orientation not completed. ");
        }
        if (!Boolean.TRUE.equals(provider.getSoc846Completed())) {
            errors.append("SOC 846 (Provider Enrollment Agreement) not completed. ");
        }
        // Per BR PVM 67-70 - Both Provider Agreement and Overtime Agreement required after FLSA
        if (!Boolean.TRUE.equals(provider.getProviderAgreementSigned())) {
            errors.append("Provider Agreement not signed. ");
        }
        if (!Boolean.TRUE.equals(provider.getOvertimeAgreementSigned())) {
            errors.append("Overtime Agreement not signed. ");
        }
        if (!Boolean.TRUE.equals(provider.getBackgroundCheckCompleted())) {
            errors.append("DOJ Background Check not completed. ");
        }

        // Check for CORI issues
        if (providerCoriRepository.hasActiveTier1Conviction(provider.getId())) {
            errors.append("Provider has active Tier 1 conviction. ");
        }
        if (providerCoriRepository.hasTier2WithoutWaiver(provider.getId())) {
            errors.append("Provider has Tier 2 conviction without waiver. ");
        }

        if (errors.length() > 0) {
            throw new RuntimeException("Enrollment requirements not met: " + errors.toString());
        }
    }

    /**
     * Set provider as ineligible (per BR PVM 15)
     * BR-15: Eligible YES→NO triggers termination cascade
     */
    @Transactional
    public ProviderEntity setProviderIneligible(Long providerId, String ineligibleReason, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        LocalDate effectiveDate = LocalDate.now().plusDays(20);

        provider.setEligible("NO");
        provider.setIneligibleReason(ineligibleReason);
        provider.setEffectiveDate(effectiveDate);
        provider.setUpdatedBy(userId);

        // Terminate all active assignments
        terminateAllProviderAssignments(providerId, effectiveDate, "Provider Not Eligible to Work", userId);

        // End General Exception waivers on active CORIs
        endActiveGeneralExceptions(providerId, effectiveDate, userId);

        // End Recipient Waivers on active assignments
        endActiveRecipientWaivers(providerId, effectiveDate, userId);

        // End Workweek Agreements (BR-73-76)
        endWorkweekAgreements(providerId, effectiveDate, userId);

        // End Travel Time segments (BR-66)
        endTravelTimeSegments(providerId, effectiveDate, userId);

        ProviderEntity saved = providerRepository.save(provider);

        // BR-87: Record enrollment history
        recordEnrollmentHistory(saved, userId, "Provider set ineligible. Reason: " + ineligibleReason);

        // PVM-14: Send termination notification
        providerNotificationService.notifyProviderTerminatedEnrollment(saved, ineligibleReason);

        // PVM-15: If WPCS provider type, send WPCS-specific termination notification
        if ("WPCS".equals(saved.getProviderType()) || "HOME_CARE_AGENCY".equals(saved.getProviderType())) {
            providerNotificationService.notifyWpcsProviderTerminated(saved, ineligibleReason);
        }

        return saved;
    }

    /**
     * Reinstate provider (per BR PVM 23, 25)
     * BR-23: Set eligible = PENDING_REINSTATEMENT (not directly to YES)
     */
    @Transactional
    public ProviderEntity reinstateProvider(Long providerId, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Check if eligible for reinstatement (within 30 days per BR PVM 25)
        if (!provider.canBeReinstated()) {
            throw new RuntimeException("Provider cannot be reinstated - outside 30-day window or due to overtime violations");
        }

        provider.setEligible("PENDING_REINSTATEMENT");
        provider.setUpdatedBy(userId);

        ProviderEntity saved = providerRepository.save(provider);

        // BR-87: Record enrollment history
        recordEnrollmentHistory(saved, userId, "Provider reinstatement requested");

        // PVM-10: Generate task to supervisor for approval
        providerNotificationService.notifyReinstatementRequested(saved);

        return saved;
    }

    /**
     * Re-enroll provider (per BR PVM 24, 26)
     * BR-21: Default DOJ county to user's county
     */
    @Transactional
    public ProviderEntity reEnrollProvider(Long providerId, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (!"NO".equals(provider.getEligible())) {
            throw new RuntimeException("Can only re-enroll ineligible providers");
        }

        // Clear enrollment information per BR PVM 24
        provider.setEligible("PENDING");
        provider.setIneligibleReason(null);
        provider.setSoc426Completed(false);
        provider.setSoc426Date(null);
        provider.setOrientationCompleted(false);
        provider.setOrientationDate(null);
        provider.setSoc846Completed(false);
        provider.setSoc846Date(null);
        provider.setProviderAgreementSigned(false);
        provider.setOvertimeAgreementSigned(false);
        provider.setBackgroundCheckCompleted(false);
        provider.setBackgroundCheckDate(null);
        // Reset enrollment dates
        provider.setEnrollmentBeginDate(LocalDate.now());
        provider.setEnrollmentDueDate(LocalDate.now().plusDays(90));
        provider.setUpdatedBy(userId);

        ProviderEntity saved = providerRepository.save(provider);

        // BR-87: Record enrollment history
        recordEnrollmentHistory(saved, userId, "Provider re-enrolled");

        return saved;
    }

    // ==================== PROVIDER ASSIGNMENT ====================

    /**
     * Assign provider to case (per BR PVM 40-43)
     */
    @Transactional
    public ProviderAssignmentEntity assignProviderToCase(Long providerId, Long caseId, String providerType,
                                                          String relationship, Double assignedHours, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (!provider.isEligibleToServe()) {
            throw new RuntimeException("Provider is not eligible to serve");
        }

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        ProviderAssignmentEntity assignment = ProviderAssignmentEntity.builder()
                .caseId(caseId)
                .providerId(providerId)
                .recipientId(caseEntity.getRecipientId())
                .providerType(providerType)
                .status(AssignmentStatus.ACTIVE)
                .beginDate(LocalDate.now())
                .relationshipToRecipient(relationship)
                .assignedHours(assignedHours)
                .assignedHoursFormIndicated(assignedHours != null)
                .impactsFundingSource(checkFundingSourceImpact(relationship))
                .createdBy(userId)
                .build();

        assignment = providerAssignmentRepository.save(assignment);

        // Update provider active case count
        updateProviderActiveCaseCount(providerId);

        // Per BR PVM 40 - Generate SOC 2271 notification
        assignment.setInitialNotificationSent(true);
        assignment.setNotificationSentDate(LocalDate.now());

        // Per BR PVM 13 - Update funding source if spouse or parent of minor
        if (assignment.getImpactsFundingSource()) {
            updateCaseFundingSource(caseId, relationship, userId);
        }

        // PVM-40: Notify provider assigned
        providerNotificationService.notifyProviderAssigned(assignment);

        // PVM-42: SOC 2271 notification generated
        providerNotificationService.notifySoc2271Generated(assignment);

        // PVM-45: Check if provider address matches recipient address
        if (provider.getStreetAddress() != null && caseEntity.getRecipientId() != null) {
            // Compare provider address with recipient address (simplified check)
            providerNotificationService.notifyProviderAddressMatchesRecipient(provider, caseEntity.getRecipientId());
        }

        // Stub: Payroll PROO939A call
        log.info("STUB: Payroll PROO939A call for provider {} assigned to case {}", providerId, caseId);

        log.info("Provider {} assigned to case {}", providerId, caseId);
        return assignment;
    }

    /**
     * Terminate provider from case (per BR PVM 44-47)
     * BR-35: End Recipient Waivers
     * BR-66: Handle Travel Time segments
     * BR-73-76: End Workweek Agreements
     */
    @Transactional
    public ProviderAssignmentEntity terminateProviderAssignment(Long assignmentId, String reason, String userId) {
        ProviderAssignmentEntity assignment = providerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        LocalDate terminationDate = LocalDate.now();

        // BR-35: End Recipient Waivers
        if (Boolean.TRUE.equals(assignment.getHasRecipientWaiver()) && assignment.isRecipientWaiverActive()) {
            assignment.setRecipientWaiverEndDate(terminationDate);
        }

        // BR-73-76: End Workweek Agreements
        if (Boolean.TRUE.equals(assignment.getHasWorkweekAgreement())) {
            assignment.setHasWorkweekAgreement(false);
            log.info("Workweek agreement ended for assignment {} due to termination", assignmentId);
        }

        // BR-66: Handle Travel Time segments
        if (Boolean.TRUE.equals(assignment.getHasTravelTimeAgreement())) {
            assignment.setHasTravelTimeAgreement(false);
            log.info("Travel time agreement ended for assignment {} due to termination", assignmentId);
        }

        assignment.setStatus(AssignmentStatus.TERMINATED);
        assignment.setLeaveTerminationEffectiveDate(terminationDate);
        assignment.setTerminationReason(reason);
        assignment.setUpdatedBy(userId);

        assignment = providerAssignmentRepository.save(assignment);

        // Update provider active case count
        updateProviderActiveCaseCount(assignment.getProviderId());

        // PVM-04: Check if provider added and terminated in single pay period
        if (assignment.getBeginDate() != null) {
            long daysBetween = ChronoUnit.DAYS.between(assignment.getBeginDate(), terminationDate);
            if (daysBetween <= 15) { // within a single pay period
                providerNotificationService.notifyProviderAddedTerminatedSamePeriod(assignment);
            }
        }

        // PVM-07: Check if active provider end date within 30 days
        if (assignment.getEndDate() != null &&
                !assignment.getEndDate().isAfter(LocalDate.now().plusDays(30))) {
            providerNotificationService.notifyProviderEndDateWithin30Days(assignment);
        }

        // PVM-14,15: Send termination notification
        ProviderEntity provider = providerRepository.findById(assignment.getProviderId()).orElse(null);
        if (provider != null) {
            providerNotificationService.notifyProviderTerminatedEnrollment(provider, reason);
            // BR-87: Record enrollment history
            recordEnrollmentHistory(provider, userId, "Provider terminated from case. Reason: " + reason);
        }

        // Stub: Payroll status update call
        log.info("STUB: Payroll status update for terminated assignment {}", assignmentId);

        return assignment;
    }

    /**
     * Inactivate provider on a case (pending provider removed)
     * PVM-39: Sends notification when pending provider is inactivated
     */
    @Transactional
    public ProviderAssignmentEntity inactivateProviderOnCase(Long assignmentId, String reason, String userId) {
        ProviderAssignmentEntity assignment = providerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setStatus(AssignmentStatus.TERMINATED);
        assignment.setLeaveTerminationEffectiveDate(LocalDate.now());
        assignment.setTerminationReason(reason != null ? reason : "Provider Inactivated");
        assignment.setUpdatedBy(userId);

        assignment = providerAssignmentRepository.save(assignment);

        // Update provider active case count
        updateProviderActiveCaseCount(assignment.getProviderId());

        // PVM-39: Pending provider inactivated on case
        providerNotificationService.notifyProviderInactivatedOnCase(assignment);

        return assignment;
    }

    /**
     * Place provider on leave for a case
     * BR-66: Handle Travel Time segments
     */
    @Transactional
    public ProviderAssignmentEntity placeProviderOnLeave(Long assignmentId, String reason, String userId) {
        ProviderAssignmentEntity assignment = providerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // BR-66: Handle Travel Time segments
        if (Boolean.TRUE.equals(assignment.getHasTravelTimeAgreement())) {
            assignment.setHasTravelTimeAgreement(false);
            log.info("Travel time agreement suspended for assignment {} due to leave", assignmentId);
        }

        assignment.setStatus(AssignmentStatus.ON_LEAVE);
        assignment.setLeaveTerminationEffectiveDate(LocalDate.now());
        assignment.setTerminationReason(reason);
        assignment.setUpdatedBy(userId);

        ProviderAssignmentEntity saved = providerAssignmentRepository.save(assignment);

        // Stub: Payroll status update call
        log.info("STUB: Payroll status update for leave assignment {}", assignmentId);

        return saved;
    }

    // ==================== CORI MANAGEMENT ====================

    /**
     * Get all CORI records for a provider
     */
    public List<ProviderCoriEntity> getProviderCoriRecords(Long providerId) {
        return providerCoriRepository.findByProviderId(providerId);
    }

    /**
     * Create CORI record (per BR PVM 31)
     * Validates with EM-172 through EM-189, then triggers ineligibility cascade.
     */
    @Transactional
    public ProviderCoriEntity createCori(Long providerId, ProviderCoriEntity cori, String userId) {
        // Validate CORI before saving
        List<ProviderCoriEntity> existingRecords = providerCoriRepository.findByProviderId(providerId);
        List<ValidationError> errors = validationService.validateCreateCori(cori, existingRecords);
        if (!errors.isEmpty()) {
            throw new ProviderValidationException("CORI validation failed", errors);
        }

        cori.setProviderId(providerId);
        cori.setCreatedBy(userId);
        cori = providerCoriRepository.save(cori);

        // Per BR PVM 31 - Set provider ineligible
        String ineligibleReason;
        Long coriCount = providerCoriRepository.countActiveCoriByProviderId(providerId);

        if ("TIER_1".equals(cori.getTier())) {
            ineligibleReason = coriCount > 1 ? "SUBSEQUENT_TIER_1_CONVICTION" : "TIER_1_CONVICTION";
        } else {
            ineligibleReason = coriCount > 1 ? "SUBSEQUENT_TIER_2_CONVICTION" : "TIER_2_CONVICTION";
        }

        // BR-31: Setting ineligible also ends waivers/agreements via setProviderIneligible
        setProviderIneligible(providerId, ineligibleReason, userId);

        return cori;
    }

    /**
     * Add general exception waiver to CORI (per BR PVM 33)
     */
    @Transactional
    public ProviderCoriEntity addGeneralException(Long coriId, LocalDate beginDate, LocalDate endDate, String notes, String userId) {
        ProviderCoriEntity cori = providerCoriRepository.findById(coriId)
                .orElseThrow(() -> new RuntimeException("CORI not found"));

        cori.setGeneralExceptionGranted(true);
        cori.setGeneralExceptionBeginDate(beginDate);
        cori.setGeneralExceptionEndDate(endDate);
        cori.setGeneralExceptionNotes(notes);
        cori.setUpdatedBy(userId);

        cori = providerCoriRepository.save(cori);

        // Per BR PVM 33 - Set provider eligible if all other requirements met
        ProviderEntity provider = providerRepository.findById(cori.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (!providerCoriRepository.hasActiveTier1Conviction(provider.getId()) &&
            !providerCoriRepository.hasTier2WithoutWaiver(provider.getId())) {
            // Check other enrollment requirements and set eligible if met
            try {
                validateEnrollmentRequirements(provider);
                provider.setEligible("YES");
                provider.setIneligibleReason(null);
                providerRepository.save(provider);
            } catch (Exception e) {
                // Other requirements not met, keep ineligible
                log.info("Provider {} has general exception but other requirements not met: {}", provider.getId(), e.getMessage());
            }
        }

        return cori;
    }

    /**
     * Add recipient waiver (per BR PVM 35)
     */
    @Transactional
    public ProviderAssignmentEntity addRecipientWaiver(Long assignmentId, Long coriId, LocalDate endDate, String userId) {
        ProviderAssignmentEntity assignment = providerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setHasRecipientWaiver(true);
        assignment.setRecipientWaiverCoriId(coriId);
        assignment.setRecipientWaiverBeginDate(LocalDate.now());
        assignment.setRecipientWaiverEndDate(endDate);
        assignment.setUpdatedBy(userId);

        return providerAssignmentRepository.save(assignment);
    }

    // ==================== OVERTIME VIOLATIONS ====================

    /**
     * Create overtime violation (per BR PVM 90+)
     */
    @Transactional
    public OvertimeViolationEntity createOvertimeViolation(Long providerId, String violationType,
                                                            Integer month, Integer year,
                                                            Double hoursClaimed, Double maximumAllowed, String userId) {
        // Check if violation already exists for this service month (one per month rule)
        if (overtimeViolationRepository.hasViolationForServiceMonth(providerId, month, year)) {
            throw new RuntimeException("Provider already has a violation for this service month");
        }

        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Check if provider has overtime exemption
        if (Boolean.TRUE.equals(provider.getHasOvertimeExemption())) {
            // Create inactive exemption violation
            OvertimeViolationEntity violation = OvertimeViolationEntity.builder()
                    .providerId(providerId)
                    .violationType(violationType)
                    .serviceMonth(month)
                    .serviceYear(year)
                    .violationDate(LocalDate.now())
                    .hoursClaimed(hoursClaimed)
                    .maximumAllowed(maximumAllowed)
                    .hoursExceeded(hoursClaimed - maximumAllowed)
                    .status(ViolationStatus.INACTIVE_EXEMPTION)
                    .createdBy(userId)
                    .build();
            return overtimeViolationRepository.save(violation);
        }

        // Determine violation number
        Integer activeViolationCount = overtimeViolationRepository.countActiveViolationsByProviderId(providerId);
        Integer violationNumber = activeViolationCount + 1;

        OvertimeViolationEntity violation = OvertimeViolationEntity.builder()
                .providerId(providerId)
                .violationNumber(violationNumber)
                .violationType(violationType)
                .serviceMonth(month)
                .serviceYear(year)
                .violationDate(LocalDate.now())
                .hoursClaimed(hoursClaimed)
                .maximumAllowed(maximumAllowed)
                .hoursExceeded(hoursClaimed - maximumAllowed)
                .status(ViolationStatus.PENDING_REVIEW)
                .createdBy(userId)
                .build();

        // If violation #2, set up training
        if (violationNumber == 2 && !Boolean.TRUE.equals(provider.getTrainingCompletedForViolation2())) {
            violation.setTrainingOffered(true);
            violation.setTrainingDueDate(LocalDate.now().plusDays(14));
        }

        violation = overtimeViolationRepository.save(violation);

        // Create task for county review (3 business days)
        createOvertimeViolationReviewTask(violation, userId);

        return violation;
    }

    /**
     * County review of violation (per BR PVM 90+)
     */
    @Transactional
    public OvertimeViolationEntity countyReviewViolation(Long violationId, String outcome, String comments, String reviewerId) {
        OvertimeViolationEntity violation = overtimeViolationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found"));

        violation.setCountyReviewDate(LocalDate.now());
        violation.setCountyReviewerId(reviewerId);
        violation.setCountyReviewOutcome(outcome);
        violation.setCountyReviewComments(comments);

        if ("UPHELD".equals(outcome)) {
            violation.setStatus(ViolationStatus.ACTIVE);
            // Process consequences based on violation number
            processViolationConsequences(violation, reviewerId);
        } else if ("OVERRIDE_REQUESTED".equals(outcome)) {
            // Create task for supervisor review
            createSupervisorReviewTask(violation, reviewerId);
        }

        return overtimeViolationRepository.save(violation);
    }

    /**
     * Supervisor review of violation override request
     */
    @Transactional
    public OvertimeViolationEntity supervisorReviewViolation(Long violationId, String outcome, String comments, String reviewerId) {
        OvertimeViolationEntity violation = overtimeViolationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found"));

        violation.setSupervisorReviewDate(LocalDate.now());
        violation.setSupervisorReviewerId(reviewerId);
        violation.setSupervisorReviewOutcome(outcome);
        violation.setSupervisorReviewComments(comments);

        if ("APPROVED".equals(outcome)) {
            violation.setStatus(ViolationStatus.INACTIVE);
            // PVM-28: Violation dismissed notification
            ProviderEntity provider = providerRepository.findById(violation.getProviderId()).orElse(null);
            if (provider != null) {
                providerNotificationService.notifyOTViolationDismissed(provider);
            }
        } else if ("REJECTED".equals(outcome)) {
            violation.setStatus(ViolationStatus.ACTIVE);
            processViolationConsequences(violation, reviewerId);
        }

        return overtimeViolationRepository.save(violation);
    }

    /**
     * Record training completion for Violation #2
     */
    @Transactional
    public OvertimeViolationEntity recordTrainingCompletion(Long violationId, String userId) {
        OvertimeViolationEntity violation = overtimeViolationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found"));

        if (violation.getViolationNumber() != 2) {
            throw new RuntimeException("Training only available for Violation #2");
        }

        violation.setTrainingCompleted(true);
        violation.setTrainingCompletionDate(LocalDate.now());
        violation.setTrainingCountyEntryDate(LocalDate.now());
        violation.setStatus(ViolationStatus.INACTIVE);

        // Update provider
        ProviderEntity provider = providerRepository.findById(violation.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        provider.setTrainingCompletedForViolation2(true);
        provider.setTrainingCompletionDate(LocalDate.now());
        providerRepository.save(provider);

        return overtimeViolationRepository.save(violation);
    }

    private void processViolationConsequences(OvertimeViolationEntity violation, String userId) {
        ProviderEntity provider = providerRepository.findById(violation.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        provider.setOvertimeViolationCount(violation.getViolationNumber());

        if (violation.getViolationNumber() >= 3) {
            // Violation #3: 90-day suspension, Violation #4: 365-day suspension
            int suspensionDays = violation.getSuspensionDays();
            LocalDate terminationDate = LocalDate.now().plusDays(20);
            LocalDate reinstatementDate = terminationDate.plusDays(suspensionDays);

            violation.setTerminationEffectiveDate(terminationDate);
            violation.setReinstatementDate(reinstatementDate);

            String ineligibleReason = violation.getViolationNumber() == 3 ?
                    "THIRD_OVERTIME_VIOLATION" : "FOURTH_OVERTIME_VIOLATION";

            provider.setEligible("NO");
            provider.setIneligibleReason(ineligibleReason);
            provider.setEffectiveDate(terminationDate);

            // Terminate all assignments
            terminateAllProviderAssignments(provider.getId(), terminationDate, "Provider Enrollment Ineligible", userId);

            // PVM-24/25: OT violation ineligible notification
            providerNotificationService.notifyOTViolationIneligible(provider);

            // BR-87: Record enrollment history for OT violation ineligibility
            recordEnrollmentHistory(provider, userId,
                    "Provider set ineligible due to OT violation #" + violation.getViolationNumber());
        }

        providerRepository.save(provider);
    }

    // ==================== SICK LEAVE ====================

    /**
     * Check and accrue sick leave (per User Story 5)
     */
    @Transactional
    public void checkAndAccrueSickLeave(Long providerId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Check if already accrued
        if (provider.getSickLeaveAccruedDate() != null) {
            return;
        }

        // Check if worked 100 hours
        if (provider.getTotalServiceHoursWorked() != null && provider.getTotalServiceHoursWorked() >= 100) {
            // Get fiscal year allocation
            int fiscalYear = LocalDate.now().getMonthValue() >= 7 ?
                    LocalDate.now().getYear() : LocalDate.now().getYear() - 1;

            double allocation;
            if (fiscalYear >= 2022) {
                allocation = 24.0; // 24 hours per fiscal year
            } else if (fiscalYear >= 2020) {
                allocation = 16.0;
            } else {
                allocation = 8.0;
            }

            provider.setSickLeaveAccruedHours(allocation);
            provider.setSickLeaveAccruedDate(LocalDate.now());
            providerRepository.save(provider);

            log.info("Provider {} accrued {} sick leave hours", providerId, allocation);
        }
    }

    // ==================== ENROLLMENT MANAGEMENT (Phase 2) ====================

    /**
     * Modify provider enrollment (Phase 2)
     * Handles all enrollment field changes with full validation and business rule cascades.
     */
    @Transactional
    public ProviderEntity modifyEnrollment(Long providerId, ProviderEntity enrollmentData, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Save previous state for validation comparison
        String previousEligible = provider.getEligible();
        ProviderEntity previousState = createSnapshot(provider);

        // Apply enrollment changes
        applyEnrollmentChanges(provider, enrollmentData);

        // Validate
        List<ValidationError> errors = validationService.validateModifyEnrollment(provider, previousState);
        if (!errors.isEmpty()) {
            throw new ProviderValidationException("Enrollment modification validation failed", errors);
        }

        // BR-15: If eligible changed from YES → NO
        if ("YES".equals(previousEligible) && "NO".equals(provider.getEligible())) {
            LocalDate effectiveDate = LocalDate.now().plusDays(20);
            provider.setEffectiveDate(effectiveDate);
            terminateAllProviderAssignments(providerId, effectiveDate, "Provider Not Eligible to Work", userId);
            endActiveGeneralExceptions(providerId, effectiveDate, userId);
            endActiveRecipientWaivers(providerId, effectiveDate, userId);
            endWorkweekAgreements(providerId, effectiveDate, userId);
            endTravelTimeSegments(providerId, effectiveDate, userId);
            providerNotificationService.notifyProviderTerminatedEnrollment(provider, provider.getIneligibleReason());
        }

        // BR-55: If DOJ Background Check newly checked, set DOJ county = user's county
        if (!Boolean.TRUE.equals(previousState.getBackgroundCheckCompleted()) &&
                Boolean.TRUE.equals(provider.getBackgroundCheckCompleted())) {
            if (provider.getCountyCode() != null) {
                provider.setDojCountyName(provider.getCountyCode());
                log.info("BR-55: DOJ county set to {} for provider {}", provider.getCountyCode(), providerId);
            }
        }

        // BR-03: Reset SSN verification when SSN/DOB/Name/Gender changed
        if (enrollmentData.getSsn() != null || enrollmentData.getDateOfBirth() != null ||
                enrollmentData.getFirstName() != null || enrollmentData.getLastName() != null ||
                enrollmentData.getGender() != null) {
            String prevSsn = previousState.getSsn();
            String newSsn = provider.getSsn();
            boolean ssnChanged = prevSsn != null && newSsn != null && !prevSsn.equals(newSsn);
            boolean dobChanged = enrollmentData.getDateOfBirth() != null;
            boolean nameChanged = enrollmentData.getFirstName() != null || enrollmentData.getLastName() != null;
            boolean genderChanged = enrollmentData.getGender() != null;

            if (ssnChanged || dobChanged || nameChanged || genderChanged) {
                provider.setSsnVerificationStatus("NOT_YET_VERIFIED");
                log.info("BR-03: SSN verification reset to NOT_YET_VERIFIED for provider {} due to demographic change", providerId);
            }
        }

        // PVM-06: Check if address state changed to non-CA
        if (enrollmentData.getState() != null && !"CA".equalsIgnoreCase(enrollmentData.getState())) {
            providerNotificationService.notifyProviderMovedOutOfState(provider);
        }

        // BR-69/70: Auto-check both SOC 846 checkboxes if one is checked (after cutoff date)
        applySoc846DualCheckbox(provider);

        provider.setUpdatedBy(userId);
        ProviderEntity saved = providerRepository.save(provider);

        // BR-87: Record enrollment history
        recordEnrollmentHistory(saved, userId, "Enrollment modified");

        return saved;
    }

    /**
     * Reject provider enrollment (reinstatement rejection)
     */
    @Transactional
    public ProviderEntity rejectEnrollment(Long providerId, String comments, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        provider.setEligible("NO");
        provider.setUpdatedBy(userId);

        ProviderEntity saved = providerRepository.save(provider);

        // BR-87: Record enrollment history
        recordEnrollmentHistory(saved, userId, "Enrollment rejected. Comments: " + comments);

        // PVM-12: Notification for reinstatement rejection
        providerNotificationService.notifyReinstatementRejected(saved);

        return saved;
    }

    // ==================== CORI MODIFICATION (Phase 4) ====================

    /**
     * Modify CORI record - handles all CORI edit scenarios (BR-33 through BR-39)
     */
    @Transactional
    public ProviderCoriEntity modifyCori(Long coriId, ProviderCoriEntity coriData, String userId) {
        ProviderCoriEntity cori = providerCoriRepository.findById(coriId)
                .orElseThrow(() -> new RuntimeException("CORI not found"));

        ProviderEntity provider = providerRepository.findById(cori.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        String previousTier = cori.getTier();

        // Apply changes
        if (coriData.getCoriDate() != null) cori.setCoriDate(coriData.getCoriDate());
        if (coriData.getConvictionDate() != null) cori.setConvictionDate(coriData.getConvictionDate());
        if (coriData.getTier() != null) cori.setTier(coriData.getTier());
        if (coriData.getCrimeDescription() != null) cori.setCrimeDescription(coriData.getCrimeDescription());
        if (coriData.getCrimeCode() != null) cori.setCrimeCode(coriData.getCrimeCode());
        if (coriData.getCoriEndDate() != null) cori.setCoriEndDate(coriData.getCoriEndDate());
        if (coriData.getGeneralExceptionGranted() != null) cori.setGeneralExceptionGranted(coriData.getGeneralExceptionGranted());
        if (coriData.getGeneralExceptionBeginDate() != null) cori.setGeneralExceptionBeginDate(coriData.getGeneralExceptionBeginDate());
        if (coriData.getGeneralExceptionEndDate() != null) cori.setGeneralExceptionEndDate(coriData.getGeneralExceptionEndDate());
        if (coriData.getGeneralExceptionNotes() != null) cori.setGeneralExceptionNotes(coriData.getGeneralExceptionNotes());
        cori.setUpdatedBy(userId);

        // BR-33: GE Begin Date populated, no End Date, all enrollment criteria met → set eligible=YES
        if (cori.getGeneralExceptionBeginDate() != null && cori.getGeneralExceptionEndDate() == null &&
                Boolean.TRUE.equals(cori.getGeneralExceptionGranted())) {
            if (!providerCoriRepository.hasActiveTier1Conviction(provider.getId()) &&
                    !providerCoriRepository.hasTier2WithoutWaiver(provider.getId())) {
                try {
                    validateEnrollmentRequirements(provider);
                    provider.setEligible("YES");
                    provider.setIneligibleReason(null);
                    provider.setEffectiveDate(cori.getGeneralExceptionBeginDate());
                    providerRepository.save(provider);
                    log.info("BR-33: Provider {} set eligible via GE waiver", provider.getId());
                } catch (Exception e) {
                    log.info("BR-33: Provider {} GE granted but other requirements not met", provider.getId());
                }
            }
        }

        // BR-34: GE End Date populated, eligible=YES, end date before CORI end date
        if (cori.getGeneralExceptionEndDate() != null && "YES".equals(provider.getEligible())) {
            if (cori.getCoriEndDate() == null || cori.getGeneralExceptionEndDate().isBefore(cori.getCoriEndDate())) {
                provider.setEligible("NO");
                provider.setIneligibleReason("TIER_2_CONVICTION");
                provider.setEffectiveDate(cori.getGeneralExceptionEndDate().plusDays(1));
                terminateAllProviderAssignments(provider.getId(), provider.getEffectiveDate(),
                        "General Exception waiver ended", userId);
                providerRepository.save(provider);
                log.info("BR-34: Provider {} set ineligible - GE ended", provider.getId());
            }
        }

        // BR-36: Tier changed from 2 → 1
        if ("TIER_2".equals(previousTier) && "TIER_1".equals(cori.getTier())) {
            if ("YES".equals(provider.getEligible())) {
                provider.setEligible("NO");
                provider.setIneligibleReason("TIER_1_CONVICTION");
                provider.setEffectiveDate(LocalDate.now().plusDays(20));
                // Terminate Recipient Waivers
                endActiveRecipientWaivers(provider.getId(), provider.getEffectiveDate(), userId);
                endWorkweekAgreements(provider.getId(), provider.getEffectiveDate(), userId);
                providerRepository.save(provider);
                log.info("BR-36: Provider {} set ineligible - tier changed to 1", provider.getId());
            }
        }

        // BR-37: CORI End Date populated + active Recipient Waiver
        if (cori.getCoriEndDate() != null) {
            List<ProviderAssignmentEntity> assignments = providerAssignmentRepository
                    .findByProviderIdAndStatus(provider.getId(), AssignmentStatus.ACTIVE);
            for (ProviderAssignmentEntity assignment : assignments) {
                if (assignment.isRecipientWaiverActive() &&
                        Long.valueOf(coriId).equals(assignment.getRecipientWaiverCoriId())) {
                    assignment.setRecipientWaiverEndDate(cori.getCoriEndDate());
                    assignment.setUpdatedBy(userId);
                    providerAssignmentRepository.save(assignment);
                    log.info("BR-37: Recipient waiver end date set for assignment {}", assignment.getId());
                }
            }
        }

        // BR-38: CORI End Date populated + active General Exception
        if (cori.getCoriEndDate() != null && cori.hasActiveGeneralException()) {
            cori.setGeneralExceptionEndDate(cori.getCoriEndDate());
            log.info("BR-38: GE end date set to CORI end date for CORI {}", coriId);
        }

        // BR-39: Tier changed from 1 → 2
        if ("TIER_1".equals(previousTier) && "TIER_2".equals(cori.getTier())) {
            Long coriCount = providerCoriRepository.countActiveCoriByProviderId(provider.getId());
            String reason = coriCount > 1 ? "SUBSEQUENT_TIER_2_CONVICTION" : "TIER_2_CONVICTION";
            if (!"NO".equals(provider.getEligible())) {
                provider.setEligible("NO");
                provider.setIneligibleReason(reason);
                provider.setEffectiveDate(LocalDate.now().plusDays(20));
                // Terminate assignments and end waivers/agreements
                terminateAllProviderAssignments(provider.getId(), provider.getEffectiveDate(), reason, userId);
                endActiveRecipientWaivers(provider.getId(), provider.getEffectiveDate(), userId);
                endWorkweekAgreements(provider.getId(), provider.getEffectiveDate(), userId);
                providerRepository.save(provider);
                providerNotificationService.notifyProviderTerminatedEnrollment(provider, reason);
            }
            log.info("BR-39: Tier changed 1→2 for CORI {}, reason: {}", coriId, reason);
        }

        ProviderCoriEntity saved = providerCoriRepository.save(cori);

        // BR-87: Record enrollment history
        recordEnrollmentHistory(provider, userId, "CORI record modified (ID: " + coriId + ")");

        return saved;
    }

    // ==================== ENROLLMENT HISTORY (Phase 3) ====================

    /**
     * Get enrollment history for provider detail page
     */
    public List<ProviderEnrollmentHistory> getEnrollmentHistory(Long providerId) {
        return enrollmentHistoryRepository.findByProviderIdOrderByChangedAtDesc(providerId);
    }

    /**
     * Get pre-ineligibility data for reinstatement flow (BR-23)
     */
    public ProviderEnrollmentHistory getPreIneligibilityData(Long providerId) {
        return enrollmentHistoryRepository
                .findTopByProviderIdAndEligibleOrderByChangedAtDesc(providerId, "YES")
                .orElse(null);
    }

    // ==================== STUB ENDPOINTS (Phase 5) ====================

    /**
     * Stub: Get tax info (Payroll PROO915A)
     */
    public java.util.Map<String, Object> getProviderTaxInfo(Long providerId) {
        log.info("STUB: Payroll PROO915A call for provider {} tax info", providerId);
        java.util.Map<String, Object> stub = new java.util.HashMap<>();
        stub.put("providerId", providerId);
        stub.put("w4Status", "PENDING");
        stub.put("de4Status", "PENDING");
        stub.put("message", "Tax info integration pending - Payroll PROO915A");
        return stub;
    }

    /**
     * Stub: Get health benefits (Payroll PROO905A)
     */
    public java.util.Map<String, Object> getProviderHealthBenefits(Long providerId) {
        log.info("STUB: Payroll PROO905A call for provider {} health benefits", providerId);
        java.util.Map<String, Object> stub = new java.util.HashMap<>();
        stub.put("providerId", providerId);
        stub.put("enrollmentStatus", "NOT_ENROLLED");
        stub.put("message", "Health benefits integration pending - Payroll PROO905A");
        return stub;
    }

    /**
     * Stub: Get direct deposit status (CMNR931A)
     */
    public java.util.Map<String, Object> getProviderDirectDepositStatus(Long providerId) {
        log.info("STUB: CMNR931A call for provider {} direct deposit status", providerId);
        java.util.Map<String, Object> stub = new java.util.HashMap<>();
        stub.put("providerId", providerId);
        stub.put("directDepositActive", false);
        stub.put("message", "Direct deposit integration pending - CMNR931A");
        return stub;
    }

    // ==================== ENROLLMENT HISTORY RECORDING (BR-87) ====================

    /**
     * Record enrollment history snapshot (BR-87)
     * Called from every method that changes enrollment status.
     */
    public void recordEnrollmentHistory(ProviderEntity provider, String userId, String description) {
        ProviderEnrollmentHistory history = new ProviderEnrollmentHistory();
        history.setProviderId(provider.getId());
        history.setEligible(provider.getEligible());
        history.setIneligibleReason(provider.getIneligibleReason());
        history.setEffectiveDate(provider.getEffectiveDate());
        history.setSoc426Completed(provider.getSoc426Completed());
        history.setSoc846Completed(provider.getSoc846Completed());
        history.setOrientationCompleted(provider.getOrientationCompleted());
        history.setBackgroundCheckCompleted(provider.getBackgroundCheckCompleted());
        history.setSsnVerificationStatus(provider.getSsnVerificationStatus());
        history.setChangedBy(userId);
        history.setChangeDescription(description);

        enrollmentHistoryRepository.save(history);
        log.info("Enrollment history recorded for provider {}: {}", provider.getId(), description);
    }

    // ==================== VALIDATION EXCEPTION ====================

    /**
     * Custom exception for provider validation errors with structured error list.
     */
    public static class ProviderValidationException extends RuntimeException {
        private final List<ValidationError> errors;

        public ProviderValidationException(String message, List<ValidationError> errors) {
            super(message);
            this.errors = errors;
        }

        public List<ValidationError> getErrors() {
            return errors;
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * BR-69/70: Auto-check both SOC 846 checkboxes if one is checked (after FLSA cutoff date)
     */
    private void applySoc846DualCheckbox(ProviderEntity provider) {
        LocalDate effectiveDate = provider.getEffectiveDate() != null ? provider.getEffectiveDate() : LocalDate.now();
        if (!effectiveDate.isBefore(SOC_846_CUTOFF_DATE)) {
            if (Boolean.TRUE.equals(provider.getOvertimeAgreementSigned()) &&
                    !Boolean.TRUE.equals(provider.getProviderAgreementSigned())) {
                provider.setProviderAgreementSigned(true);
            }
            if (Boolean.TRUE.equals(provider.getProviderAgreementSigned()) &&
                    !Boolean.TRUE.equals(provider.getOvertimeAgreementSigned())) {
                provider.setOvertimeAgreementSigned(true);
            }
        }
    }

    /**
     * End active General Exception waivers for a provider
     */
    private void endActiveGeneralExceptions(Long providerId, LocalDate endDate, String userId) {
        List<ProviderCoriEntity> activeCori = providerCoriRepository.findActiveCoriByProviderId(providerId);
        for (ProviderCoriEntity cori : activeCori) {
            if (cori.hasActiveGeneralException()) {
                cori.setGeneralExceptionEndDate(endDate);
                cori.setUpdatedBy(userId);
                providerCoriRepository.save(cori);
                log.info("GE waiver ended for CORI {} on provider {}", cori.getId(), providerId);
            }
        }
    }

    /**
     * End active Recipient Waivers for a provider
     */
    private void endActiveRecipientWaivers(Long providerId, LocalDate endDate, String userId) {
        List<ProviderAssignmentEntity> assignments = providerAssignmentRepository
                .findByProviderIdAndStatus(providerId, AssignmentStatus.ACTIVE);
        for (ProviderAssignmentEntity assignment : assignments) {
            if (assignment.isRecipientWaiverActive()) {
                assignment.setRecipientWaiverEndDate(endDate);
                assignment.setUpdatedBy(userId);
                providerAssignmentRepository.save(assignment);
                log.info("Recipient waiver ended for assignment {} on provider {}", assignment.getId(), providerId);
            }
        }
    }

    /**
     * End Workweek Agreements for a provider (BR-73-76)
     */
    private void endWorkweekAgreements(Long providerId, LocalDate endDate, String userId) {
        List<ProviderAssignmentEntity> assignments = providerAssignmentRepository
                .findByProviderIdAndStatus(providerId, AssignmentStatus.ACTIVE);
        for (ProviderAssignmentEntity assignment : assignments) {
            if (Boolean.TRUE.equals(assignment.getHasWorkweekAgreement())) {
                assignment.setHasWorkweekAgreement(false);
                assignment.setUpdatedBy(userId);
                providerAssignmentRepository.save(assignment);
                log.info("BR-73-76: Workweek agreement ended for assignment {} on provider {}", assignment.getId(), providerId);
            }
        }
    }

    /**
     * End Travel Time segments for a provider (BR-66)
     */
    private void endTravelTimeSegments(Long providerId, LocalDate endDate, String userId) {
        List<ProviderAssignmentEntity> assignments = providerAssignmentRepository
                .findByProviderIdAndStatus(providerId, AssignmentStatus.ACTIVE);
        for (ProviderAssignmentEntity assignment : assignments) {
            if (Boolean.TRUE.equals(assignment.getHasTravelTimeAgreement())) {
                assignment.setHasTravelTimeAgreement(false);
                assignment.setUpdatedBy(userId);
                providerAssignmentRepository.save(assignment);
                log.info("BR-66: Travel time ended for assignment {} on provider {}", assignment.getId(), providerId);
            }
        }
    }

    /**
     * Create a snapshot of provider for comparison during validation
     */
    private ProviderEntity createSnapshot(ProviderEntity provider) {
        ProviderEntity snapshot = new ProviderEntity();
        snapshot.setEligible(provider.getEligible());
        snapshot.setIneligibleReason(provider.getIneligibleReason());
        snapshot.setEffectiveDate(provider.getEffectiveDate());
        snapshot.setSsnVerificationStatus(provider.getSsnVerificationStatus());
        snapshot.setMediCalSuspended(provider.getMediCalSuspended());
        snapshot.setSoc426Completed(provider.getSoc426Completed());
        snapshot.setSoc846Completed(provider.getSoc846Completed());
        snapshot.setOrientationCompleted(provider.getOrientationCompleted());
        snapshot.setBackgroundCheckCompleted(provider.getBackgroundCheckCompleted());
        snapshot.setProviderAgreementSigned(provider.getProviderAgreementSigned());
        snapshot.setOvertimeAgreementSigned(provider.getOvertimeAgreementSigned());
        snapshot.setGoodCauseExtension(provider.getGoodCauseExtension());
        snapshot.setAppealStatus(provider.getAppealStatus());
        snapshot.setAppealStatusDate(provider.getAppealStatusDate());
        snapshot.setDeathOutcomePending(provider.getDeathOutcomePending());
        // BR-03: Include demographics for SSN verification reset check
        snapshot.setSsn(provider.getSsn());
        snapshot.setFirstName(provider.getFirstName());
        snapshot.setLastName(provider.getLastName());
        snapshot.setDateOfBirth(provider.getDateOfBirth());
        snapshot.setGender(provider.getGender());
        snapshot.setState(provider.getState());
        return snapshot;
    }

    /**
     * Apply enrollment changes from request data to provider entity
     */
    private void applyEnrollmentChanges(ProviderEntity provider, ProviderEntity enrollmentData) {
        if (enrollmentData.getEligible() != null) provider.setEligible(enrollmentData.getEligible());
        if (enrollmentData.getIneligibleReason() != null) provider.setIneligibleReason(enrollmentData.getIneligibleReason());
        if (enrollmentData.getEffectiveDate() != null) provider.setEffectiveDate(enrollmentData.getEffectiveDate());
        if (enrollmentData.getSoc426Completed() != null) provider.setSoc426Completed(enrollmentData.getSoc426Completed());
        if (enrollmentData.getSoc846Completed() != null) provider.setSoc846Completed(enrollmentData.getSoc846Completed());
        if (enrollmentData.getOrientationCompleted() != null) provider.setOrientationCompleted(enrollmentData.getOrientationCompleted());
        if (enrollmentData.getOrientationDate() != null) provider.setOrientationDate(enrollmentData.getOrientationDate());
        if (enrollmentData.getBackgroundCheckCompleted() != null) provider.setBackgroundCheckCompleted(enrollmentData.getBackgroundCheckCompleted());
        if (enrollmentData.getProviderAgreementSigned() != null) provider.setProviderAgreementSigned(enrollmentData.getProviderAgreementSigned());
        if (enrollmentData.getOvertimeAgreementSigned() != null) provider.setOvertimeAgreementSigned(enrollmentData.getOvertimeAgreementSigned());
        if (enrollmentData.getAppealStatus() != null) provider.setAppealStatus(enrollmentData.getAppealStatus());
        if (enrollmentData.getAppealStatusDate() != null) provider.setAppealStatusDate(enrollmentData.getAppealStatusDate());
        if (enrollmentData.getAdminHearingDate() != null) provider.setAdminHearingDate(enrollmentData.getAdminHearingDate());
        if (enrollmentData.getGoodCauseExtension() != null) provider.setGoodCauseExtension(enrollmentData.getGoodCauseExtension());
        if (enrollmentData.getGoodCauseExtensionDate() != null) provider.setGoodCauseExtensionDate(enrollmentData.getGoodCauseExtensionDate());
        if (enrollmentData.getDeathOutcomePending() != null) provider.setDeathOutcomePending(enrollmentData.getDeathOutcomePending());
        if (enrollmentData.getCountyUse1() != null) provider.setCountyUse1(enrollmentData.getCountyUse1());
        if (enrollmentData.getCountyUse2() != null) provider.setCountyUse2(enrollmentData.getCountyUse2());
        if (enrollmentData.getCountyUse3() != null) provider.setCountyUse3(enrollmentData.getCountyUse3());
        if (enrollmentData.getCountyUse4() != null) provider.setCountyUse4(enrollmentData.getCountyUse4());
        // BR-03: Demographics that trigger SSN verification reset
        if (enrollmentData.getSsn() != null) provider.setSsn(enrollmentData.getSsn());
        if (enrollmentData.getFirstName() != null) provider.setFirstName(enrollmentData.getFirstName());
        if (enrollmentData.getLastName() != null) provider.setLastName(enrollmentData.getLastName());
        if (enrollmentData.getDateOfBirth() != null) provider.setDateOfBirth(enrollmentData.getDateOfBirth());
        if (enrollmentData.getGender() != null) provider.setGender(enrollmentData.getGender());
        // PVM-06: Address fields (state change triggers notification)
        if (enrollmentData.getStreetAddress() != null) provider.setStreetAddress(enrollmentData.getStreetAddress());
        if (enrollmentData.getCity() != null) provider.setCity(enrollmentData.getCity());
        if (enrollmentData.getState() != null) provider.setState(enrollmentData.getState());
        if (enrollmentData.getZipCode() != null) provider.setZipCode(enrollmentData.getZipCode());
        if (enrollmentData.getCountyCode() != null) provider.setCountyCode(enrollmentData.getCountyCode());
    }

    private String generateProviderNumber() {
        return "P" + System.currentTimeMillis();
    }

    private void updateProviderActiveCaseCount(Long providerId) {
        Integer count = providerAssignmentRepository.countActiveCasesByProvider(providerId);
        ProviderEntity provider = providerRepository.findById(providerId).orElse(null);
        if (provider != null) {
            provider.setNumberOfActiveCases(count);
            providerRepository.save(provider);
        }
    }

    private void terminateAllProviderAssignments(Long providerId, LocalDate effectiveDate, String reason, String userId) {
        List<ProviderAssignmentEntity> assignments = providerAssignmentRepository
                .findByProviderIdAndStatus(providerId, AssignmentStatus.ACTIVE);

        for (ProviderAssignmentEntity assignment : assignments) {
            assignment.setStatus(AssignmentStatus.TERMINATED);
            assignment.setLeaveTerminationEffectiveDate(effectiveDate);
            assignment.setTerminationReason(reason);
            assignment.setUpdatedBy(userId);
            providerAssignmentRepository.save(assignment);
        }
    }

    private boolean checkFundingSourceImpact(String relationship) {
        return "SPOUSE".equals(relationship) || "PARENT_OF_MINOR".equals(relationship);
    }

    private void updateCaseFundingSource(Long caseId, String relationship, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId).orElse(null);
        if (caseEntity != null) {
            // Per BR PVM 13 - Update to IPO if spouse or parent of minor
            if ("SPOUSE".equals(relationship) || "PARENT_OF_MINOR".equals(relationship)) {
                caseEntity.setFundingSource("IPO");
                caseEntity.setUpdatedBy(userId);
                caseRepository.save(caseEntity);
            }
        }
    }

    private void createOvertimeViolationReviewTask(OvertimeViolationEntity violation, String userId) {
        Task task = Task.builder()
                .title("Review Overtime Violation for Provider " + violation.getProviderId())
                .description("Overtime violation pending county review")
                .workQueue("COUNTY_OVERTIME_VIOLATION")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3).atStartOfDay())
                .build();

        Task savedTask = taskService.createTask(task);
        violation.setCountyReviewTaskId(savedTask.getId());
    }

    private void createSupervisorReviewTask(OvertimeViolationEntity violation, String userId) {
        Task task = Task.builder()
                .title("Supervisor Review for Provider Overtime Violation " + violation.getProviderId())
                .description("Override request pending supervisor review")
                .workQueue("SUPERVISOR_OVERTIME_VIOLATION")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(2).atStartOfDay())
                .build();

        Task savedTask = taskService.createTask(task);
        violation.setSupervisorReviewTaskId(savedTask.getId());
    }
}
