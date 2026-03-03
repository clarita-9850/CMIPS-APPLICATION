package com.cmips.service;

import com.cmips.entity.Task;
import com.cmips.entity.TaskType;
import com.cmips.repository.TaskRepository;
import com.cmips.repository.TaskTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DSD GAP 3 — Auto-close tasks when their indicated action occurs.
 * <p>
 * Per DSD Section 30, some tasks should auto-close when the triggering condition
 * is resolved. E.g., CM-013 (No Assigned Providers) auto-closes when a provider
 * is assigned; CM-029 (No Initial Medi-Cal) auto-closes when eligibility received.
 * <p>
 * Business events call {@link #onBusinessEvent(String, String)} with the event name
 * and case number, and this service closes all matching open tasks whose TaskType
 * has that event configured in its {@code autoCloseEvent} field.
 */
@Service
public class TaskAutoCloseService {

    private static final Logger log = LoggerFactory.getLogger(TaskAutoCloseService.class);

    private final TaskRepository taskRepository;
    private final TaskTypeRepository taskTypeRepository;
    private final TaskLifecycleService lifecycleService;

    public TaskAutoCloseService(TaskRepository taskRepository,
                                 TaskTypeRepository taskTypeRepository,
                                 TaskLifecycleService lifecycleService) {
        this.taskRepository = taskRepository;
        this.taskTypeRepository = taskTypeRepository;
        this.lifecycleService = lifecycleService;
    }

    /**
     * Called when a business event occurs that may resolve open tasks.
     * Looks up all TaskTypes that have this autoCloseEvent, then closes any
     * open/reserved tasks for the given case number with those task type codes.
     *
     * @param eventName  the business event (e.g., "provider.assigned", "medi_cal.eligibility_received")
     * @param caseNumber the case number to scope the auto-close
     */
    @Transactional
    public int onBusinessEvent(String eventName, String caseNumber) {
        if (eventName == null || caseNumber == null) return 0;

        List<TaskType> matchingTypes = taskTypeRepository.findByAutoCloseEvent(eventName);
        if (matchingTypes.isEmpty()) return 0;

        int closed = 0;
        for (TaskType tt : matchingTypes) {
            List<Task> openTasks = taskRepository.findByCaseNumberAndTaskTypeCodeAndStatusIn(
                    caseNumber, tt.getTaskTypeCode(),
                    List.of(Task.TaskStatus.OPEN, Task.TaskStatus.RESERVED));

            for (Task task : openTasks) {
                try {
                    lifecycleService.autoCloseTask(task.getId());
                    closed++;
                    log.info("Auto-closed task {} ({}) for case {} on event {}",
                            task.getId(), tt.getTaskTypeCode(), caseNumber, eventName);
                } catch (Exception e) {
                    log.error("Failed to auto-close task {}: {}", task.getId(), e.getMessage());
                }
            }
        }

        if (closed > 0) {
            log.info("Auto-closed {} tasks for case {} on event {}", closed, caseNumber, eventName);
        }
        return closed;
    }

    /**
     * Called when a specific task type should be auto-closed for a case.
     */
    @Transactional
    public int autoCloseByTaskType(String taskTypeCode, String caseNumber) {
        if (taskTypeCode == null || caseNumber == null) return 0;

        List<Task> openTasks = taskRepository.findByCaseNumberAndTaskTypeCodeAndStatusIn(
                caseNumber, taskTypeCode,
                List.of(Task.TaskStatus.OPEN, Task.TaskStatus.RESERVED));

        int closed = 0;
        for (Task task : openTasks) {
            try {
                lifecycleService.autoCloseTask(task.getId());
                closed++;
            } catch (Exception e) {
                log.error("Failed to auto-close task {}: {}", task.getId(), e.getMessage());
            }
        }
        return closed;
    }

    // ==================== Predefined Business Events ====================
    // Call these from the appropriate service methods

    /** Provider assigned to case → auto-close CM-013 */
    public static final String EVENT_PROVIDER_ASSIGNED = "provider.assigned";

    /** Medi-Cal eligibility received → auto-close CM-029 */
    public static final String EVENT_MEDI_CAL_ELIGIBILITY_RECEIVED = "medi_cal.eligibility_received";

    /** Change assessment created → auto-close CM-002, CM-008 */
    public static final String EVENT_CHANGE_ASSESSMENT_CREATED = "change_assessment.created";

    /** Advance pay reconciled → auto-close CM-007, CM-008 */
    public static final String EVENT_ADVANCE_PAY_RECONCILED = "advance_pay.reconciled";

    /** Protective supervision approved → auto-close CM-026 */
    public static final String EVENT_PROTECTIVE_SUPERVISION_APPROVED = "protective_supervision.approved";

    /** Paramedical authorization approved → auto-close CM-027 */
    public static final String EVENT_PARAMEDICAL_AUTH_APPROVED = "paramedical_auth.approved";

    /** SSN/ITIN provided → auto-close CM-046 */
    public static final String EVENT_SSN_ITIN_PROVIDED = "ssn_itin.provided";

    /** Health care cert submitted → auto-close CM-053, CI-770242 */
    public static final String EVENT_HEALTH_CARE_CERT_SUBMITTED = "health_care_cert.submitted";

    /** Case terminated → auto-close CM-001 */
    public static final String EVENT_CASE_TERMINATED = "case.terminated";

    /** Discontinuance NOA updated → auto-close CM-032 */
    public static final String EVENT_DISCONTINUANCE_NOA_UPDATED = "discontinuance_noa.updated";

    /** CIN clearance completed → auto-close CM-054, CM-055 */
    public static final String EVENT_CIN_CLEARANCE_COMPLETED = "cin_clearance.completed";

    /** ICT completed → auto-close CM-006, CM-023-T */
    public static final String EVENT_ICT_COMPLETED = "ict.completed";
}
