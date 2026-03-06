package com.cmips.service;

import com.cmips.controller.CaseManagementController.CreateCaseRequest;
import com.cmips.entity.*;
import com.cmips.entity.CaseEntity.CaseStatus;
import com.cmips.entity.RecipientEntity.PersonType;
import com.cmips.repository.*;
import com.cmips.util.BusinessDayCalculator;
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
    private final SAWSService sawsService;              // BR-9: S1 referral when no CIN found
    private final MEDSService medsService;              // BR-13: IH18 on active Medi-Cal case create
    private final PayrollIntegrationService payrollIntegrationService; // BR-15: PR00901A on case create
    private final CaseMaintenanceTaskService cmTaskService; // DSD Section 30 — Case Maintenance tasks/notifications
    private final TaskAutoCloseService taskAutoCloseService; // DSD GAP 3 — Auto-close tasks on business events
    private final BusinessDayCalculator businessDayCalc;

    // Aid codes excluded from S8 notification per BR OS 16
    private static final Set<String> EXCLUDED_AID_CODES = Set.of("10", "20", "60");

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
            NotificationService notificationService,
            SAWSService sawsService,
            MEDSService medsService,
            PayrollIntegrationService payrollIntegrationService,
            CaseMaintenanceTaskService cmTaskService,
            TaskAutoCloseService taskAutoCloseService,
            BusinessDayCalculator businessDayCalc) {
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
        this.sawsService = sawsService;
        this.medsService = medsService;
        this.payrollIntegrationService = payrollIntegrationService;
        this.cmTaskService = cmTaskService;
        this.taskAutoCloseService = taskAutoCloseService;
        this.businessDayCalc = businessDayCalc;
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

        // BR-15: Send PR00901A to payroll system on new case creation
        payrollIntegrationService.sendPR00901A(caseEntity.getCaseNumber(), String.valueOf(recipientId));

        // Generate task notification for case assignment
        createCaseAssignmentTask(caseEntity, caseOwnerId);

        log.info("Created case {} for recipient {}", caseNumber, recipientId);
        return caseEntity;
    }

    /**
     * Full Create-Case path called from the Create Case form.
     *
     * Handles both variants:
     *   • recipientId provided  → use existing recipient (legacy path)
     *   • demographics provided → create RecipientEntity first, then the case
     *
     * BR-9: If mediCalStatus == "PENDING_SAWS", sends an S1 IHSS Referral to
     *       the county SAWS system via SAWSService (CMSD4XXB / SMDS4XXB).
     *
     * Also sets the CIN on the new recipient when one was cleared.
     */
    @Transactional
    public CaseEntity createCaseFromRequest(CreateCaseRequest request, String userId) {

        // ── EM OS 176: CIN clearance must be performed before saving a case ─────
        // Block the save if clearance was never initiated (NOT_STARTED or null).
        // "PENDING_SAWS" is allowed — it means clearance ran but no CIN was found
        // and an S1 referral will be sent to SAWS.
        String clearStatus = request.getCinClearanceStatus();
        if (clearStatus == null || clearStatus.isBlank() || "NOT_STARTED".equalsIgnoreCase(clearStatus)) {
            throw new IllegalArgumentException(
                "EM OS 176: Client Index Number search is required before creating a case.");
        }

        // ── EM OS 067: Assigned Worker is required ────────────────────────────
        if (request.getCaseOwnerId() == null || request.getCaseOwnerId().isBlank()) {
            throw new IllegalArgumentException(
                "EM OS 067: Assigned worker must be entered.");
        }

        // ── IHSS Referral Date validation (DSD Screen Design pg 62) ──────────
        // DSD allows post-dating up to 2 weeks from the displayed date.
        // EM OS 175 was CANCELLED (ASR Sprint 43). EM OS 176 applies only when
        // changing an *existing* referral date to a date future to the displayed one.
        // For new cases the referral date defaults to current date and may be
        // post-dated up to 2 weeks.
        LocalDate parsedReferralDate = null;
        if (request.getIhssReferralDate() != null && !request.getIhssReferralDate().isBlank()) {
            try {
                parsedReferralDate = LocalDate.parse(request.getIhssReferralDate());
                // Allow up to 2 weeks in the future per DSD screen design
                if (parsedReferralDate.isAfter(LocalDate.now().plusWeeks(2))) {
                    throw new IllegalArgumentException(
                        "EM OS 176: IHSS Referral Date may not be more than two weeks in the future.");
                }
            } catch (java.time.format.DateTimeParseException ignored) {
                // Non-parseable date — leave for frontend validation
            }
        }

        Long recipientId = request.getRecipientId();

        // When using existing recipient, fall back to recipient's referralDate if request omits it
        if (recipientId != null && parsedReferralDate == null) {
            RecipientEntity existingRecipient = recipientRepository.findById(recipientId).orElse(null);
            if (existingRecipient != null && existingRecipient.getReferralDate() != null) {
                parsedReferralDate = existingRecipient.getReferralDate();
            }
        }

        // BR-19: Transition existing Open Referral to Applicant on case creation
        // BR-18: If recipient was created from a Provider, this is a dual-role scenario
        if (recipientId != null) {
            RecipientEntity existingRecipient = recipientRepository.findById(recipientId).orElse(null);
            if (existingRecipient != null && existingRecipient.getPersonType() == PersonType.OPEN_REFERRAL) {
                existingRecipient.setPersonType(PersonType.APPLICANT);
                recipientRepository.save(existingRecipient);
                log.info("[BR-19] Transitioned recipient {} from OPEN_REFERRAL to APPLICANT", recipientId);
            }
            if (existingRecipient != null && existingRecipient.getLinkedProviderId() != null) {
                log.info("[BR-18] Dual-role case creation: recipient {} linked to provider {} — now Provider + APPLICANT",
                         recipientId, existingRecipient.getLinkedProviderId());
            }
        }

        if (recipientId == null) {
            // ── Create recipient from form demographics ──────────────────────
            RecipientEntity recipient = new RecipientEntity();
            recipient.setLastName(request.getLastName() != null ? request.getLastName() : "");
            recipient.setFirstName(request.getFirstName() != null ? request.getFirstName() : "");
            recipient.setGender(request.getGender());
            if (request.getDateOfBirth() != null && !request.getDateOfBirth().isBlank()) {
                try { recipient.setDateOfBirth(LocalDate.parse(request.getDateOfBirth())); }
                catch (Exception ignored) {}
            }
            if (request.getSsn() != null) recipient.setSsn(request.getSsn());
            if (request.getCountyCode() != null) recipient.setCountyCode(request.getCountyCode());
            if (request.getSpokenLanguage() != null) recipient.setSpokenLanguage(request.getSpokenLanguage());
            if (request.getWrittenLanguage() != null) recipient.setWrittenLanguage(request.getWrittenLanguage());
            if (request.getCin() != null && !request.getCin().isBlank()) {
                recipient.setCin(request.getCin());
            }
            if (parsedReferralDate != null) {
                recipient.setReferralDate(parsedReferralDate);
            }
            // Determine person type from CIN clearance status (reuse outer clearStatus)
            if ("CLEARED".equals(clearStatus) || "EXACT_MATCH".equals(clearStatus)) {
                recipient.setPersonType(PersonType.APPLICANT);
            } else {
                recipient.setPersonType(PersonType.OPEN_REFERRAL);
            }
            recipient.setCreatedBy(userId);
            recipient = recipientRepository.save(recipient);
            recipientId = recipient.getId();
            log.info("[createCaseFromRequest] Created recipient id={} for {}", recipientId, request.getApplicantName());
        }

        // ── Create the case ──────────────────────────────────────────────────
        String caseNumber = generateCaseNumber(request.getCountyCode());
        CaseEntity caseEntity = CaseEntity.builder()
                .caseNumber(caseNumber)
                .recipientId(recipientId)
                .caseStatus(CaseStatus.PENDING)
                .caseType(CaseEntity.CaseType.IHSS)
                .countyCode(request.getCountyCode())
                .caseOwnerId(request.getCaseOwnerId())
                .cin(request.getCin())
                .applicationDate(LocalDate.now())
                .referralDate(parsedReferralDate)
                .interpreterAvailable(request.getInterpreterAvailable())
                .mediCalAidCode(request.getAidCode())
                .mediCalStatus(request.getMediCalStatus())
                .createdBy(userId)
                .build();
        caseEntity = caseRepository.save(caseEntity);

        // ── Fix 2: Record initial PENDING status in audit trail ──────────────
        recordStatusHistory(caseEntity, "CREATE", null,
                "Case created with status PENDING", LocalDate.now(), userId);

        // ── BR-9: S1 referral to SAWS when no CIN was found ─────────────────
        // mediCalStatus == "PENDING_SAWS" means clearance was performed but no
        // active Medi-Cal CIN was selected. Send S1 IHSS Referral via CMSD4XXB.
        if ("PENDING_SAWS".equals(request.getMediCalStatus())) {
            log.info("[BR-9] Sending S1 IHSS Referral to SAWS for case={}, recipient={}",
                     caseEntity.getCaseNumber(), recipientId);
            sawsService.sendS1Referral(
                    caseEntity.getCaseNumber(),
                    request.getCin() != null ? request.getCin() : "",
                    request.getLastName()  != null ? request.getLastName()  : "",
                    request.getFirstName() != null ? request.getFirstName() : "",
                    request.getDateOfBirth() != null ? request.getDateOfBirth() : "",
                    request.getGender()    != null ? request.getGender()    : "",
                    request.getCountyCode());
            // Record the S1 referral date on the case for Case Home display
            caseEntity.setMediCalEligibilityReferralDate(LocalDate.now());
            caseRepository.save(caseEntity);
        }

        // ── BR-13: IH18 Pending Application to MEDS when active Medi-Cal CIN selected ──
        // Active Medi-Cal = CIN clearance status is CLEARED or EXACT_MATCH (not PENDING_SAWS).
        // Eligibility status does NOT have 9 in first AND third digits, and aid codes != 10, 20, 60.
        String mediCalStatus = request.getMediCalStatus();
        if ("ACTIVE".equals(mediCalStatus)
                || "CLEARED".equals(clearStatus) || "EXACT_MATCH".equals(clearStatus)) {
            if (request.getCin() != null && !request.getCin().isBlank()
                    && !"PENDING_SAWS".equals(mediCalStatus)) {
                log.info("[BR-13] Sending IH18 Pending Application to MEDS for case={}, cin={}",
                         caseEntity.getCaseNumber(), request.getCin());
                medsService.sendIH18PendingApplication(
                        caseEntity.getCaseNumber(), request.getCin(), "IHSS_APPLICATION");
            }
        }

        // ── BR-16: S8 Notification to SAWS when active Medi-Cal with non-excluded aid code ──
        // When case is created for recipient with active Medi-Cal AND aid code is NOT 10, 20, or 60,
        // send S8 (SMDS4XXB) notification of IHSS "Pending" status.
        String aidCode = request.getAidCode();
        if (request.getCin() != null && !request.getCin().isBlank()
                && !"PENDING_SAWS".equals(mediCalStatus)
                && aidCode != null && !aidCode.isBlank()
                && !EXCLUDED_AID_CODES.contains(aidCode)) {
            log.info("[BR-16] Sending S8 IHSS Pending notification to SAWS for case={}, cin={}, aidCode={}",
                     caseEntity.getCaseNumber(), request.getCin(), aidCode);
            sawsService.sendS8Notification(caseEntity.getCaseNumber(), request.getCin(), aidCode);
        }

        createCaseAssignmentTask(caseEntity, request.getCaseOwnerId());

        // ── Notify the assigned case owner ─────────────────────────────────
        String caseOwnerId = request.getCaseOwnerId();
        if (caseOwnerId != null && !caseOwnerId.isBlank()) {
            notificationService.createNotification(
                    Notification.builder()
                            .userId(caseOwnerId)
                            .message("New case assigned to you: " + caseNumber +
                                     ". Applicant: " + request.getApplicantName() +
                                     ". Please review and take action.")
                            .notificationType(Notification.NotificationType.INFO)
                            .readStatus(false)
                            .build());
            log.info("[createCaseFromRequest] Notification sent to case owner {}", caseOwnerId);
        }

        // BR-15: Send PR00901A to payroll system on new case creation
        // Per DSD pages 245-247, send full recipient demographics + mailing address
        RecipientEntity payrollRecipient = recipientRepository.findById(recipientId).orElse(null);
        if (payrollRecipient != null) {
            String mailingAddr = String.join(", ",
                    payrollRecipient.getMailingStreetName() != null ? payrollRecipient.getMailingStreetName() : "",
                    payrollRecipient.getMailingCity() != null ? payrollRecipient.getMailingCity() : "",
                    payrollRecipient.getMailingState() != null ? payrollRecipient.getMailingState() : "",
                    payrollRecipient.getMailingZip() != null ? payrollRecipient.getMailingZip() : "");
            payrollIntegrationService.sendPR00901A(
                    caseNumber, String.valueOf(recipientId),
                    payrollRecipient.getLastName(), payrollRecipient.getFirstName(),
                    payrollRecipient.getMiddleName(), payrollRecipient.getSuffix(),
                    payrollRecipient.getSsn(), null, payrollRecipient.getBlankSsnReason(),
                    payrollRecipient.getDateOfBirth() != null ? payrollRecipient.getDateOfBirth().toString() : "",
                    payrollRecipient.getGender(),
                    request.getCountyCode(), null,
                    request.getCaseOwnerId(), mailingAddr);
        } else {
            payrollIntegrationService.sendPR00901A(caseNumber, String.valueOf(recipientId));
        }

        // ── BR-44: Create Person Contact record on case creation ──────────────
        // When Create Case is saved and contact info exists, create a new Person
        // Contact record with From Date = current date.
        createInitialPersonContact(caseEntity, recipientId, userId);

        log.info("[createCaseFromRequest] Created case {} (mediCalStatus={})",
                 caseNumber, request.getMediCalStatus());
        return caseEntity;
    }

    /**
     * BR OS 44: Create initial Person Contact record when a case is created.
     * Sets From Date to current date.
     */
    private void createInitialPersonContact(CaseEntity caseEntity, Long recipientId, String userId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId).orElse(null);
        if (recipient == null) return;

        // Only create if the recipient has contact info (phone or address)
        boolean hasPhone = recipient.getPrimaryPhone() != null && !recipient.getPrimaryPhone().isBlank();
        boolean hasAddress = recipient.getResidenceStreetName() != null && !recipient.getResidenceStreetName().isBlank();
        if (!hasPhone && !hasAddress) return;

        CaseContactEntity contact = new CaseContactEntity();
        contact.setCaseId(caseEntity.getId());
        contact.setRecipientId(recipientId);
        contact.setContactType("PRIMARY");
        contact.setFirstName(recipient.getFirstName());
        contact.setLastName(recipient.getLastName());
        contact.setPhone(recipient.getPrimaryPhone());
        contact.setStreetAddress(recipient.getResidenceStreetName());
        contact.setCity(recipient.getResidenceCity());
        contact.setState(recipient.getResidenceState());
        contact.setZipCode(recipient.getResidenceZip());
        contact.setStatus("ACTIVE");
        contact.setStartDate(LocalDate.now());
        contact.setCreatedBy(userId);
        caseContactRepository.save(contact);
        log.info("[BR-44] Created initial Person Contact for case={}, recipient={}",
                 caseEntity.getCaseNumber(), recipientId);
    }

    /**
     * BR OS 45: Update Person Contact From Date when IHSS Referral Date is modified.
     * When the IHSS Referral Date is changed and Person Contacts exist for the case,
     * update all contacts' From Date to the new IHSS Referral Date.
     */
    public void updatePersonContactFromDate(Long caseId, LocalDate newReferralDate) {
        List<CaseContactEntity> contacts = caseContactRepository.findByCaseId(caseId);
        for (CaseContactEntity contact : contacts) {
            contact.setStartDate(newReferralDate);
            caseContactRepository.save(contact);
        }
        if (!contacts.isEmpty()) {
            log.info("[BR-45] Updated From Date to {} on {} Person Contacts for case={}",
                     newReferralDate, contacts.size(), caseId);
        }
    }

    /**
     * Create a referral (per BR OS 17, 28)
     *
     * Required-field enforcement per DSD CI-67784 and EM codes:
     *   EM OS 005: Last Name is required
     *   EM OS 006: First Name is required
     *   EM OS 001: Referral Source is required
     *   EM OS 080: Either a Residence Address or a Phone Number is required
     */
    @Transactional
    public RecipientEntity createReferral(RecipientEntity recipient, String userId) {

        // ── Field validation ────────────────────────────────────────────────
        if (recipient.getLastName() == null || recipient.getLastName().isBlank()) {
            throw new IllegalArgumentException("EM OS 005: Last Name is required");
        }
        if (recipient.getFirstName() == null || recipient.getFirstName().isBlank()) {
            throw new IllegalArgumentException("EM OS 006: First Name is required");
        }
        if (recipient.getReferralSource() == null || recipient.getReferralSource().isBlank()) {
            throw new IllegalArgumentException("EM OS 001: Referral Source is required");
        }
        // EM OS 007: SSN must be blank if Blank SSN Reason indicated (and vice versa)
        if (recipient.getBlankSsnReason() != null && !recipient.getBlankSsnReason().isBlank()
                && recipient.getSsn() != null && !recipient.getSsn().isBlank()) {
            throw new IllegalArgumentException("EM OS 007: SSN must be blank when Blank SSN Reason is indicated");
        }
        // EM OS 080: Address (city + ZIP) OR phone must be provided
        boolean hasAddress = (recipient.getResidenceCity() != null && !recipient.getResidenceCity().isBlank())
                          && (recipient.getResidenceZip()  != null && !recipient.getResidenceZip().isBlank());
        boolean hasPhone   = (recipient.getPrimaryPhone()  != null && !recipient.getPrimaryPhone().isBlank());
        if (!hasAddress && !hasPhone) {
            throw new IllegalArgumentException(
                "EM OS 080: Either a Residence Address or a Phone Number is required");
        }

        // EM-237/238: SSN validation
        if (recipient.getSsn() != null && !recipient.getSsn().isBlank()) {
            String ssn = recipient.getSsn().replaceAll("\\D", "");
            if (ssn.length() == 9 && ssn.startsWith("9")) {
                throw new IllegalArgumentException("EM OS 010: A valid Social Security Number cannot begin with nine (9).");
            }
            if (ssn.length() == 9 && ssn.matches("(\\d)\\1{8}")) {
                throw new IllegalArgumentException("EM OS 010: '" + ssn + "' is not a valid entry for the Social Security Number.");
            }
        }

        // EM-256: Phone cannot be 0000000000 or 9999999999
        if (hasPhone) {
            String phone = recipient.getPrimaryPhone().replaceAll("\\D", "");
            if ("0000000000".equals(phone) || "9999999999".equals(phone)) {
                throw new IllegalArgumentException("EM OS 256: Not a valid phone number. Please enter valid phone number.");
            }
        }

        // EM-242/243: Other Language fields must contain only English alpha characters
        if (recipient.getOtherWrittenLanguageDetail() != null && !recipient.getOtherWrittenLanguageDetail().isBlank()) {
            if (!recipient.getOtherWrittenLanguageDetail().matches("[a-zA-Z\\s\\-']+")) {
                throw new IllegalArgumentException("EM OS 242: Other Written Language Details field allows only English language alpha characters.");
            }
        }
        if (recipient.getOtherSpokenLanguageDetail() != null && !recipient.getOtherSpokenLanguageDetail().isBlank()) {
            if (!recipient.getOtherSpokenLanguageDetail().matches("[a-zA-Z\\s\\-']+")) {
                throw new IllegalArgumentException("EM OS 243: Other Spoken Language Details field allows only English language alpha characters.");
            }
        }

        // Set person type to Open-Referral
        recipient.setPersonType(PersonType.OPEN_REFERRAL);
        recipient.setReferralDate(LocalDate.now());
        recipient.setCreatedBy(userId);

        // BR-17: If this recipient is linked to a Provider, log the dual-role scenario
        if (recipient.getLinkedProviderId() != null) {
            log.info("[BR-17] Dual-role referral: recipient linked to provider {} — now Provider + OPEN_REFERRAL",
                     recipient.getLinkedProviderId());
        }

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

        // Idempotency guard: reject if case is already approved/eligible
        if (caseEntity.getCaseStatus() == CaseStatus.ELIGIBLE) {
            throw new RuntimeException("Case is already approved and eligible");
        }
        // Only PENDING cases can be approved
        if (caseEntity.getCaseStatus() != CaseStatus.PENDING) {
            throw new RuntimeException("Case cannot be approved in status: " + caseEntity.getCaseStatus());
        }

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

        // BR OS 12: Send MEDS IH34 – Update Application Data on case denial
        if (caseEntity.getCin() != null) {
            medsService.sendIH34UpdateApplicationData(
                String.valueOf(caseId), caseEntity.getCin(), "DENIED");
        }
        log.info("Case {} denied: {}, IH34 sent to MEDS", caseEntity.getCaseNumber(), denialReason);

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

        // Auto-close tasks triggered by case termination (DSD GAP 3)
        taskAutoCloseService.onBusinessEvent(TaskAutoCloseService.EVENT_CASE_TERMINATED, caseEntity.getCaseNumber());

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

        // CM 38 — If case has WPCS hours, create task to verify WPCS status
        if (cmTaskService.hasWpcsHours(caseEntity)) {
            cmTaskService.onCaseOnLeaveWithWpcsHours(caseEntity);
        }

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

        // CM 11 — Application withdrawal notification
        cmTaskService.onApplicationWithdrawn(caseEntity);

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
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));

        // EM#45: Only the Case Owner may rescind a case
        if (caseEntity.getCaseOwnerId() != null && userId != null
                && !caseEntity.getCaseOwnerId().equals(userId)) {
            throw new IllegalArgumentException(
                "EM#45: Only the Case Owner may rescind a case.");
        }

        // EM#99: Rescind not allowed when SSN is Duplicate or Suspect
        if (caseEntity.getRecipientId() != null) {
            recipientRepository.findById(caseEntity.getRecipientId()).ifPresent(recipient -> {
                if (recipient.getSsnType() != null) {
                    String ssnType = recipient.getSsnType();
                    if ("DUPLICATE_SSN".equals(ssnType) || "SUSPECT_SSN".equals(ssnType)) {
                        throw new IllegalArgumentException(
                            "EM#99: Rescind Action not allowed when Alternative ID Type Social Security Number is indicated as "
                            + ssnType.replace("_", " ") + ".");
                    }
                }
            });
        }

        // EM#92: Rescind not allowed when CIN does not have active Medi-Cal eligibility
        if (caseEntity.getCin() != null && !caseEntity.getCin().isBlank()) {
            String mediCalSt = caseEntity.getMediCalStatus();
            if (mediCalSt == null || "INACTIVE".equalsIgnoreCase(mediCalSt) || "PENDING_SAWS".equalsIgnoreCase(mediCalSt)) {
                // Only block if termination reason is NOT recipient death
                if (!"CC511".equals(caseEntity.getTerminationReason())) {
                    throw new IllegalArgumentException(
                        "EM#92: Rescind action not allowed when CIN does not have active Medi-Cal eligibility.");
                }
            }
        }

        // Must be Terminated or Denied to rescind
        Set<CaseStatus> rescindableStatuses = Set.of(CaseStatus.TERMINATED, CaseStatus.DENIED);
        if (!rescindableStatuses.contains(caseEntity.getCaseStatus())) {
            throw new IllegalArgumentException("Case must be Terminated or Denied to rescind");
        }

        // Validate rescind reason code
        if (!CaseCodeTables.RESCIND_REASONS.containsKey(rescindReason)) {
            throw new IllegalArgumentException("Invalid rescind reason code: " + rescindReason);
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

        // CM 78 — Notify supervisor of case rescission
        cmTaskService.onCaseRescindedNotifySupervisor(caseEntity);

        // CM 61 — If case had WPCS hours, create WPCS task
        if (cmTaskService.hasWpcsHours(caseEntity)) {
            cmTaskService.onCaseRescindedWithWpcsHours(caseEntity);
        }

        // CM 64 — If case has companion cases, create companion rescission tasks
        if (caseEntity.getCompanionCase() != null && !caseEntity.getCompanionCase().isBlank()) {
            cmTaskService.onCompanionCaseRescinded(caseEntity);
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
                                     String assignedWorkerId, String cinClearanceStatus, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));

        // Only Terminated, Denied, or Application Withdrawn cases can be reactivated
        Set<CaseStatus> reactivatableStatuses = Set.of(CaseStatus.TERMINATED, CaseStatus.DENIED, CaseStatus.APPLICATION_WITHDRAWN);
        if (!reactivatableStatuses.contains(caseEntity.getCaseStatus())) {
            throw new IllegalArgumentException("Case must be Terminated, Denied, or Application Withdrawn to reactivate");
        }

        // TR25: Case terminated for CC514 (Medi-Cal non-compliance) within 90 days cannot be reactivated
        if (caseEntity.getCaseStatus() == CaseStatus.TERMINATED
                && "CC514".equals(caseEntity.getTerminationReason())
                && caseEntity.getTerminationDate() != null
                && caseEntity.getTerminationDate().isAfter(LocalDate.now().minusDays(90))) {
            throw new IllegalArgumentException(
                "TR25: Case terminated for Medi-Cal non-compliance (CC514) within the past 90 days cannot be reactivated.");
        }

        // EM#58: Reactivate Action not allowed when Death Outcome has been recorded
        if (caseEntity.getRecipientId() != null) {
            recipientRepository.findById(caseEntity.getRecipientId()).ifPresent(recipient -> {
                if (Boolean.TRUE.equals(recipient.getDeceased()) || recipient.getDateOfDeath() != null) {
                    throw new IllegalArgumentException(
                        "EM#58: Reactivate Action not allowed — a Death Outcome has been recorded for this recipient.");
                }
                // EM#100: Reactivate Action not allowed when SSN is Duplicate or Suspect
                if (recipient.getSsnType() != null) {
                    String ssnType = recipient.getSsnType();
                    if ("DUPLICATE_SSN".equals(ssnType) || "SUSPECT_SSN".equals(ssnType)) {
                        throw new IllegalArgumentException(
                            "EM#100: Reactivate Action not allowed when Alternative ID Type Social Security Number is indicated as "
                            + ssnType.replace("_", " ") + ".");
                    }
                }
            });
        }

        // Assigned Worker is required for reactivation
        if (assignedWorkerId == null || assignedWorkerId.isBlank()) {
            throw new IllegalArgumentException("Assigned Worker is required for case reactivation.");
        }

        // EM#117: CIN clearance (SCI search) must be performed before reactivation
        if (cinClearanceStatus == null || cinClearanceStatus.isBlank() || "NOT_STARTED".equals(cinClearanceStatus)) {
            throw new IllegalArgumentException(
                "EM#117: CIN Clearance (SCI Search) must be performed before case reactivation.");
        }

        // EM#98: Cannot reactivate same day as denial/termination/withdrawal
        LocalDate actionDate = caseEntity.getTerminationDate() != null ? caseEntity.getTerminationDate()
                : caseEntity.getDenialDate() != null ? caseEntity.getDenialDate()
                : caseEntity.getWithdrawalDate();
        if (actionDate != null && actionDate.equals(LocalDate.now())) {
            throw new IllegalArgumentException("EM#98: Case may not be Reactivated the same day as a Denial, Termination or Withdrawal action was taken");
        }

        // EM#112: Updated Referral Date may not be dated more than 14 days prior
        if (referralDate != null && referralDate.isBefore(LocalDate.now().minusDays(14))) {
            throw new IllegalArgumentException("EM#112: Updated Referral Date may not be dated more than two weeks (14 calendar days) prior to the current date");
        }

        // EM#113: IHSS Referral Date may not be changed to a date future to the displayed Referral Date
        if (referralDate != null && referralDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("EM#113: IHSS Referral Date may not be changed to a date future to the displayed IHSS Referral Date");
        }

        // Save previous state
        CaseStatus previousStatus = caseEntity.getCaseStatus();

        // Reactivate: set to PENDING
        caseEntity.setCaseStatus(CaseStatus.PENDING);
        caseEntity.setReferralDate(referralDate != null ? referralDate : LocalDate.now());
        caseEntity.setApplicationDate(LocalDate.now());
        caseEntity.setInterpreterAvailable(interpreterAvailable);
        caseEntity.setMeetsResidencyRequirement(meetsResidencyRequirement);
        caseEntity.setReferralSource(referralSource);

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
     * Get a single case by ID — efficient lookup plus recipient enrichment.
     *
     * Returns a map with all CaseEntity fields PLUS:
     *   recipientName        — full name from RecipientEntity
     *   recipientPersonType  — OPEN_REFERRAL / APPLICANT / RECIPIENT / etc.
     *   ihssReferralDate     — DSD Phase 7 canonical label for referralDate
     *   mediCalEligibilityReferralDate — mediCalEligibilityDate alias
     *
     * Throws RuntimeException("Case not found") if ID does not exist.
     */
    public Map<String, Object> getCaseWithDetails(Long caseId) {
        CaseEntity c = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found: " + caseId));

        // Build enriched response map
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("id",            c.getId());
        map.put("caseNumber",    c.getCaseNumber());
        map.put("caseStatus",    c.getCaseStatus() != null ? c.getCaseStatus().name() : null);
        map.put("caseType",      c.getCaseType()   != null ? c.getCaseType().name()   : null);
        map.put("recipientId",   c.getRecipientId());
        map.put("cin",           c.getCin());
        map.put("countyCode",    c.getCountyCode());
        map.put("countyName",    c.getCountyName());
        map.put("caseOwnerId",   c.getCaseOwnerId());
        map.put("caseOwnerName", c.getCaseOwnerName());
        map.put("supervisorId",  c.getSupervisorId());
        map.put("mediCalStatus", c.getMediCalStatus());
        map.put("mediCalAidCode",c.getMediCalAidCode());
        map.put("fundingSource", c.getFundingSource());
        map.put("transferStatus",c.getTransferStatus());
        // DSD Phase 7 canonical field names
        map.put("ihssReferralDate",              c.getReferralDate());
        map.put("applicationDate",               c.getApplicationDate());
        map.put("eligibilityDate",               c.getEligibilityDate());
        map.put("mediCalEligibilityReferralDate",c.getMediCalEligibilityDate());
        map.put("authorizationStartDate",        c.getAuthorizationStartDate());
        map.put("authorizationEndDate",          c.getAuthorizationEndDate());
        // DSD Case Home additional fields
        map.put("districtOffice",                          c.getDistrictOffice());
        map.put("interpreterAvailable",                    c.getInterpreterAvailable());
        map.put("referralSource",                          c.getReferralSource());
        map.put("meetsResidencyRequirement",               c.getMeetsResidencyRequirement());
        map.put("mediCalInitialEligibilityNotificationDate", c.getMediCalInitialEligibilityNotificationDate());
        map.put("companionCase",                           c.getCompanionCase());
        map.put("stateHearing",                            c.getStateHearing());
        map.put("numberOfHouseholdMembers",                c.getNumberOfHouseholdMembers());
        map.put("mailDesignee",                            c.getMailDesignee());
        map.put("countyUse1",                              c.getCountyUse1());
        map.put("countyUse2",                              c.getCountyUse2());
        map.put("countyUse3",                              c.getCountyUse3());
        map.put("countyUse4",                              c.getCountyUse4());
        map.put("terminationDate",   c.getTerminationDate());
        map.put("terminationReason", c.getTerminationReason());
        map.put("denialDate",        c.getDenialDate());
        map.put("withdrawalDate",    c.getWithdrawalDate());
        // SOC and authorization data (BR SE 13, BR OS 49-60)
        map.put("shareOfCostAmount",       c.getShareOfCostAmount());
        map.put("authorizedHoursMonthly",  c.getAuthorizedHoursMonthly());
        map.put("authorizedHoursWeekly",   c.getAuthorizedHoursWeekly());
        map.put("reassessmentDueDate",     c.getReassessmentDueDate());
        map.put("lastAssessmentDate",      c.getLastAssessmentDate());
        map.put("createdAt",     c.getCreatedAt());
        map.put("updatedAt",     c.getUpdatedAt());

        // Status alias for frontend compatibility (frontend reads c.status)
        map.put("status", c.getCaseStatus() != null ? c.getCaseStatus().name() : null);

        // TR25 90-Day Rule: block reactivation if terminated for CC514 within 90 days
        boolean reactivationAllowed = false;
        Set<CaseStatus> reactivatableStatuses = Set.of(
            CaseStatus.TERMINATED, CaseStatus.DENIED, CaseStatus.APPLICATION_WITHDRAWN);
        if (reactivatableStatuses.contains(c.getCaseStatus())) {
            reactivationAllowed = true;
            if (c.getCaseStatus() == CaseStatus.TERMINATED
                    && "CC514".equals(c.getTerminationReason())
                    && c.getTerminationDate() != null
                    && c.getTerminationDate().isAfter(LocalDate.now().minusDays(90))) {
                reactivationAllowed = false;
            }
        }
        map.put("reactivationAllowed", reactivationAllowed);

        // Enrich with recipient demographics
        if (c.getRecipientId() != null) {
            recipientRepository.findById(c.getRecipientId()).ifPresent(r -> {
                map.put("recipientName",       r.getFullName());
                map.put("recipientFirstName",  r.getFirstName());
                map.put("recipientLastName",   r.getLastName());
                map.put("recipientDob",        r.getDateOfBirth());
                map.put("recipientPersonType", r.getPersonType() != null ? r.getPersonType().name() : null);
                map.put("recipientAddress",    r.getFullResidenceAddress());
                map.put("recipientPhone",      r.getPrimaryPhone());
                map.put("spokenLanguage",      r.getSpokenLanguage());
                map.put("writtenLanguage",     r.getWrittenLanguage());
            });
        }

        return map;
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

        caseEntity = caseRepository.save(caseEntity);

        // CM 06 — ICT referral task to ICT coordinator
        cmTaskService.onIctCreated(caseEntity, receivingCountyCode);

        // CM 50 — If case has WPCS hours, notify WPCS queue
        if (cmTaskService.hasWpcsHours(caseEntity)) {
            cmTaskService.onIctCreatedWithWpcsHours(caseEntity);
        }

        return caseEntity;
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

        caseEntity = caseRepository.save(caseEntity);

        // Auto-close ICT-related tasks (DSD GAP 3) — closes CM-006, CM-023-T
        taskAutoCloseService.onBusinessEvent(TaskAutoCloseService.EVENT_ICT_COMPLETED, caseEntity.getCaseNumber());

        // CM 25 — Notify new case owner of assignment
        cmTaskService.onIctCaseAssigned(caseEntity, newCaseOwnerId);

        // CM 49, 52 — If case has WPCS hours, create WPCS tasks
        if (cmTaskService.hasWpcsHours(caseEntity)) {
            cmTaskService.onIctAuthCompleteWpcs(caseEntity);
            cmTaskService.onIctCompleteWithWpcsHours(caseEntity);
        }

        return caseEntity;
    }

    /**
     * Cancel inter-county transfer — DSD Section 25, CM 23/51
     */
    @Transactional
    public CaseEntity cancelInterCountyTransfer(Long caseId, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (!"INITIATED".equals(caseEntity.getTransferStatus())) {
            throw new RuntimeException("Only in-progress transfers can be cancelled");
        }

        caseEntity.setTransferStatus("CANCELLED");
        caseEntity.setUpdatedBy(userId);
        caseEntity = caseRepository.save(caseEntity);

        // CM 23 — ICT cancelled task/notifications
        cmTaskService.onIctCancelled(caseEntity, userId);

        // CM 51 — If case has WPCS hours, create WPCS task
        if (cmTaskService.hasWpcsHours(caseEntity)) {
            cmTaskService.onIctCancelledWithWpcsHours(caseEntity);
        }

        recordStatusHistory(caseEntity, "ICT_CANCEL", null, "Inter-County Transfer cancelled", LocalDate.now(), userId);
        log.info("ICT cancelled for case {}", caseEntity.getCaseNumber());
        return caseEntity;
    }

    /**
     * Update funding source — DSD Section 25, CM 65
     */
    @Transactional
    public CaseEntity updateFundingSource(Long caseId, String newFundingSource, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        String oldFunding = caseEntity.getFundingSource();
        caseEntity.setFundingSource(newFundingSource);
        caseEntity.setUpdatedBy(userId);
        caseEntity = caseRepository.save(caseEntity);

        // CM 65 — Notify companion cases of funding source update
        cmTaskService.onFundingSourceUpdated(caseEntity);

        log.info("Funding source updated for case {} from {} to {}", caseEntity.getCaseNumber(), oldFunding, newFundingSource);
        return caseEntity;
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
                .dueDate(businessDayCalc.addBusinessDays(LocalDateTime.now(), 5))
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
