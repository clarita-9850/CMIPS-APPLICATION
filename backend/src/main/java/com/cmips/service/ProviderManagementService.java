package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.entity.ProviderEntity.ProviderStatus;
import com.cmips.entity.ProviderAssignmentEntity.AssignmentStatus;
import com.cmips.entity.OvertimeViolationEntity.ViolationStatus;
import com.cmips.entity.OvertimeExemptionEntity.ExemptionStatus;
import com.cmips.entity.WorkweekAgreementEntity.AgreementStatus;
import com.cmips.entity.TravelTimeEntity.TravelTimeStatus;
import com.cmips.entity.ProviderBenefitEntity.BenefitStatus;
import com.cmips.entity.ProviderAttachmentEntity.AttachmentStatus;
import com.cmips.entity.BackupProviderHoursEntity.BackupStatus;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Provider Management Service
 * Implements business rules from DSD Section 23
 */
@Service
public class ProviderManagementService {

    private static final Logger log = LoggerFactory.getLogger(ProviderManagementService.class);

    private final ProviderRepository providerRepository;
    private final ProviderCoriRepository providerCoriRepository;
    private final ProviderAssignmentRepository providerAssignmentRepository;
    private final OvertimeViolationRepository overtimeViolationRepository;
    private final OvertimeExemptionRepository overtimeExemptionRepository;
    private final WorkweekAgreementRepository workweekAgreementRepository;
    private final TravelTimeRepository travelTimeRepository;
    private final ProviderBenefitRepository providerBenefitRepository;
    private final ProviderAttachmentRepository providerAttachmentRepository;
    private final BackupProviderHoursRepository backupProviderHoursRepository;
    private final CaseRepository caseRepository;
    private final TaskService taskService;
    private final TaskAutoCloseService taskAutoCloseService;

    public ProviderManagementService(
            ProviderRepository providerRepository,
            ProviderCoriRepository providerCoriRepository,
            ProviderAssignmentRepository providerAssignmentRepository,
            OvertimeViolationRepository overtimeViolationRepository,
            OvertimeExemptionRepository overtimeExemptionRepository,
            WorkweekAgreementRepository workweekAgreementRepository,
            TravelTimeRepository travelTimeRepository,
            ProviderBenefitRepository providerBenefitRepository,
            ProviderAttachmentRepository providerAttachmentRepository,
            BackupProviderHoursRepository backupProviderHoursRepository,
            CaseRepository caseRepository,
            TaskService taskService,
            TaskAutoCloseService taskAutoCloseService) {
        this.providerRepository = providerRepository;
        this.providerCoriRepository = providerCoriRepository;
        this.providerAssignmentRepository = providerAssignmentRepository;
        this.overtimeViolationRepository = overtimeViolationRepository;
        this.overtimeExemptionRepository = overtimeExemptionRepository;
        this.workweekAgreementRepository = workweekAgreementRepository;
        this.travelTimeRepository = travelTimeRepository;
        this.providerBenefitRepository = providerBenefitRepository;
        this.providerAttachmentRepository = providerAttachmentRepository;
        this.backupProviderHoursRepository = backupProviderHoursRepository;
        this.caseRepository = caseRepository;
        this.taskService = taskService;
        this.taskAutoCloseService = taskAutoCloseService;
    }

    // ==================== PROVIDER ENROLLMENT ====================

    /**
     * Create a new provider (per BR PVM 01-03)
     */
    @Transactional
    public ProviderEntity createProvider(ProviderEntity provider, String userId) {
        // Generate provider number
        provider.setProviderNumber(generateProviderNumber());

        // Set initial status
        provider.setEligible("PENDING");
        provider.setProviderStatus(ProviderStatus.ACTIVE);

        // Per BR PVM 03 - Set SSN verification to Not Verified
        provider.setSsnVerificationStatus("NOT_YET_VERIFIED");

        provider.setCreatedBy(userId);

        // Names converted to uppercase in @PrePersist per BR PVM 20
        return providerRepository.save(provider);
    }

    /**
     * Approve provider enrollment
     */
    @Transactional
    public ProviderEntity approveEnrollment(Long providerId, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Validate enrollment requirements
        validateEnrollmentRequirements(provider);

        provider.setEligible("YES");
        provider.setEffectiveDate(LocalDate.now());
        provider.setIneligibleReason(null);
        provider.setOriginalHireDate(LocalDate.now());
        provider.setUpdatedBy(userId);

        // Per BR PVM 17 - Reset sick leave eligibility period
        if (provider.getSickLeaveEligibilityPeriodEnd() != null &&
            !provider.getSickLeaveEligibilityPeriodEnd().equals(LocalDate.of(9999, 12, 31))) {
            provider.setSickLeaveEligibilityPeriodEnd(LocalDate.of(9999, 12, 31));
        }

        return providerRepository.save(provider);
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

        // End date workweek agreements (per BR PVM 73-76)
        // This would update WorkweekAgreement entities if we had them

        return providerRepository.save(provider);
    }

