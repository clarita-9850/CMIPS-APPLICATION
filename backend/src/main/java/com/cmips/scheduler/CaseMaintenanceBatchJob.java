package com.cmips.scheduler;

import com.cmips.entity.CaseEntity;
import com.cmips.entity.CaseEntity.CaseStatus;
import com.cmips.entity.CaseEntity.CaseType;
import com.cmips.entity.ElectronicFormEntity;
import com.cmips.entity.ElectronicFormEntity.PrintMethod;
import com.cmips.entity.ElectronicFormEntity.FormStatus;
import com.cmips.entity.NoticeOfActionEntity;
import com.cmips.entity.NoticeOfActionEntity.NoaStatus;
import com.cmips.entity.Notification;
import com.cmips.entity.RecipientEntity;
import com.cmips.entity.Task;
import com.cmips.entity.TaskType;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.ElectronicFormRepository;
import com.cmips.repository.NoticeOfActionRepository;
import com.cmips.repository.RecipientRepository;
import com.cmips.repository.TaskRepository;
import com.cmips.repository.TaskTypeRepository;
import com.cmips.service.NotificationService;
import com.cmips.service.TaskService;
import com.cmips.util.BusinessDayCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Set;

/**
 * Batch logic for DSD Section 30 — Case Maintenance time-based triggers.
 * Scheduling moved to external scheduler app (CASE_MAINTENANCE_DAILY_JOB, CASE_MAINTENANCE_MONTHLY_JOB, NIGHTLY_BATCH_PRINT_JOB).
 */
@Component
public class CaseMaintenanceBatchJob {

    private static final Logger log = LoggerFactory.getLogger(CaseMaintenanceBatchJob.class);

    private static final Set<CaseStatus> ACTIVE_STATUSES = Set.of(
            CaseStatus.ELIGIBLE, CaseStatus.PRESUMPTIVE_ELIGIBLE, CaseStatus.PENDING);

    private final CaseRepository caseRepo;
    private final RecipientRepository recipientRepo;
    private final TaskRepository taskRepo;
    private final TaskTypeRepository taskTypeRepo;
    private final TaskService taskService;
    private final NotificationService notificationService;
    private final BusinessDayCalculator businessDayCalc;
    private final NoticeOfActionRepository noaRepository;
    private final ElectronicFormRepository formRepository;

    public CaseMaintenanceBatchJob(CaseRepository caseRepo,
                                    RecipientRepository recipientRepo,
                                    TaskRepository taskRepo,
                                    TaskTypeRepository taskTypeRepo,
                                    TaskService taskService,
                                    NotificationService notificationService,
                                    BusinessDayCalculator businessDayCalc,
                                    NoticeOfActionRepository noaRepository,
                                    ElectronicFormRepository formRepository) {
        this.caseRepo = caseRepo;
        this.recipientRepo = recipientRepo;
        this.taskRepo = taskRepo;
        this.taskTypeRepo = taskTypeRepo;
        this.taskService = taskService;
        this.notificationService = notificationService;
        this.businessDayCalc = businessDayCalc;
        this.noaRepository = noaRepository;
        this.formRepository = formRepository;
    }

