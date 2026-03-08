package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.*;
import com.cmips.entity.AuthorizationSegmentEntity.SegmentStatus;
import com.cmips.entity.AuthorizationSegmentEntity.ModeOfService;
import com.cmips.entity.CountyPayRateEntity.RateType;
import com.cmips.repository.*;
import com.cmips.service.PayrollIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Evidence Hub Controller — DSD Sections 21 & 22
 *
 * Covers all remaining gaps:
 *   Household Evidence      (BR SE 26-27)
 *   Program Evidence        (program-level eligibility factors)
 *   Disaster Preparedness   (DisasterPreparedness.* permission group)
 *   County Pay Rates        (CaseProvider.findDefaultCountyPayRateByDateAndCountyCode)
 *   Authorization Segments  (date-bounded authorization periods)
 *   Authorization Summary   (Authorization.readSummary / printSOC293)
 *   SOC Spend-Down 4-Week   (BR SE 49 + Section 22 calculation)
 *   PRO0927A Trigger        (send authorized hours to Advantage Payroll)
 */
@RestController
@CrossOrigin(origins = "*")
public class EvidenceHubController {

    private static final Logger log = LoggerFactory.getLogger(EvidenceHubController.class);

    private final HouseholdEvidenceRepository householdRepo;
    private final ProgramEvidenceRepository programEvidenceRepo;
    private final DisasterPreparednessContactRepository disasterRepo;
    private final CountyPayRateRepository countyPayRateRepo;
    private final AuthorizationSegmentRepository authSegmentRepo;
    private final ServiceEligibilityRepository eligibilityRepo;
    private final PayrollIntegrationService payrollIntegrationService;

    public EvidenceHubController(HouseholdEvidenceRepository householdRepo,
                                  ProgramEvidenceRepository programEvidenceRepo,
                                  DisasterPreparednessContactRepository disasterRepo,
                                  CountyPayRateRepository countyPayRateRepo,
                                  AuthorizationSegmentRepository authSegmentRepo,
                                  ServiceEligibilityRepository eligibilityRepo,
                                  PayrollIntegrationService payrollIntegrationService) {
        this.householdRepo = householdRepo;
        this.programEvidenceRepo = programEvidenceRepo;
        this.disasterRepo = disasterRepo;
        this.countyPayRateRepo = countyPayRateRepo;
        this.authSegmentRepo = authSegmentRepo;
        this.eligibilityRepo = eligibilityRepo;
        this.payrollIntegrationService = payrollIntegrationService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. HOUSEHOLD EVIDENCE  (BR SE 26-27)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/api/cases/{caseId}/household-evidence")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<HouseholdEvidenceEntity>> getHouseholdEvidence(@PathVariable Long caseId) {
        return ResponseEntity.ok(householdRepo.findByCaseIdOrderByCreatedAtDesc(caseId));
    }

