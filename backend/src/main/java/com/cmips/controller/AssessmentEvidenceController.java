package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.*;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Assessment Evidence REST Controller — DSD Section 21 child entities.
 *
 * Manages the normalized evidence records that support an IHSS assessment:
 *   - Household Evidence (living arrangement, appliances)
 *   - Household Members
 *   - Functional Index (14 ranks)
 *   - Service Type Evidence (25 service types with deductions)
 *   - Service Task Evidence (task-level detail per service type)
 *   - Paramedical Evidence
 *   - Protective Supervision Evidence
 *   - Disaster Preparedness
 *   - Share of Cost Evidence + Income Evidence
 *   - SAWS Notifications
 */
@RestController
@RequestMapping("/api/assessments/{assessmentId}/evidence")
@CrossOrigin(origins = "*")
public class AssessmentEvidenceController {

    private static final Logger log = LoggerFactory.getLogger(AssessmentEvidenceController.class);

    private final HouseholdEvidenceRepository householdRepo;
    private final HouseholdMemberEvidenceRepository memberRepo;
    private final FunctionalIndexEvidenceRepository functionalIndexRepo;
    private final ServiceTypeEvidenceRepository serviceTypeRepo;
    private final ServiceTaskEvidenceRepository serviceTaskRepo;
    private final ParamedicalEvidenceRepository paramedicalRepo;
    private final ProtectiveSupervisionEvidenceRepository protectiveRepo;
    private final DisasterPreparednessRepository disasterRepo;
    private final ShareOfCostEvidenceRepository socRepo;
    private final IncomeEvidenceRepository incomeRepo;
    private final SAWSNotificationRepository sawsRepo;
    private final HourlyTaskGuidelinesRepository htgRepo;

    public AssessmentEvidenceController(
            HouseholdEvidenceRepository householdRepo,
            HouseholdMemberEvidenceRepository memberRepo,
            FunctionalIndexEvidenceRepository functionalIndexRepo,
            ServiceTypeEvidenceRepository serviceTypeRepo,
            ServiceTaskEvidenceRepository serviceTaskRepo,
            ParamedicalEvidenceRepository paramedicalRepo,
            ProtectiveSupervisionEvidenceRepository protectiveRepo,
            DisasterPreparednessRepository disasterRepo,
            ShareOfCostEvidenceRepository socRepo,
            IncomeEvidenceRepository incomeRepo,
            SAWSNotificationRepository sawsRepo,
            HourlyTaskGuidelinesRepository htgRepo) {
        this.householdRepo = householdRepo;
        this.memberRepo = memberRepo;
        this.functionalIndexRepo = functionalIndexRepo;
        this.serviceTypeRepo = serviceTypeRepo;
        this.serviceTaskRepo = serviceTaskRepo;
        this.paramedicalRepo = paramedicalRepo;
        this.protectiveRepo = protectiveRepo;
        this.disasterRepo = disasterRepo;
        this.socRepo = socRepo;
        this.incomeRepo = incomeRepo;
        this.sawsRepo = sawsRepo;
        this.htgRepo = htgRepo;
    }

    // ==================== HOUSEHOLD EVIDENCE ====================

    @GetMapping("/household")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<HouseholdEvidenceEntity>> getHouseholdEvidence(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(householdRepo.findByAssessmentEvidenceId(assessmentId));
    }