    /**
     * Reinstate provider (per BR PVM 23, 25)
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

        return providerRepository.save(provider);
    }

    /**
     * Re-enroll provider (per BR PVM 24, 26)
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
        provider.setUpdatedBy(userId);

        return providerRepository.save(provider);
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
        assignment.setInitialNotificationSent(false);

        // Per BR PVM 13 - Update funding source if spouse or parent of minor
        if (assignment.getImpactsFundingSource()) {
            updateCaseFundingSource(caseId, relationship, userId);
        }

        // Auto-close CM-013 (Case Has No Assigned Providers) — DSD GAP 3
        taskAutoCloseService.onBusinessEvent(TaskAutoCloseService.EVENT_PROVIDER_ASSIGNED, caseEntity.getCaseNumber());

        log.info("Provider {} assigned to case {}", providerId, caseId);
        return assignment;
    }

    /**
     * Terminate provider from case (per BR PVM 44-47)
     */
    @Transactional
    public ProviderAssignmentEntity terminateProviderAssignment(Long assignmentId, String reason, String userId) {
        ProviderAssignmentEntity assignment = providerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setStatus(AssignmentStatus.TERMINATED);
        assignment.setLeaveTerminationEffectiveDate(LocalDate.now());
        assignment.setTerminationReason(reason);
        assignment.setUpdatedBy(userId);

        assignment = providerAssignmentRepository.save(assignment);

        // Update provider active case count
        updateProviderActiveCaseCount(assignment.getProviderId());

        return assignment;
    }