    @PostMapping("/api/cases/{caseId}/household-evidence")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createHouseholdEvidence(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            HouseholdEvidenceEntity e = new HouseholdEvidenceEntity();
            e.setCaseId(caseId);
            e.setCreatedBy(userId);
            e.setUpdatedBy(userId);
            applyHouseholdFields(e, req);
            return ResponseEntity.ok(householdRepo.save(e));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/api/cases/household-evidence/{id}")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> updateHouseholdEvidence(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        HouseholdEvidenceEntity e = householdRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Household evidence not found: " + id));
        e.setUpdatedBy(userId);
        applyHouseholdFields(e, req);
        return ResponseEntity.ok(householdRepo.save(e));
    }

    private void applyHouseholdFields(HouseholdEvidenceEntity e, Map<String, Object> req) {
        if (req.containsKey("stoveInd"))
            e.setStoveInd(Boolean.TRUE.equals(req.get("stoveInd")));
        if (req.containsKey("refrigeratorInd"))
            e.setRefrigeratorInd(Boolean.TRUE.equals(req.get("refrigeratorInd")));
        if (req.containsKey("washerInd"))
            e.setWasherInd(Boolean.TRUE.equals(req.get("washerInd")));
        if (req.containsKey("dryerInd"))
            e.setDryerInd(Boolean.TRUE.equals(req.get("dryerInd")));
        if (req.containsKey("yardInd"))
            e.setYardInd(Boolean.TRUE.equals(req.get("yardInd")));
        if (req.containsKey("livingArrangeCode"))
            e.setLivingArrangeCode((String) req.get("livingArrangeCode"));
        if (req.containsKey("residenceTypeCode"))
            e.setResidenceTypeCode((String) req.get("residenceTypeCode"));
        if (req.containsKey("roomsPrivate") && req.get("roomsPrivate") != null)
            e.setRoomsPrivate(((Number) req.get("roomsPrivate")).intValue());
        if (req.containsKey("roomsShared") && req.get("roomsShared") != null)
            e.setRoomsShared(((Number) req.get("roomsShared")).intValue());
        if (req.containsKey("roomsUnused") && req.get("roomsUnused") != null)
            e.setRoomsUnused(((Number) req.get("roomsUnused")).intValue());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. PROGRAM EVIDENCE
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/api/cases/{caseId}/program-evidence")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<ProgramEvidenceEntity>> getProgramEvidence(@PathVariable Long caseId) {
        return ResponseEntity.ok(programEvidenceRepo.findByCaseIdOrderByCreatedAtDesc(caseId));
    }

    @PostMapping("/api/cases/{caseId}/program-evidence")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createProgramEvidence(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            ProgramEvidenceEntity e = new ProgramEvidenceEntity();
            e.setCaseId(caseId);
            e.setCreatedBy(userId);
            e.setUpdatedBy(userId);
            applyProgramEvidenceFields(e, req);
            return ResponseEntity.ok(programEvidenceRepo.save(e));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/api/cases/program-evidence/{id}")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> updateProgramEvidence(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        ProgramEvidenceEntity e = programEvidenceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program evidence not found: " + id));
        e.setUpdatedBy(userId);
        applyProgramEvidenceFields(e, req);
        return ResponseEntity.ok(programEvidenceRepo.save(e));
    }

    private void applyProgramEvidenceFields(ProgramEvidenceEntity e, Map<String, Object> req) {
        if (req.containsKey("programType") && req.get("programType") != null)
            e.setProgramType(ProgramEvidenceEntity.ProgramType.valueOf((String) req.get("programType")));
        if (req.containsKey("fundingCategory") && req.get("fundingCategory") != null)
            e.setFundingCategory(ProgramEvidenceEntity.FundingCategory.valueOf((String) req.get("fundingCategory")));
        if (req.containsKey("mediCalAidCode"))
            e.setMediCalAidCode((String) req.get("mediCalAidCode"));
        if (req.containsKey("mediCalActive"))
            e.setMediCalActive(Boolean.TRUE.equals(req.get("mediCalActive")));
        if (req.containsKey("functionalNeedMet"))
            e.setFunctionalNeedMet(Boolean.TRUE.equals(req.get("functionalNeedMet")));
        if (req.containsKey("financialEligibilityMet"))
            e.setFinancialEligibilityMet(Boolean.TRUE.equals(req.get("financialEligibilityMet")));
        if (req.containsKey("eligibilityReason"))
            e.setEligibilityReason((String) req.get("eligibilityReason"));
        if (req.containsKey("effectiveDate") && req.get("effectiveDate") != null)
            e.setEffectiveDate(LocalDate.parse((String) req.get("effectiveDate")));
        if (req.containsKey("endDate") && req.get("endDate") != null)
            e.setEndDate(LocalDate.parse((String) req.get("endDate")));
        if (req.containsKey("status"))
            e.setStatus((String) req.get("status"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. DISASTER PREPAREDNESS CONTACTS
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/api/cases/{caseId}/disaster-contacts")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<DisasterPreparednessContactEntity>> getDisasterContacts(@PathVariable Long caseId) {
        return ResponseEntity.ok(disasterRepo.findByCaseIdOrderByCreatedAtDesc(caseId));
    }

    @PostMapping("/api/cases/{caseId}/disaster-contacts")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createDisasterContact(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            if (req.get("contactName") == null || ((String) req.get("contactName")).isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Contact name is required."));
            DisasterPreparednessContactEntity e = new DisasterPreparednessContactEntity();
            e.setCaseId(caseId);
            e.setCreatedBy(userId);
            e.setUpdatedBy(userId);
            applyDisasterContactFields(e, req);
            return ResponseEntity.ok(disasterRepo.save(e));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/api/cases/disaster-contacts/{id}")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> updateDisasterContact(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        DisasterPreparednessContactEntity e = disasterRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Disaster contact not found: " + id));
        e.setUpdatedBy(userId);
        applyDisasterContactFields(e, req);
        return ResponseEntity.ok(disasterRepo.save(e));
    }

    @PutMapping("/api/cases/disaster-contacts/{id}/inactivate")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> inactivateDisasterContact(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        DisasterPreparednessContactEntity e = disasterRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Disaster contact not found: " + id));
        e.setStatus("INACTIVE");
        e.setUpdatedBy(userId);
        return ResponseEntity.ok(disasterRepo.save(e));
    }

    private void applyDisasterContactFields(DisasterPreparednessContactEntity e, Map<String, Object> req) {
        if (req.containsKey("contactName")) e.setContactName((String) req.get("contactName"));
        if (req.containsKey("relationship") && req.get("relationship") != null)
            e.setRelationship(DisasterPreparednessContactEntity.Relationship.valueOf((String) req.get("relationship")));
        if (req.containsKey("relationshipOther")) e.setRelationshipOther((String) req.get("relationshipOther"));
        if (req.containsKey("primaryPhone")) e.setPrimaryPhone((String) req.get("primaryPhone"));
        if (req.containsKey("alternatePhone")) e.setAlternatePhone((String) req.get("alternatePhone"));
        if (req.containsKey("streetAddress")) e.setStreetAddress((String) req.get("streetAddress"));
        if (req.containsKey("city")) e.setCity((String) req.get("city"));
        if (req.containsKey("state")) e.setState((String) req.get("state"));
        if (req.containsKey("zip")) e.setZip((String) req.get("zip"));
        if (req.containsKey("canEvacuateIndependently"))
            e.setCanEvacuateIndependently(Boolean.TRUE.equals(req.get("canEvacuateIndependently")));
        if (req.containsKey("requiresSpecializedTransport"))
            e.setRequiresSpecializedTransport(Boolean.TRUE.equals(req.get("requiresSpecializedTransport")));
        if (req.containsKey("specialNeedsNotes")) e.setSpecialNeedsNotes((String) req.get("specialNeedsNotes"));
        if (req.containsKey("status")) e.setStatus((String) req.get("status"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. COUNTY PAY RATES — moved to dedicated CountyPayRateController
    // ─────────────────────────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────────────────────────
    // 5. AUTHORIZATION SEGMENTS
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/api/cases/{caseId}/authorization-segments")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<AuthorizationSegmentEntity>> getAuthSegments(@PathVariable Long caseId) {
        return ResponseEntity.ok(authSegmentRepo.findByCaseIdOrderBySegmentStartDateDesc(caseId));
    }

    @PostMapping("/api/cases/{caseId}/authorization-segments")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createAuthSegment(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            // Supersede any existing ACTIVE segment
            authSegmentRepo.findByCaseIdAndStatusOrderBySegmentStartDateDesc(caseId, SegmentStatus.ACTIVE)
                    .forEach(seg -> {
                        seg.setStatus(SegmentStatus.SUPERSEDED);
                        seg.setUpdatedBy(userId);
                        authSegmentRepo.save(seg);
                    });
            AuthorizationSegmentEntity seg = new AuthorizationSegmentEntity();
            seg.setCaseId(caseId);
            seg.setCreatedBy(userId);
            seg.setUpdatedBy(userId);
            applySegmentFields(seg, req);
            seg.setStatus(SegmentStatus.ACTIVE);
            return ResponseEntity.ok(authSegmentRepo.save(seg));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/api/cases/authorization-segments/{id}/inactivate")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> inactivateAuthSegment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        AuthorizationSegmentEntity seg = authSegmentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Segment not found: " + id));
        seg.setStatus(SegmentStatus.INACTIVE);
        seg.setUpdatedBy(userId);
        return ResponseEntity.ok(authSegmentRepo.save(seg));
    }

    private void applySegmentFields(AuthorizationSegmentEntity seg, Map<String, Object> req) {
        if (req.containsKey("assessmentId") && req.get("assessmentId") != null)
            seg.setAssessmentId(((Number) req.get("assessmentId")).longValue());
        if (req.containsKey("segmentStartDate") && req.get("segmentStartDate") != null)
            seg.setSegmentStartDate(LocalDate.parse((String) req.get("segmentStartDate")));
        if (req.containsKey("segmentEndDate") && req.get("segmentEndDate") != null)
            seg.setSegmentEndDate(LocalDate.parse((String) req.get("segmentEndDate")));
        if (req.containsKey("authorizedHoursMonthly") && req.get("authorizedHoursMonthly") != null)
            seg.setAuthorizedHoursMonthly(((Number) req.get("authorizedHoursMonthly")).doubleValue());
        if (req.containsKey("authorizedHoursWeekly") && req.get("authorizedHoursWeekly") != null)
            seg.setAuthorizedHoursWeekly(((Number) req.get("authorizedHoursWeekly")).doubleValue());
        if (req.containsKey("modeOfService") && req.get("modeOfService") != null)
            seg.setModeOfService(ModeOfService.valueOf((String) req.get("modeOfService")));
        if (req.containsKey("fundingSource")) seg.setFundingSource((String) req.get("fundingSource"));
        if (req.containsKey("countyIpRate") && req.get("countyIpRate") != null)
            seg.setCountyIpRate(((Number) req.get("countyIpRate")).doubleValue());
        if (req.containsKey("shareOfCostAmount") && req.get("shareOfCostAmount") != null)
            seg.setShareOfCostAmount(((Number) req.get("shareOfCostAmount")).doubleValue());
        if (req.containsKey("segmentReason")) seg.setSegmentReason((String) req.get("segmentReason"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. AUTHORIZATION SUMMARY  (Authorization.readSummary / printSOC293)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/api/cases/{caseId}/authorization-summary")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getAuthorizationSummary(@PathVariable Long caseId) {
        // Use the most recent ACTIVE assessment
        List<ServiceEligibilityEntity> assessments = eligibilityRepo.findByCaseId(caseId);
        ServiceEligibilityEntity active = assessments.stream()
                .filter(a -> "ACTIVE".equals(a.getStatus()))
                .findFirst()
                .orElse(assessments.isEmpty() ? null : assessments.get(0));

        if (active == null)
            return ResponseEntity.ok(Map.of("message", "No assessment found for this case."));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("assessmentId", active.getId());
        summary.put("assessmentType", active.getAssessmentType());
        summary.put("assessmentDate", active.getAssessmentDate());
        summary.put("authorizationStartDate", active.getAuthorizationStartDate());
        summary.put("authorizationEndDate", active.getAuthorizationEndDate());
        summary.put("status", active.getStatus());
        summary.put("totalAuthorizedHoursMonthly", active.getTotalAuthorizedHoursMonthly());
        summary.put("totalAuthorizedHoursWeekly", active.getTotalAuthorizedHoursWeekly());
        summary.put("modeOfService", deriveModeOfService(active));
        summary.put("fundingSource", deriveFundingSource(active));
        summary.put("waiverProgram", active.getWaiverProgram());
        summary.put("shareOfCostAmount", active.getIhssShareOfCost());
        summary.put("countableIncome", active.getCountableIncome());
        summary.put("countyIpRate", active.getCountyIpRate());

        // All 25 service type hours
        Map<String, Object> serviceHours = new LinkedHashMap<>();
        serviceHours.put("domesticServices", active.getDomesticServicesHours());
        serviceHours.put("relatedServices", active.getRelatedServicesHours());
        serviceHours.put("personalCare", active.getPersonalCareHours());
        serviceHours.put("paramedical", active.getParamedicalHours());
        serviceHours.put("protectiveSupervision", active.getProtectiveSupervisionHours());
        serviceHours.put("mealPreparation", active.getMealPreparationHours());
        serviceHours.put("mealCleanup", active.getMealCleanupHours());
        serviceHours.put("laundry", active.getLaundryHours());
        serviceHours.put("shoppingErrands", active.getShoppingErrandsHours());
        serviceHours.put("ambulation", active.getAmbulationHours());
        serviceHours.put("bathingOralHygiene", active.getBathingOralHygieneHours());
        serviceHours.put("grooming", active.getGroomingHours());
        serviceHours.put("dressing", active.getDressingHours());
        serviceHours.put("bowelBladderCare", active.getBowelBladderCareHours());
        serviceHours.put("transferRepositioning", active.getTransferRepositioningHours());
        serviceHours.put("feeding", active.getFeedingHours());
        serviceHours.put("respiration", active.getRespirationHours());
        serviceHours.put("skinCare", active.getSkinCareHours());
        serviceHours.put("menstrualCare", active.getMenstrualCareHours());
        serviceHours.put("accompanimentMedical", active.getAccompanimentMedicalHours());
        serviceHours.put("accompanimentAltResources", active.getAccompanimentAltResourcesHours());
        serviceHours.put("heavyCleaning", active.getHeavyCleaningHours());
        serviceHours.put("yardHazardAbatement", active.getYardHazardAbatementHours());
        serviceHours.put("snowRemoval", active.getSnowRemovalHours());
        serviceHours.put("teachingDemo", active.getTeachingDemoHours());
        summary.put("serviceHours", serviceHours);

        // Functional index scores
        Map<String, Object> fi = new LinkedHashMap<>();
        fi.put("housework", active.getFiHousework());
        fi.put("laundry", active.getFiLaundry());
        fi.put("shopping", active.getFiShopping());
        fi.put("mealPrep", active.getFiMealPrep());
        fi.put("ambulation", active.getFiAmbulation());
        fi.put("bathing", active.getFiBathing());
        fi.put("dressing", active.getFiDressing());
        fi.put("bowelBladder", active.getFiBowelBladder());
        fi.put("transfer", active.getFiTransfer());
        fi.put("feeding", active.getFiFeeding());
        fi.put("respiration", active.getFiRespiration());
        fi.put("memory", active.getFiMemory());
        fi.put("orientation", active.getFiOrientation());
        fi.put("judgment", active.getFiJudgment());
        summary.put("functionalIndexScores", fi);

        // Active auth segment
        authSegmentRepo.findFirstByCaseIdAndStatusOrderBySegmentStartDateDesc(caseId, SegmentStatus.ACTIVE)
                .ifPresent(seg -> summary.put("activeSegment", seg));

        return ResponseEntity.ok(summary);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. SOC SPEND-DOWN 4-WEEK SPLIT  (BR SE 49 + DSD Section 22)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/api/eligibility/{id}/soc-spend-down")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<?> getSocSpendDown(@PathVariable Long id) {
        ServiceEligibilityEntity a = eligibilityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + id));

        double monthlyHours = a.getTotalAuthorizedHoursMonthly() != null ? a.getTotalAuthorizedHoursMonthly() : 0.0;
        double ipRate = a.getCountyIpRate() != null ? a.getCountyIpRate() : 0.0;

        // 4-week split: monthly hours ÷ 4.33 weeks = weekly hours
        double weeklyHours = monthlyHours / 4.33;
        double regularWeeklyHours = Math.min(weeklyHours, 40.0);
        double overtimeWeeklyHours = Math.max(0.0, weeklyHours - 40.0);
        double otRate = ipRate * 1.5;

        double regularPayWeek = regularWeeklyHours * ipRate;
        double overtimePayWeek = overtimeWeeklyHours * otRate;
        double grossPayWeek = regularPayWeek + overtimePayWeek;
        double grossPayMonth = grossPayWeek * 4.33;

        // SOC vs cost-of-care cap
        double countableIncome = a.getCountableIncome() != null ? a.getCountableIncome() : 0.0;
        double rawSoc = Math.max(0.0, countableIncome - 600.0);
        double socAmount = Math.min(rawSoc, grossPayMonth);

        // 4-week breakdown
        List<Map<String, Object>> weeklyBreakdown = new ArrayList<>();
        for (int week = 1; week <= 4; week++) {
            Map<String, Object> wk = new LinkedHashMap<>();
            wk.put("week", week);
            wk.put("regularHours", round2(regularWeeklyHours));
            wk.put("overtimeHours", round2(overtimeWeeklyHours));
            wk.put("regularPay", round2(regularPayWeek));
            wk.put("overtimePay", round2(overtimePayWeek));
            wk.put("grossPay", round2(grossPayWeek));
            weeklyBreakdown.add(wk);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assessmentId", id);
        result.put("monthlyAuthorizedHours", round2(monthlyHours));
        result.put("weeklyAuthorizedHours", round2(weeklyHours));
        result.put("countyIpRate", round2(ipRate));
        result.put("overtimeRate", round2(otRate));
        result.put("grossPayMonthly", round2(grossPayMonth));
        result.put("countableIncome", round2(countableIncome));
        result.put("rawShareOfCost", round2(rawSoc));
        result.put("socAfterCostOfCareCap", round2(socAmount));
        result.put("weeklyBreakdown", weeklyBreakdown);

        log.info("[SOC-4WK] Assessment {}: monthly={} hrs, grossMonth=${}, SOC=${}", id, monthlyHours, grossPayMonth, socAmount);
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. INTERFACE PRO0927A — Send authorized hours to Advantage Payroll
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/api/eligibility/{id}/send-pro0927a")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<?> sendPro0927A(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        ServiceEligibilityEntity a = eligibilityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + id));

        if (!"ACTIVE".equals(a.getStatus()) && !"APPROVED".equals(a.getStatus()))
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "PRO0927A can only be sent for ACTIVE or APPROVED assessments."));

        Map<String, Object> result = payrollIntegrationService.sendPRO0927A(
                String.valueOf(a.getCaseId()),
                String.valueOf(a.getRecipientId()),
                a.getTotalAuthorizedHoursMonthly() != null ? a.getTotalAuthorizedHoursMonthly() : 0.0,
                a.getAuthorizationStartDate() != null ? a.getAuthorizationStartDate().toString() : "",
                userId);

        log.info("[PRO0927A] Sent authorized hours for assessment {} case {} by {}", id, a.getCaseId(), userId);
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String deriveModeOfService(ServiceEligibilityEntity a) {
        String w = a.getWaiverProgram();
        if ("WPCS".equalsIgnoreCase(w) && !Boolean.TRUE.equals(a.getRecipientDeclinesCfco())) return "WPCS";
        if ("CFCO".equalsIgnoreCase(w) && !Boolean.TRUE.equals(a.getRecipientDeclinesCfco())) return "CFCO";
        if ("IPO".equalsIgnoreCase(w)) return "IPO";
        double dom = nvl(a.getDomesticServicesHours()) + nvl(a.getRelatedServicesHours());
        double total = dom + nvl(a.getPersonalCareHours()) + nvl(a.getParamedicalHours());
        return (total > 0 && dom / total > 0.70) ? "HM" : "IH";
    }

    private String deriveFundingSource(ServiceEligibilityEntity a) {
        String w = a.getWaiverProgram();
        return (w != null && !w.isBlank() && !Boolean.TRUE.equals(a.getRecipientDeclinesCfco())) ? w : "IHSS";
    }

    private double nvl(Double v) { return v != null ? v : 0.0; }
    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
