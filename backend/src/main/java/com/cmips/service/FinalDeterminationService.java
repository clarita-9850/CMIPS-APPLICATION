package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Final Determination Service — DSD Section 22
 *
 * Implements the complete authorization workflow:
 *   1. Check Eligibility (preview)
 *   2. Create IHSS Authorization from approved assessment
 *   3. Assign Modes of Service (IP/CC/HM split)
 *   4. SOC Spend Down calculation (4-week work-week model)
 *   5. Case Service Month initialization
 *
 * Business rules:
 *   - Functional Index Score = sum of 14 ranks / 14
 *   - Unmet Need = Total Assessed Need - (Refused + Alternate + Voluntary)
 *   - Auth to Purchase = Unmet Need + Adjustments (legislative mandate, etc.)
 *   - SOC Spend Down splits IP hours across 4 work weeks:
 *       Regular hours first (up to 40:00/wk × county pay rate)
 *       Then OT hours (× 1.5 rate)
 *   - Mode of Service: IP hours default unless CC or HM assigned
 *   - CaseServiceMonth tracks monthly remaining hours per mode
 */
@Service
public class FinalDeterminationService {

    private static final Logger log = LoggerFactory.getLogger(FinalDeterminationService.class);

    private final ServiceEligibilityRepository assessmentRepository;
    private final IHSSAuthorizationRepository authorizationRepository;
    private final IHSSAuthorizedServiceRepository authorizedServiceRepository;
    private final ModeOfServiceRepository modeOfServiceRepository;
    private final ModeOfServiceSnapshotRepository mosSnapshotRepository;
    private final CaseServiceMonthRepository caseServiceMonthRepository;
    private final CaseParticipantServiceMonthRepository participantMonthRepository;
    private final CaseSOCHoursRepository socHoursRepository;
    private final SOCSpendDownTriggerRepository socTriggerRepository;
    private final ServiceTypeEvidenceRepository serviceTypeEvidenceRepository;
    private final FunctionalIndexEvidenceRepository functionalIndexRepository;
    private final CaseRepository caseRepository;
    private final NoticeOfActionRepository noaRepository;
    private final NoaContentAssemblerService noaContentAssembler;
    private final TaskService taskService;

    public FinalDeterminationService(
            ServiceEligibilityRepository assessmentRepository,
            IHSSAuthorizationRepository authorizationRepository,
            IHSSAuthorizedServiceRepository authorizedServiceRepository,
            ModeOfServiceRepository modeOfServiceRepository,
            ModeOfServiceSnapshotRepository mosSnapshotRepository,
            CaseServiceMonthRepository caseServiceMonthRepository,
            CaseParticipantServiceMonthRepository participantMonthRepository,
            CaseSOCHoursRepository socHoursRepository,
            SOCSpendDownTriggerRepository socTriggerRepository,
            ServiceTypeEvidenceRepository serviceTypeEvidenceRepository,
            FunctionalIndexEvidenceRepository functionalIndexRepository,
            CaseRepository caseRepository,
            NoticeOfActionRepository noaRepository,
            NoaContentAssemblerService noaContentAssembler,
            TaskService taskService) {
        this.assessmentRepository = assessmentRepository;
        this.authorizationRepository = authorizationRepository;
        this.authorizedServiceRepository = authorizedServiceRepository;
        this.modeOfServiceRepository = modeOfServiceRepository;
        this.mosSnapshotRepository = mosSnapshotRepository;
        this.caseServiceMonthRepository = caseServiceMonthRepository;
        this.participantMonthRepository = participantMonthRepository;
        this.socHoursRepository = socHoursRepository;
        this.socTriggerRepository = socTriggerRepository;
        this.serviceTypeEvidenceRepository = serviceTypeEvidenceRepository;
        this.functionalIndexRepository = functionalIndexRepository;
        this.caseRepository = caseRepository;
        this.noaRepository = noaRepository;
        this.noaContentAssembler = noaContentAssembler;
        this.taskService = taskService;
    }

    // ==================== 1. CREATE AUTHORIZATION ====================

