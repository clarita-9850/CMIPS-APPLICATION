package com.cmips.service;

import com.cmips.entity.CaseEntity;
import com.cmips.entity.Notification;
import com.cmips.entity.Task;
import com.cmips.entity.TaskType;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.TaskRepository;
import com.cmips.repository.TaskTypeRepository;
import com.cmips.util.BusinessDayCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles creation of tasks and notifications for DSD Section 30 — Case Maintenance
 * (CM items 01 through 81). Each public method corresponds to a trigger condition
 * defined in the DSD.
 */
@Service
public class CaseMaintenanceTaskService {

    private static final Logger log = LoggerFactory.getLogger(CaseMaintenanceTaskService.class);

    private final TaskService taskService;
    private final NotificationService notificationService;
    private final TaskTypeRepository taskTypeRepo;
    private final TaskRepository taskRepo;
    private final CaseRepository caseRepo;
    private final BusinessDayCalculator businessDayCalc;

    public CaseMaintenanceTaskService(TaskService taskService,
                                       NotificationService notificationService,
                                       TaskTypeRepository taskTypeRepo,
                                       TaskRepository taskRepo,
                                       CaseRepository caseRepo,
                                       BusinessDayCalculator businessDayCalc) {
        this.taskService = taskService;
        this.notificationService = notificationService;
        this.taskTypeRepo = taskTypeRepo;
        this.taskRepo = taskRepo;
        this.caseRepo = caseRepo;
        this.businessDayCalc = businessDayCalc;
    }

    // ============================================================
    // Group A: Online/Screen-Action Triggers
    // ============================================================

    /** CM 01 — Recipient address changed to out of state */
    public void onAddressChangedOutOfState(CaseEntity caseEntity, String changedByUserId) {
        createCmTask("CM-001", caseEntity,
                "Address Out of State - " + caseEntity.getCaseNumber(),
                "Recipient address changed to outside California — requires case review");
        log.info("CM-001: Address out-of-state task created for case {}", caseEntity.getCaseNumber());
    }

    /** CM 05 — Recipient SSN changed (notification) */
    public void onSsnChanged(CaseEntity caseEntity, String oldSsn, String newSsn) {
        createCmNotification("CM-005", caseEntity,
                "Recipient SSN updated on case " + caseEntity.getCaseNumber());
        log.info("CM-005: SSN changed notification for case {}", caseEntity.getCaseNumber());
    }

    /** CM 06 — ICT referral received from sending county */
    public void onIctCreated(CaseEntity caseEntity, String receivingCounty) {
        createCmTask("CM-006", caseEntity,
                "ICT Referral - " + caseEntity.getCaseNumber(),
                "Inter-County Transfer referral received from sending county " + caseEntity.getSendingCountyCode());

        // CM 10 — If case has existing state hearing, also send notification
        if (caseEntity.getStateHearing() != null && !caseEntity.getStateHearing().isEmpty()) {
            createCmNotification("CM-010", caseEntity,
                    "ICT requested for case " + caseEntity.getCaseNumber() + " which has a pending state hearing");
        }
        log.info("CM-006: ICT referral task created for case {}", caseEntity.getCaseNumber());
    }

    /** CM 09 — State hearing added for case with active ICT (task + 2 notifications) */
    public void onStateHearingAddedWithIct(CaseEntity caseEntity, String hearingId) {
        // Task to ICT coordinator
        createCmTask("CM-009-T", caseEntity,
                "ICT State Hearing - " + caseEntity.getCaseNumber(),
                "State hearing added for case with active ICT — coordinator action required");

        // Notification to case owner
        createCmNotification("CM-009-N1", caseEntity,
                "State hearing " + hearingId + " added for ICT case " + caseEntity.getCaseNumber());

        // Notification to receiving worker (use case owner for now; in full implementation, lookup receiving worker)
        createCmNotification("CM-009-N2", caseEntity,
                "State hearing " + hearingId + " added for ICT case " + caseEntity.getCaseNumber() + " — receiving worker notification");

        log.info("CM-009: State hearing + ICT task/notifications for case {}", caseEntity.getCaseNumber());
    }

    /** CM 11 — Application withdrawal (notification) */
    public void onApplicationWithdrawn(CaseEntity caseEntity) {
        createCmNotification("CM-011", caseEntity,
                "Application withdrawn for case " + caseEntity.getCaseNumber());
        log.info("CM-011: Application withdrawal notification for case {}", caseEntity.getCaseNumber());
    }

