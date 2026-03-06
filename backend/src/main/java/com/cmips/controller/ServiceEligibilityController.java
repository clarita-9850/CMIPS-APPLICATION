package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.*;
import com.cmips.entity.ServiceEligibilityEntity.AssessmentType;
import com.cmips.repository.ServiceEligibilityRepository;
import com.cmips.repository.HealthCareCertificationRepository;
import com.cmips.service.ServiceEligibilityService;
import com.cmips.service.FieldLevelAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service Eligibility REST Controller
 * Implements DSD Section 21 - Service Eligibility Business Rules
 * All endpoints are protected by configurable Keycloak permissions
 */
@RestController
@RequestMapping("/api/eligibility")
@CrossOrigin(origins = "*")
public class ServiceEligibilityController {

    private static final Logger log = LoggerFactory.getLogger(ServiceEligibilityController.class);

    private final ServiceEligibilityService eligibilityService;
    private final ServiceEligibilityRepository eligibilityRepository;
    private final HealthCareCertificationRepository healthCertRepository;
    private final FieldLevelAuthorizationService fieldAuthService;

    public ServiceEligibilityController(ServiceEligibilityService eligibilityService,
                                        ServiceEligibilityRepository eligibilityRepository,
                                        HealthCareCertificationRepository healthCertRepository,
                                        FieldLevelAuthorizationService fieldAuthService) {
        this.eligibilityService = eligibilityService;
        this.eligibilityRepository = eligibilityRepository;
        this.healthCertRepository = healthCertRepository;
        this.fieldAuthService = fieldAuthService;
    }

    // ==================== ASSESSMENT MANAGEMENT (BR SE 01-06) ====================

