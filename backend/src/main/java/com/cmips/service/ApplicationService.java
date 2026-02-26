package com.cmips.service;

import com.cmips.entity.ApplicationEntity;
import com.cmips.entity.ApplicationEntity.*;
import com.cmips.entity.ReferralEntity;
import com.cmips.entity.RecipientEntity;
import com.cmips.entity.CaseEntity;
import com.cmips.repository.ApplicationRepository;
import com.cmips.repository.ReferralRepository;
import com.cmips.repository.RecipientRepository;
import com.cmips.repository.CaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for Application Processing
 * Implements DSD Section 20 - Application Processing with 45-day timeline
 */
@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);
    private static final int PROCESSING_DAYS = 45; // Federal requirement

    private final ApplicationRepository applicationRepository;
    private final ReferralRepository    referralRepository;
    private final RecipientRepository   recipientRepository;
    private final CaseRepository        caseRepository;
    private final ReferralService       referralService;
    // External interface services (separate from SCI/CMOO106A)
    private final SAWSService           sawsService;   // CMSD4XXB / SMDS4XXB
    private final MEDSService           medsService;   // CMDS103C

    public ApplicationService(ApplicationRepository applicationRepository,
                              ReferralRepository referralRepository,
                              RecipientRepository recipientRepository,
                              CaseRepository caseRepository,
                              ReferralService referralService,
                              SAWSService sawsService,
                              MEDSService medsService) {
        this.applicationRepository = applicationRepository;
        this.referralRepository    = referralRepository;
        this.recipientRepository   = recipientRepository;
        this.caseRepository        = caseRepository;
        this.referralService       = referralService;
        this.sawsService           = sawsService;
        this.medsService           = medsService;
    }

    // ==================== CREATE APPLICATION ====================

    /**
     * Create a new application directly (without referral)
     */
    @Transactional
    public ApplicationEntity createApplication(ApplicationEntity application, Long recipientId, String userId) {
        log.info("Creating new application for recipient: {}", recipientId);

        // Set defaults
        application.setRecipientId(recipientId);
        application.setApplicationDate(LocalDate.now());
        application.setDeadlineDate(LocalDate.now().plusDays(PROCESSING_DAYS));
        application.setStatus(ApplicationStatus.PENDING);
        application.setCreatedBy(userId);

        // Update recipient to APPLICANT status
        updateRecipientToApplicant(recipientId, userId);

        ApplicationEntity saved = applicationRepository.save(application);
        log.info("Application created: {} with deadline: {}", saved.getApplicationNumber(), saved.getDeadlineDate());

        return saved;
    }

    /**
     * Create application from referral conversion
     */
    @Transactional
    public ApplicationEntity createFromReferral(String referralId, ApplicationEntity application, String userId) {
        log.info("Creating application from referral: {}", referralId);

        ReferralEntity referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new RuntimeException("Referral not found: " + referralId));

        // Create recipient from referral info if not exists
        Long recipientId = referral.getRecipientId();
        if (recipientId == null) {
            RecipientEntity recipient = createRecipientFromReferral(referral, userId);
            recipientId = recipient.getId();
        }

        // Set referral link
        application.setReferralId(referralId);

        // Create application
        ApplicationEntity saved = createApplication(application, recipientId, userId);

        // Update referral status
        referralService.convertToApplication(referralId, saved.getId(), recipientId, userId);

        return saved;
    }

    // ==================== CIN CLEARANCE ====================

    /**
     * Perform CIN clearance (simulated for MVP)
     */
    @Transactional
    public ApplicationEntity performCINClearance(String applicationId, String userId) {
        log.info("Performing CIN clearance for application: {}", applicationId);

        ApplicationEntity application = getApplicationById(applicationId);

        // Simulate SCI query
        application.setCinClearanceStatus(CINClearanceStatus.IN_PROGRESS);
        application.setCinClearanceDate(LocalDateTime.now());

        // Simulate finding/creating CIN
        String simulatedCin = generateSimulatedCIN();
        application.setCin(simulatedCin);
        application.setCinClearanceStatus(CINClearanceStatus.NEW_CIN_CREATED);
        application.setCinMatchType("NEW");
        application.setUpdatedBy(userId);

        // Update recipient with CIN
        if (application.getRecipientId() != null) {
            RecipientEntity recipient = recipientRepository.findById(application.getRecipientId()).orElse(null);
            if (recipient != null) {
                recipient.setCin(simulatedCin);
                recipientRepository.save(recipient);
            }
        }

        log.info("CIN clearance completed for application {}. CIN: {}", applicationId, simulatedCin);
        return applicationRepository.save(application);
    }

    /**
     * Handle CIN match selection (when possible matches found)
     */
    @Transactional
    public ApplicationEntity selectCINMatch(String applicationId, String selectedCin, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setCin(selectedCin);
        application.setCinClearanceStatus(CINClearanceStatus.CLEARED);
        application.setCinMatchType("EXACT");
        application.setUpdatedBy(userId);

        // Update recipient with CIN
        if (application.getRecipientId() != null) {
            RecipientEntity recipient = recipientRepository.findById(application.getRecipientId()).orElse(null);
            if (recipient != null) {
                recipient.setCin(selectedCin);
                recipientRepository.save(recipient);
            }
        }

        log.info("CIN match selected for application {}. CIN: {}", applicationId, selectedCin);
        return applicationRepository.save(application);
    }

    // ==================== CIN SELECT WITH DEMOGRAPHIC CHECK ====================

    /**
     * Completes the CIN selection after the EL/OM transaction (Scenarios 4, 5, 6).
     *
     * Scenario 4 (BR 1)  – demographics match  → assign CIN, save MEDS info
     * Scenario 5         – demographics mismatch → return MISMATCH, do NOT assign
     * Scenario 6 (EM-202)– CIN in use elsewhere → return CIN_IN_USE
     * BR 13              – if Medi-Cal active → log IH18 Pending Application
     */
    @Transactional
    public java.util.Map<String, Object> selectCINWithDemographicCheck(
            String applicationId, String selectedCin,
            java.util.Map<String, Object> mediCalData, String userId) {

        boolean isNewCase = (applicationId == null || applicationId.isBlank()
                || "new-case".equalsIgnoreCase(applicationId));

        // ── STATELESS PATH: new case not yet saved in DB ───────────────────────
        // When a worker runs CIN clearance during Create Case before saving,
        // the applicationId is empty. Compare form demographics from the request
        // body (applicantLastName / applicantFirstName / applicantGender) against
        // the MEDS OM demographics (lastName / firstName / gender).
        if (isNewCase) {
            String formLastName  = normalize((String) mediCalData.getOrDefault("applicantLastName",  ""));
            String formFirstName = normalize((String) mediCalData.getOrDefault("applicantFirstName", ""));
            String formGender    = normGender((String) mediCalData.getOrDefault("applicantGender",   ""));

            String cinLastName  = normalize((String) mediCalData.getOrDefault("lastName",  ""));
            String cinFirstName = normalize((String) mediCalData.getOrDefault("firstName", ""));
            String cinGender    = normGender((String) mediCalData.getOrDefault("gender",   ""));

            boolean nameMatch   = cinLastName.equalsIgnoreCase(formLastName)
                               && cinFirstName.equalsIgnoreCase(formFirstName);
            boolean genderMatch = cinGender.equalsIgnoreCase(formGender);

            if (!nameMatch || !genderMatch) {
                log.warn("[select-cin stateless] Mismatch: CIN={} MEDS={}/{}/{} form={}/{}/{}",
                         selectedCin, cinLastName, cinFirstName, cinGender,
                         formLastName, formFirstName, formGender);
                return java.util.Map.of("result", "MISMATCH",
                                        "message", "CIN data does not match Applicant data");
            }

            boolean mediCalActive = Boolean.TRUE.equals(mediCalData.get("mediCalActive"));
            log.info("[select-cin stateless] CIN={} cleared for new case, mediCalActive={}", selectedCin, mediCalActive);
            return java.util.Map.of("result", "SUCCESS",
                                    "mediCalStatus", mediCalActive ? "ACTIVE" : "INACTIVE",
                                    "message", "CIN selected successfully");
        }

        // ── PERSISTED PATH: application already exists in DB ──────────────────
        ApplicationEntity application = getApplicationById(applicationId);

        // Scenario 6 / EM-233 or EM-234: CIN already assigned to a different record.
        //   EM-233: CIN is on an APPLICANT (application-stage recipient)
        //   EM-234: CIN is on a RECIPIENT (active case recipient)
        //   EM-202: kept for generic "duplicate application" conflicts (not CIN-in-use)
        boolean takenByOtherApp = applicationRepository.findByCin(selectedCin)
                .map(a -> !a.getId().equals(applicationId))
                .orElse(false);
        if (takenByOtherApp) {
            // Determine whether the CIN holder is an Applicant or Recipient for correct EM code
            RecipientEntity cinHolder = recipientRepository.findByCin(selectedCin).orElse(null);
            String errorCode = "EM-233";
            String msg = "This Client Index Number (CIN) is already associated with an Applicant. " +
                         "Please resolve the conflict and perform CIN clearance again.";
            if (cinHolder != null && cinHolder.getPersonType() == com.cmips.entity.RecipientEntity.PersonType.RECIPIENT) {
                errorCode = "EM-234";
                msg = "This Client Index Number (CIN) is already associated with an active Recipient. " +
                      "Please resolve the conflict and perform CIN clearance again.";
            }
            return java.util.Map.of(
                "result",    "CIN_IN_USE",
                "errorCode", errorCode,
                "message",   msg
            );
        }

        // Scenario 4/5: demographic comparison
        // Compare MEDS OM demographics against the stored recipient (if linked).
        // Also accept form demographics from request body as fallback when
        // the application has no linked recipient yet.
        RecipientEntity recipient = application.getRecipientId() != null
                ? recipientRepository.findById(application.getRecipientId()).orElse(null)
                : null;

        String cinLastName  = normalize((String) mediCalData.getOrDefault("lastName",  ""));
        String cinFirstName = normalize((String) mediCalData.getOrDefault("firstName", ""));
        String cinGender    = normGender((String) mediCalData.getOrDefault("gender",   ""));

        if (recipient != null) {
            boolean nameMatch   = cinLastName.equalsIgnoreCase(normalize(recipient.getLastName()))
                               && cinFirstName.equalsIgnoreCase(normalize(recipient.getFirstName()));
            // Normalize both sides to M/F so "Male" == "M" does not cause a false mismatch
            boolean genderMatch = cinGender.equalsIgnoreCase(normGender(recipient.getGender()));

            if (!nameMatch || !genderMatch) {
                log.warn("[select-cin] Demographic mismatch for application {}. CIN={}", applicationId, selectedCin);
                return java.util.Map.of("result", "MISMATCH",
                                        "message", "CIN data does not match Applicant data");
            }
        } else {
            // No recipient linked — fall back to form demographics from request body
            String formLastName  = normalize((String) mediCalData.getOrDefault("applicantLastName",  ""));
            String formFirstName = normalize((String) mediCalData.getOrDefault("applicantFirstName", ""));
            String formGender    = normGender((String) mediCalData.getOrDefault("applicantGender",   ""));

            if (!formLastName.isEmpty()) {
                boolean nameMatch   = cinLastName.equalsIgnoreCase(formLastName)
                                   && cinFirstName.equalsIgnoreCase(formFirstName);
                boolean genderMatch = cinGender.equalsIgnoreCase(formGender);
                if (!nameMatch || !genderMatch) {
                    log.warn("[select-cin] Demographic mismatch (form fallback) for application {}. CIN={}",
                             applicationId, selectedCin);
                    return java.util.Map.of("result", "MISMATCH",
                                            "message", "CIN data does not match Applicant data");
                }
            }
        }

        // Scenario 4 (BR 1): match — assign CIN and persist MEDS info
        boolean mediCalActive = Boolean.TRUE.equals(mediCalData.get("mediCalActive"));
        application.setCin(selectedCin);
        application.setCinClearanceStatus(CINClearanceStatus.CLEARED);
        application.setCinMatchType("EXACT");
        application.setMediCalAidCode(String.valueOf(mediCalData.getOrDefault("aidCode", "")));
        application.setMediCalStatus(mediCalActive ? "ACTIVE" : "INACTIVE");
        String effDate = String.valueOf(mediCalData.getOrDefault("effectiveDate", ""));
        if (!effDate.isBlank()) {
            try { application.setMediCalEffectiveDate(LocalDate.parse(effDate)); } catch (Exception ignored) {}
        }
        application.setUpdatedBy(userId);

        if (recipient != null) { recipient.setCin(selectedCin); recipientRepository.save(recipient); }

        if (mediCalActive) {
            // BR-13: Send IH18 Pending Application to MEDS via CMDS103C
            medsService.sendIH18PendingApplication(applicationId, selectedCin, "IHSS_APPLICATION");

            // BR-16: Send S8 to SAWS when aid code is NOT 10, 20, or 60
            String aidCode = String.valueOf(mediCalData.getOrDefault("aidCode", ""));
            if (!aidCode.isBlank() && !Set.of("10", "20", "60").contains(aidCode)) {
                sawsService.sendS8Notification(applicationId, selectedCin, aidCode);
            }
        }
        applicationRepository.save(application);
        log.info("[select-cin] CIN={} cleared for application={}, mediCalActive={}", selectedCin, applicationId, mediCalActive);
        return java.util.Map.of("result", "SUCCESS", "cin", selectedCin,
                                "mediCalStatus", mediCalActive ? "ACTIVE" : "INACTIVE",
                                "message", "CIN selected successfully");
    }

    // ==================== SAVE WITHOUT CIN (EM-176 / EM-185 / BR 9) ====================

    /**
     * Validates and handles a "Save" attempt on Create Case when CIN is blank.
     *
     * EM-176: CIN clearance was NOT performed → block the save.
     * EM-185 / BR 9: Clearance WAS performed (no match or inactive Medi-Cal)
     *               → allow save, trigger S1 IHSS Referral to SAWS.
     */
    @Transactional
    public java.util.Map<String, Object> saveWithoutCIN(String applicationId, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);
        CINClearanceStatus clearStatus = application.getCinClearanceStatus();

        // EM-176: clearance not performed at all
        if (clearStatus == null || clearStatus == CINClearanceStatus.NOT_STARTED) {
            return java.util.Map.of(
                "result", "BLOCKED", "errorCode", "EM-176",
                "message", "Client Index Number search is required.");
        }

        // EM-185 / BR-9: clearance was done, no active Medi-Cal → send S1 to SAWS via CMSD4XXB
        // Note: S1 goes to SAWS (CMSD4XXB), NOT through SCI (CMOO106A) — these are separate interfaces
        RecipientEntity recipient = application.getRecipientId() != null
                ? recipientRepository.findById(application.getRecipientId()).orElse(null)
                : null;
        sawsService.sendS1Referral(
            applicationId,
            application.getCin(),
            recipient != null ? recipient.getLastName()                                   : "",
            recipient != null ? recipient.getFirstName()                                  : "",
            recipient != null && recipient.getDateOfBirth() != null
                    ? recipient.getDateOfBirth().toString()                                : "",
            recipient != null ? recipient.getGender()                                     : "",
            application.getCountyCode()
        );
        application.setMediCalStatus("PENDING_SAWS");
        application.setUpdatedBy(userId);
        applicationRepository.save(application);
        return java.util.Map.of(
            "result", "S1_SENT", "errorCode", "EM-185",
            "message", "CIN not selected, Medi-Cal Eligibility Referral will be sent to SAWS.",
            "applicationId", applicationId);
    }

    // ==================== MEDS VERIFICATION ====================

    /**
     * Perform MEDS verification (simulated for MVP)
     */
    @Transactional
    public ApplicationEntity performMEDSVerification(String applicationId, String userId) {
        log.info("Performing MEDS verification for application: {}", applicationId);

        ApplicationEntity application = getApplicationById(applicationId);

        // Simulate MEDS query
        application.setMedsVerificationDate(LocalDateTime.now());

        // Simulate active Medi-Cal (for MVP, always return active)
        application.setMediCalAidCode("1X");
        application.setMediCalStatus("ACTIVE");
        application.setMediCalEffectiveDate(LocalDate.now().minusMonths(6));
        application.setUpdatedBy(userId);

        log.info("MEDS verification completed for application {}. Status: {}", applicationId, application.getMediCalStatus());
        return applicationRepository.save(application);
    }

    // ==================== STATUS UPDATES ====================

    /**
     * Update application status
     */
    @Transactional
    public ApplicationEntity updateStatus(String applicationId, ApplicationStatus newStatus, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(newStatus);
        application.setUpdatedBy(userId);

        log.info("Application {} status changed from {} to {}", applicationId, oldStatus, newStatus);
        return applicationRepository.save(application);
    }

    /**
     * Schedule assessment
     */
    @Transactional
    public ApplicationEntity scheduleAssessment(String applicationId, LocalDate assessmentDate, String assessmentType, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setAssessmentScheduled(true);
        application.setAssessmentDate(assessmentDate);
        application.setAssessmentType(assessmentType);
        application.setStatus(ApplicationStatus.ASSESSMENT_SCHEDULED);
        application.setUpdatedBy(userId);

        log.info("Assessment scheduled for application {} on {}", applicationId, assessmentDate);
        return applicationRepository.save(application);
    }

    /**
     * Complete assessment
     */
    @Transactional
    public ApplicationEntity completeAssessment(String applicationId, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setAssessmentCompleted(true);
        application.setAssessmentCompletionDate(LocalDate.now());
        application.setStatus(ApplicationStatus.ASSESSMENT_COMPLETED);
        application.setUpdatedBy(userId);

        log.info("Assessment completed for application {}", applicationId);
        return applicationRepository.save(application);
    }

    // ==================== APPROVE/DENY ====================

    /**
     * Approve application and create case
     */
    @Transactional
    public ApplicationEntity approveApplication(String applicationId, String userId) {
        log.info("Approving application: {}", applicationId);

        ApplicationEntity application = getApplicationById(applicationId);

        // Validate prerequisites
        validateApprovalPrerequisites(application);

        // Update application
        application.setStatus(ApplicationStatus.APPROVED);
        application.setDecisionDate(LocalDate.now());
        application.setEffectiveDate(LocalDate.now());
        application.setUpdatedBy(userId);

        // Update recipient status
        if (application.getRecipientId() != null) {
            RecipientEntity recipient = recipientRepository.findById(application.getRecipientId()).orElse(null);
            if (recipient != null) {
                recipient.setPersonType(RecipientEntity.PersonType.RECIPIENT);
                recipientRepository.save(recipient);
            }
        }

        ApplicationEntity saved = applicationRepository.save(application);
        log.info("Application {} approved", applicationId);

        return saved;
    }

    /**
     * Deny application
     */
    @Transactional
    public ApplicationEntity denyApplication(String applicationId, DenialCode denialCode, String denialReason, String userId) {
        log.info("Denying application: {}", applicationId);

        ApplicationEntity application = getApplicationById(applicationId);

        application.setStatus(ApplicationStatus.DENIED);
        application.setDecisionDate(LocalDate.now());
        application.setDenialCode(denialCode);
        application.setDenialReason(denialReason);
        application.setUpdatedBy(userId);

        ApplicationEntity saved = applicationRepository.save(application);

        // BR-12: Send IH34 to MEDS when application is denied
        if (saved.getCin() != null && !saved.getCin().isBlank()) {
            medsService.sendIH34UpdateApplicationData(applicationId, saved.getCin(), "DENIED");
        }

        log.info("Application {} denied. Reason: {}", applicationId, denialCode);
        return saved;
    }

    /**
     * Withdraw application
     */
    @Transactional
    public ApplicationEntity withdrawApplication(String applicationId, String withdrawalReason, String userId) {
        log.info("Withdrawing application: {}", applicationId);

        ApplicationEntity application = getApplicationById(applicationId);

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setWithdrawalDate(LocalDate.now());
        application.setWithdrawalReason(withdrawalReason);
        application.setUpdatedBy(userId);

        log.info("Application {} withdrawn", applicationId);
        return applicationRepository.save(application);
    }

    // ==================== DEADLINE EXTENSION ====================

    /**
     * Extend 45-day deadline
     */
    @Transactional
    public ApplicationEntity extendDeadline(String applicationId, int additionalDays, String extensionReason, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        LocalDate currentDeadline = application.getExtendedDeadlineDate() != null ?
                application.getExtendedDeadlineDate() : application.getDeadlineDate();

        application.setExtensionDate(LocalDate.now());
        application.setExtensionReason(extensionReason);
        application.setExtendedDeadlineDate(currentDeadline.plusDays(additionalDays));
        application.setUpdatedBy(userId);

        log.info("Application {} deadline extended to {}", applicationId, application.getExtendedDeadlineDate());
        return applicationRepository.save(application);
    }

    // ==================== DOCUMENTATION ====================

    /**
     * Update SOC 873 received status
     */
    @Transactional
    public ApplicationEntity updateSoc873Status(String applicationId, boolean received, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setSoc873Received(received);
        if (received) {
            application.setSoc873Date(LocalDate.now());
        }
        application.setUpdatedBy(userId);

        checkDocumentationComplete(application);

        return applicationRepository.save(application);
    }

    /**
     * Update medical certification received status
     */
    @Transactional
    public ApplicationEntity updateMedicalCertification(String applicationId, boolean received, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setMedicalCertificationReceived(received);
        if (received) {
            application.setMedicalCertificationDate(LocalDate.now());
        }
        application.setUpdatedBy(userId);

        checkDocumentationComplete(application);

        return applicationRepository.save(application);
    }

    // ==================== LINK TO CASE ====================

    /**
     * Link application to created case
     */
    @Transactional
    public ApplicationEntity linkToCase(String applicationId, Long caseId, String caseNumber, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setCaseCreated(true);
        application.setCaseId(caseId);
        application.setCaseNumber(caseNumber);
        application.setCaseCreationDate(LocalDate.now());
        application.setUpdatedBy(userId);

        log.info("Application {} linked to case {}", applicationId, caseNumber);
        return applicationRepository.save(application);
    }

    // ==================== QUERY METHODS ====================

    public ApplicationEntity getApplicationById(String applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));
    }

    public ApplicationEntity getApplicationByNumber(String applicationNumber) {
        return applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationNumber));
    }

    public List<ApplicationEntity> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<ApplicationEntity> getApplicationsByCounty(String countyCode) {
        return applicationRepository.findByCountyCode(countyCode);
    }

    public List<ApplicationEntity> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public List<ApplicationEntity> getPendingApplications() {
        return applicationRepository.findPendingApplications();
    }

    public List<ApplicationEntity> getPendingApplicationsByCounty(String countyCode) {
        return applicationRepository.findPendingApplicationsByCounty(countyCode);
    }

    public List<ApplicationEntity> getApplicationsByWorker(String workerId) {
        return applicationRepository.findByAssignedWorkerId(workerId);
    }

    public List<ApplicationEntity> getApproachingDeadline(int withinDays) {
        return applicationRepository.findApproachingDeadline(LocalDate.now().plusDays(withinDays));
    }

    public List<ApplicationEntity> getOverdueApplications() {
        return applicationRepository.findOverdueApplications();
    }

    public List<ApplicationEntity> getOverdueApplicationsByCounty(String countyCode) {
        return applicationRepository.findOverdueApplicationsByCounty(countyCode);
    }

    public List<ApplicationEntity> searchApplications(String applicationNumber,
                                                       ApplicationStatus status,
                                                       String countyCode,
                                                       ProgramType programType,
                                                       String assignedWorkerId,
                                                       String cin,
                                                       LocalDate startDate,
                                                       LocalDate endDate) {
        return applicationRepository.searchApplications(applicationNumber, status, countyCode, programType,
                assignedWorkerId, cin, startDate, endDate);
    }

    // ==================== STATISTICS ====================

    public Long countPendingByCounty(String countyCode) {
        return applicationRepository.countPendingByCounty(countyCode);
    }

    public Long countOverdueByCounty(String countyCode) {
        return applicationRepository.countOverdueByCounty(countyCode);
    }

    // ==================== DUPLICATE CHECK (BR-1, BR-4, BR-5) ====================

    /**
     * Check for duplicate persons before creating a referral or application.
     * Searches by SSN (exact) then by Soundex phonetic name match.
     * Returns list of potential duplicates for worker to review.
     */
    public java.util.List<RecipientEntity> findDuplicates(DuplicateCheckRequest req) {
        // SSN exact match — strong duplicate signal (BR-1)
        if (req.getSsn() != null && !req.getSsn().isBlank()) {
            String normalizedSsn = req.getSsn().replaceAll("[^0-9]", "");
            validateSsn(normalizedSsn);
            java.util.Optional<RecipientEntity> bySsn = recipientRepository.findBySsn(normalizedSsn);
            if (bySsn.isPresent()) {
                return java.util.List.of(bySsn.get());
            }
        }

        // Soundex name + DOB match (BR-4, BR-5)
        java.util.List<RecipientEntity> soundexMatches = java.util.Collections.emptyList();
        if (req.getLastName() != null && req.getFirstName() != null) {
            soundexMatches = recipientRepository.findBySoundex(req.getLastName(), req.getFirstName());
        }

        // If DOB also provided, filter soundex matches by DOB
        if (req.getDateOfBirth() != null && !soundexMatches.isEmpty()) {
            LocalDate reqDob = LocalDate.parse(req.getDateOfBirth());
            soundexMatches = soundexMatches.stream()
                    .filter(r -> reqDob.equals(r.getDateOfBirth()))
                    .collect(java.util.stream.Collectors.toList());
        }

        return soundexMatches;
    }

    // ==================== HELPER METHODS ====================

    private void updateRecipientToApplicant(Long recipientId, String userId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found: " + recipientId));

        recipient.setPersonType(RecipientEntity.PersonType.APPLICANT);
        recipient.setUpdatedBy(userId);
        recipientRepository.save(recipient);
    }

    private RecipientEntity createRecipientFromReferral(ReferralEntity referral, String userId) {
        RecipientEntity recipient = new RecipientEntity();

        // Parse name from potential recipient name
        String name = referral.getPotentialRecipientName();
        if (name != null && name.contains(" ")) {
            String[] parts = name.split(" ", 2);
            recipient.setFirstName(parts[0].toUpperCase());
            recipient.setLastName(parts[1].toUpperCase());
        } else if (name != null) {
            recipient.setLastName(name.toUpperCase());
            recipient.setFirstName("UNKNOWN");
        }

        recipient.setDateOfBirth(referral.getPotentialRecipientDob());
        recipient.setSsn(referral.getPotentialRecipientSsn());
        recipient.setPrimaryPhone(referral.getPotentialRecipientPhone());
        recipient.setResidenceStreetName(referral.getStreetAddress());
        recipient.setResidenceCity(referral.getCity());
        recipient.setResidenceState(referral.getState());
        recipient.setResidenceZip(referral.getZipCode());
        recipient.setCountyCode(referral.getCountyCode());
        recipient.setCountyName(referral.getCountyName());
        recipient.setSpokenLanguage(referral.getPreferredLanguage());
        recipient.setPersonType(RecipientEntity.PersonType.APPLICANT);
        recipient.setCreatedBy(userId);

        return recipientRepository.save(recipient);
    }

    /**
     * Validate SSN per DSD rules.
     * EM-237: SSN cannot start with 9
     * EM-238: SSN cannot have all same digits
     * EM-240: SSN must be exactly 9 digits
     */
    public void validateSsn(String ssn) {
        if (ssn == null || ssn.isBlank()) return; // SSN is optional at referral stage
        if (!ssn.matches("\\d{9}")) {
            throw new IllegalArgumentException("EM-240: SSN must be exactly 9 digits");
        }
        if (ssn.startsWith("9")) {
            throw new IllegalArgumentException("EM-237: SSN cannot begin with digit 9");
        }
        if (ssn.matches("(\\d)\\1{8}")) {
            throw new IllegalArgumentException("EM-238: SSN cannot consist of all identical digits");
        }
    }

    /**
     * Validate date of birth per DSD rules.
     * EM-203: DOB cannot be in the future
     * EM-204: DOB cannot be more than 120 years ago
     */
    public void validateDob(String dobString) {
        if (dobString == null || dobString.isBlank()) return;
        LocalDate dob = LocalDate.parse(dobString);
        if (dob.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("EM-203: Date of birth cannot be in the future");
        }
        if (LocalDate.now().getYear() - dob.getYear() > 120) {
            throw new IllegalArgumentException("EM-204: Date of birth cannot be more than 120 years ago");
        }
    }

    private String generateSimulatedCIN() {
        // Generate simulated CIN: 8 digits + 1 letter (EM-188 format)
        long digits = (long) (Math.random() * 100000000);
        char letter = (char) ('A' + (int) (Math.random() * 26));
        return String.format("%08d%c", digits, letter);
    }

    /** Null-safe trim for demographic string comparison */
    private String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Normalizes a gender value to SCI wire format (M / F) so that
     * "Male" and "M" are treated as equal when comparing form data to MEDS data.
     */
    private String normGender(String gender) {
        if (gender == null) return "";
        return switch (gender.trim().toUpperCase()) {
            case "MALE",   "M" -> "M";
            case "FEMALE", "F" -> "F";
            default            -> gender.trim().toUpperCase();
        };
    }

    private void validateApprovalPrerequisites(ApplicationEntity application) {
        StringBuilder errors = new StringBuilder();

        if (!"ACTIVE".equals(application.getMediCalStatus())) {
            errors.append("Medi-Cal must be active. ");
        }
        if (application.getCin() == null || application.getCin().isEmpty()) {
            errors.append("CIN clearance required. ");
        }
        if (!Boolean.TRUE.equals(application.getAssessmentCompleted())) {
            errors.append("Assessment must be completed. ");
        }

        if (errors.length() > 0) {
            throw new IllegalStateException("Cannot approve application: " + errors);
        }
    }

    private void checkDocumentationComplete(ApplicationEntity application) {
        boolean complete = Boolean.TRUE.equals(application.getSoc873Received()) &&
                           Boolean.TRUE.equals(application.getMedicalCertificationReceived());
        application.setRequiredDocsComplete(complete);

        if (!complete) {
            StringBuilder missing = new StringBuilder();
            if (!Boolean.TRUE.equals(application.getSoc873Received())) {
                missing.append("SOC 873, ");
            }
            if (!Boolean.TRUE.equals(application.getMedicalCertificationReceived())) {
                missing.append("Medical Certification, ");
            }
            application.setMissingDocuments(missing.toString().replaceAll(", $", ""));
        } else {
            application.setMissingDocuments(null);
        }
    }

    // DTO for duplicate check requests
    public static class DuplicateCheckRequest {
        private String lastName;
        private String firstName;
        private String dateOfBirth; // yyyy-MM-dd
        private String ssn;         // optional; 9 digits if provided

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public String getSsn() { return ssn; }
        public void setSsn(String ssn) { this.ssn = ssn; }
    }
}
