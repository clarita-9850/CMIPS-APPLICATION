package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.entity.NoticeOfActionEntity;
import com.cmips.entity.ServiceEligibilityEntity.AssessmentType;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service Eligibility Service
 * Implements business rules from DSD Section 21
 */
@Service
public class ServiceEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(ServiceEligibilityService.class);

    private final ServiceEligibilityRepository serviceEligibilityRepository;
    private final CaseRepository caseRepository;
    private final HealthCareCertificationRepository healthCareCertificationRepository;
    private final TaskService taskService;
    private final NoticeOfActionRepository noaRepository;
    private final NoaContentAssemblerService noaContentAssembler;

    public ServiceEligibilityService(ServiceEligibilityRepository serviceEligibilityRepository,
                                     CaseRepository caseRepository,
                                     HealthCareCertificationRepository healthCareCertificationRepository,
                                     TaskService taskService,
                                     NoticeOfActionRepository noaRepository,
                                     NoaContentAssemblerService noaContentAssembler) {
        this.serviceEligibilityRepository = serviceEligibilityRepository;
        this.caseRepository = caseRepository;
        this.healthCareCertificationRepository = healthCareCertificationRepository;
        this.taskService = taskService;
        this.noaRepository = noaRepository;
        this.noaContentAssembler = noaContentAssembler;
    }

    // ==================== ASSESSMENT MANAGEMENT ====================

    /**
     * Create a new service eligibility assessment (per BR SE 03-06)
     */
    @Transactional
    public ServiceEligibilityEntity createAssessment(Long caseId, AssessmentType assessmentType, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        ServiceEligibilityEntity assessment = ServiceEligibilityEntity.builder()
                .caseId(caseId)
                .recipientId(caseEntity.getRecipientId())
                .assessmentType(assessmentType)
                .assessmentDate(LocalDate.now())
                .status("PENDING")
                .assessorId(userId)
                .createdBy(userId)
                .build();

        // Apply date rules based on assessment type
        applyAssessmentTypeDateRules(assessment, caseId);

        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Apply date field rules based on assessment type (per BR SE 03-06, 50)
     */
    private void applyAssessmentTypeDateRules(ServiceEligibilityEntity assessment, Long caseId) {
        Optional<ServiceEligibilityEntity> lastActiveOpt = serviceEligibilityRepository.findActiveEligibilityByCaseId(caseId);

        switch (assessment.getAssessmentType()) {
            case CHANGE:
                // Per BR SE 03 - Copy dates from last active evidence
                if (lastActiveOpt.isPresent()) {
                    ServiceEligibilityEntity lastActive = lastActiveOpt.get();
                    assessment.setAuthorizationEndDate(lastActive.getAuthorizationEndDate());
                    assessment.setReassessmentDueDate(lastActive.getReassessmentDueDate());
                    assessment.setHomeVisitDate(lastActive.getHomeVisitDate());
                    // Copy waiver and reduced hours info per BR SE 20
                    assessment.setWaiverProgram(lastActive.getWaiverProgram());
                    assessment.setReinstatedHours(lastActive.getReinstatedHours());
                    assessment.setSocialWorkerCertification(lastActive.getSocialWorkerCertification());
                    assessment.setVerifiedByCaseOwnerOrSupervisor(true);
                }
                break;

            case INITIAL:
                // Per BR SE 04, 18 - All date fields blank
                // Waiver program and reduced hours reset
                assessment.setWaiverProgram(null);
                assessment.setReinstatedHours(null);
                assessment.setSocialWorkerCertification(null);
                assessment.setVerifiedByCaseOwnerOrSupervisor(false);
                break;

            case REASSESSMENT:
            case INTER_COUNTY_TRANSFER:
            case TELEHEALTH:
                // Per BR SE 05, 06, 50 - All date fields blank
                // Reassessment due date set to 1 year from home visit date if not user entered
                if (lastActiveOpt.isPresent()) {
                    ServiceEligibilityEntity lastActive = lastActiveOpt.get();
                    assessment.setWaiverProgram(lastActive.getWaiverProgram());
                }
                break;
        }
    }

    /**
     * Update assessment with home visit and calculate dates (per BR SE 04-06)
     */
    @Transactional
    public ServiceEligibilityEntity updateHomeVisitDate(Long assessmentId, LocalDate homeVisitDate, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        assessment.setHomeVisitDate(homeVisitDate);

        // Per BR SE 04, 05, 06, 50 - Set reassessment due date to 1 year from home visit if not user entered
        if (assessment.getReassessmentDueDate() == null &&
            assessment.getAssessmentType() != AssessmentType.CHANGE) {
            assessment.setDefaultReassessmentDueDate();
        }

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Calculate total assessed need (per BR SE 01)
     */
    @Transactional
    public ServiceEligibilityEntity calculateTotalAssessedNeed(Long assessmentId, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        Double totalHours = assessment.calculateTotalAssessedNeed();
        assessment.setTotalAuthorizedHoursMonthly(totalHours);
        assessment.setTotalAuthorizedHoursWeekly(totalHours / 4.33); // Monthly to weekly conversion

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Update service hours and calculate HTG indicators (per BR SE 07, 08)
     */
    @Transactional
    public ServiceEligibilityEntity updateServiceHours(Long assessmentId, ServiceHoursUpdate update, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        // Capture previous total for NOA NA_1253/NA_1254 comparison
        final Double prevHoursMonthly = assessment.getTotalAuthorizedHoursMonthly();

        // Update individual service hours — all 25 DSD Section 21 service types
        if (update.getDomesticServicesHours() != null)          assessment.setDomesticServicesHours(update.getDomesticServicesHours());
        if (update.getRelatedServicesHours() != null)           assessment.setRelatedServicesHours(update.getRelatedServicesHours());
        if (update.getPersonalCareHours() != null)              assessment.setPersonalCareHours(update.getPersonalCareHours());
        if (update.getParamedicalHours() != null)               assessment.setParamedicalHours(update.getParamedicalHours());
        if (update.getProtectiveSupervisionHours() != null)     assessment.setProtectiveSupervisionHours(update.getProtectiveSupervisionHours());
        if (update.getMealPreparationHours() != null)           assessment.setMealPreparationHours(update.getMealPreparationHours());
        if (update.getMealCleanupHours() != null)               assessment.setMealCleanupHours(update.getMealCleanupHours());
        if (update.getLaundryHours() != null)                   assessment.setLaundryHours(update.getLaundryHours());
        if (update.getShoppingErrandsHours() != null)           assessment.setShoppingErrandsHours(update.getShoppingErrandsHours());
        if (update.getAmbulationHours() != null)                assessment.setAmbulationHours(update.getAmbulationHours());
        if (update.getBathingOralHygieneHours() != null)        assessment.setBathingOralHygieneHours(update.getBathingOralHygieneHours());
        if (update.getGroomingHours() != null)                  assessment.setGroomingHours(update.getGroomingHours());
        if (update.getDressingHours() != null)                  assessment.setDressingHours(update.getDressingHours());
        if (update.getBowelBladderCareHours() != null)          assessment.setBowelBladderCareHours(update.getBowelBladderCareHours());
        if (update.getTransferRepositioningHours() != null)     assessment.setTransferRepositioningHours(update.getTransferRepositioningHours());
        if (update.getFeedingHours() != null)                   assessment.setFeedingHours(update.getFeedingHours());
        if (update.getRespirationHours() != null)               assessment.setRespirationHours(update.getRespirationHours());
        if (update.getSkinCareHours() != null)                  assessment.setSkinCareHours(update.getSkinCareHours());
        // 7 newly added service types (completing all 25)
        if (update.getMenstrualCareHours() != null)             assessment.setMenstrualCareHours(update.getMenstrualCareHours());
        if (update.getAccompanimentMedicalHours() != null)      assessment.setAccompanimentMedicalHours(update.getAccompanimentMedicalHours());
        if (update.getAccompanimentAltResourcesHours() != null) assessment.setAccompanimentAltResourcesHours(update.getAccompanimentAltResourcesHours());
        if (update.getHeavyCleaningHours() != null)             assessment.setHeavyCleaningHours(update.getHeavyCleaningHours());
        if (update.getYardHazardAbatementHours() != null)       assessment.setYardHazardAbatementHours(update.getYardHazardAbatementHours());
        if (update.getSnowRemovalHours() != null)               assessment.setSnowRemovalHours(update.getSnowRemovalHours());
        if (update.getTeachingDemoHours() != null)              assessment.setTeachingDemoHours(update.getTeachingDemoHours());

        // Calculate HTG indicators
        calculateHtgIndicators(assessment);

        // Recalculate total
        assessment.setTotalAuthorizedHoursMonthly(assessment.calculateTotalAssessedNeed());

        // Per BR SE 23 - Reset verified flag if net adjusted need changes
        if (Boolean.TRUE.equals(assessment.getVerifiedByCaseOwnerOrSupervisor())) {
            assessment.setVerifiedByCaseOwnerOrSupervisor(false);
        }

        // BR SE 49/50 — Recalculate SOC whenever authorized hours change (cost of care cap may shift)
        // Only recalculate if we have existing income data
        if (assessment.getCountableIncome() != null && assessment.getCountyIpRate() != null) {
            final double MAINTENANCE_NEED_LEVEL = 600.0;
            double rawSoc = Math.max(0.0, assessment.getCountableIncome() - MAINTENANCE_NEED_LEVEL);
            double monthlyCostOfCare = assessment.getTotalAuthorizedHoursMonthly() * assessment.getCountyIpRate();
            final double finalSoc = Math.min(rawSoc, monthlyCostOfCare);
            assessment.setIhssShareOfCost(finalSoc);
            // Cascade to CaseEntity
            caseRepository.findById(assessment.getCaseId()).ifPresent(c -> {
                c.setShareOfCostAmount(finalSoc);
                caseRepository.save(c);
            });
            log.info("[BR SE 49] SOC recalculated after hours change: assessmentId={}, newSOC={}", assessmentId, finalSoc);
        }

        // BR SE 51/52 — Cascade new authorized hours to CaseEntity
        final Double newHoursMonthly = assessment.getTotalAuthorizedHoursMonthly();
        caseRepository.findById(assessment.getCaseId()).ifPresent(c -> {
            c.setAuthorizedHoursMonthly(newHoursMonthly);
            c.setAuthorizedHoursWeekly(newHoursMonthly != null ? newHoursMonthly / 4.33 : null);
            caseRepository.save(c);
            log.info("[BR SE 51] Authorized hours cascaded to case {}: {}/month", c.getId(), newHoursMonthly);

            // Auto-generate NA 1253 (increase) or NA 1254 (decrease) for ELIGIBLE cases
            // per DSD Section 31: Change in Award triggers when reassessment modifies authorized hours
            boolean hoursChanged = prevHoursMonthly == null
                    || (newHoursMonthly != null && Math.abs(prevHoursMonthly - newHoursMonthly) > 0.01);
            if (hoursChanged && newHoursMonthly != null
                    && c.getCaseStatus() != null && "ELIGIBLE".equals(c.getCaseStatus().name())) {
                try {
                    boolean isIncrease = prevHoursMonthly == null || newHoursMonthly > prevHoursMonthly;
                    NoticeOfActionEntity.NoaType noaType = isIncrease
                            ? NoticeOfActionEntity.NoaType.NA_1253
                            : NoticeOfActionEntity.NoaType.NA_1254;
                    String hrCode = isIncrease ? "HR03" : "HR04";

                    NoticeOfActionEntity noa = new NoticeOfActionEntity();
                    noa.setCaseId(c.getId());
                    noa.setRecipientId(c.getRecipientId());
                    noa.setNoaType(noaType);
                    noa.setTriggerAction("HOURS_CHANGE");
                    noa.setTriggerReasonCode(hrCode);
                    noa.setEffectiveDate(LocalDate.now());
                    noa.setStatus(NoticeOfActionEntity.NoaStatus.PENDING);
                    noa.setCreatedBy(userId);
                    noa = noaRepository.save(noa);
                    noaContentAssembler.assemble(noa);
                    noaRepository.save(noa);
                    log.info("[NOA-AUTO] {} generated for case {} (hours: {} -> {}, categories={})",
                            noaType, c.getId(), prevHoursMonthly, newHoursMonthly, noa.getAssembledCategories());
                } catch (Exception ex) {
                    log.error("[NOA-AUTO] Failed to auto-generate hours-change NOA for case {}: {}", c.getId(), ex.getMessage());
                }
            }
        });

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * HTG (Hourly Task Guideline) reference table per DSD BR SE 07/08.
     *
     * Source: CDSS IHSS Program Requirements — HTG limits by functional index rank (1-6)
     * for each of the 4 assessable service categories.
     *
     * Rank 1 = Independent / Minimal assistance needed (low hours)
     * Rank 6 = Totally Dependent (high hours)
     *
     * Limits below are WEEKLY hours.
     */
    private static final java.util.Map<String, double[]> HTG_TABLE = new java.util.HashMap<>();
    static {
        // DOMESTIC: laundry, shopping, meal prep, meal cleanup, heavy cleaning, etc.
        // Ranks 1-6 → weekly hours guideline (per CDSS SOC 846 HTG reference)
        HTG_TABLE.put("DOMESTIC",  new double[]{0.0, 1.5, 3.0, 5.0, 7.5, 10.0, 13.0});
        // RELATED: related services, accompaniment
        HTG_TABLE.put("RELATED",   new double[]{0.0, 0.5, 1.0, 2.0, 3.0, 4.5, 6.0});
        // PERSONAL: bathing, dressing, grooming, bowel/bladder, transfer, feeding, respiration
        HTG_TABLE.put("PERSONAL",  new double[]{0.0, 2.0, 4.0, 7.0, 11.0, 16.0, 21.0});
        // PARAMEDICAL: injections, wound care, catheter, colostomy, range-of-motion
        HTG_TABLE.put("PARAMEDICAL", new double[]{0.0, 0.5, 1.0, 2.0, 3.5, 5.0, 7.0});
    }

    /**
     * Look up the HTG weekly-hours limit for a service category at a given functional rank.
     *
     * @param serviceType   "DOMESTIC" | "RELATED" | "PERSONAL" | "PARAMEDICAL"
     * @param functionalRank 1–6 per DSD assessment scale
     * @return guideline weekly hours (null if rank is invalid or type unknown)
     */
    private Double getHtgLimit(String serviceType, Integer functionalRank) {
        if (serviceType == null || functionalRank == null) return null;
        double[] limits = HTG_TABLE.get(serviceType.toUpperCase());
        if (limits == null || functionalRank < 1 || functionalRank >= limits.length) return null;
        return limits[functionalRank];
    }

    /**
     * Calculate HTG indicators (per BR SE 07, 08).
     *
     * "+" means authorized hours EXCEED the guideline (needs justification narrative).
     * "-" means authorized hours are BELOW the guideline (no action required, just noted).
     * null (blank) means hours are within the guideline band (±20%).
     */
    private void calculateHtgIndicators(ServiceEligibilityEntity assessment) {
        assessment.setHtgDomestic(
            computeHtg(assessment.getDomesticServicesHours(), "DOMESTIC", assessment.getFunctionalRankDomestic()));
        assessment.setHtgRelated(
            computeHtg(assessment.getRelatedServicesHours(), "RELATED", assessment.getFunctionalRankRelated()));
        assessment.setHtgPersonal(
            computeHtg(assessment.getPersonalCareHours(), "PERSONAL", assessment.getFunctionalRankPersonal()));
        assessment.setHtgParamedical(
            computeHtg(assessment.getParamedicalHours(), "PARAMEDICAL", assessment.getFunctionalRankParamedical()));
    }

    private String computeHtg(Double authorizedHours, String serviceType, Integer functionalRank) {
        if (authorizedHours == null || functionalRank == null) return null;
        Double limit = getHtgLimit(serviceType, functionalRank);
        if (limit == null || limit == 0.0) return null;
        if (authorizedHours > limit * 1.20) return "+";  // Exceeds guideline by >20%
        if (authorizedHours < limit * 0.80) return "-";  // Below guideline by >20%
        return null; // Within acceptable range
    }

    /**
     * Set advance pay rate (per BR SE 09)
     */
    @Transactional
    public ServiceEligibilityEntity setAdvancePayRate(Long assessmentId, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (Boolean.TRUE.equals(assessment.getAdvancePayIndicated()) && assessment.getAdvancePayRate() == null) {
            // Per BR SE 09 - Set to highest county IP rate
            assessment.setAdvancePayRate(assessment.getCountyIpRate());
        }

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Update Share of Cost (per BR SE 13-15)
     *
     * Medi-Cal Share of Cost formula (per Welfare & Institutions Code §14005.7):
     *   SOC = max(0, countableIncome - MaintenanceNeedLevel)
     *
     * Maintenance Need Levels (2024, per ACWDL 23-03):
     *   Individual (no spouse):           $600 / month
     *   Individual with ineligible spouse: $934 / month
     *
     * The SOC is capped at the actual cost of IHSS services (authorized monthly cost).
     * For MVP we use the individual MNL of $600/month.
     *
     * Per BR SE 13 — SOC cascades to CaseEntity.shareOfCostAmount.
     * Per BR SE 14 — If income evidence changes, reset verification flag.
     * Per BR SE 15 — SOC is recalculated whenever countable income changes.
     */
    @Transactional
    public ServiceEligibilityEntity updateShareOfCost(Long assessmentId, Double netIncome, Double countableIncome, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        Double previousCountableIncome = assessment.getCountableIncome();
        assessment.setNetIncome(netIncome);
        assessment.setCountableIncome(countableIncome);

        if (countableIncome != null) {
            // Medi-Cal Maintenance Need Level (individual, 2024)
            final double MAINTENANCE_NEED_LEVEL = 600.0;

            // SOC = countableIncome - MNL (never negative)
            double rawSoc = Math.max(0.0, countableIncome - MAINTENANCE_NEED_LEVEL);

            // Cap SOC at actual IHSS monthly cost if we have authorized hours and a wage rate
            // (Per BR SE 15: SOC cannot exceed the cost of care)
            final double finalSoc;
            if (assessment.getTotalAuthorizedHoursMonthly() != null
                    && assessment.getCountyIpRate() != null
                    && assessment.getCountyIpRate() > 0) {
                double monthlyCostOfCare = assessment.getTotalAuthorizedHoursMonthly() * assessment.getCountyIpRate();
                finalSoc = Math.min(rawSoc, monthlyCostOfCare);
            } else {
                finalSoc = rawSoc;
            }

            assessment.setIhssShareOfCost(finalSoc);

            // BR SE 13 — Cascade SOC to CaseEntity; auto-generate NA 1256 if SOC changed
            caseRepository.findById(assessment.getCaseId()).ifPresent(caseEntity -> {
                Double prevSoc = caseEntity.getShareOfCostAmount();
                caseEntity.setShareOfCostAmount(finalSoc);
                caseRepository.save(caseEntity);
                log.info("[BR SE 13] SOC cascaded to case {}: ${}", caseEntity.getId(), finalSoc);

                // Auto-generate NA 1256 (Share of Cost) when SOC amount changes on an ELIGIBLE case
                boolean socChanged = prevSoc == null || Math.abs(prevSoc - finalSoc) > 0.01;
                if (socChanged && caseEntity.getCaseStatus() != null
                        && "ELIGIBLE".equals(caseEntity.getCaseStatus().name())) {
                    try {
                        String socReasonCode = prevSoc == null ? "NEW"
                                : finalSoc > prevSoc ? "INCREASE"
                                : finalSoc < prevSoc ? "DECREASE" : "NEW";
                        NoticeOfActionEntity noa = new NoticeOfActionEntity();
                        noa.setCaseId(caseEntity.getId());
                        noa.setRecipientId(caseEntity.getRecipientId());
                        noa.setNoaType(NoticeOfActionEntity.NoaType.NA_1256);
                        noa.setTriggerAction("SOC_CHANGE");
                        noa.setTriggerReasonCode(socReasonCode);
                        noa.setEffectiveDate(java.time.LocalDate.now());
                        noa.setStatus(NoticeOfActionEntity.NoaStatus.PENDING);
                        noa.setCreatedBy(userId);
                        noa = noaRepository.save(noa);
                        noaContentAssembler.assemble(noa);
                        noaRepository.save(noa);
                        log.info("[NOA-AUTO] NA_1256 generated for case {} (SOC changed: {} -> {}, categories={})",
                                caseEntity.getId(), prevSoc, finalSoc, noa.getAssembledCategories());
                    } catch (Exception ex) {
                        log.error("[NOA-AUTO] Failed to auto-generate NA_1256 for case {}: {}", caseEntity.getId(), ex.getMessage());
                    }
                }
            });

            // BR SE 14 — If countable income changed, reset verification flag
            boolean incomeChanged = previousCountableIncome == null
                    || Math.abs(previousCountableIncome - countableIncome) > 0.01;
            if (incomeChanged && Boolean.TRUE.equals(assessment.getVerifiedByCaseOwnerOrSupervisor())) {
                assessment.setVerifiedByCaseOwnerOrSupervisor(false);
                log.info("[BR SE 14] Verification flag reset for assessment {} due to income change", assessmentId);
            }

            log.info("[BR SE 15] SOC recalculated: assessmentId={}, countableIncome={}, MNL={}, SOC={}",
                    assessmentId, countableIncome, MAINTENANCE_NEED_LEVEL, finalSoc);
        }

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Update waiver program and reset verification (per BR SE 22)
     */
    @Transactional
    public ServiceEligibilityEntity updateWaiverProgram(Long assessmentId, String waiverProgram, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        String oldWaiverProgram = assessment.getWaiverProgram();
        assessment.setWaiverProgram(waiverProgram);

        // Per BR SE 22 - Reset verified flag if waiver program changed
        boolean waiverChanged = (oldWaiverProgram == null && waiverProgram != null)
                || (oldWaiverProgram != null && !oldWaiverProgram.equals(waiverProgram));
        if (waiverChanged && Boolean.TRUE.equals(assessment.getVerifiedByCaseOwnerOrSupervisor())) {
            assessment.setVerifiedByCaseOwnerOrSupervisor(false);
            log.info("[BR SE 22] Verification flag reset for assessment {} due to waiver program change: {} -> {}",
                    assessmentId, oldWaiverProgram, waiverProgram);
        }

        // BR SE 53-55 — Waiver program change may affect authorized hours cap
        // (WPCS waiver allows hours beyond the standard IHSS cap)
        // Reset WPCS-specific hours flags so the case owner must re-verify
        if (waiverChanged) {
            assessment.setReinstatedHours(null); // cleared so worker re-enters waiver-specific hours
            log.info("[BR SE 53] Reinstated hours cleared for assessment {} after waiver program change", assessmentId);

            // Auto-generate NA 1257 (Multi-Program) when case enrolls in PCSP/CFCO/WPCS waiver
            // per DSD Section 31: Multi-program NOA required when program type changes
            boolean isMultiProgram = waiverProgram != null
                    && (waiverProgram.contains("PCSP") || waiverProgram.contains("CFCO")
                        || waiverProgram.contains("WPCS") || waiverProgram.contains("IFO"));
            if (isMultiProgram) {
                caseRepository.findById(assessment.getCaseId()).ifPresent(c -> {
                    if (c.getCaseStatus() != null && "ELIGIBLE".equals(c.getCaseStatus().name())) {
                        try {
                            NoticeOfActionEntity noa = new NoticeOfActionEntity();
                            noa.setCaseId(c.getId());
                            noa.setRecipientId(c.getRecipientId());
                            noa.setNoaType(NoticeOfActionEntity.NoaType.NA_1257);
                            noa.setTriggerAction("WAIVER_PROGRAM_CHANGE");
                            noa.setTriggerReasonCode(waiverProgram);
                            noa.setEffectiveDate(LocalDate.now());
                            noa.setStatus(NoticeOfActionEntity.NoaStatus.PENDING);
                            noa.setCreatedBy(userId);
                            noa = noaRepository.save(noa);
                            noaContentAssembler.assemble(noa);
                            noaRepository.save(noa);
                            log.info("[NOA-AUTO] NA_1257 generated for case {} (waiver: {} -> {}, categories={})",
                                    c.getId(), oldWaiverProgram, waiverProgram, noa.getAssembledCategories());
                        } catch (Exception ex) {
                            log.error("[NOA-AUTO] Failed to auto-generate NA_1257 for case {}: {}", c.getId(), ex.getMessage());
                        }
                    }
                });
            }
        }

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Submit assessment for approval
     */
    @Transactional
    public ServiceEligibilityEntity submitForApproval(Long assessmentId, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        assessment.setStatus("PENDING_APPROVAL");
        assessment.setUpdatedBy(userId);

        // Create approval task
        Task task = Task.builder()
                .title("Approve Assessment for Case " + assessment.getCaseId())
                .description("Service eligibility assessment pending approval")
                .workQueue("ASSESSMENT_APPROVAL")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(5).atStartOfDay())
                .build();
        taskService.createTask(task);

        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Approve assessment and update case authorization
     */
    @Transactional
    public ServiceEligibilityEntity approveAssessment(Long assessmentId, String approverId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        // Supersede any previous active assessment
        Optional<ServiceEligibilityEntity> previousActive = serviceEligibilityRepository.findActiveEligibilityByCaseId(assessment.getCaseId());
        if (previousActive.isPresent() && !previousActive.get().getId().equals(assessmentId)) {
            ServiceEligibilityEntity prev = previousActive.get();
            prev.setStatus("SUPERSEDED");
            serviceEligibilityRepository.save(prev);
        }

        assessment.setStatus("ACTIVE");
        assessment.setApprovedById(approverId);
        assessment.setApprovalDate(LocalDate.now());

        // Update case with new authorization info
        CaseEntity caseEntity = caseRepository.findById(assessment.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found"));
        caseEntity.setAuthorizedHoursMonthly(assessment.getTotalAuthorizedHoursMonthly());
        caseEntity.setAuthorizedHoursWeekly(assessment.getTotalAuthorizedHoursWeekly());
        caseEntity.setAuthorizationStartDate(assessment.getAuthorizationStartDate());
        caseEntity.setAuthorizationEndDate(assessment.getAuthorizationEndDate());
        caseEntity.setReassessmentDueDate(assessment.getReassessmentDueDate());
        caseEntity.setLastAssessmentDate(assessment.getAssessmentDate());
        caseEntity.setAssessmentType(assessment.getAssessmentType().name());
        caseRepository.save(caseEntity);

        return serviceEligibilityRepository.save(assessment);
    }

    // ==================== HEALTH CARE CERTIFICATION ====================

    /**
     * Create health care certification (per BR SE 28-50)
     */
    @Transactional
    public HealthCareCertificationEntity createHealthCareCertification(Long caseId, String method, String formType,
                                                                         String printOption, String language, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        HealthCareCertificationEntity cert = HealthCareCertificationEntity.builder()
                .caseId(caseId)
                .recipientId(caseEntity.getRecipientId())
                .certificationMethod(method)
                .formType(formType)
                .printOption(printOption)
                .language(language)
                .printDate(LocalDate.now())
                .status("ACTIVE")
                .createdBy(userId)
                .build();

        // Per BR SE 28 - Calculate initial due date (45 days from print date)
        cert.calculateInitialDueDate();

        if ("SEND_ESP".equals(printOption)) {
            cert.setSentToEsp(true);
            cert.setElectronicFormDueDate(cert.getDueDate());
        }

        cert = healthCareCertificationRepository.save(cert);

        // Create task triggers
        createHealthCareCertificationTasks(cert);

        return cert;
    }

    /**
     * Create task triggers for health care certification (per BR SE 28)
     */
    private void createHealthCareCertificationTasks(HealthCareCertificationEntity cert) {
        // First task: 10 business days before due date
        LocalDate firstTaskDate = cert.getDueDate().minusDays(14); // ~10 business days
        Task firstTask = Task.builder()
                .title("Health Care Certification Due Soon - Case " + cert.getCaseId())
                .description("SOC 873 due in 10 business days")
                .workQueue("HEALTH_CARE_CERTIFICATION")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(firstTaskDate.atStartOfDay())
                .build();
        taskService.createTask(firstTask);

        // Second task: 1 business day before due date
        LocalDate secondTaskDate = cert.getDueDate().minusDays(1);
        Task secondTask = Task.builder()
                .title("Health Care Certification Due Tomorrow - Case " + cert.getCaseId())
                .description("SOC 873 due tomorrow - take action if not received")
                .workQueue("HEALTH_CARE_CERTIFICATION")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.HIGH)
                .dueDate(secondTaskDate.atStartOfDay())
                .build();
        taskService.createTask(secondTask);
    }

    /**
     * Record documentation received (per BR SE 32)
     */
    @Transactional
    public HealthCareCertificationEntity recordDocumentationReceived(Long certId, String certificationType,
                                                                      LocalDate receivedDate, String userId) {
        HealthCareCertificationEntity cert = healthCareCertificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        cert.setCertificationType(certificationType);
        cert.setDocumentationReceivedDate(receivedDate);
        cert.complete();
        cert.setUpdatedBy(userId);

        return healthCareCertificationRepository.save(cert);
    }

    /**
     * Request good cause extension (per BR SE 31)
     */
    @Transactional
    public HealthCareCertificationEntity requestGoodCauseExtension(Long certId, LocalDate extensionDate, String userId) {
        HealthCareCertificationEntity cert = healthCareCertificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        // Extension must be requested before initial due date
        if (cert.getDueDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot request extension after due date has passed");
        }

        cert.setGoodCauseExtensionRequested(true);
        cert.setGoodCauseExtensionDate(extensionDate);
        cert.calculateGoodCauseExtensionDueDate();
        cert.setUpdatedBy(userId);

        return healthCareCertificationRepository.save(cert);
    }

    /**
     * Grant exception (per BR SE 30)
     */
    @Transactional
    public HealthCareCertificationEntity grantException(Long certId, String exceptionReason,
                                                         LocalDate exceptionDate, String userId) {
        HealthCareCertificationEntity cert = healthCareCertificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        cert.setExceptionGranted(true);
        cert.setExceptionReason(exceptionReason);
        cert.setExceptionGrantedDate(exceptionDate);
        cert.calculateDueDateFromException();
        cert.setUpdatedBy(userId);

        return healthCareCertificationRepository.save(cert);
    }

    /**
     * Inactivate health care certification (per BR SE 34)
     */
    @Transactional
    public HealthCareCertificationEntity inactivateCertification(Long certId, String userId) {
        HealthCareCertificationEntity cert = healthCareCertificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        // Check if case has been authorized - cannot inactivate after authorization
        CaseEntity caseEntity = caseRepository.findById(cert.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (caseEntity.getCaseStatus() == CaseEntity.CaseStatus.ELIGIBLE ||
            caseEntity.getCaseStatus() == CaseEntity.CaseStatus.DENIED) {
            throw new RuntimeException("Cannot inactivate certification after case authorization");
        }

        cert.inactivate(userId);
        return healthCareCertificationRepository.save(cert);
    }

    // ==================== QUERIES ====================

    /**
     * Get evidence history (per BR SE 25)
     */
    public List<ServiceEligibilityEntity> getEvidenceHistory(Long caseId) {
        return serviceEligibilityRepository.findEvidenceHistoryByCaseId(caseId);
    }

    /**
     * Get active eligibility for case
     */
    public Optional<ServiceEligibilityEntity> getActiveEligibility(Long caseId) {
        return serviceEligibilityRepository.findActiveEligibilityByCaseId(caseId);
    }

    /**
     * Get pending approvals
     */
    public List<ServiceEligibilityEntity> getPendingApprovals() {
        return serviceEligibilityRepository.findPendingApprovals();
    }

    // ==================== DTOs ====================

    public static class ServiceHoursUpdate {
        private Double domesticServicesHours;
        private Double relatedServicesHours;
        private Double personalCareHours;
        private Double paramedicalHours;
        private Double protectiveSupervisionHours;
        private Double mealPreparationHours;
        private Double mealCleanupHours;
        private Double laundryHours;
        private Double shoppingErrandsHours;
        private Double ambulationHours;
        private Double bathingOralHygieneHours;
        private Double groomingHours;
        private Double dressingHours;
        private Double bowelBladderCareHours;
        private Double transferRepositioningHours;
        private Double feedingHours;
        private Double respirationHours;
        private Double skinCareHours;

        public ServiceHoursUpdate() {}

        public Double getDomesticServicesHours() { return domesticServicesHours; }
        public void setDomesticServicesHours(Double domesticServicesHours) { this.domesticServicesHours = domesticServicesHours; }

        public Double getRelatedServicesHours() { return relatedServicesHours; }
        public void setRelatedServicesHours(Double relatedServicesHours) { this.relatedServicesHours = relatedServicesHours; }

        public Double getPersonalCareHours() { return personalCareHours; }
        public void setPersonalCareHours(Double personalCareHours) { this.personalCareHours = personalCareHours; }

        public Double getParamedicalHours() { return paramedicalHours; }
        public void setParamedicalHours(Double paramedicalHours) { this.paramedicalHours = paramedicalHours; }

        public Double getProtectiveSupervisionHours() { return protectiveSupervisionHours; }
        public void setProtectiveSupervisionHours(Double protectiveSupervisionHours) { this.protectiveSupervisionHours = protectiveSupervisionHours; }

        public Double getMealPreparationHours() { return mealPreparationHours; }
        public void setMealPreparationHours(Double mealPreparationHours) { this.mealPreparationHours = mealPreparationHours; }

        public Double getMealCleanupHours() { return mealCleanupHours; }
        public void setMealCleanupHours(Double mealCleanupHours) { this.mealCleanupHours = mealCleanupHours; }

        public Double getLaundryHours() { return laundryHours; }
        public void setLaundryHours(Double laundryHours) { this.laundryHours = laundryHours; }

        public Double getShoppingErrandsHours() { return shoppingErrandsHours; }
        public void setShoppingErrandsHours(Double shoppingErrandsHours) { this.shoppingErrandsHours = shoppingErrandsHours; }

        public Double getAmbulationHours() { return ambulationHours; }
        public void setAmbulationHours(Double ambulationHours) { this.ambulationHours = ambulationHours; }

        public Double getBathingOralHygieneHours() { return bathingOralHygieneHours; }
        public void setBathingOralHygieneHours(Double bathingOralHygieneHours) { this.bathingOralHygieneHours = bathingOralHygieneHours; }

        public Double getGroomingHours() { return groomingHours; }
        public void setGroomingHours(Double groomingHours) { this.groomingHours = groomingHours; }

        public Double getDressingHours() { return dressingHours; }
        public void setDressingHours(Double dressingHours) { this.dressingHours = dressingHours; }

        public Double getBowelBladderCareHours() { return bowelBladderCareHours; }
        public void setBowelBladderCareHours(Double bowelBladderCareHours) { this.bowelBladderCareHours = bowelBladderCareHours; }

        public Double getTransferRepositioningHours() { return transferRepositioningHours; }
        public void setTransferRepositioningHours(Double transferRepositioningHours) { this.transferRepositioningHours = transferRepositioningHours; }

        public Double getFeedingHours() { return feedingHours; }
        public void setFeedingHours(Double feedingHours) { this.feedingHours = feedingHours; }

        public Double getRespirationHours() { return respirationHours; }
        public void setRespirationHours(Double respirationHours) { this.respirationHours = respirationHours; }

        public Double getSkinCareHours() { return skinCareHours; }
        public void setSkinCareHours(Double skinCareHours) { this.skinCareHours = skinCareHours; }

        // 7 additional service types completing all 25 per DSD Section 21
        private Double menstrualCareHours;
        private Double accompanimentMedicalHours;
        private Double accompanimentAltResourcesHours;
        private Double heavyCleaningHours;
        private Double yardHazardAbatementHours;
        private Double snowRemovalHours;
        private Double teachingDemoHours;

        public Double getMenstrualCareHours() { return menstrualCareHours; }
        public void setMenstrualCareHours(Double v) { this.menstrualCareHours = v; }
        public Double getAccompanimentMedicalHours() { return accompanimentMedicalHours; }
        public void setAccompanimentMedicalHours(Double v) { this.accompanimentMedicalHours = v; }
        public Double getAccompanimentAltResourcesHours() { return accompanimentAltResourcesHours; }
        public void setAccompanimentAltResourcesHours(Double v) { this.accompanimentAltResourcesHours = v; }
        public Double getHeavyCleaningHours() { return heavyCleaningHours; }
        public void setHeavyCleaningHours(Double v) { this.heavyCleaningHours = v; }
        public Double getYardHazardAbatementHours() { return yardHazardAbatementHours; }
        public void setYardHazardAbatementHours(Double v) { this.yardHazardAbatementHours = v; }
        public Double getSnowRemovalHours() { return snowRemovalHours; }
        public void setSnowRemovalHours(Double v) { this.snowRemovalHours = v; }
        public Double getTeachingDemoHours() { return teachingDemoHours; }
        public void setTeachingDemoHours(Double v) { this.teachingDemoHours = v; }
    }
}