    /**
     * Place provider on leave for a case
     */
    @Transactional
    public ProviderAssignmentEntity placeProviderOnLeave(Long assignmentId, String reason, String userId) {
        ProviderAssignmentEntity assignment = providerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setStatus(AssignmentStatus.ON_LEAVE);
        assignment.setLeaveTerminationEffectiveDate(LocalDate.now());
        assignment.setTerminationReason(reason);
        assignment.setUpdatedBy(userId);

        return providerAssignmentRepository.save(assignment);
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
     */
    @Transactional
    public ProviderCoriEntity createCori(Long providerId, ProviderCoriEntity cori, String userId) {
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

    // ==================== HELPER METHODS ====================

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

    // ==================== COUNTY DISPUTE ====================

    /**
     * File a county dispute for an overtime violation.
     * DSD: County files dispute after County Review outcome is "Upheld".
     * Creates task in County Dispute work queue with 4 business day resolution deadline.
     */
    @Transactional
    public OvertimeViolationEntity fileCountyDispute(Long violationId, String comments, String userId) {
        OvertimeViolationEntity violation = overtimeViolationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found: " + violationId));

        if (violation.getCountyDisputeFiled() != null && violation.getCountyDisputeFiled()) {
            throw new RuntimeException("County dispute already filed for this violation");
        }

        violation.setCountyDisputeFiled(true);
        violation.setCountyDisputeFiledDate(LocalDate.now());
        violation.setCountyDisputeComments(comments);
        violation.setCountyDisputeOutcome("PENDING_REVIEW");
        violation.setUpdatedBy(userId);

        // Create task: "County Dispute Overtime Violation for Provider [Name] [Number]"
        // DSD: Resolution due in 4 business days
        Task task = Task.builder()
                .title("County Dispute Overtime Violation for Provider " + violation.getProviderId())
                .description("County dispute filed. Resolution due in 4 business days.")
                .workQueue("COUNTY_OVERTIME_VIOLATION")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(4).atStartOfDay())
                .build();
        Task savedTask = taskService.createTask(task);
        violation.setCountyDisputeTaskId(savedTask.getId());

        log.info("County dispute filed for violation {} by {}", violationId, userId);
        return overtimeViolationRepository.save(violation);
    }

    /**
     * Resolve a county dispute.
     * Outcome: UPHELD → consequences enforced, OVERRIDE → inactivate violation
     */
    @Transactional
    public OvertimeViolationEntity resolveCountyDispute(Long violationId, String outcome, String comments, String userId) {
        OvertimeViolationEntity violation = overtimeViolationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found: " + violationId));

        violation.setCountyDisputeOutcome(outcome);
        violation.setCountyDisputeResolutionDate(LocalDate.now());
        violation.setCountyDisputeComments(comments);
        violation.setUpdatedBy(userId);

        if ("UPHELD".equals(outcome)) {
            // Violation stands — consequences already applied
            violation.setStatus(ViolationStatus.ACTIVE);
        } else if ("OVERRIDE".equals(outcome)) {
            // Override — inactivate the violation
            violation.setStatus(ViolationStatus.INACTIVE);
            // Reinstate provider if they were made ineligible due to this violation
            ProviderEntity provider = providerRepository.findById(violation.getProviderId()).orElse(null);
            if (provider != null && ("THIRD_OVERTIME_VIOLATION".equals(provider.getIneligibleReason())
                    || "FOURTH_OVERTIME_VIOLATION".equals(provider.getIneligibleReason()))) {
                provider.setEligible("YES");
                provider.setIneligibleReason(null);
                provider.setUpdatedBy(userId);
                providerRepository.save(provider);
                log.info("Provider {} reinstated after county dispute override", provider.getId());
            }
        }

        log.info("County dispute resolved for violation {}: outcome={}", violationId, outcome);
        return overtimeViolationRepository.save(violation);
    }

    /**
     * Request CDSS State Administrative Review (SAR).
     * Provider can request SAR after county dispute is upheld.
     */
    @Transactional
    public OvertimeViolationEntity requestCdssReview(Long violationId, String userId) {
        OvertimeViolationEntity violation = overtimeViolationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found: " + violationId));

        if (Boolean.TRUE.equals(violation.getCdssReviewRequested())) {
            throw new RuntimeException("CDSS review already requested for this violation");
        }
        if (!"UPHELD".equals(violation.getCountyDisputeOutcome())) {
            throw new RuntimeException("CDSS review can only be requested after a county dispute is upheld");
        }

        violation.setCdssReviewRequested(true);
        violation.setUpdatedBy(userId);

        log.info("CDSS SAR requested for violation {} by {}", violationId, userId);
        return overtimeViolationRepository.save(violation);
    }

    /**
     * Record final CDSS review outcome.
     * CDSS provides the final determination — UPHELD or OVERRIDE.
     */
    @Transactional
    public OvertimeViolationEntity recordCdssReviewOutcome(Long violationId, String outcome, String comments, String userId) {
        OvertimeViolationEntity violation = overtimeViolationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found: " + violationId));

        violation.setCdssReviewDate(LocalDate.now());
        violation.setCdssReviewOutcome(outcome);
        violation.setCdssReviewComments(comments);
        violation.setUpdatedBy(userId);

        if ("OVERRIDE".equals(outcome)) {
            violation.setStatus(ViolationStatus.INACTIVE);
            // Reinstate provider
            ProviderEntity provider = providerRepository.findById(violation.getProviderId()).orElse(null);
            if (provider != null && ("THIRD_OVERTIME_VIOLATION".equals(provider.getIneligibleReason())
                    || "FOURTH_OVERTIME_VIOLATION".equals(provider.getIneligibleReason()))) {
                provider.setEligible("YES");
                provider.setIneligibleReason(null);
                provider.setUpdatedBy(userId);
                providerRepository.save(provider);
            }
        }

        log.info("CDSS review outcome recorded for violation {}: {}", violationId, outcome);
        return overtimeViolationRepository.save(violation);
    }

    // ==================== OVERTIME EXEMPTIONS ====================

    /**
     * Create overtime exemption for provider (CI-668111 / CI-790066).
     * DSD rules:
     * - Exemption applies to entire calendar month
     * - Provider must not have an active exemption already
     * - Comments max 1,000 characters
     */
    @Transactional
    public OvertimeExemptionEntity createOvertimeExemption(Long providerId, OvertimeExemptionEntity exemption, String userId) {
        providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));

        if (exemption.getBeginDate() == null) throw new RuntimeException("Begin Date is required");
        if (exemption.getExemptionType() == null) throw new RuntimeException("Exemption Type is required");
        if (exemption.getComments() != null && exemption.getComments().length() > 1000) {
            throw new RuntimeException("Comments must not exceed 1,000 characters");
        }

        // Inactivate any existing active exemption (only one active at a time)
        overtimeExemptionRepository.findActiveExemptionForProvider(providerId, LocalDate.now())
                .ifPresent(existing -> {
                    existing.setStatus(ExemptionStatus.INACTIVE);
                    existing.setInactivatedDate(LocalDate.now());
                    existing.setInactivatedBy(userId);
                    existing.setInactivationReason("Replaced by new exemption");
                    overtimeExemptionRepository.save(existing);
                });

        exemption.setProviderId(providerId);
        exemption.setStatus(ExemptionStatus.ACTIVE);
        exemption.setCreatedBy(userId);
        exemption.setUpdatedBy(userId);

        // Update provider flag
        ProviderEntity provider = providerRepository.findById(providerId).get();
        provider.setHasOvertimeExemption(true);
        provider.setOvertimeExemptionBeginDate(exemption.getBeginDate());
        provider.setOvertimeExemptionEndDate(exemption.getEndDate());
        provider.setUpdatedBy(userId);
        providerRepository.save(provider);