    @GetMapping("/case/{caseId}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<ServiceEligibilityEntity>> getAssessmentsForCase(@PathVariable Long caseId) {
        List<ServiceEligibilityEntity> assessments = eligibilityRepository.findByCaseId(caseId);
        return ResponseEntity.ok(assessments);
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<ServiceEligibilityEntity> getAssessmentById(@PathVariable Long id) {
        ServiceEligibilityEntity assessment = eligibilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));
        return ResponseEntity.ok(assessment);
    }

    @PostMapping
    @RequirePermission(resource = "Service Eligibility Resource", scope = "create")
    public ResponseEntity<ServiceEligibilityEntity> createAssessment(
            @RequestBody CreateAssessmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR SE 03-06: Assessment type determines date rules
        ServiceEligibilityEntity assessment = eligibilityService.createAssessment(
                request.getCaseId(),
                request.getAssessmentType(),
                userId);

        return ResponseEntity.ok(assessment);
    }

    @PutMapping("/{id}/home-visit")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceEligibilityEntity> recordHomeVisit(
            @PathVariable Long id,
            @RequestBody HomeVisitRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR SE 04-06: Home visit date sets reassessment due date
        ServiceEligibilityEntity assessment = eligibilityService.updateHomeVisitDate(
                id, request.getHomeVisitDate(), userId);

        return ResponseEntity.ok(assessment);
    }

    // ==================== SERVICE HOURS (BR SE 01, 07-08) ====================

    @PutMapping("/{id}/service-hours")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceEligibilityEntity> updateServiceHours(
            @PathVariable Long id,
            @RequestBody ServiceEligibilityService.ServiceHoursUpdate update,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR SE 07-08: HTG indicators (+/-/blank)
        ServiceEligibilityEntity assessment = eligibilityService.updateServiceHours(id, update, userId);
        return ResponseEntity.ok(assessment);
    }

    @GetMapping("/{id}/total-hours")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getTotalAssessedNeed(@PathVariable Long id) {
        // BR SE 01: Calculate total assessed need
        ServiceEligibilityEntity assessment = eligibilityService.calculateTotalAssessedNeed(id, "system");
        Map<String, Object> response = new HashMap<>();
        response.put("totalHoursMonthly", assessment.getTotalAuthorizedHoursMonthly());
        response.put("totalHoursWeekly", assessment.getTotalAuthorizedHoursWeekly());
        return ResponseEntity.ok(response);
    }

    // ==================== FUNCTIONAL RANKS ====================

    @PutMapping("/{id}/functional-ranks")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceEligibilityEntity> updateFunctionalRanks(
            @PathVariable Long id,
            @RequestBody FunctionalRanksRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ServiceEligibilityEntity assessment = eligibilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        // Category-level ranks (legacy 1-5)
        if (request.getDomesticRank() != null) assessment.setFunctionalRankDomestic(request.getDomesticRank());
        if (request.getRelatedRank() != null) assessment.setFunctionalRankRelated(request.getRelatedRank());
        if (request.getPersonalRank() != null) assessment.setFunctionalRankPersonal(request.getPersonalRank());
        if (request.getParamedicalRank() != null) assessment.setFunctionalRankParamedical(request.getParamedicalRank());
        // 14 functional index scores (1-6) per DSD Section 21
        if (request.getFiHousework() != null) assessment.setFiHousework(request.getFiHousework());
        if (request.getFiLaundry() != null) assessment.setFiLaundry(request.getFiLaundry());
        if (request.getFiShopping() != null) assessment.setFiShopping(request.getFiShopping());
        if (request.getFiMealPrep() != null) assessment.setFiMealPrep(request.getFiMealPrep());
        if (request.getFiAmbulation() != null) assessment.setFiAmbulation(request.getFiAmbulation());
        if (request.getFiBathing() != null) assessment.setFiBathing(request.getFiBathing());
        if (request.getFiDressing() != null) assessment.setFiDressing(request.getFiDressing());
        if (request.getFiBowelBladder() != null) assessment.setFiBowelBladder(request.getFiBowelBladder());
        if (request.getFiTransfer() != null) assessment.setFiTransfer(request.getFiTransfer());
        if (request.getFiFeeding() != null) assessment.setFiFeeding(request.getFiFeeding());
        if (request.getFiRespiration() != null) assessment.setFiRespiration(request.getFiRespiration());
        if (request.getFiMemory() != null) assessment.setFiMemory(request.getFiMemory());
        if (request.getFiOrientation() != null) assessment.setFiOrientation(request.getFiOrientation());
        if (request.getFiJudgment() != null) assessment.setFiJudgment(request.getFiJudgment());

        assessment.setUpdatedBy(userId);
        ServiceEligibilityEntity saved = eligibilityRepository.save(assessment);
        return ResponseEntity.ok(saved);
    }

    // ==================== SHARE OF COST (BR SE 13-15) ====================

    @PutMapping("/{id}/share-of-cost")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceEligibilityEntity> updateShareOfCost(
            @PathVariable Long id,
            @RequestBody ShareOfCostRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR SE 13-15: Share of cost management
        ServiceEligibilityEntity assessment = eligibilityService.updateShareOfCost(
                id, request.getNetIncome(), request.getCountableIncome(), userId);

        return ResponseEntity.ok(assessment);
    }

    // ==================== WAIVER PROGRAMS (BR SE 16-22) ====================

    @PutMapping("/{id}/waiver-program")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceEligibilityEntity> updateWaiverProgram(
            @PathVariable Long id,
            @RequestBody WaiverProgramRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR SE 22: Waiver program changes reset verification
        ServiceEligibilityEntity assessment = eligibilityService.updateWaiverProgram(
                id, request.getWaiverProgram(), userId);

        return ResponseEntity.ok(assessment);
    }

    // ==================== ADVANCE PAY (BR SE 09) ====================

    @PutMapping("/{id}/advance-pay")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceEligibilityEntity> updateAdvancePay(
            @PathVariable Long id,
            @RequestBody AdvancePayRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR SE 09: Advance pay eligibility
        ServiceEligibilityEntity assessment = eligibilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        assessment.setAdvancePayIndicated(request.getAdvancePayIndicated());
        assessment.setAdvancePayRate(request.getAdvancePayRate());
        assessment.setUpdatedBy(userId);

        ServiceEligibilityEntity saved = eligibilityRepository.save(assessment);
        return ResponseEntity.ok(saved);
    }

    // ==================== HEALTH CARE CERTIFICATION (BR SE 28-50) ====================

    @GetMapping("/case/{caseId}/health-cert")
    @RequirePermission(resource = "Health Care Certification Resource", scope = "view")
    public ResponseEntity<List<HealthCareCertificationEntity>> getHealthCareCertifications(
            @PathVariable Long caseId) {

        List<HealthCareCertificationEntity> certs = healthCertRepository.findByCaseId(caseId);
        return ResponseEntity.ok(certs);
    }

    @PostMapping("/health-cert")
    @RequirePermission(resource = "Health Care Certification Resource", scope = "create")
    public ResponseEntity<HealthCareCertificationEntity> createHealthCareCertification(
            @RequestBody CreateHealthCertRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR SE 28-50: Health care certification (SOC 873)
        HealthCareCertificationEntity cert = eligibilityService.createHealthCareCertification(
                request.getCaseId(),
                request.getCertificationMethod(),
                request.getFormType(),
                request.getPrintOption(),
                request.getLanguage(),
                userId);

        return ResponseEntity.ok(cert);
    }

    @PutMapping("/health-cert/{id}/documentation-received")
    @RequirePermission(resource = "Health Care Certification Resource", scope = "edit")
    public ResponseEntity<HealthCareCertificationEntity> recordDocumentationReceived(
            @PathVariable Long id,
            @RequestBody DocumentationReceivedRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        HealthCareCertificationEntity cert = eligibilityService.recordDocumentationReceived(
                id, request.getCertificationType(), request.getReceivedDate(), userId);
        return ResponseEntity.ok(cert);
    }

    @PutMapping("/health-cert/{id}/good-cause-extension")
    @RequirePermission(resource = "Health Care Certification Resource", scope = "edit")
    public ResponseEntity<HealthCareCertificationEntity> requestGoodCauseExtension(
            @PathVariable Long id,
            @RequestBody GoodCauseRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR SE 31: Good cause extension (+45 days)
        HealthCareCertificationEntity cert = eligibilityService.requestGoodCauseExtension(
                id, request.getExtensionDate(), userId);

        return ResponseEntity.ok(cert);
    }

    @PutMapping("/health-cert/{id}/exception")
    @RequirePermission(resource = "Health Care Certification Resource", scope = "approve")
    public ResponseEntity<HealthCareCertificationEntity> grantException(
            @PathVariable Long id,
            @RequestBody ExceptionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR SE 30: Exception for hospital discharge or risk of out-of-home placement
        HealthCareCertificationEntity cert = eligibilityService.grantException(
                id, request.getExceptionReason(), request.getExceptionDate(), userId);

        return ResponseEntity.ok(cert);
    }

    @GetMapping("/health-cert/overdue")
    @RequirePermission(resource = "Health Care Certification Resource", scope = "view")
    public ResponseEntity<List<HealthCareCertificationEntity>> getOverdueCertifications() {
        List<HealthCareCertificationEntity> certs = healthCertRepository.findOverdueCertifications(LocalDate.now());
        return ResponseEntity.ok(certs);
    }

    // ==================== REASSESSMENT DUE (BR SE 04-06) ====================

    @GetMapping("/due-for-reassessment")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<ServiceEligibilityEntity>> getDueForReassessment(
            @RequestParam(required = false) String date) {

        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now().plusDays(30);
        List<ServiceEligibilityEntity> assessments = eligibilityRepository.findEligibilitiesDueForReassessment(targetDate);
        return ResponseEntity.ok(assessments);
    }

    // ==================== APPROVAL WORKFLOW ====================

    @PutMapping("/{id}/submit-for-approval")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceEligibilityEntity> submitForApproval(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ServiceEligibilityEntity assessment = eligibilityService.submitForApproval(id, userId);
        return ResponseEntity.ok(assessment);
    }

    @PutMapping("/{id}/approve")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "approve")
    public ResponseEntity<ServiceEligibilityEntity> approveAssessment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ServiceEligibilityEntity assessment = eligibilityService.approveAssessment(id, userId);
        return ResponseEntity.ok(assessment);
    }

    /**
     * Reject assessment (supervisor) — DSD Section 22
     * Transitions PENDING_SUPERVISOR_REVIEW → REJECTED with reason.
     */
    @PutMapping("/{id}/reject")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "approve")
    public ResponseEntity<ServiceEligibilityEntity> rejectAssessment(
            @PathVariable Long id,
            @RequestBody RejectRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ServiceEligibilityEntity assessment = eligibilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found: " + id));
        if (!"PENDING_SUPERVISOR_REVIEW".equals(assessment.getStatus()) && !"PENDING_APPROVAL".equals(assessment.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        assessment.setStatus("REJECTED");
        assessment.setApprovedById(userId);
        assessment.setApprovalDate(LocalDate.now());
        assessment.setUpdatedBy(userId);
        assessment.setSocialWorkerCertification(
            "REJECTED: " + (request.getReason() != null ? request.getReason() : "No reason provided"));
        ServiceEligibilityEntity saved = eligibilityRepository.save(assessment);
        log.info("[SE] Assessment {} rejected by {}", id, userId);
        return ResponseEntity.ok(saved);
    }

    /**
     * Check Eligibility — DSD Section 22 (preview mode).
     * Runs the determination algorithm without saving or generating NOA.
     * Returns a preview of authorized hours, SOC, mode of service, and funding source.
     */
    @PostMapping("/{id}/check-eligibility")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> checkEligibility(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ServiceEligibilityEntity a = eligibilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found: " + id));

        double totalMonthly = a.calculateTotalAssessedNeed();
        double totalWeekly  = totalMonthly / 4.33;

        // SOC preview (BR SE 13): SOC = max(0, countableIncome - 600)
        double countableIncome = a.getCountableIncome() != null ? a.getCountableIncome() : 0.0;
        double rawSoc = Math.max(0.0, countableIncome - 600.0);
        double ipRate = a.getCountyIpRate() != null ? a.getCountyIpRate() : 0.0;
        double socPreview = (ipRate > 0) ? Math.min(rawSoc, totalMonthly * ipRate) : rawSoc;

        // Mode of service determination (DSD Section 22)
        // Priority: waiver program → service composition → default IH
        String modeOfService;
        String waiverPgm = a.getWaiverProgram();
        if ("WPCS".equalsIgnoreCase(waiverPgm) && !Boolean.TRUE.equals(a.getRecipientDeclinesCfco())) {
            modeOfService = "WPCS";  // Waiver Personal Care Services
        } else if ("CFCO".equalsIgnoreCase(waiverPgm) && !Boolean.TRUE.equals(a.getRecipientDeclinesCfco())) {
            modeOfService = "CFCO";  // Community First Choice Option
        } else if ("IPO".equalsIgnoreCase(waiverPgm)) {
            modeOfService = "IPO";   // Independence Plus Option
        } else {
            // Determine IH vs HM based on service composition: >70% domestic → county homemaker
            double domHours = a.getDomesticServicesHours() != null ? a.getDomesticServicesHours() : 0.0;
            double relHours = a.getRelatedServicesHours() != null ? a.getRelatedServicesHours() : 0.0;
            double perHours = a.getPersonalCareHours() != null ? a.getPersonalCareHours() : 0.0;
            double paraHours = a.getParamedicalHours() != null ? a.getParamedicalHours() : 0.0;
            double totalHours = domHours + relHours + perHours + paraHours;
            if (totalHours > 0 && (domHours / totalHours) > 0.70) {
                modeOfService = "HM";  // County Homemaker (primarily domestic services)
            } else {
                modeOfService = "IH";  // Standard In-Home Individual Provider
            }
        }

        // Funding source determination: waiver program if active and not declined, else IHSS
        String fundingSource;
        if (waiverPgm != null && !waiverPgm.isBlank() && !Boolean.TRUE.equals(a.getRecipientDeclinesCfco())) {
            fundingSource = waiverPgm;
        } else {
            fundingSource = "IHSS";
        }

        // Eligibility determination
        boolean eligible = totalMonthly > 0;
        String determination = eligible ? "ELIGIBLE" : "INELIGIBLE - No assessed service need";

        Map<String, Object> preview = new java.util.LinkedHashMap<>();
        preview.put("previewOnly", true);
        preview.put("determination", determination);
        preview.put("eligible", eligible);
        preview.put("totalAssessedNeedMonthly", Math.round(totalMonthly * 100.0) / 100.0);
        preview.put("totalAssessedNeedWeekly",  Math.round(totalWeekly  * 100.0) / 100.0);
        preview.put("estimatedShareOfCost", Math.round(socPreview * 100.0) / 100.0);
        preview.put("modeOfService", modeOfService);
        preview.put("fundingSource", fundingSource);
        preview.put("advancePayRate", a.getAdvancePayRate());
        preview.put("waiverProgram", waiverPgm);
        preview.put("noaGenerated", false);
        preview.put("message", "This is a preview. No records have been saved and no NOA has been generated.");

        log.info("[SE] Check eligibility preview for assessment {}: determination={}, hours={}", id, determination, totalMonthly);
        return ResponseEntity.ok(preview);
    }

    // ==================== REQUEST DTOs ====================

    public static class RejectRequest {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class CreateAssessmentRequest {
        private Long caseId;
        private AssessmentType assessmentType;

        public Long getCaseId() { return caseId; }
        public void setCaseId(Long caseId) { this.caseId = caseId; }

        public AssessmentType getAssessmentType() { return assessmentType; }
        public void setAssessmentType(AssessmentType assessmentType) { this.assessmentType = assessmentType; }
    }

    public static class HomeVisitRequest {
        private LocalDate homeVisitDate;

        public LocalDate getHomeVisitDate() { return homeVisitDate; }
        public void setHomeVisitDate(LocalDate homeVisitDate) { this.homeVisitDate = homeVisitDate; }
    }

    public static class FunctionalRanksRequest {
        private Integer domesticRank;
        private Integer relatedRank;
        private Integer personalRank;
        private Integer paramedicalRank;
        // 14 functional index scores (1-6) per DSD Section 21
        private Integer fiHousework, fiLaundry, fiShopping, fiMealPrep, fiAmbulation;
        private Integer fiBathing, fiDressing, fiBowelBladder, fiTransfer, fiFeeding;
        private Integer fiRespiration, fiMemory, fiOrientation, fiJudgment;

        public Integer getDomesticRank() { return domesticRank; }
        public void setDomesticRank(Integer v) { this.domesticRank = v; }
        public Integer getRelatedRank() { return relatedRank; }
        public void setRelatedRank(Integer v) { this.relatedRank = v; }
        public Integer getPersonalRank() { return personalRank; }
        public void setPersonalRank(Integer v) { this.personalRank = v; }
        public Integer getParamedicalRank() { return paramedicalRank; }
        public void setParamedicalRank(Integer v) { this.paramedicalRank = v; }

        public Integer getFiHousework() { return fiHousework; } public void setFiHousework(Integer v) { fiHousework = v; }
        public Integer getFiLaundry() { return fiLaundry; } public void setFiLaundry(Integer v) { fiLaundry = v; }
        public Integer getFiShopping() { return fiShopping; } public void setFiShopping(Integer v) { fiShopping = v; }
        public Integer getFiMealPrep() { return fiMealPrep; } public void setFiMealPrep(Integer v) { fiMealPrep = v; }
        public Integer getFiAmbulation() { return fiAmbulation; } public void setFiAmbulation(Integer v) { fiAmbulation = v; }
        public Integer getFiBathing() { return fiBathing; } public void setFiBathing(Integer v) { fiBathing = v; }
        public Integer getFiDressing() { return fiDressing; } public void setFiDressing(Integer v) { fiDressing = v; }
        public Integer getFiBowelBladder() { return fiBowelBladder; } public void setFiBowelBladder(Integer v) { fiBowelBladder = v; }
        public Integer getFiTransfer() { return fiTransfer; } public void setFiTransfer(Integer v) { fiTransfer = v; }
        public Integer getFiFeeding() { return fiFeeding; } public void setFiFeeding(Integer v) { fiFeeding = v; }
        public Integer getFiRespiration() { return fiRespiration; } public void setFiRespiration(Integer v) { fiRespiration = v; }
        public Integer getFiMemory() { return fiMemory; } public void setFiMemory(Integer v) { fiMemory = v; }
        public Integer getFiOrientation() { return fiOrientation; } public void setFiOrientation(Integer v) { fiOrientation = v; }
        public Integer getFiJudgment() { return fiJudgment; } public void setFiJudgment(Integer v) { fiJudgment = v; }
    }

    public static class ShareOfCostRequest {
        private Double netIncome;
        private Double countableIncome;

        public Double getNetIncome() { return netIncome; }
        public void setNetIncome(Double netIncome) { this.netIncome = netIncome; }

        public Double getCountableIncome() { return countableIncome; }
        public void setCountableIncome(Double countableIncome) { this.countableIncome = countableIncome; }
    }

    public static class WaiverProgramRequest {
        private String waiverProgram;

        public String getWaiverProgram() { return waiverProgram; }
        public void setWaiverProgram(String waiverProgram) { this.waiverProgram = waiverProgram; }
    }

    public static class AdvancePayRequest {
        private Boolean advancePayIndicated;
        private Double advancePayRate;

        public Boolean getAdvancePayIndicated() { return advancePayIndicated; }
        public void setAdvancePayIndicated(Boolean advancePayIndicated) { this.advancePayIndicated = advancePayIndicated; }

        public Double getAdvancePayRate() { return advancePayRate; }
        public void setAdvancePayRate(Double advancePayRate) { this.advancePayRate = advancePayRate; }
    }

    public static class CreateHealthCertRequest {
        private Long caseId;
        private String certificationMethod;
        private String formType;
        private String printOption;
        private String language;

        public Long getCaseId() { return caseId; }
        public void setCaseId(Long caseId) { this.caseId = caseId; }

        public String getCertificationMethod() { return certificationMethod; }
        public void setCertificationMethod(String certificationMethod) { this.certificationMethod = certificationMethod; }

        public String getFormType() { return formType; }
        public void setFormType(String formType) { this.formType = formType; }

        public String getPrintOption() { return printOption; }
        public void setPrintOption(String printOption) { this.printOption = printOption; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    public static class DocumentationReceivedRequest {
        private String certificationType;
        private LocalDate receivedDate;

        public String getCertificationType() { return certificationType; }
        public void setCertificationType(String certificationType) { this.certificationType = certificationType; }

        public LocalDate getReceivedDate() { return receivedDate; }
        public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    }

    public static class GoodCauseRequest {
        private LocalDate extensionDate;

        public LocalDate getExtensionDate() { return extensionDate; }
        public void setExtensionDate(LocalDate extensionDate) { this.extensionDate = extensionDate; }
    }

    public static class ExceptionRequest {
        private String exceptionReason;
        private LocalDate exceptionDate;

        public String getExceptionReason() { return exceptionReason; }
        public void setExceptionReason(String exceptionReason) { this.exceptionReason = exceptionReason; }

        public LocalDate getExceptionDate() { return exceptionDate; }
        public void setExceptionDate(LocalDate exceptionDate) { this.exceptionDate = exceptionDate; }
    }
}
