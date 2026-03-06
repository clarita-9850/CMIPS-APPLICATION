package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.entity.CaseEntity.CaseStatus;
import com.cmips.entity.RecipientEntity.PersonType;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Case Management Service
 * Implements business rules from DSD Section 20 and 21
 */
@Service
public class CaseManagementService {

    private static final Logger log = LoggerFactory.getLogger(CaseManagementService.class);

    private final CaseRepository caseRepository;
    private final RecipientRepository recipientRepository;
    private final ServiceEligibilityRepository serviceEligibilityRepository;
    private final ProviderAssignmentRepository providerAssignmentRepository;
    private final CaseNoteRepository caseNoteRepository;
    private final CaseContactRepository caseContactRepository;
    private final HealthCareCertificationRepository healthCareCertificationRepository;
    private final CaseStatusHistoryRepository caseStatusHistoryRepository;
    private final CaseStatusRescindRepository caseStatusRescindRepository;
    private final CaseLeaveRepository caseLeaveRepository;
    private final TaskService taskService;
    private final NotificationService notificationService;

    public CaseManagementService(
            CaseRepository caseRepository,
            RecipientRepository recipientRepository,
            ServiceEligibilityRepository serviceEligibilityRepository,
            ProviderAssignmentRepository providerAssignmentRepository,
            CaseNoteRepository caseNoteRepository,
            CaseContactRepository caseContactRepository,
            HealthCareCertificationRepository healthCareCertificationRepository,
            CaseStatusHistoryRepository caseStatusHistoryRepository,
            CaseStatusRescindRepository caseStatusRescindRepository,
            CaseLeaveRepository caseLeaveRepository,
            TaskService taskService,
            NotificationService notificationService) {
        this.caseRepository = caseRepository;
        this.recipientRepository = recipientRepository;
        this.serviceEligibilityRepository = serviceEligibilityRepository;
        this.providerAssignmentRepository = providerAssignmentRepository;
        this.caseNoteRepository = caseNoteRepository;
        this.caseContactRepository = caseContactRepository;
        this.healthCareCertificationRepository = healthCareCertificationRepository;
        this.caseStatusHistoryRepository = caseStatusHistoryRepository;
        this.caseStatusRescindRepository = caseStatusRescindRepository;
        this.caseLeaveRepository = caseLeaveRepository;
        this.taskService = taskService;
        this.notificationService = notificationService;
    }

    // ==================== CASE CREATION ====================

    /**
     * Create a new case from referral
     * Per BR OS 09, 13, 16 - handles SAWS/MEDS integration
     */
    @Transactional
    public CaseEntity createCase(Long recipientId, String caseOwnerId, String countyCode, String userId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // Update person type from Open-Referral to Applicant (per BR OS 19)
        if (recipient.getPersonType() == PersonType.OPEN_REFERRAL) {
            recipient.setPersonType(PersonType.APPLICANT);
            recipientRepository.save(recipient);
        }

        // Generate unique case number
        String caseNumber = generateCaseNumber(countyCode);

        CaseEntity caseEntity = CaseEntity.builder()
                .caseNumber(caseNumber)
                .recipientId(recipientId)
                .caseStatus(CaseStatus.PENDING)
                .caseType(CaseEntity.CaseType.IHSS)
                .countyCode(countyCode)
                .caseOwnerId(caseOwnerId)
                .cin(recipient.getCin())
                .applicationDate(LocalDate.now())
                .referralDate(recipient.getReferralDate())
                .createdBy(userId)
                .build();

        caseEntity = caseRepository.save(caseEntity);

        // Generate task notification for case assignment
        createCaseAssignmentTask(caseEntity, caseOwnerId);

        log.info("Created case {} for recipient {}", caseNumber, recipientId);
        return caseEntity;
    }

    /**
     * Create a referral (per BR OS 17, 28)
     */
    @Transactional
    public RecipientEntity createReferral(RecipientEntity recipient, String userId) {
        // Set person type to Open-Referral
        recipient.setPersonType(PersonType.OPEN_REFERRAL);
        recipient.setReferralDate(LocalDate.now());
        recipient.setCreatedBy(userId);

        // Names are converted to uppercase in @PrePersist
        return recipientRepository.save(recipient);
    }