    /** CM 23 — ICT cancelled (task + 2 notifications) */
    public void onIctCancelled(CaseEntity caseEntity, String cancelledBy) {
        // Task to receiving worker
        createCmTask("CM-023-T", caseEntity,
                "ICT Cancelled - " + caseEntity.getCaseNumber(),
                "Inter-County Transfer cancelled — complete remaining actions");

        // Notification to case owner
        createCmNotification("CM-023-N1", caseEntity,
                "ICT cancelled for case " + caseEntity.getCaseNumber() + " by " + cancelledBy);

        // Notification to ICT coordinator
        createCmTaskWithQueue("CM-023-N2", caseEntity, "ICT_COORDINATOR",
                "ICT cancelled for case " + caseEntity.getCaseNumber());

        log.info("CM-023: ICT cancelled task/notifications for case {}", caseEntity.getCaseNumber());
    }

    /** CM 25 — ICT case assigned to new worker (notification) */
    public void onIctCaseAssigned(CaseEntity caseEntity, String newWorkerId) {
        createCmNotification("CM-025", caseEntity,
                "ICT case " + caseEntity.getCaseNumber() + " has been assigned to you");
        log.info("CM-025: ICT case assigned notification for case {}", caseEntity.getCaseNumber());
    }

    /** CM 35 — State hearing granted, compliance form due */
    public void onStateHearingGrantedNeedsCompliance(CaseEntity caseEntity, String hearingId) {
        createCmTask("CM-035", caseEntity,
                "Compliance Form Due - " + caseEntity.getCaseNumber(),
                "State hearing granted — compliance form due for hearing " + hearingId);
        log.info("CM-035: State hearing compliance task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 38 — IHSS recipient placed on leave, verify WPCS */
    public void onCaseOnLeaveWithWpcsHours(CaseEntity caseEntity) {
        createCmTaskWithQueue("CM-038", caseEntity, "WPCS_CASE",
                "IHSS recipient on leave — verify WPCS hours for case " + caseEntity.getCaseNumber());
        log.info("CM-038: Case on leave WPCS task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 39 — Recipient address changed by non-case owner (notification) */
    public void onAddressChangedByNonCaseOwner(CaseEntity caseEntity, String changedBy) {
        createCmNotification("CM-039", caseEntity,
                "Recipient address changed on case " + caseEntity.getCaseNumber() + " by " + changedBy);
        log.info("CM-039: Address changed by non-owner notification for case {}", caseEntity.getCaseNumber());
    }

    /** CM 40 — WPCS recipient back from leave */
    public void onCaseBackFromLeaveWithWpcsHours(CaseEntity caseEntity) {
        createCmTaskWithQueue("CM-040", caseEntity, "WPCS_CASE",
                "WPCS recipient back from leave — verify hours for case " + caseEntity.getCaseNumber());
        log.info("CM-040: Back from leave WPCS task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 49 — ICT authorization complete, assign WPCS provider */
    public void onIctAuthCompleteWpcs(CaseEntity caseEntity) {
        createCmTaskWithQueue("CM-049", caseEntity, "WPCS_CASE",
                "ICT authorization complete — assign WPCS provider for case " + caseEntity.getCaseNumber());
        log.info("CM-049: ICT auth complete WPCS task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 50 — ICT created with WPCS hours */
    public void onIctCreatedWithWpcsHours(CaseEntity caseEntity) {
        createCmTaskWithQueue("CM-050", caseEntity, "WPCS_CASE",
                "ICT created for case " + caseEntity.getCaseNumber() + " with active WPCS hours");
        log.info("CM-050: ICT created WPCS task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 51 — ICT cancelled with WPCS hours */
    public void onIctCancelledWithWpcsHours(CaseEntity caseEntity) {
        createCmTaskWithQueue("CM-051", caseEntity, "WPCS_CASE",
                "ICT cancelled for case " + caseEntity.getCaseNumber() + " with active WPCS hours — review needed");
        log.info("CM-051: ICT cancelled WPCS task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 52 — ICT complete, review WPCS hours (notification) */
    public void onIctCompleteWithWpcsHours(CaseEntity caseEntity) {
        createCmNotification("CM-052", caseEntity,
                "ICT completed for case " + caseEntity.getCaseNumber() + " — review WPCS hours in receiving county");
        log.info("CM-052: ICT complete WPCS notification for case {}", caseEntity.getCaseNumber());
    }

    /** CM 61 — Case rescission with WPCS hours */
    public void onCaseRescindedWithWpcsHours(CaseEntity caseEntity) {
        createCmTaskWithQueue("CM-061", caseEntity, "WPCS_CASE",
                "Case " + caseEntity.getCaseNumber() + " rescinded — had active WPCS hours");
        log.info("CM-061: Case rescission WPCS task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 64 — Companion case rescinded (uses existing CI-488670) */
    public void onCompanionCaseRescinded(CaseEntity caseEntity) {
        createCmTask("CI-488670", caseEntity,
                "Companion Case Rescission - " + caseEntity.getCaseNumber(),
                "Terminated case in companion collection rescinded — review required");
        log.info("CM-064: Companion case rescission task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 65 — Funding source updated (uses existing CI-488671) */
    public void onFundingSourceUpdated(CaseEntity caseEntity) {
        createCmTask("CI-488671", caseEntity,
                "Funding Source Update - " + caseEntity.getCaseNumber(),
                "Funding source updated for case in companion collection");
        log.info("CM-065: Funding source update task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 70 — UHV non-compliance */
    public void onUhvNonCompliance(CaseEntity caseEntity, String outcome) {
        createCmTask("CM-070", caseEntity,
                "UHV Non-Compliance - " + caseEntity.getCaseNumber(),
                "Unannounced home visit non-compliance — outcome: " + outcome);
        log.info("CM-070: UHV non-compliance task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 78 — Case rescission notification to supervisor */
    public void onCaseRescindedNotifySupervisor(CaseEntity caseEntity) {
        String supervisorId = caseEntity.getSupervisorId();
        if (supervisorId == null) supervisorId = caseEntity.getCaseOwnerId();

        Notification n = Notification.builder()
                .userId(supervisorId)
                .message("Case " + caseEntity.getCaseNumber() + " has been rescinded")
                .notificationType(Notification.NotificationType.INFO)
                .actionLink("/cases/" + caseEntity.getId())
                .relatedEntityType("CASE")
                .relatedEntityId(caseEntity.getId())
                .readStatus(false)
                .build();
        notificationService.createNotification(n);
        log.info("CM-078: Case rescission notification for case {}", caseEntity.getCaseNumber());
    }

    /** CM 79 — Recipient address matches a provider address */
    public void onRecipientAddressMatchesProvider(CaseEntity caseEntity, String providerId) {
        createCmTask("CM-079", caseEntity,
                "Address Match - Provider " + providerId,
                "Recipient address matches assigned provider " + providerId + " on case " + caseEntity.getCaseNumber());
        log.info("CM-079: Address match task for case {} / provider {}", caseEntity.getCaseNumber(), providerId);
    }

    /** CM 80/81 — Electronic form submitted (CSP or ESP) */
    public void onElectronicFormSubmitted(CaseEntity caseEntity, String formId, String source) {
        String taskTypeCode = "ESP".equalsIgnoreCase(source) ? "CM-081" : "CM-080";
        createCmTask(taskTypeCode, caseEntity,
                "Electronic Form - " + caseEntity.getCaseNumber(),
                "Electronic form " + formId + " submitted via " + source);
        log.info("CM-080/81: Electronic form task for case {} from {}", caseEntity.getCaseNumber(), source);
    }

    /** CM 54 — CIN re-clearance needed due to name change */
    public void onCinReClearanceNameChange(CaseEntity caseEntity) {
        createCmTask("CM-054", caseEntity,
                "CIN Re-Clearance (Name) - " + caseEntity.getCaseNumber(),
                "Recipient name changed — CIN re-clearance required");
        log.info("CM-054: CIN re-clearance (name) task for case {}", caseEntity.getCaseNumber());
    }

    /** CM 55 — CIN re-clearance needed due to DOB/gender change */
    public void onCinReClearanceDobGenderChange(CaseEntity caseEntity) {
        createCmTask("CM-055", caseEntity,
                "CIN Re-Clearance (DOB/Gender) - " + caseEntity.getCaseNumber(),
                "Recipient DOB or gender changed — CIN re-clearance required");
        log.info("CM-055: CIN re-clearance (DOB/gender) task for case {}", caseEntity.getCaseNumber());
    }

    // ============================================================
    // Helper: Check if case has WPCS hours
    // ============================================================

    public boolean hasWpcsHours(CaseEntity caseEntity) {
        return caseEntity.getCaseType() == CaseEntity.CaseType.WPCS
            || caseEntity.getCaseType() == CaseEntity.CaseType.IHSS_WPCS;
    }

    // ============================================================
    // Private helpers
    // ============================================================

    /**
     * Creates a task for the given CM task type code, assigned to the case owner.
     * Idempotent — skips if an OPEN/RESERVED task for this case+type already exists.
     */
    private void createCmTask(String taskTypeCode, CaseEntity caseEntity, String title, String description) {
        if (isDuplicate(taskTypeCode, caseEntity.getCaseNumber())) {
            log.debug("Skipping duplicate task {} for case {}", taskTypeCode, caseEntity.getCaseNumber());
            return;
        }

        TaskType tt = taskTypeRepo.findByTaskTypeCode(taskTypeCode).orElse(null);
        if (tt == null) {
            log.warn("Task type {} not found — skipping task creation for case {}", taskTypeCode, caseEntity.getCaseNumber());
            return;
        }

        Task task = buildTask(tt, caseEntity, title, description, caseEntity.getCaseOwnerId());
        taskService.createTask(task);
    }

    /**
     * Creates a task with an explicit queue override (for WPCS_CASE, ICT_COORDINATOR, etc.)
     */
    private void createCmTaskWithQueue(String taskTypeCode, CaseEntity caseEntity, String queueOverride, String description) {
        if (isDuplicate(taskTypeCode, caseEntity.getCaseNumber())) {
            log.debug("Skipping duplicate task {} for case {}", taskTypeCode, caseEntity.getCaseNumber());
            return;
        }

        TaskType tt = taskTypeRepo.findByTaskTypeCode(taskTypeCode).orElse(null);
        if (tt == null) {
            log.warn("Task type {} not found — skipping task creation for case {}", taskTypeCode, caseEntity.getCaseNumber());
            return;
        }

        String assignTo = queueOverride != null ? queueOverride : caseEntity.getCaseOwnerId();
        Task task = buildTask(tt, caseEntity, tt.getName() + " - " + caseEntity.getCaseNumber(), description, assignTo);
        if (queueOverride != null) {
            task.setWorkQueue(queueOverride);
        }
        taskService.createTask(task);
    }

    /**
     * Creates a notification (isNotification=true) for the case owner.
     */
    private void createCmNotification(String taskTypeCode, CaseEntity caseEntity, String message) {
        TaskType tt = taskTypeRepo.findByTaskTypeCode(taskTypeCode).orElse(null);
        boolean isNotification = tt != null && tt.isNotification();

        if (isNotification) {
            // Create as a Notification entity
            Notification n = Notification.builder()
                    .userId(caseEntity.getCaseOwnerId() != null ? caseEntity.getCaseOwnerId() : "SYSTEM")
                    .message(message)
                    .notificationType(Notification.NotificationType.INFO)
                    .actionLink("/cases/" + caseEntity.getId())
                    .relatedEntityType("CASE")
                    .relatedEntityId(caseEntity.getId())
                    .readStatus(false)
                    .build();
            notificationService.createNotification(n);
        } else {
            // Fallback: create as task with isNotification flag
            createCmTask(taskTypeCode, caseEntity, message, message);
        }
    }

    /**
     * Builds a Task entity from a TaskType definition and case context.
     */
    private Task buildTask(TaskType tt, CaseEntity caseEntity, String title, String description, String assignTo) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = tt.getDeadlineBusinessDays() > 0
                ? businessDayCalc.addBusinessDays(now, tt.getDeadlineBusinessDays())
                : null;

        Task task = Task.builder()
                .title(title)
                .description(description)
                .assignedTo(assignTo != null ? assignTo : "SYSTEM")
                .status(Task.TaskStatus.OPEN)
                .priority(tt.getPriorityDefault())
                .workQueue(tt.getTargetQueue())
                .triggerCondition(tt.getDescription())
                .actionLink("/cases/" + caseEntity.getId())
                .relatedEntityType("CASE")
                .relatedEntityId(caseEntity.getId())
                .dueDate(dueDate)
                .createdAt(now)
                .build();

        // Set fields not in builder
        task.setTaskTypeCode(tt.getTaskTypeCode());
        task.setCaseNumber(caseEntity.getCaseNumber());
        task.setSubject(caseEntity.getCaseNumber() + " " + title);
        task.setNotification(tt.isNotification());
        task.setCounty(caseEntity.getCountyCode());

        return task;
    }

    /**
     * Idempotency check: returns true if an OPEN or RESERVED task already exists
     * for this case + task type code.
     */
    private boolean isDuplicate(String taskTypeCode, String caseNumber) {
        if (caseNumber == null || taskTypeCode == null) return false;
        List<Task> existing = taskRepo.findByCaseNumberAndTaskTypeCodeAndStatusIn(
                caseNumber, taskTypeCode, List.of(Task.TaskStatus.OPEN, Task.TaskStatus.RESERVED));
        return !existing.isEmpty();
    }
}