    /**
     * Daily batch — triggered by scheduler via CASE_MAINTENANCE_DAILY_JOB.
     * Checks for time-based triggers: advance pay, health care certs, pending authorizations, etc.
     */
    public void dailyBatch() {
        log.info("Starting Case Maintenance daily batch...");
        LocalDate today = LocalDate.now();
        int count = 0;

        List<CaseEntity> allActive = caseRepo.findAll().stream()
                .filter(c -> ACTIVE_STATUSES.contains(c.getCaseStatus()))
                .toList();

        for (CaseEntity c : allActive) {
            try {
                // CM 07 — Advance pay not reconciled 45 days
                if (Boolean.TRUE.equals(c.getAdvancePayIndicator())
                        && c.getCreatedAt() != null
                        && c.getCreatedAt().toLocalDate().plusDays(45).isBefore(today)) {
                    if (createNotificationIfNew("CM-007", c,
                            "Advance pay not reconciled after 45 days for case " + c.getCaseNumber())) count++;
                }

                // CM 08 — Advance pay not reconciled 75 days
                if (Boolean.TRUE.equals(c.getAdvancePayIndicator())
                        && c.getCreatedAt() != null
                        && c.getCreatedAt().toLocalDate().plusDays(75).isBefore(today)) {
                    if (createTaskIfNew("CM-008", c)) count++;
                }

                // CM 13 — Case with IP mode of service but no assigned providers
                if ("IP".equalsIgnoreCase(c.getModeOfService())) {
                    // Simplified check: no providers assigned
                    // Full implementation would query ProviderAssignmentRepository
                    if (createNotificationIfNew("CM-013", c,
                            "Case " + c.getCaseNumber() + " has IP mode of service but no assigned providers")) count++;
                }

                // CM 15 — Paramedical service auth expiring within 15 days
                if (c.getAuthorizationEndDate() != null
                        && c.getAssessmentType() != null && c.getAssessmentType().contains("PARAMEDICAL")
                        && c.getAuthorizationEndDate().minusDays(15).isBefore(today)
                        && c.getAuthorizationEndDate().isAfter(today)) {
                    if (createNotificationIfNew("CM-015", c,
                            "Paramedical service authorization expiring in 15 days for case " + c.getCaseNumber())) count++;
                }

                // CM 16 — Time limited service expired
                if (c.getAuthorizationEndDate() != null
                        && c.getAuthorizationEndDate().isBefore(today)
                        && c.getAssessmentType() != null && c.getAssessmentType().contains("TIME_LIMITED")) {
                    if (createNotificationIfNew("CM-016", c,
                            "Time-limited service expired for case " + c.getCaseNumber())) count++;
                }

                // CM 26 — Pending protective supervision 30+ days
                if ("PENDING_PROTECTIVE_SUPERVISION".equalsIgnoreCase(c.getHealthCareCertStatus())
                        && c.getCreatedAt() != null
                        && c.getCreatedAt().toLocalDate().plusDays(30).isBefore(today)) {
                    if (createNotificationIfNew("CM-026", c,
                            "Protective supervision pending 30+ days for case " + c.getCaseNumber())) count++;
                }

                // CM 27 — Pending paramedical authorization 30+ days
                if ("PENDING_PARAMEDICAL".equalsIgnoreCase(c.getHealthCareCertStatus())
                        && c.getCreatedAt() != null
                        && c.getCreatedAt().toLocalDate().plusDays(30).isBefore(today)) {
                    if (createNotificationIfNew("CM-027", c,
                            "Paramedical authorization pending 30+ days for case " + c.getCaseNumber())) count++;
                }

                // CM 29 — No initial Medi-Cal eligibility within 90 days
                if (c.getMediCalEligibilityDate() == null
                        && c.getCreatedAt() != null
                        && c.getCreatedAt().toLocalDate().plusDays(90).isBefore(today)
                        && c.getCaseStatus() == CaseStatus.PENDING) {
                    if (createTaskIfNew("CM-029", c)) count++;
                }

                // CM 46 — SSN/ITIN pending 120 days
                if (c.getRecipientId() != null) {
                    recipientRepo.findById(c.getRecipientId()).ifPresent(r -> {
                        if ((r.getSsn() == null || r.getSsn().isBlank())
                                && r.getCreatedAt() != null
                                && r.getCreatedAt().toLocalDate().plusDays(120).isBefore(today)) {
                            createTaskIfNew("CM-046", c);
                        }
                    });
                }

                // CM 53 — Health care cert due in 1 business day
                if (c.getHealthCareCertDueDate() != null
                        && c.getHealthCareCertDueDate().minusDays(1).equals(today)) {
                    if (createTaskIfNew("CM-053", c)) count++;
                }

                // CM 71 — Health care cert due in 10 days (uses existing CI-770242)
                if (c.getHealthCareCertDueDate() != null
                        && c.getHealthCareCertDueDate().minusDays(10).equals(today)) {
                    if (createTaskIfNew("CI-770242", c)) count++;
                }

            } catch (Exception e) {
                log.error("Error processing daily batch for case {}: {}", c.getCaseNumber(), e.getMessage());
            }
        }

        // CM 75 — Non-compliance 90 days, auto-rescind disabled
        List<CaseEntity> terminated = caseRepo.findByCaseStatus(CaseStatus.TERMINATED);
        for (CaseEntity c : terminated) {
            if ("CC514".equals(c.getTerminationReason())
                    && c.getTerminationDate() != null
                    && c.getTerminationDate().plusDays(90).isBefore(today)
                    && Boolean.FALSE.equals(c.getRecipientDeclinesCfco())) {
                if (createNotificationIfNew("CM-075", c,
                        "Case " + c.getCaseNumber() + " terminated 90+ days for non-compliance, auto-rescind disabled")) count++;
            }
        }

        log.info("Case Maintenance daily batch completed — {} items created", count);
    }