    /**
     * Create IHSS Authorization from an approved assessment.
     * Per DSD Section 22: after supervisor approval, system generates the authorization record
     * with calculated functional index score, unmet need, and auth-to-purchase hours.
     */
    @Transactional
    public IHSSAuthorizationEntity createAuthorization(Long assessmentId, String userId) {
        ServiceEligibilityEntity assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found: " + assessmentId));

        if (!"ACTIVE".equals(assessment.getStatus())) {
            throw new RuntimeException("Assessment must be ACTIVE (approved) to create authorization. Current status: " + assessment.getStatus());
        }

        Long caseId = assessment.getCaseId();

        // Supersede any previous active authorization for this case
        List<IHSSAuthorizationEntity> previousActive = authorizationRepository
                .findByCaseIdAndActiveAuthorizationInd(caseId, true);
        for (IHSSAuthorizationEntity prev : previousActive) {
            prev.setActiveAuthorizationInd(false);
            prev.setStatusCode("SUPERSEDED");
            prev.setUpdatedBy(userId);
            authorizationRepository.save(prev);
            log.info("[FD] Superseded previous authorization {} for case {}", prev.getId(), caseId);
        }

        // Calculate functional index score (average of 14 ranks)
        BigDecimal fiScore = calculateFunctionalIndexScore(assessment);

        // Get service type evidence to calculate totals
        List<ServiceTypeEvidenceEntity> serviceTypes = serviceTypeEvidenceRepository
                .findByAssessmentEvidenceId(assessmentId);

        // Calculate total minutes from service type evidence (or fall back to flat entity fields)
        int totalAssessedNeedMin = 0;
        int totalUnmetNeedMin = 0;
        int totalAuthToPurchaseMin = 0;

        if (!serviceTypes.isEmpty()) {
            for (ServiceTypeEvidenceEntity st : serviceTypes) {
                int assessed = st.getAssessedNeedMin() != null ? st.getAssessedNeedMin() : 0;
                int refused = st.getRefusedServicesMin() != null ? st.getRefusedServicesMin() : 0;
                int alternate = st.getAlternateResourcesMin() != null ? st.getAlternateResourcesMin() : 0;
                int voluntary = st.getVoluntaryServicesMin() != null ? st.getVoluntaryServicesMin() : 0;
                int adjustments = st.getAdjustmentsMin() != null ? st.getAdjustmentsMin() : 0;
                int netAdj = st.getNetAdjNeedMin() != null ? st.getNetAdjNeedMin() : 0;

                totalAssessedNeedMin += assessed;
                int unmet = Math.max(0, assessed - refused - alternate - voluntary);
                totalUnmetNeedMin += unmet;
                totalAuthToPurchaseMin += (netAdj > 0 ? netAdj : unmet + adjustments);
            }
        } else {
            // Fall back to flat hours on the assessment entity (converted to minutes)
            double totalHours = assessment.calculateTotalAssessedNeed();
            totalAssessedNeedMin = (int) Math.round(totalHours * 60);
            totalUnmetNeedMin = totalAssessedNeedMin;
            totalAuthToPurchaseMin = totalAssessedNeedMin;
        }

        // Determine severely impaired indicator (FI score >= 4.0)
        boolean severelyImpaired = fiScore.compareTo(new BigDecimal("4.0")) >= 0;

        // Calculate SOC
        double countableIncome = assessment.getCountableIncome() != null ? assessment.getCountableIncome() : 0.0;
        double rawSoc = Math.max(0.0, countableIncome - 600.0); // MNL = $600
        double ipRate = assessment.getCountyIpRate() != null ? assessment.getCountyIpRate() : 0.0;
        double monthlyCost = (totalAuthToPurchaseMin / 60.0) * ipRate;
        double calculatedSoc = ipRate > 0 ? Math.min(rawSoc, monthlyCost) : rawSoc;

        // Build authorization
        IHSSAuthorizationEntity auth = new IHSSAuthorizationEntity();
        auth.setAssessmentId(assessmentId);
        auth.setCaseId(caseId);
        auth.setFunctionalIndexScore(fiScore);
        auth.setTotalAssessedNeedMin(totalAssessedNeedMin);
        auth.setTotalUnmetNeedMin(totalUnmetNeedMin);
        auth.setAuthToPurchaseMin(totalAuthToPurchaseMin);
        auth.setSeverelyImpairedInd(severelyImpaired);
        auth.setCalculatedSOC(BigDecimal.valueOf(calculatedSoc).setScale(2, RoundingMode.HALF_UP));
        auth.setFundingAidCode(determineFundingAidCode(assessment));
        auth.setCompareCost(BigDecimal.valueOf(monthlyCost).setScale(2, RoundingMode.HALF_UP));
        auth.setAdvancePayInd(Boolean.TRUE.equals(assessment.getAdvancePayIndicated()));
        auth.setRestaurantMealsInd(Boolean.TRUE.equals(assessment.getRestaurantMealsAllowed()));
        auth.setParentOfMinorChildInd(Boolean.TRUE.equals(assessment.getParentOfMinorProvider()));
        auth.setSpouseProviderInd(Boolean.TRUE.equals(assessment.getSpouseProvider()));
        auth.setActiveAuthorizationInd(true);
        auth.setAuthStartDate(assessment.getAuthorizationStartDate());
        auth.setAuthEndDate(assessment.getAuthorizationEndDate());
        auth.setStatusCode("ACTIVE");
        auth.setCreatedBy(userId);

        // Weekly distribution (equal across 7 days by default)
        int weeklyMin = totalAuthToPurchaseMin / 4; // ~monthly / 4 weeks
        int dailyMin = weeklyMin / 7;
        auth.setWeeklyAuthMonday(dailyMin);
        auth.setWeeklyAuthTuesday(dailyMin);
        auth.setWeeklyAuthWednesday(dailyMin);
        auth.setWeeklyAuthThursday(dailyMin);
        auth.setWeeklyAuthFriday(dailyMin);
        auth.setWeeklyAuthSaturday(dailyMin);
        auth.setWeeklyAuthSunday(dailyMin);

        // Monthly OT max: per FLSA, OT starts after 40hrs/wk = 2400 min/wk
        // Monthly OT max = (weeklyAuth - 2400) * 4 if weekly > 2400
        int monthlyOTMax = weeklyMin > 2400 ? (weeklyMin - 2400) * 4 : 0;
        auth.setCaseMonthlyOTMax(monthlyOTMax);

        auth = authorizationRepository.save(auth);
        log.info("[FD] Created authorization {} for case {} (assessedNeed={}min, authToPurchase={}min, SOC=${})",
                auth.getId(), caseId, totalAssessedNeedMin, totalAuthToPurchaseMin, calculatedSoc);

        // Create authorized service records per service type
        createAuthorizedServices(auth, serviceTypes);

        // Update case entity
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found: " + caseId));
        caseEntity.setAuthorizedHoursMonthly(totalAuthToPurchaseMin / 60.0);
        caseEntity.setAuthorizedHoursWeekly(weeklyMin / 60.0);
        caseEntity.setShareOfCostAmount(calculatedSoc);
        caseRepository.save(caseEntity);

        return auth;
    }

