package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.*;
import com.cmips.entity.CaseEntity.CaseStatus;
import com.cmips.service.CaseMaintenanceService;
import com.cmips.service.CaseManagementService;
import com.cmips.service.FieldLevelAuthorizationService;
import com.cmips.service.PDFReportGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.cmips.entity.CaseStatusHistory;

/**
 * Case Management REST Controller
 * All endpoints are protected by configurable Keycloak permissions
 */
@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CaseManagementController {

    private static final Logger log = LoggerFactory.getLogger(CaseManagementController.class);

    private final CaseManagementService caseManagementService;
    private final FieldLevelAuthorizationService fieldAuthService;
    private final CaseMaintenanceService caseMaintenanceService;
    private final PDFReportGeneratorService pdfService;

    public CaseManagementController(CaseManagementService caseManagementService,
                                    FieldLevelAuthorizationService fieldAuthService,
                                    CaseMaintenanceService caseMaintenanceService,
                                    PDFReportGeneratorService pdfService) {
        this.caseManagementService = caseManagementService;
        this.fieldAuthService = fieldAuthService;
        this.caseMaintenanceService = caseMaintenanceService;
        this.pdfService = pdfService;
    }

    // ==================== CASE CRUD ====================

    @GetMapping
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<Map<String, Object>>> getAllCases(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String caseOwnerId,
            @RequestParam(required = false) String status) {

        List<CaseEntity> cases;
        if (countyCode != null) {
            cases = caseManagementService.getActiveCasesByCounty(countyCode);
        } else if (caseOwnerId != null) {
            cases = caseManagementService.getCasesForCaseworker(caseOwnerId);
        } else {
            CaseStatus caseStatus = status != null ? CaseStatus.valueOf(status) : null;
            cases = caseManagementService.searchCases(null, null, null, null, caseStatus);
        }

        // Apply field-level authorization
        List<Map<String, Object>> filteredCases = cases.stream()
                .map(c -> fieldAuthService.filterFieldsForRole(c, roles, "Case Resource"))
                .toList();

        return ResponseEntity.ok(filteredCases);
    }

    /**
     * Get case by ID — returns enriched response per DSD Phase 7 Case Home fields.
     *
     * Includes: ihssReferralDate, mediCalEligibilityReferralDate, recipientName,
     * caseOwnerName, and all standard case fields.
     */
    @GetMapping("/{id}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getCaseById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {

        try {
            // Use enriched lookup (efficient findById + recipient join)
            Map<String, Object> enrichedCase = caseManagementService.getCaseWithDetails(id);
            return ResponseEntity.ok(enrichedCase);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Case not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    @PostMapping
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createCase(
            @RequestBody CreateCaseRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        // Resolve userId: prefer the header, fall back to request body createdBy field
        String userId = (headerUserId != null && !headerUserId.isBlank())
                ? headerUserId
                : (request.getCreatedBy() != null ? request.getCreatedBy() : "unknown");

        try {
            CaseEntity caseEntity = caseManagementService.createCaseFromRequest(request, userId);

            // EM OS 186: If CIN is blank and CIN clearance was performed, include
            // informational message so frontend can display the SAWS referral notice
            if ((request.getCin() == null || request.getCin().isBlank())
                    && "PENDING_SAWS".equals(request.getMediCalStatus())) {
                Map<String, Object> response = new java.util.LinkedHashMap<>();
                response.put("case", caseEntity);
                response.put("infoMessage", "EM OS 186: CIN not selected, Medi-Cal Eligibility Referral will be sent to SAWS.");
                response.put("sawsReferralSent", true);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(caseEntity);
        } catch (IllegalArgumentException e) {
            // EM OS 067, EM OS 176, EM OS 177, and other field-level validation errors
            log.warn("[createCase] Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/approve")
    @RequirePermission(resource = "Case Resource", scope = "approve")
    public ResponseEntity<?> approveCase(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            CaseEntity caseEntity = caseManagementService.approveCase(id, userId);
            return ResponseEntity.ok(caseEntity);
        } catch (RuntimeException e) {
            log.warn("[approveCase] Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/deny")
    @RequirePermission(resource = "Case Resource", scope = "deny")
    public ResponseEntity<CaseEntity> denyCase(
            @PathVariable Long id,
            @RequestBody DenialRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.denyCase(id, request.getReason(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    @PutMapping("/{id}/terminate")
    @RequirePermission(resource = "Case Resource", scope = "terminate")
    public ResponseEntity<?> terminateCase(
            @PathVariable Long id,
            @RequestBody TerminationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            LocalDate authEndDate = request.getAuthorizationEndDate() != null
                    ? LocalDate.parse(request.getAuthorizationEndDate()) : null;
            CaseEntity caseEntity = caseManagementService.terminateCase(id, request.getReason(), authEndDate, userId);
            return ResponseEntity.ok(caseEntity);
        } catch (RuntimeException e) {
            log.warn("[terminateCase] Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/leave")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> placeCaseOnLeave(
            @PathVariable Long id,
            @RequestBody LeaveRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            LocalDate authEndDate = request.getAuthorizationEndDate() != null
                    ? LocalDate.parse(request.getAuthorizationEndDate()) : null;
            LocalDate suspEndDate = request.getResourceSuspensionEndDate() != null
                    ? LocalDate.parse(request.getResourceSuspensionEndDate()) : null;
            CaseEntity caseEntity = caseManagementService.placeCaseOnLeave(
                    id, request.getReason(), authEndDate, suspEndDate, userId);
            return ResponseEntity.ok(caseEntity);
        } catch (RuntimeException e) {
            log.warn("[placeCaseOnLeave] Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/withdraw")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> withdrawApplication(
            @PathVariable Long id,
            @RequestBody WithdrawalRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            LocalDate withdrawalDate = request.getWithdrawalDate() != null
                    ? LocalDate.parse(request.getWithdrawalDate()) : null;
            CaseEntity caseEntity = caseManagementService.withdrawApplication(
                    id, request.getReason(), withdrawalDate, userId);
            return ResponseEntity.ok(caseEntity);
        } catch (RuntimeException e) {
            log.warn("[withdrawApplication] Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== RESCIND (DSD Section 3.4) ====================

    @PutMapping("/{id}/rescind")
    @RequirePermission(resource = "Case Resource", scope = "rescind")
    public ResponseEntity<?> rescindCase(
            @PathVariable Long id,
            @RequestBody RescindRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            CaseEntity caseEntity = caseManagementService.rescindCase(id, request.getReason(), userId);
            return ResponseEntity.ok(caseEntity);
        } catch (IllegalArgumentException e) {
            log.warn("[rescindCase] Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // ==================== REACTIVATE (DSD Section 3.6) ====================

    @PutMapping("/{id}/reactivate")
    @RequirePermission(resource = "Case Resource", scope = "reactivate")
    public ResponseEntity<?> reactivateCase(
            @PathVariable Long id,
            @RequestBody ReactivateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        try {
            LocalDate refDate = request.getReferralDate() != null
                    ? LocalDate.parse(request.getReferralDate()) : null;
            CaseEntity caseEntity = caseManagementService.reactivateCase(
                    id, refDate, request.getMeetsResidencyRequirement(),
                    request.getReferralSource(), request.isInterpreterAvailable(),
                    request.getAssignedWorkerId(), request.getCinClearanceStatus(), userId);
            return ResponseEntity.ok(caseEntity);
        } catch (IllegalArgumentException e) {
            log.warn("[reactivateCase] Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== STATUS HISTORY ====================

    @GetMapping("/{id}/status-history")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<CaseStatusHistory>> getCaseStatusHistory(@PathVariable Long id) {
        List<CaseStatusHistory> history = caseManagementService.getCaseStatusHistory(id);
        return ResponseEntity.ok(history);
    }

    // ==================== CODE TABLES ====================

    @GetMapping("/code-tables")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getCodeTables() {
        return ResponseEntity.ok(caseManagementService.getCodeTables());
    }

    @PutMapping("/{id}/assign")
    @RequirePermission(resource = "Case Resource", scope = "assign")
    public ResponseEntity<CaseEntity> assignCase(
            @PathVariable Long id,
            @RequestBody AssignmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.assignCaseToCaseworker(id, request.getCaseOwnerId(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    // ==================== SEARCH ====================

    @GetMapping("/search")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<Map<String, Object>>> searchCases(
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) String cin,
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String caseOwnerId,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {

        CaseStatus caseStatus = status != null ? CaseStatus.valueOf(status) : null;
        List<CaseEntity> cases = caseManagementService.searchCases(caseNumber, cin, countyCode, caseOwnerId, caseStatus);

        List<Map<String, Object>> filteredCases = cases.stream()
                .map(c -> fieldAuthService.filterFieldsForRole(c, roles, "Case Resource"))
                .toList();

        return ResponseEntity.ok(filteredCases);
    }

    @GetMapping("/due-for-reassessment")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<CaseEntity>> getCasesDueForReassessment(
            @RequestParam(required = false) String date) {

        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now().plusDays(30);
        List<CaseEntity> cases = caseManagementService.getCasesDueForReassessment(targetDate);
        return ResponseEntity.ok(cases);
    }

    // ==================== CASE NOTES ====================

    @GetMapping("/{id}/notes")
    @RequirePermission(resource = "Case Notes Resource", scope = "view")
    public ResponseEntity<List<CaseNoteEntity>> getCaseNotes(@PathVariable Long id) {
        List<CaseNoteEntity> notes = caseManagementService.getCaseNotes(id);
        return ResponseEntity.ok(notes);
    }

    /**
     * Download all case notes as a PDF report.
     * GET /api/cases/{id}/notes/pdf
     */
    @GetMapping("/{id}/notes/pdf")
    @RequirePermission(resource = "Case Notes Resource", scope = "view")
    public ResponseEntity<?> downloadCaseNotesPdf(@PathVariable Long id) {
        try {
            List<CaseNoteEntity> notes = caseManagementService.getCaseNotes(id);
            byte[] pdfBytes = pdfService.generateCaseNotesPDF(id, notes);
            String filename = "case-notes-" + id + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("Error generating case notes PDF for caseId={}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/notes")
    @RequirePermission(resource = "Case Notes Resource", scope = "create")
    public ResponseEntity<CaseNoteEntity> addCaseNote(
            @PathVariable Long id,
            @RequestBody CaseNoteRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseNoteEntity note = caseManagementService.addCaseNote(
                id, request.getNoteType(), request.getSubject(), request.getContent(), userId);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/notes/{noteId}/append")
    @RequirePermission(resource = "Case Notes Resource", scope = "edit")
    public ResponseEntity<CaseNoteEntity> appendToNote(
            @PathVariable Long noteId,
            @RequestBody AppendNoteRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseNoteEntity note = caseManagementService.appendToNote(noteId, request.getContent(), userId);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/notes/{noteId}/cancel")
    @RequirePermission(resource = "Case Notes Resource", scope = "delete")
    public ResponseEntity<CaseNoteEntity> cancelNote(
            @PathVariable Long noteId,
            @RequestBody CancelNoteRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseNoteEntity note = caseManagementService.cancelNote(noteId, request.getReason(), userId);
        return ResponseEntity.ok(note);
    }

    // ==================== CASE CONTACTS ====================

    @GetMapping("/{id}/contacts")
    @RequirePermission(resource = "Case Contacts Resource", scope = "view")
    public ResponseEntity<List<CaseContactEntity>> getCaseContacts(@PathVariable Long id) {
        List<CaseContactEntity> contacts = caseManagementService.getActiveContacts(id);
        return ResponseEntity.ok(contacts);
    }

    @PostMapping("/{id}/contacts")
    @RequirePermission(resource = "Case Contacts Resource", scope = "create")
    public ResponseEntity<CaseContactEntity> addCaseContact(
            @PathVariable Long id,
            @RequestBody CaseContactEntity contact,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        contact.setCaseId(id);
        CaseContactEntity savedContact = caseManagementService.addCaseContact(contact, userId);
        return ResponseEntity.ok(savedContact);
    }

    @PutMapping("/contacts/{contactId}/inactivate")
    @RequirePermission(resource = "Case Contacts Resource", scope = "delete")
    public ResponseEntity<CaseContactEntity> inactivateContact(
            @PathVariable Long contactId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseContactEntity contact = caseManagementService.inactivateContact(contactId, userId);
        return ResponseEntity.ok(contact);
    }

    // ==================== INTER-COUNTY TRANSFER ====================

    @PostMapping("/{id}/transfer/initiate")
    @RequirePermission(resource = "Case Resource", scope = "transfer")
    public ResponseEntity<CaseEntity> initiateTransfer(
            @PathVariable Long id,
            @RequestBody TransferRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.initiateInterCountyTransfer(
                id, request.getReceivingCountyCode(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    @PostMapping("/{id}/transfer/complete")
    @RequirePermission(resource = "Case Resource", scope = "transfer")
    public ResponseEntity<CaseEntity> completeTransfer(
            @PathVariable Long id,
            @RequestBody CompleteTransferRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.completeInterCountyTransfer(
                id, request.getNewCaseOwnerId(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    @PostMapping("/{id}/transfer/cancel")
    @RequirePermission(resource = "Case Resource", scope = "transfer")
    public ResponseEntity<CaseEntity> cancelTransfer(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.cancelInterCountyTransfer(id, userId);
        return ResponseEntity.ok(caseEntity);
    }

    @PutMapping("/{id}/funding-source")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<CaseEntity> updateFundingSource(
            @PathVariable Long id,
            @RequestBody FundingSourceRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.updateFundingSource(
                id, request.getFundingSource(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    // ==================== COMPANION CASES ====================

    @GetMapping("/{recipientId}/companion-cases")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<RecipientEntity>> findCompanionCases(@PathVariable Long recipientId) {
        List<RecipientEntity> companions = caseManagementService.findCompanionCases(recipientId);
        return ResponseEntity.ok(companions);
    }

    // ==================== STATISTICS ====================

    @GetMapping("/statistics/{countyCode}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<CaseManagementService.CaseStatistics> getCaseStatistics(
            @PathVariable String countyCode) {

        CaseManagementService.CaseStatistics stats = caseManagementService.getCaseStatistics(countyCode);
        return ResponseEntity.ok(stats);
    }

    // ==================== REQUEST DTOs ====================

    @lombok.Data
    public static class CreateCaseRequest {
        // Existing path: provide a pre-existing recipientId
        private Long recipientId;
        private String caseOwnerId;
        private String countyCode;

        // New-case path: demographics sent directly from Create Case form.
        // When recipientId is null, the service creates the RecipientEntity first.
        private String lastName;
        private String firstName;
        private String gender;
        private String dateOfBirth;
        private String ssn;
        private String zipCode;
        private String spokenLanguage;
        private String writtenLanguage;
        private Boolean interpreterAvailable;
        private String ihssReferralDate;

        // CIN clearance info carried through from the frontend
        private String cin;
        private String cinClearanceStatus;   // e.g. "CLEARED", "NO_MATCH", "NOT_STARTED"
        private String mediCalStatus;        // "ACTIVE", "INACTIVE", "PENDING_SAWS"
        private String aidCode;
        private String createdBy;
        private String applicantName;
    }

    @lombok.Data
    public static class DenialRequest {
        private String reason;
    }

    @lombok.Data
    public static class TerminationRequest {
        private String reason;
        private String authorizationEndDate; // DSD: required, MM/DD/YYYY
    }

    @lombok.Data
    public static class LeaveRequest {
        private String reason;
        private String authorizationEndDate;       // DSD: required
        private String resourceSuspensionEndDate;  // DSD: conditional (L0006 only)
    }

    @lombok.Data
    public static class WithdrawalRequest {
        private String reason;
        private String withdrawalDate; // DSD: required
    }

    @lombok.Data
    public static class RescindRequest {
        private String reason; // R0001-R0005
    }

    @lombok.Data
    public static class ReactivateRequest {
        private String referralDate;
        private String meetsResidencyRequirement;
        private String referralSource;
        private boolean interpreterAvailable;
        private String assignedWorkerId;
        private String cinClearanceStatus;  // CLEARED, NO_MATCH, WITHOUT_CIN, NOT_STARTED
    }

    @lombok.Data
    public static class AssignmentRequest {
        private String caseOwnerId;
    }

    @lombok.Data
    public static class CaseNoteRequest {
        private String noteType;
        private String subject;
        private String content;
    }

    @lombok.Data
    public static class AppendNoteRequest {
        private String content;
    }

    @lombok.Data
    public static class CancelNoteRequest {
        private String reason;
    }

    @lombok.Data
    public static class TransferRequest {
        private String receivingCountyCode;
    }

    @lombok.Data
    public static class CompleteTransferRequest {
        private String newCaseOwnerId;
    }

    @lombok.Data
    public static class FundingSourceRequest {
        private String fundingSource;
    }

    // ==================== CASE MAINTENANCE: WORKWEEK AGREEMENTS ====================

    @GetMapping("/{caseId}/workweek-agreements")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getWorkweekAgreements(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseMaintenanceService.getWorkweekAgreementsForCase(caseId));
    }

    @GetMapping("/{caseId}/workweek-agreements/history")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getWorkweekAgreementHistory(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseMaintenanceService.getWorkweekAgreementHistoryForCase(caseId));
    }

    @PostMapping("/{caseId}/workweek-agreements")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createWorkweekAgreement(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.createWorkweekAgreement(caseId, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/workweek-agreements/{id}")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> updateWorkweekAgreement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.updateWorkweekAgreement(id, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/workweek-agreements/{id}/inactivate")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> inactivateWorkweekAgreement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            String reason = request.get("reason") != null ? (String) request.get("reason") : null;
            return ResponseEntity.ok(caseMaintenanceService.inactivateWorkweekAgreement(id, reason, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CASE MAINTENANCE: OVERTIME AGREEMENTS ====================

    @GetMapping("/{caseId}/overtime-agreements")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getOvertimeAgreements(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseMaintenanceService.getOvertimeAgreements(caseId));
    }

    @PostMapping("/{caseId}/overtime-agreements")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createOvertimeAgreement(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.createOvertimeAgreement(caseId, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/overtime-agreements/{id}/inactivate")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> inactivateOvertimeAgreement(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.inactivateOvertimeAgreement(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CASE MAINTENANCE: WPCS HOURS ====================

    @GetMapping("/{caseId}/wpcs-hours")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getWpcsHours(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseMaintenanceService.getWpcsHours(caseId));
    }

    @PostMapping("/{caseId}/wpcs-hours")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createWpcsHours(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.createWpcsHours(caseId, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/wpcs-hours/{id}/inactivate")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> inactivateWpcsHours(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.inactivateWpcsHours(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CASE MAINTENANCE: WORKPLACE HOURS ====================

    @GetMapping("/{caseId}/workplace-hours")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getWorkplaceHours(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseMaintenanceService.getWorkplaceHours(caseId));
    }

    @PostMapping("/{caseId}/workplace-hours")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createWorkplaceHours(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.createWorkplaceHours(caseId, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/workplace-hours/{id}/inactivate")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> inactivateWorkplaceHours(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.inactivateWorkplaceHours(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CASE MAINTENANCE: ESP ENROLLMENT ====================

    @GetMapping("/{caseId}/esp-registrations")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getEspRegistrations(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseMaintenanceService.getEspRegistrations(caseId));
    }

    @PutMapping("/esp-registrations/{espId}/inactivate")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> inactivateEspRegistration(
            @PathVariable String espId,
            @RequestBody(required = false) Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            String reason = request != null && request.get("reason") != null
                    ? (String) request.get("reason") : null;
            return ResponseEntity.ok(caseMaintenanceService.inactivateEspRegistration(espId, reason, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/esp-registrations/{espId}/reactivate")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> reactivateEspRegistration(
            @PathVariable String espId,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.reactivateEspRegistration(espId, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/esp-registrations/{espId}/soc2321")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> downloadSoc2321(@PathVariable String espId) {
        return ResponseEntity.ok(Map.of(
                "espId", espId,
                "formType", "SOC_2321",
                "message", "SOC 2321 — Account Inactivation Notice. PDF generation endpoint."
        ));
    }

    // ==================== CASE MAINTENANCE: REASSESSMENT ====================

    @PostMapping("/{id}/schedule-reassessment")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> scheduleReassessment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            String rawDate = request.get("dueDate") != null
                    ? (String) request.get("dueDate")
                    : (request.get("reassessmentDueDate") != null ? (String) request.get("reassessmentDueDate") : null);
            LocalDate dueDate = rawDate != null ? LocalDate.parse(rawDate) : null;
            return ResponseEntity.ok(caseMaintenanceService.scheduleReassessment(id, dueDate, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CASE MAINTENANCE: MEDI-CAL SOC ====================

    @GetMapping("/{caseId}/medi-cal-soc")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getMediCalSoc(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseMaintenanceService.getMediCalSoc(caseId));
    }

    @PutMapping("/{caseId}/medi-cal-soc")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> updateMediCalSoc(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            CaseEntity updated = caseMaintenanceService.updateMediCalSoc(caseId, request, userId);
            return ResponseEntity.ok(Map.of(
                    "caseId", updated.getId(),
                    "shareOfCostAmount", updated.getShareOfCostAmount() != null ? updated.getShareOfCostAmount() : 0,
                    "countableIncome", updated.getCountableIncome() != null ? updated.getCountableIncome() : 0,
                    "netIncome", updated.getNetIncome() != null ? updated.getNetIncome() : 0
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{caseId}/medi-cal-eligibility")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getMediCalEligibility(@PathVariable Long caseId) {
        // Delegates to SAWSService for real eligibility lookup — mock for now
        return ResponseEntity.ok(Map.of(
                "caseId", caseId,
                "message", "Medi-Cal eligibility lookup — connects to SAWS in production.",
                "eligible", true
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // Health Care Certification (DSD Section 21 — BR SE 28-50)
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/{caseId}/health-care-cert")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getHealthCareCerts(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseMaintenanceService.getHealthCareCertifications(caseId));
    }

    @PostMapping("/{caseId}/health-care-cert")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> createHealthCareCert(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.createHealthCareCertification(caseId, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/health-care-cert/{certId}")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> updateHealthCareCert(
            @PathVariable Long certId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.updateHealthCareCertification(certId, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{caseId}/health-care-cert/good-cause")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> grantGoodCauseExtension(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.grantGoodCauseExtension(caseId, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/health-care-cert/{certId}/inactivate")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> inactivateHealthCareCert(
            @PathVariable Long certId,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.inactivateHealthCareCertification(certId, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Notices of Action (NOA) — NA 1250–1257
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/{caseId}/noas")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getNoasForCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseMaintenanceService.getNoasForCase(caseId));
    }

    @PostMapping("/{caseId}/noas")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> generateNoa(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.generateNoa(caseId, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/noas/{noaId}/print")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> printNoa(
            @PathVariable Long noaId,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(caseMaintenanceService.printNoa(noaId, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/noas/{noaId}/suppress")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> suppressNoa(
            @PathVariable Long noaId,
            @RequestBody(required = false) Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            Map<String, Object> body = request != null ? request : Map.of();
            return ResponseEntity.ok(caseMaintenanceService.suppressNoa(noaId, body, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== FPO ELIGIBILITY (CI-67555) ====================

    /** GET current active FPO eligibility for a case */
    @GetMapping("/{caseId}/fpo-eligibility")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getFpoEligibility(
            @PathVariable Long caseId) {
        try {
            return ResponseEntity.ok(caseManagementService.getFpoEligibility(caseId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET full FPO eligibility history for a case */
    @GetMapping("/{caseId}/fpo-eligibility/history")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getFpoEligibilityHistory(
            @PathVariable Long caseId) {
        try {
            return ResponseEntity.ok(caseManagementService.getFpoEligibilityHistory(caseId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT set (create or update) FPO eligibility for a case */
    @PutMapping("/{caseId}/fpo-eligibility")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> setFpoEligibility(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            Boolean fpoEligible = request.get("fpoEligible") != null
                    ? Boolean.parseBoolean(request.get("fpoEligible").toString()) : null;
            LocalDate beginDate = request.get("beginDate") != null
                    ? LocalDate.parse(request.get("beginDate").toString()) : null;
            LocalDate endDate = request.get("endDate") != null
                    ? LocalDate.parse(request.get("endDate").toString()) : null;
            String notes = request.get("notes") != null ? request.get("notes").toString() : null;

            return ResponseEntity.ok(caseManagementService.setFpoEligibility(
                    caseId, fpoEligible, beginDate, endDate, notes, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