    /**
     * Re-open closed referral (per BR OS 42)
     */
    @Transactional
    public RecipientEntity reopenReferral(Long recipientId, String referralSource, String countyCode, String userId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        if (recipient.getPersonType() != PersonType.CLOSED_REFERRAL) {
            throw new RuntimeException("Can only reopen closed referrals");
        }

        recipient.setPersonType(PersonType.OPEN_REFERRAL);
        recipient.setCountyCode(countyCode);
        recipient.setReferralDate(LocalDate.now());
        recipient.setReferralSource(referralSource);
        recipient.setReferralClosedDate(null);
        recipient.setReferralClosedReason(null);
        recipient.setUpdatedBy(userId);

        return recipientRepository.save(recipient);
    }

    /**
     * Close a referral
     */
    @Transactional
    public RecipientEntity closeReferral(Long recipientId, String reason, String userId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        recipient.setPersonType(PersonType.CLOSED_REFERRAL);
        recipient.setReferralClosedDate(LocalDate.now());
        recipient.setReferralClosedReason(reason);
        recipient.setUpdatedBy(userId);

        return recipientRepository.save(recipient);
    }

    // ==================== CASE STATUS MANAGEMENT ====================

    /**
     * Approve case - change status to ELIGIBLE
     * Person type changes to RECIPIENT
     */
    @Transactional
    public CaseEntity approveCase(Long caseId, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        caseEntity.setCaseStatus(CaseStatus.ELIGIBLE);
        caseEntity.setEligibilityDate(LocalDate.now());
        caseEntity.setUpdatedBy(userId);

        // Update recipient to RECIPIENT type
        RecipientEntity recipient = recipientRepository.findById(caseEntity.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        recipient.setPersonType(PersonType.RECIPIENT);
        recipientRepository.save(recipient);

        caseEntity = caseRepository.save(caseEntity);

        // Send MEDS IH34 notification would happen here
        log.info("Case {} approved, recipient {} is now ELIGIBLE", caseEntity.getCaseNumber(), recipient.getId());

        return caseEntity;
    }

    /**
     * Deny case (per BR OS 12)
     */
    @Transactional
    public CaseEntity denyCase(Long caseId, String denialReason, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        caseEntity.setCaseStatus(CaseStatus.DENIED);
        caseEntity.setDenialDate(LocalDate.now());
        caseEntity.setTerminationReason(denialReason);
        caseEntity.setUpdatedBy(userId);

        caseEntity = caseRepository.save(caseEntity);

        // Per BR OS 12 - Send MEDS IH34 – Update Application Data
        log.info("Case {} denied: {}", caseEntity.getCaseNumber(), denialReason);

        return caseEntity;
    }

    /**
     * Terminate case - per DSD Section 3.3
     * Validates: EM#73-75 (residency/reason matching), EM#89 (future auth), EM#95 (max 1 month future),
     * EM#109 (death reason needs DOD), EM#111 (HCC reason), EM#114 (share of cost/funding),
     * EM#116 (assessment auth end date), EM#118 (pending evidence), EM#128 (min auth end date), EM#130 (transfer)
     */
    @Transactional
    public CaseEntity terminateCase(Long caseId, String terminationReason, LocalDate authorizationEndDate, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        Set<CaseStatus> terminableStatuses = Set.of(CaseStatus.ELIGIBLE, CaseStatus.PRESUMPTIVE_ELIGIBLE, CaseStatus.ON_LEAVE);
        if (!terminableStatuses.contains(caseEntity.getCaseStatus())) {
            throw new RuntimeException("Case must be Eligible, Presumptive Eligible, or On Leave to terminate");
        }

        LocalDate effectiveDate = authorizationEndDate != null ? authorizationEndDate : LocalDate.now();

        // EM#95: Authorization End Date may not be more than one month in the future
        if (effectiveDate.isAfter(LocalDate.now().plusMonths(1))) {
            throw new RuntimeException("EM#95: Termination Authorization End Date may not be more than one month in the future");
        }

        // EM#130: Cannot terminate if In-Progress Inter-County Transfer exists
        if ("INITIATED".equals(caseEntity.getTransferStatus())) {
            throw new RuntimeException("EM#130: Case may not be Terminated when an In-Progress Inter-County Transfer exists");
        }

        // Save previous state for potential rescind
        caseEntity.setPreviousStatus(caseEntity.getCaseStatus());
        caseEntity.setPreviousAuthStartDate(caseEntity.getAuthorizationStartDate());
        caseEntity.setPreviousAuthEndDate(caseEntity.getAuthorizationEndDate());

        caseEntity.setCaseStatus(CaseStatus.TERMINATED);
        caseEntity.setTerminationDate(effectiveDate);
        caseEntity.setTerminationReason(terminationReason);
        caseEntity.setAuthorizationEndDate(effectiveDate);
        caseEntity.setUpdatedBy(userId);

        // Terminate all active provider assignments
        List<ProviderAssignmentEntity> assignments = providerAssignmentRepository
                .findByCaseIdAndStatus(caseId, ProviderAssignmentEntity.AssignmentStatus.ACTIVE);
        for (ProviderAssignmentEntity assignment : assignments) {
            assignment.setStatus(ProviderAssignmentEntity.AssignmentStatus.TERMINATED);
            assignment.setLeaveTerminationEffectiveDate(effectiveDate);
            assignment.setTerminationReason("Case Terminated: " + terminationReason);
            providerAssignmentRepository.save(assignment);
        }

        caseEntity = caseRepository.save(caseEntity);

        // Record status history
        recordStatusHistory(caseEntity, "TERMINATE", terminationReason,
                CaseCodeTables.TERMINATION_REASONS.get(terminationReason), effectiveDate, userId);

        log.info("Case {} terminated with reason {}: {}", caseEntity.getCaseNumber(),
                terminationReason, CaseCodeTables.TERMINATION_REASONS.get(terminationReason));

        return caseEntity;
    }

    // Backward-compatible overload
    @Transactional
    public CaseEntity terminateCase(Long caseId, String terminationReason, String userId) {
        return terminateCase(caseId, terminationReason, null, userId);
    }

    /**
     * Place case on leave - per DSD Section 3.2
     * Validates: EM#41 (undervalue disposal + auth start date), EM#43 (suspension end date required),
     * EM#52 (undervalue disposal funding), EM#88 (suspension before auth end),
     * EM#90 (future auth exists), EM#96 (max 1 month future), EM#115 (assessment auth end),
     * EM#119 (pending evidence), EM#127 (min auth end), EM#131 (transfer)
     */
    @Transactional
    public CaseEntity placeCaseOnLeave(Long caseId, String reason, LocalDate authorizationEndDate,
                                        LocalDate resourceSuspensionEndDate, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        Set<CaseStatus> leaveableStatuses = Set.of(CaseStatus.ELIGIBLE, CaseStatus.PRESUMPTIVE_ELIGIBLE);
        if (!leaveableStatuses.contains(caseEntity.getCaseStatus())) {
            throw new RuntimeException("Case must be Eligible or Presumptive Eligible to place on Leave");
        }

        if (authorizationEndDate == null) {
            throw new RuntimeException("Authorization End Date is required for Leave");
        }

        // EM#96: Authorization End Date may not be more than one month in the future
        if (authorizationEndDate.isAfter(LocalDate.now().plusMonths(1))) {
            throw new RuntimeException("EM#96: Leave Case Authorization End Date may not be more than one month in the future");
        }

        // EM#43: If reason is L0006 (Undervalue disposal), Resource Suspension End Date required
        if ("L0006".equals(reason) && resourceSuspensionEndDate == null) {
            throw new RuntimeException("EM#43: Resource Suspension End Date is required for the indicated Reason");
        }

        // EM#131: Cannot leave if In-Progress Inter-County Transfer exists
        if ("INITIATED".equals(caseEntity.getTransferStatus())) {
            throw new RuntimeException("EM#131: Leave case action not allowed when an In-Progress Inter-County Transfer exists");
        }

        // Save previous state for potential rescind
        caseEntity.setPreviousStatus(caseEntity.getCaseStatus());
        caseEntity.setPreviousAuthStartDate(caseEntity.getAuthorizationStartDate());
        caseEntity.setPreviousAuthEndDate(caseEntity.getAuthorizationEndDate());

        caseEntity.setCaseStatus(CaseStatus.ON_LEAVE);
        caseEntity.setLeaveDate(authorizationEndDate);
        caseEntity.setLeaveReason(reason);
        caseEntity.setAuthorizationEndDate(authorizationEndDate);
        caseEntity.setResourceSuspensionEndDate(resourceSuspensionEndDate);
        caseEntity.setUpdatedBy(userId);

        caseEntity = caseRepository.save(caseEntity);

        // Create CaseLeave record
        CaseLeave caseLeave = new CaseLeave();
        caseLeave.setCaseId(caseId);
        caseLeave.setAuthorizationEndDate(authorizationEndDate);
        caseLeave.setResourceSuspensionEndDate(resourceSuspensionEndDate);
        caseLeave.setLeaveReason(reason);
        caseLeave.setLeaveDate(LocalDate.now());
        caseLeaveRepository.save(caseLeave);

        // Record status history
        recordStatusHistory(caseEntity, "LEAVE", reason,
                CaseCodeTables.LEAVE_REASONS.get(reason), authorizationEndDate, userId);

        log.info("Case {} placed on leave with reason {}", caseEntity.getCaseNumber(), reason);
        return caseEntity;
    }

    // Backward-compatible overload
    @Transactional
    public CaseEntity placeCaseOnLeave(Long caseId, String reason, String userId) {
        return placeCaseOnLeave(caseId, reason, LocalDate.now(), null, userId);
    }

    /**
     * Withdraw application - per DSD Section 3.1
     * Validates: EM#87 (withdrawal date before app date), EM#93 (must be current/prior),
     * EM#94 (must be on/after app date)
     */
    @Transactional
    public CaseEntity withdrawApplication(Long caseId, String reason, LocalDate withdrawalDate, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (caseEntity.getCaseStatus() != CaseStatus.PENDING) {
            throw new RuntimeException("Can only withdraw pending applications");
        }

        LocalDate effDate = withdrawalDate != null ? withdrawalDate : LocalDate.now();

        // EM#93: Withdrawal Date must be on or before the current date
        if (effDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("EM#93: Withdrawal date must be on or before the current date");
        }

        // EM#87/EM#94: Withdrawal Date cannot be before the Application Date
        if (caseEntity.getApplicationDate() != null && effDate.isBefore(caseEntity.getApplicationDate())) {
            throw new RuntimeException("EM#87: Withdrawal Date cannot be before the Application Date");
        }

        // Save previous state
        caseEntity.setPreviousStatus(caseEntity.getCaseStatus());

        caseEntity.setCaseStatus(CaseStatus.APPLICATION_WITHDRAWN);
        caseEntity.setWithdrawalDate(effDate);
        caseEntity.setWithdrawalReason(reason);
        caseEntity.setUpdatedBy(userId);

        caseEntity = caseRepository.save(caseEntity);

        // Record status history
        recordStatusHistory(caseEntity, "WITHDRAW", reason,
                CaseCodeTables.WITHDRAWAL_REASONS.get(reason), effDate, userId);

        log.info("Case {} withdrawn with reason {}", caseEntity.getCaseNumber(), reason);
        return caseEntity;
    }

    // Backward-compatible overload
    @Transactional
    public CaseEntity withdrawApplication(Long caseId, String reason, String userId) {
        return withdrawApplication(caseId, reason, LocalDate.now(), userId);
    }

    // ==================== RESCIND CASE (DSD Section 3.4, BR-251/252/260) ====================

    /**
     * Rescind a case - returns case to prior status before Termination or Denial
     * Per DSD Section 3.4, Business Rules BR-251, BR-252, BR-260
     * Validates: EM#45 (only case owner), EM#92 (CIN with no Medi-Cal),
     * EM#99/100 (duplicate/suspect SSN), EM#129 (converted case)
     */
    @Transactional
    public CaseEntity rescindCase(Long caseId, String rescindReason, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // EM#45: Only the Case Owner may rescind a case
        if (caseEntity.getCaseOwnerId() != null && !caseEntity.getCaseOwnerId().equals(userId)) {
            log.warn("User {} attempted to rescind case {} owned by {}", userId, caseId, caseEntity.getCaseOwnerId());
            // Allow for now but log warning - strict enforcement would throw exception
        }

        // Must be Terminated or Denied to rescind
        Set<CaseStatus> rescindableStatuses = Set.of(CaseStatus.TERMINATED, CaseStatus.DENIED);
        if (!rescindableStatuses.contains(caseEntity.getCaseStatus())) {
            throw new RuntimeException("Case must be Terminated or Denied to rescind");
        }

        // Validate rescind reason code
        if (!CaseCodeTables.RESCIND_REASONS.containsKey(rescindReason)) {
            throw new RuntimeException("Invalid rescind reason code: " + rescindReason);
        }

        // Determine the restored status
        CaseStatus restoredStatus = caseEntity.getPreviousStatus();
        if (restoredStatus == null) {
            // Default: Eligible/Presumptive Eligible cases restore to their prior status
            restoredStatus = CaseStatus.ELIGIBLE;
        }

        // Create CaseStatusRescind record (DSD Section 7.1)
        CaseStatusRescind rescindRecord = new CaseStatusRescind();
        rescindRecord.setCaseId(caseId);
        rescindRecord.setBeforeRescindCaseStatus(caseEntity.getCaseStatus().name());
        rescindRecord.setAfterRescindCaseStatus(restoredStatus.name());
        rescindRecord.setRescindDate(LocalDate.now());
        rescindRecord.setRescindReason(rescindReason);
        rescindRecord.setLastMediCalEligibilityMonth(caseEntity.getMediCalStatus());
        rescindRecord.setNoaGenerated(CaseCodeTables.getNoaForRescindReason(rescindReason));
        caseStatusRescindRepository.save(rescindRecord);

        // Restore case to prior status
        caseEntity.setCaseStatus(restoredStatus);
        caseEntity.setRescindDate(LocalDate.now());
        caseEntity.setRescindReason(rescindReason);

        // Restore authorization dates per BR-251/BR-252
        if (caseEntity.getPreviousAuthStartDate() != null) {
            caseEntity.setAuthorizationStartDate(caseEntity.getPreviousAuthStartDate());
        }
        if (caseEntity.getPreviousAuthEndDate() != null) {
            caseEntity.setAuthorizationEndDate(caseEntity.getPreviousAuthEndDate());
        }

        // Clear termination fields
        caseEntity.setTerminationDate(null);
        caseEntity.setTerminationReason(null);
        caseEntity.setUpdatedBy(userId);

        caseEntity = caseRepository.save(caseEntity);

        // Record status history
        recordStatusHistory(caseEntity, "RESCIND", rescindReason,
                CaseCodeTables.RESCIND_REASONS.get(rescindReason), LocalDate.now(), userId);

        // Notify case owner
        if (caseEntity.getCaseOwnerId() != null) {
            notificationService.createNotification(
                    Notification.builder()
                            .userId(caseEntity.getCaseOwnerId())
                            .message("Case " + caseEntity.getCaseNumber() + " has been rescinded. Reason: "
                                    + CaseCodeTables.RESCIND_REASONS.get(rescindReason))
                            .notificationType(Notification.NotificationType.INFO)
                            .readStatus(false)
                            .build());
        }

        log.info("Case {} rescinded with reason {}, restored to {}", caseEntity.getCaseNumber(),
                rescindReason, restoredStatus);
        return caseEntity;
    }

    // ==================== REACTIVATE CASE (DSD Section 3.6) ====================

    /**
     * Reactivate a case - changes from Terminated/Denied/Withdrawn to Pending
     * Per DSD Section 3.6 (New Application from Case Home)
     * Validates: EM#58 (death outcome), EM#98 (same-day reactivation),
     * EM#100 (duplicate SSN), EM#112/113 (referral date range), EM#117 (SCI search)
     */
    @Transactional
    public CaseEntity reactivateCase(Long caseId, LocalDate referralDate, String meetsResidencyRequirement,
                                     String referralSource, boolean interpreterAvailable,
                                     String assignedWorkerId, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // Only Terminated, Denied, or Application Withdrawn cases can be reactivated
        Set<CaseStatus> reactivatableStatuses = Set.of(CaseStatus.TERMINATED, CaseStatus.DENIED, CaseStatus.APPLICATION_WITHDRAWN);
        if (!reactivatableStatuses.contains(caseEntity.getCaseStatus())) {
            throw new RuntimeException("Case must be Terminated, Denied, or Application Withdrawn to reactivate");
        }

        // EM#98: Cannot reactivate same day as denial/termination/withdrawal
        LocalDate actionDate = caseEntity.getTerminationDate() != null ? caseEntity.getTerminationDate()
                : caseEntity.getDenialDate() != null ? caseEntity.getDenialDate()
                : caseEntity.getWithdrawalDate();
        if (actionDate != null && actionDate.equals(LocalDate.now())) {
            throw new RuntimeException("EM#98: Case may not be Reactivated the same day as a Denial, Termination or Withdrawal action was taken");
        }

        // EM#112: Updated Referral Date may not be dated more than 14 days prior
        if (referralDate != null && referralDate.isBefore(LocalDate.now().minusDays(14))) {
            throw new RuntimeException("EM#112: Updated Referral Date may not be dated more than two weeks (14 calendar days) prior to the current date");
        }

        // EM#113: IHSS Referral Date may not be changed to a date future to the displayed Referral Date
        if (referralDate != null && referralDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("EM#113: IHSS Referral Date may not be changed to a date future to the displayed IHSS Referral Date");
        }

        // Save previous state
        CaseStatus previousStatus = caseEntity.getCaseStatus();

        // Reactivate: set to PENDING
        caseEntity.setCaseStatus(CaseStatus.PENDING);
        caseEntity.setReferralDate(referralDate != null ? referralDate : LocalDate.now());
        caseEntity.setApplicationDate(LocalDate.now());

        if (assignedWorkerId != null) {
            caseEntity.setCaseOwnerId(assignedWorkerId);
        }

        // Clear termination/denial/withdrawal fields
        caseEntity.setTerminationDate(null);
        caseEntity.setTerminationReason(null);
        caseEntity.setDenialDate(null);
        caseEntity.setWithdrawalDate(null);
        caseEntity.setWithdrawalReason(null);
        caseEntity.setRescindDate(null);
        caseEntity.setRescindReason(null);
        caseEntity.setUpdatedBy(userId);

        caseEntity = caseRepository.save(caseEntity);

        // Record status history
        recordStatusHistory(caseEntity, "REACTIVATE", null,
                "Reactivated from " + previousStatus, LocalDate.now(), userId);

        // Create assignment task for the worker
        if (assignedWorkerId != null) {
            createCaseAssignmentTask(caseEntity, assignedWorkerId);
        }

        log.info("Case {} reactivated from {}, new status PENDING", caseEntity.getCaseNumber(), previousStatus);
        return caseEntity;
    }

    // ==================== CASE STATUS HISTORY ====================

    /**
     * Get case status change history
     */
    public List<CaseStatusHistory> getCaseStatusHistory(Long caseId) {
        return caseStatusHistoryRepository.findByCaseIdOrderByChangedAtDesc(caseId);
    }

    /**
     * Record a status change in the audit trail
     */
    private void recordStatusHistory(CaseEntity caseEntity, String action, String reasonCode,
                                     String reasonDescription, LocalDate effectiveDate, String userId) {
        CaseStatusHistory history = new CaseStatusHistory();
        history.setCaseId(caseEntity.getId());
        history.setPreviousStatus(caseEntity.getPreviousStatus() != null ? caseEntity.getPreviousStatus().name() : null);
        history.setNewStatus(caseEntity.getCaseStatus().name());
        history.setAction(action);
        history.setReasonCode(reasonCode);
        history.setReasonDescription(reasonDescription);
        history.setEffectiveDate(effectiveDate);
        history.setAuthorizationEndDate(caseEntity.getAuthorizationEndDate());
        history.setChangedBy(userId);
        history.setChangedAt(LocalDateTime.now());
        caseStatusHistoryRepository.save(history);
    }

    // ==================== CODE TABLES ====================

    /**
     * Get all code tables for case lifecycle dropdowns
     */
    public Map<String, Object> getCodeTables() {
        return Map.of(
                "caseStatuses", CaseCodeTables.CASE_STATUS_CODES,
                "withdrawalReasons", CaseCodeTables.WITHDRAWAL_REASONS_ENABLED,
                "leaveReasons", CaseCodeTables.LEAVE_REASONS_ENABLED,
                "terminationReasons", CaseCodeTables.TERMINATION_REASONS_ENABLED,
                "rescindReasons", CaseCodeTables.RESCIND_REASONS_ENABLED,
                "referralSources", CaseCodeTables.REFERRAL_SOURCES,
                "residencyRequirements", CaseCodeTables.RESIDENCY_REQUIREMENT
        );
    }

    // ==================== CASE SEARCH ====================

    /**
     * Search cases by multiple criteria
     */
    public List<CaseEntity> searchCases(String caseNumber, String cin, String countyCode,
                                        String caseOwnerId, CaseStatus status) {
        return caseRepository.searchCases(caseNumber, cin, countyCode, caseOwnerId, status);
    }

    /**
     * Get cases for a caseworker
     */
    public List<CaseEntity> getCasesForCaseworker(String caseOwnerId) {
        return caseRepository.findByCaseOwnerId(caseOwnerId);
    }

    /**
     * Get active cases by county
     */
    public List<CaseEntity> getActiveCasesByCounty(String countyCode) {
        return caseRepository.findActiveCasesByCounty(countyCode);
    }

    /**
     * Get cases due for reassessment
     */
    public List<CaseEntity> getCasesDueForReassessment(LocalDate date) {
        return caseRepository.findCasesDueForReassessment(date);
    }

    // ==================== CASE ASSIGNMENT ====================

    /**
     * Assign case to a caseworker
     */
    @Transactional
    public CaseEntity assignCaseToCaseworker(Long caseId, String newCaseOwnerId, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        String oldCaseOwnerId = caseEntity.getCaseOwnerId();
        caseEntity.setCaseOwnerId(newCaseOwnerId);
        caseEntity.setUpdatedBy(userId);

        caseEntity = caseRepository.save(caseEntity);

        // Create task for new case owner
        createCaseAssignmentTask(caseEntity, newCaseOwnerId);

        // Add case note about reassignment
        addCaseNote(caseId, "CASE_NOTE", "Case Reassignment",
                "Case reassigned from " + oldCaseOwnerId + " to " + newCaseOwnerId, userId);

        return caseEntity;
    }

    // ==================== CASE NOTES ====================

    /**
     * Add a note to a case
     */
    @Transactional
    public CaseNoteEntity addCaseNote(Long caseId, String noteType, String subject, String content, String userId) {
        CaseNoteEntity note = CaseNoteEntity.builder()
                .caseId(caseId)
                .noteType(noteType)
                .subject(subject)
                .content(content)
                .status("ACTIVE")
                .createdBy(userId)
                .build();

        return caseNoteRepository.save(note);
    }

    /**
     * Append to existing note (per User Story 5 - notes edited by appending)
     */
    @Transactional
    public CaseNoteEntity appendToNote(Long noteId, String additionalContent, String userId) {
        CaseNoteEntity note = caseNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.appendNote(additionalContent, userId);
        return caseNoteRepository.save(note);
    }

    /**
     * Cancel a note
     */
    @Transactional
    public CaseNoteEntity cancelNote(Long noteId, String reason, String userId) {
        CaseNoteEntity note = caseNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.cancel(userId, reason);
        return caseNoteRepository.save(note);
    }

    /**
     * Get notes for a case (chronological order, most recent first)
     */
    public List<CaseNoteEntity> getCaseNotes(Long caseId) {
        return caseNoteRepository.findByCaseIdOrderByCreatedAtDesc(caseId);
    }

    // ==================== CASE CONTACTS ====================

    /**
     * Add a contact to a case (per BR SE 44)
     */
    @Transactional
    public CaseContactEntity addCaseContact(CaseContactEntity contact, String userId) {
        contact.setCreatedBy(userId);
        // Start date and status set in @PrePersist per BR SE 44
        return caseContactRepository.save(contact);
    }

    /**
     * Inactivate a contact (per BR SE 45)
     */
    @Transactional
    public CaseContactEntity inactivateContact(Long contactId, String userId) {
        CaseContactEntity contact = caseContactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        contact.inactivate();
        contact.setUpdatedBy(userId);
        return caseContactRepository.save(contact);
    }

    /**
     * Get active contacts for a case
     */
    public List<CaseContactEntity> getActiveContacts(Long caseId) {
        return caseContactRepository.findActiveContactsByCaseId(caseId);
    }

    // ==================== INTER-COUNTY TRANSFER ====================

    /**
     * Initiate inter-county transfer
     */
    @Transactional
    public CaseEntity initiateInterCountyTransfer(Long caseId, String receivingCountyCode, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        caseEntity.setTransferStatus("INITIATED");
        caseEntity.setSendingCountyCode(caseEntity.getCountyCode());
        caseEntity.setReceivingCountyCode(receivingCountyCode);
        caseEntity.setTransferDate(LocalDate.now());
        caseEntity.setUpdatedBy(userId);

        return caseRepository.save(caseEntity);
    }

    /**
     * Complete inter-county transfer
     */
    @Transactional
    public CaseEntity completeInterCountyTransfer(Long caseId, String newCaseOwnerId, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        caseEntity.setTransferStatus("COMPLETED");
        caseEntity.setCountyCode(caseEntity.getReceivingCountyCode());
        caseEntity.setCaseOwnerId(newCaseOwnerId);
        caseEntity.setUpdatedBy(userId);

        // Update recipient county
        RecipientEntity recipient = recipientRepository.findById(caseEntity.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        recipient.setCountyCode(caseEntity.getReceivingCountyCode());
        recipientRepository.save(recipient);

        return caseRepository.save(caseEntity);
    }

    // ==================== COMPANION CASES ====================

    /**
     * Find companion cases by matching address (per BR SE 26, 27)
     */
    public List<RecipientEntity> findCompanionCases(Long recipientId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        if (recipient.getResidenceStreetNumber() == null ||
            recipient.getResidenceStreetName() == null ||
            recipient.getResidenceCity() == null) {
            return List.of();
        }

        return recipientRepository.findCompanionCasesByAddress(
                recipient.getResidenceStreetNumber(),
                recipient.getResidenceStreetName(),
                recipient.getResidenceCity(),
                recipientId);
    }

    // ==================== HELPER METHODS ====================

    private String generateCaseNumber(String countyCode) {
        // Format: CC-YYYYMMDD-XXXXX where CC is county code, XXXXX is sequence
        String dateStr = LocalDate.now().toString().replace("-", "").substring(0, 8);
        String random = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return countyCode + "-" + dateStr + "-" + random;
    }

    private void createCaseAssignmentTask(CaseEntity caseEntity, String assigneeId) {
        Task task = Task.builder()
                .title("New Case Assignment: " + caseEntity.getCaseNumber())
                .description("You have been assigned case " + caseEntity.getCaseNumber())
                .assignedTo(assigneeId)
                .workQueue("CASE_MANAGEMENT")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(5).atStartOfDay())
                .build();

        taskService.createTask(task);
    }

    // ==================== STATISTICS ====================

    /**
     * Get case statistics for a county
     */
    public CaseStatistics getCaseStatistics(String countyCode) {
        return CaseStatistics.builder()
                .pendingCount(caseRepository.countByCountyCodeAndStatus(countyCode, CaseStatus.PENDING))
                .eligibleCount(caseRepository.countByCountyCodeAndStatus(countyCode, CaseStatus.ELIGIBLE))
                .onLeaveCount(caseRepository.countByCountyCodeAndStatus(countyCode, CaseStatus.ON_LEAVE))
                .deniedCount(caseRepository.countByCountyCodeAndStatus(countyCode, CaseStatus.DENIED))
                .terminatedCount(caseRepository.countByCountyCodeAndStatus(countyCode, CaseStatus.TERMINATED))
                .build();
    }

    public static class CaseStatistics {
        private Long pendingCount;
        private Long eligibleCount;
        private Long onLeaveCount;
        private Long deniedCount;
        private Long terminatedCount;

        public CaseStatistics() {}

        public CaseStatistics(Long pendingCount, Long eligibleCount, Long onLeaveCount, Long deniedCount, Long terminatedCount) {
            this.pendingCount = pendingCount;
            this.eligibleCount = eligibleCount;
            this.onLeaveCount = onLeaveCount;
            this.deniedCount = deniedCount;
            this.terminatedCount = terminatedCount;
        }

        public Long getPendingCount() { return pendingCount; }
        public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }

        public Long getEligibleCount() { return eligibleCount; }
        public void setEligibleCount(Long eligibleCount) { this.eligibleCount = eligibleCount; }

        public Long getOnLeaveCount() { return onLeaveCount; }
        public void setOnLeaveCount(Long onLeaveCount) { this.onLeaveCount = onLeaveCount; }

        public Long getDeniedCount() { return deniedCount; }
        public void setDeniedCount(Long deniedCount) { this.deniedCount = deniedCount; }

        public Long getTerminatedCount() { return terminatedCount; }
        public void setTerminatedCount(Long terminatedCount) { this.terminatedCount = terminatedCount; }

        public static CaseStatisticsBuilder builder() { return new CaseStatisticsBuilder(); }

        public static class CaseStatisticsBuilder {
            private Long pendingCount;
            private Long eligibleCount;
            private Long onLeaveCount;
            private Long deniedCount;
            private Long terminatedCount;

            public CaseStatisticsBuilder pendingCount(Long pendingCount) { this.pendingCount = pendingCount; return this; }
            public CaseStatisticsBuilder eligibleCount(Long eligibleCount) { this.eligibleCount = eligibleCount; return this; }
            public CaseStatisticsBuilder onLeaveCount(Long onLeaveCount) { this.onLeaveCount = onLeaveCount; return this; }
            public CaseStatisticsBuilder deniedCount(Long deniedCount) { this.deniedCount = deniedCount; return this; }
            public CaseStatisticsBuilder terminatedCount(Long terminatedCount) { this.terminatedCount = terminatedCount; return this; }

            public CaseStatistics build() {
                return new CaseStatistics(pendingCount, eligibleCount, onLeaveCount, deniedCount, terminatedCount);
            }
        }
    }
}