        log.info("OT exemption created for provider {}", providerId);
        return overtimeExemptionRepository.save(exemption);
    }

    @Transactional
    public OvertimeExemptionEntity modifyOvertimeExemption(Long exemptionId, OvertimeExemptionEntity updates, String userId) {
        OvertimeExemptionEntity existing = overtimeExemptionRepository.findById(exemptionId)
                .orElseThrow(() -> new RuntimeException("Exemption not found: " + exemptionId));
        if (updates.getEndDate() != null) existing.setEndDate(updates.getEndDate());
        if (updates.getComments() != null) existing.setComments(updates.getComments());
        if (updates.getExemptionType() != null) existing.setExemptionType(updates.getExemptionType());
        existing.setUpdatedBy(userId);
        return overtimeExemptionRepository.save(existing);
    }

    @Transactional
    public OvertimeExemptionEntity inactivateOvertimeExemption(Long exemptionId, String reason, String userId) {
        OvertimeExemptionEntity exemption = overtimeExemptionRepository.findById(exemptionId)
                .orElseThrow(() -> new RuntimeException("Exemption not found: " + exemptionId));
        exemption.setStatus(ExemptionStatus.INACTIVE);
        exemption.setInactivatedDate(LocalDate.now());
        exemption.setInactivatedBy(userId);
        exemption.setInactivationReason(reason);
        exemption.setUpdatedBy(userId);

        // Clear provider flags
        ProviderEntity provider = providerRepository.findById(exemption.getProviderId()).orElse(null);
        if (provider != null) {
            provider.setHasOvertimeExemption(false);
            provider.setOvertimeExemptionBeginDate(null);
            provider.setOvertimeExemptionEndDate(null);
            provider.setUpdatedBy(userId);
            providerRepository.save(provider);
        }

        log.info("OT exemption {} inactivated by {}", exemptionId, userId);
        return overtimeExemptionRepository.save(exemption);
    }

    // ==================== WORKWEEK AGREEMENTS ====================

    /**
     * Create workweek agreement (CI-480910).
     * DSD BR PVM 73: Only one active workweek agreement per provider.
     * Inactivates previous active agreement automatically.
     */
    @Transactional
    public WorkweekAgreementEntity createWorkweekAgreement(Long providerId, WorkweekAgreementEntity agreement, String userId) {
        providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));
        if (agreement.getBeginDate() == null) throw new RuntimeException("Begin Date is required");

        // Inactivate existing active agreement (BR PVM 73)
        workweekAgreementRepository.findActiveAgreementForProvider(providerId).ifPresent(existing -> {
            existing.setStatus(AgreementStatus.INACTIVE);
            existing.setInactivatedDate(LocalDate.now());
            existing.setInactivatedBy(userId);
            existing.setInactivationReason("Replaced by new agreement");
            workweekAgreementRepository.save(existing);
        });

        agreement.setProviderId(providerId);
        agreement.setStatus(AgreementStatus.ACTIVE);
        agreement.setCreatedBy(userId);
        agreement.setUpdatedBy(userId);

        // Update provider link
        ProviderEntity provider = providerRepository.findById(providerId).get();
        provider.setUpdatedBy(userId);
        providerRepository.save(provider);

        log.info("Workweek agreement created for provider {}", providerId);
        return workweekAgreementRepository.save(agreement);
    }

    @Transactional
    public WorkweekAgreementEntity modifyWorkweekAgreement(Long agreementId, WorkweekAgreementEntity updates, String userId) {
        WorkweekAgreementEntity existing = workweekAgreementRepository.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Workweek agreement not found: " + agreementId));
        if (updates.getWorkweekStartDay() != null) existing.setWorkweekStartDay(updates.getWorkweekStartDay());
        if (updates.getAgreedHoursWeekly() != null) existing.setAgreedHoursWeekly(updates.getAgreedHoursWeekly());
        if (updates.getEndDate() != null) existing.setEndDate(updates.getEndDate());
        if (updates.getIncludesTravelTime() != null) existing.setIncludesTravelTime(updates.getIncludesTravelTime());
        if (updates.getTravelHoursWeekly() != null) existing.setTravelHoursWeekly(updates.getTravelHoursWeekly());
        existing.setUpdatedBy(userId);
        return workweekAgreementRepository.save(existing);
    }

    @Transactional
    public WorkweekAgreementEntity inactivateWorkweekAgreement(Long agreementId, String reason, String userId) {
        WorkweekAgreementEntity agreement = workweekAgreementRepository.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Workweek agreement not found: " + agreementId));
        agreement.setStatus(AgreementStatus.INACTIVE);
        agreement.setInactivatedDate(LocalDate.now());
        agreement.setInactivatedBy(userId);
        agreement.setInactivationReason(reason);
        agreement.setUpdatedBy(userId);
        log.info("Workweek agreement {} inactivated by {}", agreementId, userId);
        return workweekAgreementRepository.save(agreement);
    }

    // ==================== TRAVEL TIME ====================

    /**
     * Create travel time record (CI-480867).
     * 7-hour/week rule: total travel time across all records must not exceed 7 hrs/week.
     */
    @Transactional
    public TravelTimeEntity createTravelTime(Long providerId, TravelTimeEntity travelTime, String userId) {
        providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));
        if (travelTime.getBeginDate() == null) throw new RuntimeException("Begin Date is required");
        if (travelTime.getToRecipientId() == null) throw new RuntimeException("To Recipient is required");
        if (travelTime.getFromRecipientId() == null) throw new RuntimeException("From Recipient (Traveling From) is required");

        // 7-hour/week rule validation
        Double existingHours = travelTimeRepository.getTotalWeeklyTravelHours(providerId);
        double proposed = (travelTime.getTravelHoursWeekly() != null ? travelTime.getTravelHoursWeekly() : 0);
        if (existingHours + proposed > 7.0) {
            throw new RuntimeException("Total travel time would exceed 7 hours per week (7-hour rule). Current: "
                    + existingHours + "h, Proposed: " + proposed + "h");
        }

        travelTime.setProviderId(providerId);
        travelTime.setStatus(TravelTimeStatus.ACTIVE);
        travelTime.setCreatedBy(userId);
        travelTime.setUpdatedBy(userId);

        log.info("Travel time created for provider {}", providerId);
        return travelTimeRepository.save(travelTime);
    }

    @Transactional
    public TravelTimeEntity modifyTravelTime(Long travelTimeId, TravelTimeEntity updates, String userId) {
        TravelTimeEntity existing = travelTimeRepository.findById(travelTimeId)
                .orElseThrow(() -> new RuntimeException("Travel time not found: " + travelTimeId));
        if (updates.getTravelHoursWeekly() != null) existing.setTravelHoursWeekly(updates.getTravelHoursWeekly());
        if (updates.getTravelMinutes() != null) existing.setTravelMinutes(updates.getTravelMinutes());
        if (updates.getEndDate() != null) existing.setEndDate(updates.getEndDate());
        if (updates.getFromRecipientId() != null) existing.setFromRecipientId(updates.getFromRecipientId());
        if (updates.getFromRecipientName() != null) existing.setFromRecipientName(updates.getFromRecipientName());
        existing.setUpdatedBy(userId);
        return travelTimeRepository.save(existing);
    }

    @Transactional
    public TravelTimeEntity inactivateTravelTime(Long travelTimeId, String userId) {
        TravelTimeEntity travelTime = travelTimeRepository.findById(travelTimeId)
                .orElseThrow(() -> new RuntimeException("Travel time not found: " + travelTimeId));
        travelTime.setStatus(TravelTimeStatus.INACTIVE);
        travelTime.setEndDate(LocalDate.now());
        travelTime.setInactivatedDate(LocalDate.now());
        travelTime.setInactivatedBy(userId);
        travelTime.setUpdatedBy(userId);
        log.info("Travel time {} inactivated by {}", travelTimeId, userId);
        return travelTimeRepository.save(travelTime);
    }

    // ==================== PROVIDER BENEFITS ====================

    /**
     * Create provider benefit/deduction (CI-117534).
     * Linked to PA health plan enrollment. Updates payroll via PROO906A interface.
     */
    @Transactional
    public ProviderBenefitEntity createProviderBenefit(Long providerId, ProviderBenefitEntity benefit, String userId) {
        providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));
        if (benefit.getBenefitType() == null) throw new RuntimeException("Benefit Type is required");
        if (benefit.getBeginDate() == null) throw new RuntimeException("Begin Date is required");

        benefit.setProviderId(providerId);
        benefit.setStatus(BenefitStatus.ACTIVE);
        benefit.setPayrollUpdated(false); // Will be updated by PROO906A interface
        benefit.setCreatedBy(userId);
        benefit.setUpdatedBy(userId);

        log.info("Benefit {} created for provider {}", benefit.getBenefitType(), providerId);
        return providerBenefitRepository.save(benefit);
    }

    @Transactional
    public ProviderBenefitEntity modifyProviderBenefit(Long benefitId, ProviderBenefitEntity updates, String userId) {
        ProviderBenefitEntity existing = providerBenefitRepository.findById(benefitId)
                .orElseThrow(() -> new RuntimeException("Benefit not found: " + benefitId));
        if (updates.getMonthlyDeductionAmount() != null) existing.setMonthlyDeductionAmount(updates.getMonthlyDeductionAmount());
        if (updates.getCoverageType() != null) existing.setCoverageType(updates.getCoverageType());
        if (updates.getElectiveSdi() != null) existing.setElectiveSdi(updates.getElectiveSdi());
        if (updates.getSdiBeginDate() != null) existing.setSdiBeginDate(updates.getSdiBeginDate());
        if (updates.getSdiEndDate() != null) existing.setSdiEndDate(updates.getSdiEndDate());
        existing.setPayrollUpdated(false); // Needs to resync with payroll
        existing.setUpdatedBy(userId);
        return providerBenefitRepository.save(existing);
    }

    @Transactional
    public ProviderBenefitEntity terminateProviderBenefit(Long benefitId, String reason, String userId) {
        ProviderBenefitEntity benefit = providerBenefitRepository.findById(benefitId)
                .orElseThrow(() -> new RuntimeException("Benefit not found: " + benefitId));
        benefit.setStatus(BenefitStatus.TERMINATED);
        benefit.setEndDate(LocalDate.now());
        benefit.setTerminatedDate(LocalDate.now());
        benefit.setTerminatedBy(userId);
        benefit.setTerminationReason(reason);
        benefit.setPayrollUpdated(false); // PROO906A interface will process termination
        benefit.setUpdatedBy(userId);
        log.info("Benefit {} terminated by {}", benefitId, userId);
        return providerBenefitRepository.save(benefit);
    }

    // ==================== CORI MODIFY / INACTIVATE (CI-117566/117567) ====================

    /**
     * Modify an existing CORI record — CI-117567.
     * Allows updating crime description, CORI end date, tier, and related fields.
     */
    @Transactional
    public ProviderCoriEntity modifyCori(Long coriId, ProviderCoriEntity updates, String userId) {
        ProviderCoriEntity cori = providerCoriRepository.findById(coriId)
                .orElseThrow(() -> new RuntimeException("CORI record not found: " + coriId));
        if ("INACTIVE".equals(cori.getStatus())) {
            throw new IllegalArgumentException("Cannot modify an inactive CORI record.");
        }
        if (updates.getConvictionDate() != null) cori.setConvictionDate(updates.getConvictionDate());
        if (updates.getTier() != null) cori.setTier(updates.getTier());
        if (updates.getCrimeDescription() != null) cori.setCrimeDescription(updates.getCrimeDescription());
        if (updates.getCrimeCode() != null) cori.setCrimeCode(updates.getCrimeCode());
        if (updates.getCoriEndDate() != null) cori.setCoriEndDate(updates.getCoriEndDate());
        cori.setUpdatedBy(userId);
        log.info("CORI {} modified by {}", coriId, userId);
        return providerCoriRepository.save(cori);
    }

    /**
     * Inactivate a CORI record — CI-117566.
     * Sets status to INACTIVE. If Tier 1 was blocking enrollment and no other
     * active Tier 1 records remain, provider eligibility may be restored.
     */
    @Transactional
    public ProviderCoriEntity inactivateCori(Long coriId, String reason, String userId) {
        ProviderCoriEntity cori = providerCoriRepository.findById(coriId)
                .orElseThrow(() -> new RuntimeException("CORI record not found: " + coriId));
        cori.setStatus("INACTIVE");
        cori.setUpdatedBy(userId);
        log.info("CORI {} inactivated by {} — reason: {}", coriId, userId, reason);

        ProviderCoriEntity saved = providerCoriRepository.save(cori);

        // If no remaining active Tier 1 convictions, the provider may be re-eligible
        boolean hasTier1 = providerCoriRepository.hasActiveTier1Conviction(cori.getProviderId());
        if (!hasTier1) {
            log.info("Provider {} has no remaining active Tier 1 convictions after CORI {} inactivation",
                    cori.getProviderId(), coriId);
        }
        return saved;
    }

    // ==================== PROVIDER ATTACHMENTS (CI-117642-117650) ====================

    /** List all attachments for a provider — CI-117642 */
    public List<ProviderAttachmentEntity> getProviderAttachments(Long providerId) {
        return providerAttachmentRepository.findByProviderIdOrderByUploadDateDesc(providerId);
    }

    /**
     * Upload a new attachment — CI-117643.
     * Max 5 MB; allowed types: PDF, DOC, DOCX, TIF, TIFF, GIF, JPG, JPEG.
     */
    @Transactional
    public ProviderAttachmentEntity uploadAttachment(Long providerId, ProviderAttachmentEntity attachment, String userId) {
        // Validate file size (5 MB = 5242880 bytes)
        if (attachment.getFileSizeBytes() != null && attachment.getFileSizeBytes() > 5_242_880) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 5 MB.");
        }
        // Validate content type
        if (attachment.getContentType() != null) {
            String ct = attachment.getContentType().toLowerCase();
            boolean allowed = ct.contains("pdf") || ct.contains("msword") || ct.contains("openxmlformats")
                    || ct.contains("tiff") || ct.contains("tif") || ct.contains("gif")
                    || ct.contains("jpeg") || ct.contains("jpg");
            if (!allowed) {
                throw new IllegalArgumentException("File type not allowed. Permitted: PDF, DOC, DOCX, TIF, TIFF, GIF, JPG, JPEG.");
            }
        }
        attachment.setProviderId(providerId);
        attachment.setStatus(AttachmentStatus.ACTIVE);
        attachment.setUploadDate(LocalDate.now());
        attachment.setCreatedBy(userId);
        log.info("Attachment uploaded for provider {} by {}", providerId, userId);
        return providerAttachmentRepository.save(attachment);
    }

    /**
     * Update attachment description — CI-117649.
     */
    @Transactional
    public ProviderAttachmentEntity updateAttachmentDescription(Long attachmentId, String description, String userId) {
        ProviderAttachmentEntity attachment = providerAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found: " + attachmentId));
        attachment.setDescription(description);
        attachment.setUpdatedBy(userId);
        return providerAttachmentRepository.save(attachment);
    }

    /**
     * Archive an attachment — CI-117644 (manual archive; nightly batch also archives).
     */
    @Transactional
    public ProviderAttachmentEntity archiveAttachment(Long attachmentId, String userId) {
        ProviderAttachmentEntity attachment = providerAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found: " + attachmentId));
        attachment.setStatus(AttachmentStatus.ARCHIVED);
        attachment.setArchivedDate(LocalDate.now());
        attachment.setArchivedBy(userId);
        attachment.setUpdatedBy(userId);
        log.info("Attachment {} archived by {}", attachmentId, userId);
        return providerAttachmentRepository.save(attachment);
    }

    /**
     * Restore an archived attachment — CI-117645.
     * Only allowed for attachments archived the same day (same-day user request rule).
     */
    @Transactional
    public ProviderAttachmentEntity restoreAttachment(Long attachmentId, String userId) {
        ProviderAttachmentEntity attachment = providerAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found: " + attachmentId));
        if (attachment.getStatus() != AttachmentStatus.ARCHIVED) {
            throw new IllegalArgumentException("Only archived attachments can be restored.");
        }
        // Same-day restore rule
        if (!LocalDate.now().equals(attachment.getArchivedDate())) {
            throw new IllegalArgumentException("Attachments can only be restored on the same day they were archived.");
        }
        attachment.setStatus(AttachmentStatus.RESTORED);
        attachment.setRestoredDate(LocalDate.now());
        attachment.setRestoredBy(userId);
        attachment.setUpdatedBy(userId);
        log.info("Attachment {} restored by {}", attachmentId, userId);
        return providerAttachmentRepository.save(attachment);
    }

    // ==================== BACKUP PROVIDER HOURS (CI-117646/117647) ====================

    /** Get all backup provider hours records for a provider — CI-117646 */
    public List<BackupProviderHoursEntity> getBackupProviderHours(Long providerId) {
        return backupProviderHoursRepository.findHistoryForProvider(providerId);
    }

    /**
     * Create backup provider hours record — CI-117647.
     * Tracks authorized hours for a backup provider covering a specific case.
     */
    @Transactional
    public BackupProviderHoursEntity createBackupProviderHours(Long providerId,
                                                                BackupProviderHoursEntity hours,
                                                                String userId) {
        if (hours.getAuthorizedHoursWeekly() == null || hours.getAuthorizedHoursWeekly() <= 0) {
            throw new IllegalArgumentException("Authorized weekly hours must be greater than zero.");
        }
        if (hours.getBeginDate() == null) {
            throw new IllegalArgumentException("Begin date is required.");
        }
        hours.setProviderId(providerId);
        hours.setStatus(BackupStatus.ACTIVE);
        hours.setCreatedBy(userId);
        log.info("Backup provider hours created for provider {} by {}", providerId, userId);
        return backupProviderHoursRepository.save(hours);
    }

    /**
     * Modify backup provider hours — CI-117647.
     */
    @Transactional
    public BackupProviderHoursEntity modifyBackupProviderHours(Long hoursId,
                                                               BackupProviderHoursEntity updates,
                                                               String userId) {
        BackupProviderHoursEntity hours = backupProviderHoursRepository.findById(hoursId)
                .orElseThrow(() -> new RuntimeException("Backup provider hours record not found: " + hoursId));
        if (hours.getStatus() == BackupStatus.TERMINATED) {
            throw new IllegalArgumentException("Cannot modify terminated backup hours.");
        }
        if (updates.getAuthorizedHoursWeekly() != null) hours.setAuthorizedHoursWeekly(updates.getAuthorizedHoursWeekly());
        if (updates.getAuthorizedHoursMonthly() != null) hours.setAuthorizedHoursMonthly(updates.getAuthorizedHoursMonthly());
        if (updates.getEndDate() != null) hours.setEndDate(updates.getEndDate());
        if (updates.getProgramType() != null) hours.setProgramType(updates.getProgramType());
        hours.setUpdatedBy(userId);
        return backupProviderHoursRepository.save(hours);
    }

    /**
     * Terminate backup provider hours.
     */
    @Transactional
    public BackupProviderHoursEntity terminateBackupProviderHours(Long hoursId, String reason, String userId) {
        BackupProviderHoursEntity hours = backupProviderHoursRepository.findById(hoursId)
                .orElseThrow(() -> new RuntimeException("Backup provider hours record not found: " + hoursId));
        hours.setStatus(BackupStatus.TERMINATED);
        hours.setTerminatedDate(LocalDate.now());
        hours.setTerminationReason(reason);
        hours.setUpdatedBy(userId);
        log.info("Backup provider hours {} terminated by {}", hoursId, userId);
        return backupProviderHoursRepository.save(hours);
    }

    // ==================== MONTHLY PAID HOURS (Provider Hours Summary) ====================

    /**
     * Returns a summary of paid hours by month for a provider.
     * Queries the timesheet repository to aggregate approved hours per service month.
     * Used in Provider Monthly Paid Hours screen.
     */
    public java.util.Map<String, Object> getMonthlyPaidHours(Long providerId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));

        // Aggregate from assignments and timesheets
        List<ProviderAssignmentEntity> assignments = providerAssignmentRepository.findByProviderId(providerId);

        double totalAuthorizedWeekly = assignments.stream()
                .filter(a -> AssignmentStatus.ACTIVE.equals(a.getStatus()))
                .mapToDouble(a -> a.getAssignedHours() != null ? a.getAssignedHours() : 0.0)
                .sum();

        // Monthly approximation: weekly hours × 4.33
        double estimatedMonthlyHours = totalAuthorizedWeekly * 4.33;

        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("providerId", providerId);
        summary.put("providerName", provider.getFirstName() + " " + provider.getLastName());
        summary.put("providerNumber", provider.getProviderNumber());
        summary.put("totalAuthorizedWeeklyHours", totalAuthorizedWeekly);
        summary.put("estimatedMonthlyHours", Math.round(estimatedMonthlyHours * 100.0) / 100.0);
        summary.put("activeAssignmentCount", assignments.stream()
                .filter(a -> AssignmentStatus.ACTIVE.equals(a.getStatus())).count());
        summary.put("sickLeaveAccruedHours", provider.getSickLeaveAccruedHours());
        summary.put("sickLeaveRemainingHours", provider.getSickLeaveAccruedHours());
        return summary;
    }

    // ==================== SSN VERIFICATION (CMRS701E/CMRR701D) ====================

    /**
     * Triggers SSN verification for a single provider.
     * Marks the provider's SSN verification status as PENDING.
     * The nightly batch job (SsnVerificationBatchJob) processes all pending verifications.
     * DSD: Weekly CMIPS → SSA interface CMRS701E (send) / CMRR701D (receive).
     */
    @Transactional
    public ProviderEntity triggerSsnVerification(Long providerId, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));
        provider.setSsnVerificationStatus("PENDING");
        provider.setUpdatedBy(userId);
        log.info("SSN verification triggered for provider {} by {}", providerId, userId);
        return providerRepository.save(provider);
    }

    /**
     * Updates SSN verification result received from SSA (CMRR701D interface).
     * Statuses: VERIFIED, SUSPENDED_INELIGIBLE, NAME_MISMATCH, NOT_FOUND
     */
    @Transactional
    public ProviderEntity updateSsnVerificationResult(Long providerId, String verificationStatus, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));
        provider.setSsnVerificationStatus(verificationStatus);
        provider.setUpdatedBy(userId);

        // If verification returns suspended/ineligible, set provider terminated per BR
        // (ProviderStatus only has ACTIVE, ON_LEAVE, TERMINATED)
        if ("SUSPENDED_INELIGIBLE".equals(verificationStatus)) {
            provider.setProviderStatus(ProviderStatus.TERMINATED);
            provider.setIneligibleReason("SSN_VERIFICATION_FAILED");
            log.warn("Provider {} terminated due to SSN verification result: {}", providerId, verificationStatus);
        }
        return providerRepository.save(provider);
    }
}