    @PostMapping("/household")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<HouseholdEvidenceEntity> createHouseholdEvidence(
            @PathVariable Long assessmentId,
            @RequestBody HouseholdEvidenceEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setAssessmentEvidenceId(assessmentId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(householdRepo.save(entity));
    }

    @PutMapping("/household/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<HouseholdEvidenceEntity> updateHouseholdEvidence(
            @PathVariable Long assessmentId, @PathVariable Long id,
            @RequestBody HouseholdEvidenceEntity update,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        HouseholdEvidenceEntity existing = householdRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Household evidence not found: " + id));
        update.setId(existing.getId());
        update.setAssessmentEvidenceId(assessmentId);
        update.setCreatedAt(existing.getCreatedAt());
        update.setCreatedBy(existing.getCreatedBy());
        update.setUpdatedBy(userId);
        return ResponseEntity.ok(householdRepo.save(update));
    }

    // ==================== HOUSEHOLD MEMBERS ====================

    @GetMapping("/household-members")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<HouseholdMemberEvidenceEntity>> getHouseholdMembers(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(memberRepo.findByAssessmentEvidenceId(assessmentId));
    }

    @PostMapping("/household-members")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<HouseholdMemberEvidenceEntity> createHouseholdMember(
            @PathVariable Long assessmentId,
            @RequestBody HouseholdMemberEvidenceEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setAssessmentEvidenceId(assessmentId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(memberRepo.save(entity));
    }

    @PutMapping("/household-members/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<HouseholdMemberEvidenceEntity> updateHouseholdMember(
            @PathVariable Long assessmentId, @PathVariable Long id,
            @RequestBody HouseholdMemberEvidenceEntity update,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        HouseholdMemberEvidenceEntity existing = memberRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Household member not found: " + id));
        update.setId(existing.getId());
        update.setAssessmentEvidenceId(assessmentId);
        update.setCreatedAt(existing.getCreatedAt());
        update.setCreatedBy(existing.getCreatedBy());
        update.setUpdatedBy(userId);
        return ResponseEntity.ok(memberRepo.save(update));
    }

    @DeleteMapping("/household-members/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<Map<String, String>> deleteHouseholdMember(@PathVariable Long assessmentId, @PathVariable Long id) {
        memberRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "deleted", "id", id.toString()));
    }

    // ==================== FUNCTIONAL INDEX ====================

    @GetMapping("/functional-index")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<FunctionalIndexEvidenceEntity>> getFunctionalIndex(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(functionalIndexRepo.findByAssessmentEvidenceId(assessmentId));
    }

    @PostMapping("/functional-index")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<FunctionalIndexEvidenceEntity> createFunctionalIndex(
            @PathVariable Long assessmentId,
            @RequestBody FunctionalIndexEvidenceEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setAssessmentEvidenceId(assessmentId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(functionalIndexRepo.save(entity));
    }

    @PutMapping("/functional-index/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<FunctionalIndexEvidenceEntity> updateFunctionalIndex(
            @PathVariable Long assessmentId, @PathVariable Long id,
            @RequestBody FunctionalIndexEvidenceEntity update,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        FunctionalIndexEvidenceEntity existing = functionalIndexRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Functional index not found: " + id));
        update.setId(existing.getId());
        update.setAssessmentEvidenceId(assessmentId);
        update.setCreatedAt(existing.getCreatedAt());
        update.setCreatedBy(existing.getCreatedBy());
        update.setUpdatedBy(userId);
        return ResponseEntity.ok(functionalIndexRepo.save(update));
    }

    // ==================== SERVICE TYPE EVIDENCE ====================

    @GetMapping("/service-types")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<ServiceTypeEvidenceEntity>> getServiceTypes(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(serviceTypeRepo.findByAssessmentEvidenceId(assessmentId));
    }

    @PostMapping("/service-types")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceTypeEvidenceEntity> createServiceType(
            @PathVariable Long assessmentId,
            @RequestBody ServiceTypeEvidenceEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setAssessmentEvidenceId(assessmentId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(serviceTypeRepo.save(entity));
    }

    @PutMapping("/service-types/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceTypeEvidenceEntity> updateServiceType(
            @PathVariable Long assessmentId, @PathVariable Long id,
            @RequestBody ServiceTypeEvidenceEntity update,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ServiceTypeEvidenceEntity existing = serviceTypeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Service type evidence not found: " + id));
        update.setId(existing.getId());
        update.setAssessmentEvidenceId(assessmentId);
        update.setCreatedAt(existing.getCreatedAt());
        update.setCreatedBy(existing.getCreatedBy());
        update.setUpdatedBy(userId);
        return ResponseEntity.ok(serviceTypeRepo.save(update));
    }

    @DeleteMapping("/service-types/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<Map<String, String>> deleteServiceType(@PathVariable Long assessmentId, @PathVariable Long id) {
        // Also delete child tasks
        serviceTaskRepo.deleteByServiceTypeEvidenceId(id);
        serviceTypeRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "deleted", "id", id.toString()));
    }

