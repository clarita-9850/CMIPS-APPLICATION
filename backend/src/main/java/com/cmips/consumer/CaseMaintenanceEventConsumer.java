package com.cmips.consumer;

import com.cmips.entity.CaseEntity;
import com.cmips.entity.Notification;
import com.cmips.entity.Task;
import com.cmips.entity.TaskType;
import com.cmips.event.BaseEvent;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.TaskRepository;
import com.cmips.repository.TaskTypeRepository;
import com.cmips.service.NotificationService;
import com.cmips.service.TaskAutoCloseService;
import com.cmips.service.TaskService;
import com.cmips.util.BusinessDayCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Kafka consumer for DSD Section 30 — Case Maintenance interface file triggers.
 * Handles events from MEDS, SAWS, SCI, and batch processing systems.
 *
 * Topic: cmips-case-maintenance-events
 */
@Component
public class CaseMaintenanceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CaseMaintenanceEventConsumer.class);

    private final TaskService taskService;
    private final NotificationService notificationService;
    private final TaskTypeRepository taskTypeRepo;
    private final TaskRepository taskRepo;
    private final CaseRepository caseRepo;
    private final TaskAutoCloseService taskAutoCloseService;
    private final BusinessDayCalculator businessDayCalc;

    public CaseMaintenanceEventConsumer(TaskService taskService,
                                         NotificationService notificationService,
                                         TaskTypeRepository taskTypeRepo,
                                         TaskRepository taskRepo,
                                         CaseRepository caseRepo,
                                         TaskAutoCloseService taskAutoCloseService,
                                         BusinessDayCalculator businessDayCalc) {
        this.taskService = taskService;
        this.notificationService = notificationService;
        this.taskTypeRepo = taskTypeRepo;
        this.taskRepo = taskRepo;
        this.caseRepo = caseRepo;
        this.taskAutoCloseService = taskAutoCloseService;
        this.businessDayCalc = businessDayCalc;
    }

    @KafkaListener(topics = "cmips-case-maintenance-events", groupId = "case-maintenance-consumer-group")
    public void handleCaseMaintenanceEvent(BaseEvent event) {
        log.info("Received case maintenance event: {}", event.getEventType());

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) event.getPayload();
        if (payload == null) {
            log.warn("Event payload is null for event type: {}", event.getEventType());
            return;
        }

        String caseNumber = (String) payload.get("caseNumber");
        CaseEntity caseEntity = null;
        if (caseNumber != null) {
            caseEntity = caseRepo.findByCaseNumber(caseNumber).orElse(null);
            if (caseEntity == null) {
                log.warn("Case not found for caseNumber: {} in event {}", caseNumber, event.getEventType());
                return;
            }
        }

        switch (event.getEventType()) {
            // --- MEDS Interface Events ---
            case "meds.share_of_cost_change" -> handleTask("CM-002", caseEntity, payload);       // CM 02
            case "meds.disability_determination" -> handleTask("CM-019", caseEntity, payload);    // CM 19
            case "meds.initial_eligibility" -> {
                handleNotification("CM-030", caseEntity, payload); // CM 30
                // Auto-close CM-029 (No Initial Medi-Cal Eligibility 90 Days) — DSD GAP 3
                taskAutoCloseService.onBusinessEvent(TaskAutoCloseService.EVENT_MEDI_CAL_ELIGIBILITY_RECEIVED, caseEntity.getCaseNumber());
            }
            case "meds.eligibility_termination" -> {                                               // CM 34
                handleNotification("CM-034-N1", caseEntity, payload);
                handleSupervisorNotification("CM-034-N2", caseEntity, payload);
            }
            case "meds.eligibility_termination_wpcs" -> handleTask("CM-063", caseEntity, payload); // CM 63

            // --- SAWS S3 Interface Events (Discontinuance) ---
            case "saws.s3.discontinuance" -> handleTask("CM-032", caseEntity, payload);             // CM 32
            case "saws.s3.discontinuance_wpcs" -> handleTask("CM-062", caseEntity, payload);        // CM 62
            case "saws.s3.case_terminated" -> handleNotification("CM-073", caseEntity, payload);    // CM 73
            case "saws.s3.no_initial_medi_cal" -> handleTask("CM-029", caseEntity, payload);        // CM 29

            // --- SAWS S4 Interface Events (Rescission) ---
            case "saws.s4.rescind_discontinuance" -> {
                handleNotification("CM-033", caseEntity, payload); // CM 33
                // Auto-close CM-032 (SAWS Discontinuance) — discontinuance rescinded
                taskAutoCloseService.onBusinessEvent(TaskAutoCloseService.EVENT_DISCONTINUANCE_NOA_UPDATED, caseEntity.getCaseNumber());
            }
            case "saws.s4.discontinuance_rescinded" -> handleNotification("CM-074", caseEntity, payload); // CM 74

            // --- SAWS S2 Interface Events (Demographic Updates) ---
            case "saws.s2.ssn_update" -> handleNotification("CM-041", caseEntity, payload);     // CM 41
            case "saws.s2.dob_update" -> handleNotification("CM-042", caseEntity, payload);     // CM 42
            case "saws.s2.name_update" -> handleNotification("CM-043", caseEntity, payload);    // CM 43
            case "saws.s2.gender_update" -> handleNotification("CM-044", caseEntity, payload);  // CM 44
            case "saws.s2.ssn_match_another" -> handleTask("CM-076", caseEntity, payload);      // CM 76
            case "saws.s2.cin_match_another" -> handleTask("CM-077", caseEntity, payload);      // CM 77

            // --- SCI Daily Interface Events ---
            case "sci.daily.name_update" -> handleTask("CM-056", caseEntity, payload);    // CM 56
            case "sci.daily.dob_update" -> handleTask("CM-057", caseEntity, payload);     // CM 57
            case "sci.daily.gender_update" -> handleTask("CM-058", caseEntity, payload);  // CM 58
            case "sci.daily.ssn_update" -> handleTask("CM-059", caseEntity, payload);     // CM 59

            // --- Batch Processing Events ---
            case "batch.incorrect_auth_number" -> handleTask("CM-060", caseEntity, payload);  // CM 60
            case "batch.esp_inactivation" -> handleTask("CM-072", caseEntity, payload);       // CM 72
            case "batch.county_invoice" -> handleTask("CM-017", caseEntity, payload);         // CM 17

            default -> log.warn("Unknown case maintenance event type: {}", event.getEventType());
        }
    }

    /**
     * Creates a task for the given task type code, assigned to the case owner.
     */
    private void handleTask(String taskTypeCode, CaseEntity caseEntity, Map<String, Object> payload) {
        if (caseEntity == null) return;
        if (isDuplicate(taskTypeCode, caseEntity.getCaseNumber())) {
            log.debug("Skipping duplicate task {} for case {}", taskTypeCode, caseEntity.getCaseNumber());
            return;
        }

        TaskType tt = taskTypeRepo.findByTaskTypeCode(taskTypeCode).orElse(null);
        if (tt == null) {
            log.warn("Task type {} not found", taskTypeCode);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = tt.getDeadlineBusinessDays() > 0
                ? businessDayCalc.addBusinessDays(now, tt.getDeadlineBusinessDays()) : null;

        String assignTo = tt.getTargetQueue();
        if ("CASE_OWNER".equals(tt.getTargetQueue()) && caseEntity.getCaseOwnerId() != null) {
            assignTo = caseEntity.getCaseOwnerId();
        }

        String detail = payload.get("detail") != null ? (String) payload.get("detail") : tt.getDescription();

        Task task = Task.builder()
                .title(tt.getName() + " - " + caseEntity.getCaseNumber())
                .description(detail)
                .assignedTo(assignTo)
                .status(Task.TaskStatus.OPEN)
                .priority(tt.getPriorityDefault())
                .workQueue(tt.getTargetQueue())
                .triggerCondition("Interface: " + tt.getDescription())
                .actionLink("/cases/" + caseEntity.getId())
                .relatedEntityType("CASE")
                .relatedEntityId(caseEntity.getId())
                .dueDate(dueDate)
                .createdAt(now)
                .build();

        task.setTaskTypeCode(taskTypeCode);
        task.setCaseNumber(caseEntity.getCaseNumber());
        task.setSubject(caseEntity.getCaseNumber() + " " + tt.getName());
        task.setNotification(tt.isNotification());
        task.setCounty(caseEntity.getCountyCode());

        taskService.createTask(task);
        log.info("Created task {} for case {}", taskTypeCode, caseEntity.getCaseNumber());
    }

    /**
     * Creates a notification for the case owner.
     */
    private void handleNotification(String taskTypeCode, CaseEntity caseEntity, Map<String, Object> payload) {
        if (caseEntity == null) return;

        TaskType tt = taskTypeRepo.findByTaskTypeCode(taskTypeCode).orElse(null);
        String message = tt != null ? tt.getName() + " — " + caseEntity.getCaseNumber() : taskTypeCode;
        if (payload.get("detail") != null) {
            message = (String) payload.get("detail");
        }

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
        log.info("Created notification {} for case {}", taskTypeCode, caseEntity.getCaseNumber());
    }

    /**
     * Creates a notification for the case supervisor (CM 34-N2 pattern).
     */
    private void handleSupervisorNotification(String taskTypeCode, CaseEntity caseEntity, Map<String, Object> payload) {
        if (caseEntity == null) return;

        TaskType tt = taskTypeRepo.findByTaskTypeCode(taskTypeCode).orElse(null);
        String message = tt != null ? tt.getName() + " — " + caseEntity.getCaseNumber() : taskTypeCode;

        String supervisorId = caseEntity.getSupervisorId();
        if (supervisorId == null) supervisorId = caseEntity.getCaseOwnerId();
        if (supervisorId == null) supervisorId = "SYSTEM";

        Notification n = Notification.builder()
                .userId(supervisorId)
                .message(message)
                .notificationType(Notification.NotificationType.INFO)
                .actionLink("/cases/" + caseEntity.getId())
                .relatedEntityType("CASE")
                .relatedEntityId(caseEntity.getId())
                .readStatus(false)
                .build();
        notificationService.createNotification(n);
        log.info("Created supervisor notification {} for case {}", taskTypeCode, caseEntity.getCaseNumber());
    }

    private boolean isDuplicate(String taskTypeCode, String caseNumber) {
        if (caseNumber == null || taskTypeCode == null) return false;
        List<Task> existing = taskRepo.findByCaseNumberAndTaskTypeCodeAndStatusIn(
                caseNumber, taskTypeCode, List.of(Task.TaskStatus.OPEN, Task.TaskStatus.RESERVED));
        return !existing.isEmpty();
    }
}