    /**
     * Calculate functional index score as average of 14 ranks.
     * Per DSD: FI Score = (sum of all 14 functional ranks) / 14
     */
    private BigDecimal calculateFunctionalIndexScore(ServiceEligibilityEntity assessment) {
        int sum = 0;
        int count = 0;
        int[] ranks = {
            safe(assessment.getFiHousework()), safe(assessment.getFiLaundry()),
            safe(assessment.getFiShopping()), safe(assessment.getFiMealPrep()),
            safe(assessment.getFiAmbulation()), safe(assessment.getFiBathing()),
            safe(assessment.getFiDressing()), safe(assessment.getFiBowelBladder()),
            safe(assessment.getFiTransfer()), safe(assessment.getFiFeeding()),
            safe(assessment.getFiRespiration()), safe(assessment.getFiMemory()),
            safe(assessment.getFiOrientation()), safe(assessment.getFiJudgment())
        };
        for (int r : ranks) {
            if (r > 0) {
                sum += r;
                count++;
            }
        }
        if (count == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private int safe(Integer val) { return val != null ? val : 0; }

    private String determineFundingAidCode(ServiceEligibilityEntity assessment) {
        String waiver = assessment.getWaiverProgram();
        if (waiver != null && !waiver.isBlank()) {
            return switch (waiver.toUpperCase()) {
                case "WPCS" -> "W1";
                case "CFCO" -> "C1";
                case "IPO"  -> "I1";
                case "PCSP" -> "P1";
                default -> "A1"; // Standard IHSS
            };
        }
        return "A1"; // Standard IHSS
    }

    private void createAuthorizedServices(IHSSAuthorizationEntity auth,
                                           List<ServiceTypeEvidenceEntity> serviceTypes) {
        for (ServiceTypeEvidenceEntity st : serviceTypes) {
            int assessed = st.getAssessedNeedMin() != null ? st.getAssessedNeedMin() : 0;
            int refused = st.getRefusedServicesMin() != null ? st.getRefusedServicesMin() : 0;
            int alternate = st.getAlternateResourcesMin() != null ? st.getAlternateResourcesMin() : 0;
            int voluntary = st.getVoluntaryServicesMin() != null ? st.getVoluntaryServicesMin() : 0;
            int adjustments = st.getAdjustmentsMin() != null ? st.getAdjustmentsMin() : 0;
            int netAdj = st.getNetAdjNeedMin() != null ? st.getNetAdjNeedMin() : 0;

            int unmet = Math.max(0, assessed - refused - alternate - voluntary);
            int authToPurchase = netAdj > 0 ? netAdj : unmet + adjustments;

            if (authToPurchase > 0) {
                IHSSAuthorizedServiceEntity svc = new IHSSAuthorizedServiceEntity();
                svc.setIhssAuthorizationId(auth.getId());
                svc.setServiceTypeEvidenceId(st.getId());
                svc.setServiceTypeCode(st.getServiceTypeCode());
                svc.setUnmetNeedMin(unmet);
                svc.setAuthToPurchaseMin(authToPurchase);
                svc.setCreatedBy(auth.getCreatedBy());
                authorizedServiceRepository.save(svc);
            }
        }
    }

    // ==================== 2. ASSIGN MODES OF SERVICE ====================

    /**
     * Assign modes of service for an authorization.
     * Per DSD Section 22: Split authorized hours into IP, CC, and HM modes.
     *
     * @param ipMinutes  Individual Provider minutes
     * @param ccMinutes  County Contractor minutes
     * @param hmMinutes  Homemaker minutes
     */
    @Transactional
    public ModeOfServiceEntity assignModesOfService(Long authorizationId,
                                                     int ipMinutes, int ccMinutes, int hmMinutes,
                                                     LocalDate startDate, LocalDate endDate,
                                                     String userId) {
        IHSSAuthorizationEntity auth = authorizationRepository.findById(authorizationId)
                .orElseThrow(() -> new RuntimeException("Authorization not found: " + authorizationId));

        int totalRequested = ipMinutes + ccMinutes + hmMinutes;
        int authToPurchase = auth.getAuthToPurchaseMin() != null ? auth.getAuthToPurchaseMin() : 0;

        if (totalRequested > authToPurchase) {
            throw new RuntimeException("Total mode hours (" + totalRequested +
                    " min) cannot exceed auth to purchase (" + authToPurchase + " min)");
        }

        // Snapshot existing mode of service if any
        List<ModeOfServiceEntity> existingModes = modeOfServiceRepository
                .findByCaseIdAndStatusCode(auth.getCaseId(), "ACTIVE");
        for (ModeOfServiceEntity existing : existingModes) {
            snapshotModeOfService(existing, "Superseded by new MOS assignment");
            existing.setStatusCode("SUPERSEDED");
            existing.setUpdatedBy(userId);
            modeOfServiceRepository.save(existing);
        }

        // Calculate case cost: IP hours × rate + CC hours × CC rate + HM hours × HM rate
        // For simplicity, use county IP rate for all modes
        CaseEntity caseEntity = caseRepository.findById(auth.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found"));
        ServiceEligibilityEntity assessment = assessmentRepository.findById(auth.getAssessmentId())
                .orElseThrow(() -> new RuntimeException("Assessment not found"));
        double ipRate = assessment.getCountyIpRate() != null ? assessment.getCountyIpRate() : 0.0;
        double caseCost = ((ipMinutes + ccMinutes + hmMinutes) / 60.0) * ipRate;

        ModeOfServiceEntity mos = new ModeOfServiceEntity();
        mos.setIhssAuthorizationId(authorizationId);
        mos.setCaseId(auth.getCaseId());
        mos.setIpHoursMin(ipMinutes);
        mos.setCcHoursMin(ccMinutes);
        mos.setHmHoursMin(hmMinutes);
        mos.setModeOfServiceStartDate(startDate);
        mos.setModeOfServiceEndDate(endDate);
        mos.setStatusCode("ACTIVE");
        mos.setCaseCost(BigDecimal.valueOf(caseCost).setScale(2, RoundingMode.HALF_UP));
        mos.setCreatedBy(userId);

        mos = modeOfServiceRepository.save(mos);
        log.info("[FD] Assigned MOS for auth {}: IP={}min, CC={}min, HM={}min, cost=${}",
                authorizationId, ipMinutes, ccMinutes, hmMinutes, caseCost);

        // Initialize case service month for the start month
        initializeCaseServiceMonth(auth, mos, userId);

        return mos;
    }

    private void snapshotModeOfService(ModeOfServiceEntity mos, String reason) {
        ModeOfServiceSnapshotEntity snapshot = new ModeOfServiceSnapshotEntity();
        snapshot.setModeOfServiceId(mos.getId());
        snapshot.setCaseId(mos.getCaseId());
        snapshot.setIpHoursMin(mos.getIpHoursMin());
        snapshot.setCcHoursMin(mos.getCcHoursMin());
        snapshot.setHmHoursMin(mos.getHmHoursMin());
        snapshot.setModeOfServiceStartDate(mos.getModeOfServiceStartDate());
        snapshot.setModeOfServiceEndDate(mos.getModeOfServiceEndDate());
        snapshot.setStatusCode(mos.getStatusCode());
        snapshot.setCaseCost(mos.getCaseCost());
        snapshot.setSnapshotDate(LocalDateTime.now());
        snapshot.setSnapshotReason(reason);
        snapshot.setCreatedBy("system");
        mosSnapshotRepository.save(snapshot);
    }

    // ==================== 3. CASE SERVICE MONTH ====================

    /**
     * Initialize or update a CaseServiceMonth record for the authorization period.
     * This tracks remaining hours per mode for the service month.
     */
    private void initializeCaseServiceMonth(IHSSAuthorizationEntity auth,
                                             ModeOfServiceEntity mos, String userId) {
        LocalDate serviceMonth = mos.getModeOfServiceStartDate().withDayOfMonth(1);

        Optional<CaseServiceMonthEntity> existingOpt = caseServiceMonthRepository
                .findByCaseIdAndServiceMonth(auth.getCaseId(), serviceMonth);

        CaseServiceMonthEntity csm;
        if (existingOpt.isPresent()) {
            csm = existingOpt.get();
        } else {
            csm = new CaseServiceMonthEntity();
            csm.setCaseId(auth.getCaseId());
            csm.setServiceMonth(serviceMonth);
            csm.setCreatedBy(userId);
        }

        int authToPurchase = auth.getAuthToPurchaseMin() != null ? auth.getAuthToPurchaseMin() : 0;
        csm.setAuthToPurchaseMin(authToPurchase);
        csm.setIpRemainingMin(mos.getIpHoursMin() != null ? mos.getIpHoursMin() : 0);
        csm.setCcRemainingMin(mos.getCcHoursMin() != null ? mos.getCcHoursMin() : 0);
        csm.setHmRemainingMin(mos.getHmHoursMin() != null ? mos.getHmHoursMin() : 0);
        csm.setAuthToPurchaseRemainMin(authToPurchase);
        csm.setStatusCode("OPEN");

        // Weekly cap: auth to purchase / 4 weeks
        int weeklyCap = authToPurchase / 4;
        csm.setWeeklyCapMin(weeklyCap);

        // OT max: minutes over 40hrs/wk (2400 min/wk)
        int otMax = weeklyCap > 2400 ? (weeklyCap - 2400) * 4 : 0;
        csm.setOtMaxMin(otMax);

        csm.setUpdatedBy(userId);
        caseServiceMonthRepository.save(csm);

        log.info("[FD] Initialized CaseServiceMonth for case {} month {}: authTP={}min, IP={}min, CC={}min, HM={}min",
                auth.getCaseId(), serviceMonth, authToPurchase,
                csm.getIpRemainingMin(), csm.getCcRemainingMin(), csm.getHmRemainingMin());
    }

    // ==================== 4. SOC SPEND DOWN ====================

    /**
     * Calculate SOC Spend Down for a case and service month.
     * Per DSD Section 22 (pages 9-10):
     *   1. Split IP hours into 4 work weeks
     *   2. For each work week: regular hours first (up to 40:00/wk)
     *   3. Multiply regular hours × county pay rate → spend down amount
     *   4. Then OT hours (remaining after 40:00) × 1.5 × county pay rate
     *   5. SOC spend down = min(SOC amount, calculated spend down)
     *   6. Available hours = hours that can be paid after SOC obligation met
     */
    @Transactional
    public SOCSpendDownTriggerEntity calculateSOCSpendDown(Long caseId, LocalDate serviceMonth, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found: " + caseId));

        ServiceEligibilityEntity assessment = assessmentRepository.findActiveEligibilityByCaseId(caseId)
                .orElseThrow(() -> new RuntimeException("No active assessment for case: " + caseId));

        List<IHSSAuthorizationEntity> activeAuths = authorizationRepository
                .findByCaseIdAndActiveAuthorizationInd(caseId, true);
        if (activeAuths.isEmpty()) {
            throw new RuntimeException("No active authorization for case: " + caseId);
        }
        IHSSAuthorizationEntity auth = activeAuths.get(0);

        double ipRate = assessment.getCountyIpRate() != null ? assessment.getCountyIpRate() : 0.0;
        double socAmount = assessment.getIhssShareOfCost() != null ? assessment.getIhssShareOfCost() : 0.0;
        int authHoursMin = auth.getAuthToPurchaseMin() != null ? auth.getAuthToPurchaseMin() : 0;

        if (socAmount <= 0) {
            log.info("[FD-SOC] No SOC obligation for case {} month {} — skipping spend down", caseId, serviceMonth);
            return null;
        }

        // Split into 4 work weeks
        int weeklyMin = authHoursMin / 4;
        int regularWeeklyMax = 2400; // 40 hours in minutes

        // Regular hours spend down
        int totalRegularMin = Math.min(weeklyMin, regularWeeklyMax) * 4;
        double regularSpendDown = (totalRegularMin / 60.0) * ipRate;

        // OT hours spend down (1.5x rate)
        int totalOTMin = weeklyMin > regularWeeklyMax ? (weeklyMin - regularWeeklyMax) * 4 : 0;
        double otSpendDown = (totalOTMin / 60.0) * ipRate * 1.5;

        // Total IHSS spend down
        double ihssSpendDown = regularSpendDown + otSpendDown;

        // SOC hours = hours consumed by SOC obligation
        // socAmount / ipRate = hours of SOC in regular time
        double socHoursDecimal = ipRate > 0 ? socAmount / ipRate : 0;
        int socHoursMin = (int) Math.round(socHoursDecimal * 60);

        // Available hours = auth hours minus SOC hours
        int availableMin = Math.max(0, authHoursMin - socHoursMin);

        // OT SOC calculation
        double ihssOTSpendDown = 0;
        int socOTMin = 0;
        int availableOTMin = totalOTMin;
        if (socAmount > regularSpendDown && totalOTMin > 0) {
            double remainingSOC = socAmount - regularSpendDown;
            double otRate = ipRate * 1.5;
            double otSocHours = otRate > 0 ? remainingSOC / otRate : 0;
            socOTMin = (int) Math.round(otSocHours * 60);
            ihssOTSpendDown = Math.min(remainingSOC, otSpendDown);
            availableOTMin = Math.max(0, totalOTMin - socOTMin);
        }

        // Get IP/HM/CC breakdown from mode of service
        List<ModeOfServiceEntity> modes = modeOfServiceRepository.findByCaseIdAndStatusCode(caseId, "ACTIVE");
        int ipMins = 0, hmMins = 0, ccMins = 0;
        if (!modes.isEmpty()) {
            ModeOfServiceEntity mos = modes.get(0);
            ipMins = mos.getIpHoursMin() != null ? mos.getIpHoursMin() : 0;
            hmMins = mos.getHmHoursMin() != null ? mos.getHmHoursMin() : 0;
            ccMins = mos.getCcHoursMin() != null ? mos.getCcHoursMin() : 0;
        }

        // Create trigger record
        SOCSpendDownTriggerEntity trigger = new SOCSpendDownTriggerEntity();
        trigger.setCaseId(caseId);
        trigger.setServiceMonth(serviceMonth.withDayOfMonth(1));
        trigger.setIhssAuthorizationId(auth.getId());
        trigger.setApInd(Boolean.TRUE.equals(auth.getAdvancePayInd()));
        trigger.setIhssAuthHours(authHoursMin);
        trigger.setIhssSpendDownAmt(BigDecimal.valueOf(ihssSpendDown).setScale(2, RoundingMode.HALF_UP));
        trigger.setSocAmt(BigDecimal.valueOf(socAmount).setScale(2, RoundingMode.HALF_UP));
        trigger.setSocHours(socHoursMin);
        trigger.setAvailableHours(availableMin);
        trigger.setIhssOTHours(totalOTMin);
        trigger.setIhssOTSpendDownAmt(BigDecimal.valueOf(ihssOTSpendDown).setScale(2, RoundingMode.HALF_UP));
        trigger.setSocOTAmt(BigDecimal.valueOf(socAmount > regularSpendDown ? socAmount - regularSpendDown : 0)
                .setScale(2, RoundingMode.HALF_UP));
        trigger.setSocOTHours(socOTMin);
        trigger.setAvailableOTHours(availableOTMin);
        trigger.setTriggerDate(LocalDate.now());
        trigger.setTriggerType("INITIAL");
        trigger.setStatusCode("PROCESSED");
        trigger.setRecordStatus("ACTIVE");
        trigger.setIpMins(ipMins);
        trigger.setHmMins(hmMins);
        trigger.setCcMins(ccMins);
        trigger.setCreatedBy(userId);

        trigger = socTriggerRepository.save(trigger);

        // Create/update CaseSOCHours
        updateCaseSOCHours(caseId, serviceMonth, auth, trigger, userId);

        log.info("[FD-SOC] Calculated spend down for case {} month {}: SOC=${}, spendDown=${}, available={}min, OT={}min",
                caseId, serviceMonth, socAmount, ihssSpendDown, availableMin, availableOTMin);

        return trigger;
    }

    private void updateCaseSOCHours(Long caseId, LocalDate serviceMonth,
                                     IHSSAuthorizationEntity auth,
                                     SOCSpendDownTriggerEntity trigger, String userId) {
        LocalDate month = serviceMonth.withDayOfMonth(1);
        Optional<CaseSOCHoursEntity> existingOpt = socHoursRepository.findByCaseIdAndServiceMonth(caseId, month);

        CaseSOCHoursEntity socHours;
        if (existingOpt.isPresent()) {
            socHours = existingOpt.get();
        } else {
            socHours = new CaseSOCHoursEntity();
            socHours.setCaseId(caseId);
            socHours.setServiceMonth(month);
            socHours.setCreatedBy(userId);
        }

        socHours.setIhssAuthHours(trigger.getIhssAuthHours());
        socHours.setCalculatedSpendDownAmt(trigger.getIhssSpendDownAmt());
        socHours.setSocAuthAmt(trigger.getSocAmt());
        socHours.setSocHours(trigger.getSocHours());
        socHours.setAuthToPay(trigger.getAvailableHours());
        socHours.setApInd(trigger.getApInd());
        socHours.setAvailableHours(trigger.getAvailableHours());
        socHours.setIpMins(trigger.getIpMins());
        socHours.setHmMins(trigger.getHmMins());
        socHours.setCcMins(trigger.getCcMins());
        socHours.setUpdatedBy(userId);

        socHoursRepository.save(socHours);
    }

    // ==================== 5. QUERIES ====================

    public Optional<IHSSAuthorizationEntity> getActiveAuthorization(Long caseId) {
        List<IHSSAuthorizationEntity> active = authorizationRepository
                .findByCaseIdAndActiveAuthorizationInd(caseId, true);
        return active.isEmpty() ? Optional.empty() : Optional.of(active.get(0));
    }

    public List<IHSSAuthorizationEntity> getAuthorizationHistory(Long caseId) {
        return authorizationRepository.findByCaseIdOrderByCreatedAtDesc(caseId);
    }

    public List<IHSSAuthorizedServiceEntity> getAuthorizedServices(Long authorizationId) {
        return authorizedServiceRepository.findByIhssAuthorizationId(authorizationId);
    }

    public List<ModeOfServiceEntity> getModesOfService(Long caseId) {
        return modeOfServiceRepository.findByCaseIdOrderByModeOfServiceStartDateDesc(caseId);
    }

    public Optional<ModeOfServiceEntity> getActiveModeOfService(Long caseId) {
        List<ModeOfServiceEntity> active = modeOfServiceRepository
                .findByCaseIdAndStatusCode(caseId, "ACTIVE");
        return active.isEmpty() ? Optional.empty() : Optional.of(active.get(0));
    }

    public List<ModeOfServiceSnapshotEntity> getModeOfServiceHistory(Long modeOfServiceId) {
        return mosSnapshotRepository.findByModeOfServiceIdOrderBySnapshotDateDesc(modeOfServiceId);
    }

    public List<CaseServiceMonthEntity> getCaseServiceMonths(Long caseId) {
        return caseServiceMonthRepository.findByCaseIdOrderByServiceMonthDesc(caseId);
    }

    public Optional<CaseServiceMonthEntity> getCaseServiceMonth(Long caseId, LocalDate serviceMonth) {
        return caseServiceMonthRepository.findByCaseIdAndServiceMonth(caseId, serviceMonth.withDayOfMonth(1));
    }

    public List<CaseSOCHoursEntity> getSOCHours(Long caseId) {
        return socHoursRepository.findByCaseIdOrderByServiceMonthDesc(caseId);
    }

    public List<SOCSpendDownTriggerEntity> getSpendDownTriggers(Long caseId) {
        return socTriggerRepository.findByCaseIdOrderByTriggerDateDesc(caseId);
    }

    public List<SOCSpendDownTriggerEntity> getPendingSpendDownTriggers() {
        return socTriggerRepository.findByStatusCode("PENDING");
    }

    // ==================== 6. FULL DETERMINATION WORKFLOW ====================

    /**
     * Execute the full final determination workflow:
     * 1. Create authorization from approved assessment
     * 2. Assign default mode of service (100% IP)
     * 3. Calculate SOC spend down if applicable
     * 4. Generate NOA
     *
     * This is the main entry point called after supervisor approval.
     */
    @Transactional
    public Map<String, Object> executeFinalDetermination(Long assessmentId, String userId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // Step 1: Create authorization
        IHSSAuthorizationEntity auth = createAuthorization(assessmentId, userId);
        result.put("authorization", auth);

        // Step 2: Default mode of service = 100% IP
        int authToPurchase = auth.getAuthToPurchaseMin() != null ? auth.getAuthToPurchaseMin() : 0;
        LocalDate startDate = auth.getAuthStartDate() != null ? auth.getAuthStartDate() : LocalDate.now();
        LocalDate endDate = auth.getAuthEndDate() != null ? auth.getAuthEndDate() : startDate.plusYears(1);

        ModeOfServiceEntity mos = assignModesOfService(auth.getId(),
                authToPurchase, 0, 0, // 100% IP by default
                startDate, endDate, userId);
        result.put("modeOfService", mos);

        // Step 3: SOC spend down (only if SOC > 0)
        BigDecimal soc = auth.getCalculatedSOC();
        if (soc != null && soc.compareTo(BigDecimal.ZERO) > 0) {
            SOCSpendDownTriggerEntity trigger = calculateSOCSpendDown(
                    auth.getCaseId(), startDate, userId);
            result.put("socSpendDown", trigger);
        }

        // Step 4: Generate authorization NOA (NA_1250 = initial grant, NA_1253 = increase)
        try {
            ServiceEligibilityEntity assessment = assessmentRepository.findById(assessmentId).orElse(null);
            if (assessment != null) {
                NoticeOfActionEntity.NoaType noaType = assessment.getAssessmentType() ==
                        ServiceEligibilityEntity.AssessmentType.INITIAL
                        ? NoticeOfActionEntity.NoaType.NA_1250
                        : NoticeOfActionEntity.NoaType.NA_1253;

                NoticeOfActionEntity noa = new NoticeOfActionEntity();
                noa.setCaseId(auth.getCaseId());
                noa.setRecipientId(assessment.getRecipientId());
                noa.setNoaType(noaType);
                noa.setTriggerAction("FINAL_DETERMINATION");
                noa.setTriggerReasonCode("AUTH_CREATED");
                noa.setEffectiveDate(startDate);
                noa.setStatus(NoticeOfActionEntity.NoaStatus.PENDING);
                noa.setCreatedBy(userId);
                noa = noaRepository.save(noa);
                noaContentAssembler.assemble(noa);
                noaRepository.save(noa);
                result.put("noaGenerated", true);
                result.put("noaType", noaType.name());
                log.info("[FD] Generated {} for case {} after final determination", noaType, auth.getCaseId());
            }
        } catch (Exception ex) {
            log.error("[FD] Failed to generate NOA for case {}: {}", auth.getCaseId(), ex.getMessage());
            result.put("noaGenerated", false);
            result.put("noaError", ex.getMessage());
        }

        result.put("status", "COMPLETED");
        result.put("message", "Final determination completed successfully");

        return result;
    }

    /**
     * Recalculate SOC spend down for all open service months of a case.
     * Called when SOC amount changes (income change, MNL change, etc.)
     */
    @Transactional
    public List<SOCSpendDownTriggerEntity> recalculateSOCSpendDown(Long caseId, String userId) {
        List<CaseServiceMonthEntity> openMonths = caseServiceMonthRepository
                .findByCaseIdAndStatusCode(caseId, "OPEN");

        List<SOCSpendDownTriggerEntity> triggers = new ArrayList<>();
        for (CaseServiceMonthEntity csm : openMonths) {
            // Void previous triggers for this month
            List<SOCSpendDownTriggerEntity> existing = socTriggerRepository
                    .findByCaseIdAndServiceMonth(caseId, csm.getServiceMonth());
            for (SOCSpendDownTriggerEntity prev : existing) {
                if ("ACTIVE".equals(prev.getRecordStatus())) {
                    prev.setRecordStatus("VOID");
                    prev.setUpdatedBy(userId);
                    socTriggerRepository.save(prev);
                }
            }

            SOCSpendDownTriggerEntity trigger = calculateSOCSpendDown(caseId, csm.getServiceMonth(), userId);
            if (trigger != null) {
                trigger.setTriggerType("RECALC");
                socTriggerRepository.save(trigger);
                triggers.add(trigger);
            }
        }
        return triggers;
    }
}