    // ==================== SERVICE TASK EVIDENCE ====================

    @GetMapping("/service-types/{serviceTypeId}/tasks")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<ServiceTaskEvidenceEntity>> getServiceTasks(
            @PathVariable Long assessmentId, @PathVariable Long serviceTypeId) {
        return ResponseEntity.ok(serviceTaskRepo.findByServiceTypeEvidenceId(serviceTypeId));
    }

    @PostMapping("/service-types/{serviceTypeId}/tasks")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceTaskEvidenceEntity> createServiceTask(
            @PathVariable Long assessmentId, @PathVariable Long serviceTypeId,
            @RequestBody ServiceTaskEvidenceEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setServiceTypeEvidenceId(serviceTypeId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(serviceTaskRepo.save(entity));
    }

    @PutMapping("/service-tasks/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ServiceTaskEvidenceEntity> updateServiceTask(
            @PathVariable Long assessmentId, @PathVariable Long id,
            @RequestBody ServiceTaskEvidenceEntity update,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ServiceTaskEvidenceEntity existing = serviceTaskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Service task not found: " + id));
        update.setId(existing.getId());
        update.setServiceTypeEvidenceId(existing.getServiceTypeEvidenceId());
        update.setCreatedAt(existing.getCreatedAt());
        update.setCreatedBy(existing.getCreatedBy());
        update.setUpdatedBy(userId);
        return ResponseEntity.ok(serviceTaskRepo.save(update));
    }

    @DeleteMapping("/service-tasks/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<Map<String, String>> deleteServiceTask(@PathVariable Long assessmentId, @PathVariable Long id) {
        serviceTaskRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "deleted", "id", id.toString()));
    }

    // ==================== PARAMEDICAL EVIDENCE ====================

    @GetMapping("/paramedical")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<ParamedicalEvidenceEntity>> getParamedical(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(paramedicalRepo.findByAssessmentEvidenceId(assessmentId));
    }

    @PostMapping("/paramedical")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ParamedicalEvidenceEntity> createParamedical(
            @PathVariable Long assessmentId,
            @RequestBody ParamedicalEvidenceEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setAssessmentEvidenceId(assessmentId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(paramedicalRepo.save(entity));
    }

    @PutMapping("/paramedical/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ParamedicalEvidenceEntity> updateParamedical(
            @PathVariable Long assessmentId, @PathVariable Long id,
            @RequestBody ParamedicalEvidenceEntity update,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ParamedicalEvidenceEntity existing = paramedicalRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Paramedical evidence not found: " + id));
        update.setId(existing.getId());
        update.setAssessmentEvidenceId(assessmentId);
        update.setCreatedAt(existing.getCreatedAt());
        update.setCreatedBy(existing.getCreatedBy());
        update.setUpdatedBy(userId);
        return ResponseEntity.ok(paramedicalRepo.save(update));
    }

    // ==================== PROTECTIVE SUPERVISION ====================

    @GetMapping("/protective-supervision")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<ProtectiveSupervisionEvidenceEntity>> getProtectiveSupervision(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(protectiveRepo.findByAssessmentEvidenceId(assessmentId));
    }

    @PostMapping("/protective-supervision")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ProtectiveSupervisionEvidenceEntity> createProtectiveSupervision(
            @PathVariable Long assessmentId,
            @RequestBody ProtectiveSupervisionEvidenceEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setAssessmentEvidenceId(assessmentId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(protectiveRepo.save(entity));
    }

    // ==================== DISASTER PREPAREDNESS ====================

    @GetMapping("/disaster-preparedness")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<DisasterPreparednessEntity>> getDisasterPreparedness(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(disasterRepo.findByAssessmentEvidenceId(assessmentId));
    }

    @PostMapping("/disaster-preparedness")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<DisasterPreparednessEntity> createDisasterPreparedness(
            @PathVariable Long assessmentId,
            @RequestBody DisasterPreparednessEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setAssessmentEvidenceId(assessmentId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(disasterRepo.save(entity));
    }

    @PutMapping("/disaster-preparedness/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<DisasterPreparednessEntity> updateDisasterPreparedness(
            @PathVariable Long assessmentId, @PathVariable Long id,
            @RequestBody DisasterPreparednessEntity update,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        DisasterPreparednessEntity existing = disasterRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Disaster preparedness not found: " + id));
        update.setId(existing.getId());
        update.setAssessmentEvidenceId(assessmentId);
        update.setCreatedAt(existing.getCreatedAt());
        update.setCreatedBy(existing.getCreatedBy());
        update.setUpdatedBy(userId);
        return ResponseEntity.ok(disasterRepo.save(update));
    }

    // ==================== SHARE OF COST EVIDENCE ====================

    @GetMapping("/share-of-cost")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<ShareOfCostEvidenceEntity>> getShareOfCostEvidence(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(socRepo.findByAssessmentEvidenceId(assessmentId));
    }

    @PostMapping("/share-of-cost")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<ShareOfCostEvidenceEntity> createShareOfCostEvidence(
            @PathVariable Long assessmentId,
            @RequestBody ShareOfCostEvidenceEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setAssessmentEvidenceId(assessmentId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(socRepo.save(entity));
    }

    // ==================== INCOME EVIDENCE ====================

    @GetMapping("/share-of-cost/{socId}/income")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<IncomeEvidenceEntity>> getIncomeEvidence(
            @PathVariable Long assessmentId, @PathVariable Long socId) {
        return ResponseEntity.ok(incomeRepo.findByShareOfCostEvidenceId(socId));
    }

    @PostMapping("/share-of-cost/{socId}/income")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<IncomeEvidenceEntity> createIncomeEvidence(
            @PathVariable Long assessmentId, @PathVariable Long socId,
            @RequestBody IncomeEvidenceEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setShareOfCostEvidenceId(socId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(incomeRepo.save(entity));
    }

    @DeleteMapping("/income/{id}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<Map<String, String>> deleteIncomeEvidence(@PathVariable Long assessmentId, @PathVariable Long id) {
        incomeRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "deleted", "id", id.toString()));
    }

    // ==================== SAWS NOTIFICATIONS ====================

    @GetMapping("/saws-notifications")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<SAWSNotificationEntity>> getSAWSNotifications(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(sawsRepo.findByAssessmentEvidenceId(assessmentId));
    }

    @PostMapping("/saws-notifications")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "edit")
    public ResponseEntity<SAWSNotificationEntity> createSAWSNotification(
            @PathVariable Long assessmentId,
            @RequestBody SAWSNotificationEntity entity,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        entity.setAssessmentEvidenceId(assessmentId);
        entity.setCreatedBy(userId);
        return ResponseEntity.ok(sawsRepo.save(entity));
    }

    // ==================== HTG REFERENCE ====================

    /**
     * Get Hourly Task Guidelines for a service type and functional rank.
     */
    @GetMapping("/htg")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<HourlyTaskGuidelinesEntity>> getHTGReference(
            @PathVariable Long assessmentId,
            @RequestParam(required = false) String serviceTypeCode,
            @RequestParam(required = false) String funcAreaCode) {
        if (serviceTypeCode != null && funcAreaCode != null) {
            return ResponseEntity.ok(htgRepo.findByServiceTypeCodeAndFuncAreaCode(serviceTypeCode, funcAreaCode));
        } else if (serviceTypeCode != null) {
            return ResponseEntity.ok(htgRepo.findByServiceTypeCode(serviceTypeCode));
        }
        return ResponseEntity.ok(htgRepo.findAll());
    }
}
