package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.ApplicationEntity;
import com.cmips.entity.ApplicationEntity.*;
import com.cmips.entity.RecipientEntity;
import com.cmips.service.ApplicationService;
import com.cmips.service.ApplicationService.DuplicateCheckRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Application Processing
 * Implements DSD Section 20 - IHSS Application Processing with 45-day timeline
 */
@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // ==================== DUPLICATE CHECK (BR-1, BR-4, BR-5) ====================

    /**
     * Check for duplicate persons before creating a referral or application.
     * Body: { lastName, firstName, dateOfBirth (yyyy-MM-dd), ssn (optional) }
     * Returns list of matching RecipientEntity records.
     */
    @PostMapping("/duplicate-check")
    @RequirePermission(resource = "Application Resource", scope = "create")
    public ResponseEntity<?> duplicateCheck(@RequestBody DuplicateCheckRequest req) {
        try {
            java.util.List<RecipientEntity> matches = applicationService.findDuplicates(req);
            return ResponseEntity.ok(matches);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error during duplicate check", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CREATE ====================

    /**
     * Create a new application
     */
    @PostMapping
    @RequirePermission(resource = "Application Resource", scope = "create")
    public ResponseEntity<?> createApplication(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Long recipientId = request.get("recipientId") != null ?
                    ((Number) request.get("recipientId")).longValue() : null;

            // Validate recipientId is provided
            if (recipientId == null) {
                return ResponseEntity.badRequest().body(Map.of("error",
                    "recipientId is required. Search for or create a person first, then link their ID."));
            }

            // Validate SSN if provided (EM-237/238/240)
            String ssn = (String) request.get("ssn");
            if (ssn != null && !ssn.isBlank()) {
                applicationService.validateSsn(ssn.replaceAll("[^0-9]", ""));
            }

            // Validate DOB if provided (EM-203/204)
            String dob = (String) request.get("dateOfBirth");
            if (dob != null && !dob.isBlank()) {
                applicationService.validateDob(dob);
            }

            ApplicationEntity application = ApplicationEntity.builder()
                    .countyCode((String) request.get("countyCode"))
                    .programType(request.get("programType") != null ?
                            ProgramType.valueOf((String) request.get("programType")) : ProgramType.IHSS)
                    .assignedWorkerId((String) request.get("assignedWorkerId"))
                    .createdBy(userId)
                    .build();

            ApplicationEntity created = applicationService.createApplication(application, recipientId, userId);
            log.info("Application created: {} by user: {}", created.getId(), userId);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating application: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating application", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create application from referral
     */
    @PostMapping("/from-referral/{referralId}")
    @RequirePermission(resource = "Application Resource", scope = "create")
    public ResponseEntity<?> createFromReferral(@PathVariable String referralId,
                                                 @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();

            ApplicationEntity application = ApplicationEntity.builder()
                    .countyCode((String) request.get("countyCode"))
                    .programType(request.get("programType") != null ?
                            ProgramType.valueOf((String) request.get("programType")) : ProgramType.IHSS)
                    .assignedWorkerId((String) request.get("assignedWorkerId"))
                    .createdBy(userId)
                    .build();

            ApplicationEntity created = applicationService.createFromReferral(referralId, application, userId);

            log.info("Application created from referral {}: {} by {}", referralId, created.getId(), userId);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating application from referral", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CIN CLEARANCE ====================

    /**
     * Initiate CIN clearance
     */
    @PostMapping("/{applicationId}/cin-clearance")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> initiateCINClearance(@PathVariable String applicationId) {
        try {
            String userId = getCurrentUserId();
            ApplicationEntity updated = applicationService.performCINClearance(applicationId, userId);
            log.info("CIN clearance initiated for application: {} by {}", applicationId, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error initiating CIN clearance", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Select CIN match with demographic comparison (Scenarios 4, 5, 6 / BR 1, BR 13, EM-202)
     * Body: { cin, lastName, firstName, gender, dob, aidCode, eligibilityStatus, mediCalActive, effectiveDate }
     */
    @PostMapping("/{applicationId}/select-cin")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> selectCINMatch(@PathVariable String applicationId,
                                             @RequestBody Map<String, Object> request) {
        try {
            String userId      = getCurrentUserId();
            String selectedCin = (String) request.get("cin");

            // Use the demographic-aware version
            Map<String, Object> result = applicationService
                    .selectCINWithDemographicCheck(applicationId, selectedCin, request, userId);

            log.info("select-cin result for application {}: {}", applicationId, result.get("result"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error selecting CIN match", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Save Create Case without CIN (EM-176 / EM-185 / BR 9)
     * Called when user clicks Continue on the CreateCaseWithoutCIN confirmation modal.
     */
    @PostMapping("/{applicationId}/save-without-cin")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> saveWithoutCIN(@PathVariable String applicationId) {
        try {
            String userId = getCurrentUserId();
            Map<String, Object> result = applicationService.saveWithoutCIN(applicationId, userId);
            log.info("save-without-cin result for application {}: {}", applicationId, result.get("result"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in save-without-cin", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== MEDI-CAL VERIFICATION ====================

    /**
     * Initiate MEDS verification
     */
    @PostMapping("/{applicationId}/meds-verification")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> initiateMEDSVerification(@PathVariable String applicationId) {
        try {
            String userId = getCurrentUserId();
            ApplicationEntity updated = applicationService.performMEDSVerification(applicationId, userId);
            log.info("MEDS verification initiated for application: {} by {}", applicationId, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error initiating MEDS verification", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== UPDATE STATUS ====================

    /**
     * Update application status
     */
    @PatchMapping("/{applicationId}/status")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> updateStatus(@PathVariable String applicationId,
                                           @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            ApplicationStatus newStatus = ApplicationStatus.valueOf(request.get("status"));

            ApplicationEntity updated = applicationService.updateStatus(applicationId, newStatus, userId);
            log.info("Application {} status updated to {} by {}", applicationId, newStatus, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating application status", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== ASSESSMENT ====================

    /**
     * Schedule functional assessment
     */
    @PostMapping("/{applicationId}/schedule-assessment")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> scheduleAssessment(@PathVariable String applicationId,
                                                 @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            LocalDate assessmentDate = LocalDate.parse(request.get("assessmentDate"));
            String assessmentType = request.get("assessmentType"); // HOME_VISIT, PHONE, VIDEO

            ApplicationEntity updated = applicationService.scheduleAssessment(
                    applicationId, assessmentDate, assessmentType, userId);
            log.info("Assessment scheduled for application: {} on {}", applicationId, assessmentDate);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error scheduling assessment", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Complete assessment
     */
    @PostMapping("/{applicationId}/complete-assessment")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> completeAssessment(@PathVariable String applicationId) {
        try {
            String userId = getCurrentUserId();

            ApplicationEntity updated = applicationService.completeAssessment(applicationId, userId);
            log.info("Assessment completed for application: {}", applicationId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error completing assessment", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== APPROVE / DENY / WITHDRAW ====================

    /**
     * Approve application
     */
    @PostMapping("/{applicationId}/approve")
    @RequirePermission(resource = "Application Resource", scope = "approve")
    public ResponseEntity<?> approveApplication(@PathVariable String applicationId) {
        try {
            String userId = getCurrentUserId();

            ApplicationEntity approved = applicationService.approveApplication(applicationId, userId);
            log.info("Application {} approved by {}", applicationId, userId);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            log.error("Error approving application", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deny application
     */
    @PostMapping("/{applicationId}/deny")
    @RequirePermission(resource = "Application Resource", scope = "deny")
    public ResponseEntity<?> denyApplication(@PathVariable String applicationId,
                                              @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            DenialCode denialCode = DenialCode.valueOf(request.get("denialCode"));
            String denialReason = request.get("denialReason");

            ApplicationEntity denied = applicationService.denyApplication(
                    applicationId, denialCode, denialReason, userId);
            log.info("Application {} denied by {} - Reason: {}", applicationId, userId, denialCode);
            return ResponseEntity.ok(denied);
        } catch (Exception e) {
            log.error("Error denying application", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Withdraw application
     */
    @PostMapping("/{applicationId}/withdraw")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> withdrawApplication(@PathVariable String applicationId,
                                                  @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String withdrawReason = request.get("reason");

            ApplicationEntity withdrawn = applicationService.withdrawApplication(
                    applicationId, withdrawReason, userId);
            log.info("Application {} withdrawn by {}", applicationId, userId);
            return ResponseEntity.ok(withdrawn);
        } catch (Exception e) {
            log.error("Error withdrawing application", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== DEADLINE EXTENSION ====================

    /**
     * Extend 45-day deadline
     */
    @PostMapping("/{applicationId}/extend-deadline")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> extendDeadline(@PathVariable String applicationId,
                                             @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Integer extensionDays = ((Number) request.get("extensionDays")).intValue();
            String extensionReason = (String) request.get("reason");

            ApplicationEntity extended = applicationService.extendDeadline(
                    applicationId, extensionDays, extensionReason, userId);
            log.info("Application {} deadline extended by {} days by {}",
                    applicationId, extensionDays, userId);
            return ResponseEntity.ok(extended);
        } catch (Exception e) {
            log.error("Error extending deadline", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== DOCUMENTATION ====================

    /**
     * Update SOC 873 received status
     */
    @PatchMapping("/{applicationId}/soc873")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> updateSoc873(@PathVariable String applicationId,
                                           @RequestBody Map<String, Boolean> request) {
        try {
            String userId = getCurrentUserId();
            Boolean received = request.get("received");

            ApplicationEntity updated = applicationService.updateSoc873Status(applicationId, received, userId);
            log.info("SOC 873 status updated for application: {} - Received: {}", applicationId, received);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating SOC 873 status", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update medical certification received status
     */
    @PatchMapping("/{applicationId}/medical-cert")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> updateMedicalCert(@PathVariable String applicationId,
                                                @RequestBody Map<String, Boolean> request) {
        try {
            String userId = getCurrentUserId();
            Boolean received = request.get("received");

            ApplicationEntity updated = applicationService.updateMedicalCertification(applicationId, received, userId);
            log.info("Medical certification status updated for application: {} - Received: {}", applicationId, received);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating medical certification status", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Link application to case
     */
    @PostMapping("/{applicationId}/link-case")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> linkToCase(@PathVariable String applicationId,
                                         @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Long caseId = ((Number) request.get("caseId")).longValue();
            String caseNumber = (String) request.get("caseNumber");

            ApplicationEntity updated = applicationService.linkToCase(applicationId, caseId, caseNumber, userId);
            log.info("Application {} linked to case {}", applicationId, caseNumber);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error linking application to case", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== QUERY ====================

    /**
     * Get application by ID
     */
    @GetMapping("/{applicationId}")
    @RequirePermission(resource = "Application Resource", scope = "view")
    public ResponseEntity<?> getApplication(@PathVariable String applicationId) {
        try {
            ApplicationEntity application = applicationService.getApplicationById(applicationId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            log.error("Error getting application", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get application by application number
     */
    @GetMapping("/by-number/{applicationNumber}")
    @RequirePermission(resource = "Application Resource", scope = "view")
    public ResponseEntity<?> getByApplicationNumber(@PathVariable String applicationNumber) {
        try {
            ApplicationEntity application = applicationService.getApplicationByNumber(applicationNumber);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            log.error("Error getting application by number", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get applications with filters
     */
    @GetMapping
    @RequirePermission(resource = "Application Resource", scope = "view")
    public ResponseEntity<?> getApplications(
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workerId) {
        try {
            List<ApplicationEntity> applications;

            if (countyCode != null) {
                applications = applicationService.getApplicationsByCounty(countyCode);
            } else if (status != null) {
                applications = applicationService.getApplicationsByStatus(ApplicationStatus.valueOf(status));
            } else if (workerId != null) {
                applications = applicationService.getApplicationsByWorker(workerId);
            } else {
                applications = applicationService.getAllApplications();
            }

            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("Error getting applications", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get pending applications
     */
    @GetMapping("/pending")
    @RequirePermission(resource = "Application Resource", scope = "view")
    public ResponseEntity<?> getPendingApplications(@RequestParam(required = false) String countyCode) {
        try {
            List<ApplicationEntity> applications;
            if (countyCode != null) {
                applications = applicationService.getPendingApplicationsByCounty(countyCode);
            } else {
                applications = applicationService.getPendingApplications();
            }
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("Error getting pending applications", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get overdue applications (past 45-day deadline)
     */
    @GetMapping("/overdue")
    @RequirePermission(resource = "Application Resource", scope = "view")
    public ResponseEntity<?> getOverdueApplications(@RequestParam(required = false) String countyCode) {
        try {
            List<ApplicationEntity> applications;
            if (countyCode != null) {
                applications = applicationService.getOverdueApplicationsByCounty(countyCode);
            } else {
                applications = applicationService.getOverdueApplications();
            }
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("Error getting overdue applications", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get applications approaching deadline
     */
    @GetMapping("/approaching-deadline")
    @RequirePermission(resource = "Application Resource", scope = "view")
    public ResponseEntity<?> getApproachingDeadline(
            @RequestParam(defaultValue = "7") int daysUntilDeadline) {
        try {
            List<ApplicationEntity> applications = applicationService.getApproachingDeadline(daysUntilDeadline);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("Error getting applications approaching deadline", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search applications
     */
    @GetMapping("/search")
    @RequirePermission(resource = "Application Resource", scope = "view")
    public ResponseEntity<?> searchApplications(
            @RequestParam(required = false) String applicationNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String programType,
            @RequestParam(required = false) String assignedWorkerId,
            @RequestParam(required = false) String cin,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<ApplicationEntity> applications = applicationService.searchApplications(
                    applicationNumber,
                    status != null ? ApplicationStatus.valueOf(status) : null,
                    countyCode,
                    programType != null ? ProgramType.valueOf(programType) : null,
                    assignedWorkerId,
                    cin,
                    startDate != null ? LocalDate.parse(startDate) : null,
                    endDate != null ? LocalDate.parse(endDate) : null
            );
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("Error searching applications", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Get application statistics by county
     */
    @GetMapping("/stats/{countyCode}")
    @RequirePermission(resource = "Application Resource", scope = "view")
    public ResponseEntity<?> getApplicationStats(@PathVariable String countyCode) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("countyCode", countyCode);
            stats.put("pendingCount", applicationService.countPendingByCounty(countyCode));
            stats.put("overdueCount", applicationService.countOverdueByCounty(countyCode));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting application stats", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get timeline status for an application
     */
    @GetMapping("/{applicationId}/timeline")
    @RequirePermission(resource = "Application Resource", scope = "view")
    public ResponseEntity<?> getTimelineStatus(@PathVariable String applicationId) {
        try {
            ApplicationEntity application = applicationService.getApplicationById(applicationId);
            Map<String, Object> timeline = new HashMap<>();
            timeline.put("applicationId", applicationId);
            timeline.put("applicationDate", application.getApplicationDate());
            timeline.put("deadlineDate", application.getDeadlineDate());
            timeline.put("extendedDeadlineDate", application.getExtendedDeadlineDate());
            timeline.put("daysRemaining", application.getDaysRemaining());
            timeline.put("isOverdue", application.isOverdue());
            timeline.put("timelineStatus", application.getTimelineStatus());
            timeline.put("status", application.getStatus());
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            log.error("Error getting timeline status", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getName();
        }
        return "anonymous";
    }
}