    /**
     * Monthly batch — triggered by scheduler via CASE_MAINTENANCE_MONTHLY_JOB.
     * Checks for age-based triggers.
     */
    public void monthlyBatch() {
        log.info("Starting Case Maintenance monthly batch...");
        int count = 0;
        LocalDate today = LocalDate.now();

        List<CaseEntity> allActive = caseRepo.findAll().stream()
                .filter(c -> ACTIVE_STATUSES.contains(c.getCaseStatus()))
                .toList();

        for (CaseEntity c : allActive) {
            try {
                if (c.getRecipientId() == null) continue;
                RecipientEntity recipient = recipientRepo.findById(c.getRecipientId()).orElse(null);
                if (recipient == null || recipient.getDateOfBirth() == null) continue;

                int ageNextMonth = Period.between(recipient.getDateOfBirth(), today.plusMonths(1)).getYears();

                // CM 14 — Recipient turns 18 next month
                if (ageNextMonth == 18) {
                    int ageThisMonth = Period.between(recipient.getDateOfBirth(), today).getYears();
                    if (ageThisMonth < 18) {
                        if (createTaskIfNew("CM-014", c)) count++;
                    }
                }

                // CM 20 — Recipient child turned 14
                // This is approximated: check if recipient's DOB means they turn 14 this month
                if (ageNextMonth == 14) {
                    int ageThisMonth = Period.between(recipient.getDateOfBirth(), today).getYears();
                    if (ageThisMonth < 14) {
                        if (createTaskIfNew("CM-020", c)) count++;
                    }
                }

            } catch (Exception e) {
                log.error("Error processing monthly batch for case {}: {}", c.getCaseNumber(), e.getMessage());
            }
        }

        log.info("Case Maintenance monthly batch completed — {} items created", count);
    }

    /**
     * Nightly batch print — triggered by scheduler via NIGHTLY_BATCH_PRINT_JOB.
     * Per DSD Section 31 (CI-116202): all NIGHTLY_BATCH NOAs and forms with status PENDING
     * are marked PRINTED, simulating the Curam Correspondence → XSL Processor → Printer pipeline.
     * Each printed NOA also generates a mailing task for the case worker.
     */
    public void nightlyBatchPrint() {
        log.info("Starting Nightly Batch Print job (NOAs + Forms)...");
        LocalDate today = LocalDate.now();
        int noaPrinted = 0;
        int formsPrinted = 0;

        // ── Process PENDING NIGHTLY_BATCH NOAs ────────────────────────────────
        List<NoticeOfActionEntity> pendingNoas = noaRepository.findPendingForBatchPrint();
        for (NoticeOfActionEntity noa : pendingNoas) {
            try {
                noa.setStatus(NoaStatus.PRINTED);
                noa.setPrintDate(today);
                noaRepository.save(noa);
                noaPrinted++;

                // Create mailing task for case worker (DSD Section 31: two copies mailed to recipient)
                caseRepo.findById(noa.getCaseId()).ifPresent(c -> {
                    Notification n = Notification.builder()
                            .userId(c.getCaseOwnerId() != null ? c.getCaseOwnerId() : "SYSTEM")
                            .message("NOA " + noa.getNoaType() + " printed for case " + c.getCaseNumber()
                                    + " — please mail two copies to recipient.")
                            .notificationType(Notification.NotificationType.INFO)
                            .actionLink("/cases/" + c.getId())
                            .relatedEntityType("NOA")
                            .relatedEntityId(noa.getId())
                            .readStatus(false)
                            .build();
                    notificationService.createNotification(n);
                });
            } catch (Exception ex) {
                log.error("Nightly batch: failed to print NOA id={}: {}", noa.getId(), ex.getMessage());
            }
        }

        // ── Process PENDING NIGHTLY_BATCH Electronic Forms ─────────────────────
        List<ElectronicFormEntity> pendingForms = formRepository.findByStatusAndPrintMethod(
                FormStatus.PENDING, PrintMethod.NIGHTLY_BATCH);
        for (ElectronicFormEntity form : pendingForms) {
            try {
                form.setStatus(FormStatus.PRINTED);
                form.setPrintDate(today);
                formRepository.save(form);
                formsPrinted++;
            } catch (Exception ex) {
                log.error("Nightly batch: failed to print form id={}: {}", form.getId(), ex.getMessage());
            }
        }

        log.info("Nightly Batch Print completed — {} NOAs printed, {} forms printed", noaPrinted, formsPrinted);
    }

    // ==================== Helpers ====================

    /**
     * Creates a task if no OPEN/RESERVED task already exists for this case + task type.
     * Returns true if a new task was created.
     */
    private boolean createTaskIfNew(String taskTypeCode, CaseEntity c) {
        if (isDuplicate(taskTypeCode, c.getCaseNumber())) return false;

        TaskType tt = taskTypeRepo.findByTaskTypeCode(taskTypeCode).orElse(null);
        if (tt == null) {
            log.warn("Task type {} not found in batch job", taskTypeCode);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = tt.getDeadlineBusinessDays() > 0
                ? businessDayCalc.addBusinessDays(now, tt.getDeadlineBusinessDays()) : null;

        Task task = Task.builder()
                .title(tt.getName() + " - " + c.getCaseNumber())
                .description(tt.getDescription())
                .assignedTo(c.getCaseOwnerId() != null ? c.getCaseOwnerId() : tt.getTargetQueue())
                .status(Task.TaskStatus.OPEN)
                .priority(tt.getPriorityDefault())
                .workQueue(tt.getTargetQueue())
                .triggerCondition("Batch: " + tt.getDescription())
                .actionLink("/cases/" + c.getId())
                .relatedEntityType("CASE")
                .relatedEntityId(c.getId())
                .dueDate(dueDate)
                .createdAt(now)
                .build();

        task.setTaskTypeCode(taskTypeCode);
        task.setCaseNumber(c.getCaseNumber());
        task.setSubject(c.getCaseNumber() + " " + tt.getName());
        task.setNotification(tt.isNotification());
        task.setCounty(c.getCountyCode());

        taskService.createTask(task);
        return true;
    }

    /**
     * Creates a notification if no OPEN/RESERVED task already exists for this case + task type.
     * Returns true if a new notification was created.
     */
    private boolean createNotificationIfNew(String taskTypeCode, CaseEntity c, String message) {
        if (isDuplicate(taskTypeCode, c.getCaseNumber())) return false;

        Notification n = Notification.builder()
                .userId(c.getCaseOwnerId() != null ? c.getCaseOwnerId() : "SYSTEM")
                .message(message)
                .notificationType(Notification.NotificationType.INFO)
                .actionLink("/cases/" + c.getId())
                .relatedEntityType("CASE")
                .relatedEntityId(c.getId())
                .readStatus(false)
                .build();
        notificationService.createNotification(n);
        return true;
    }

    private boolean isDuplicate(String taskTypeCode, String caseNumber) {
        if (caseNumber == null || taskTypeCode == null) return false;
        List<Task> existing = taskRepo.findByCaseNumberAndTaskTypeCodeAndStatusIn(
                caseNumber, taskTypeCode, List.of(Task.TaskStatus.OPEN, Task.TaskStatus.RESERVED));
        return !existing.isEmpty();
    }
}
