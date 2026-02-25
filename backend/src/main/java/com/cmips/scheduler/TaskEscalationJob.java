package com.cmips.scheduler;

import com.cmips.entity.Task;
import com.cmips.entity.TaskType;
import com.cmips.repository.TaskRepository;
import com.cmips.repository.TaskTypeRepository;
import com.cmips.service.TaskLifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Scheduled job that checks for overdue tasks and escalates them.
 * Runs every 15 minutes.
 *
 * Escalation logic:
 * - If TaskType has autoCloseEnabled=true → auto-close the task
 * - If TaskType has escalationEnabled=true → escalate to the configured target queue
 * - Payment tasks use dual-stage escalation (NOT_RESERVED + NOT_COMPLETED)
 */
@Component
public class TaskEscalationJob {

    private static final Logger log = LoggerFactory.getLogger(TaskEscalationJob.class);
    private static final String DEFAULT_ESCALATION_QUEUE = "ESCALATED";

    private final TaskRepository taskRepository;
    private final TaskTypeRepository taskTypeRepository;
    private final TaskLifecycleService lifecycleService;

    public TaskEscalationJob(TaskRepository taskRepository,
                             TaskTypeRepository taskTypeRepository,
                             TaskLifecycleService lifecycleService) {
        this.taskRepository = taskRepository;
        this.taskTypeRepository = taskTypeRepository;
        this.lifecycleService = lifecycleService;
    }

    @Scheduled(fixedDelay = 900000) // Every 15 minutes
    public void checkOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Escalation check started at {}", now);

        int escalated = 0;
        int autoClosed = 0;

        // Check overdue OPEN tasks (not reserved within deadline)
        List<Task> overdueOpen = taskRepository.findOverdueOpenTasks(now);
        for (Task task : overdueOpen) {
            try {
                Optional<TaskType> typeOpt = task.getTaskTypeCode() != null
                        ? taskTypeRepository.findByTaskTypeCode(task.getTaskTypeCode())
                        : Optional.empty();

                if (typeOpt.isPresent()) {
                    TaskType type = typeOpt.get();

                    if (type.isAutoCloseEnabled()) {
                        lifecycleService.autoCloseTask(task.getId());
                        autoClosed++;
                        continue;
                    }

                    if (!type.isEscalationEnabled()) {
                        continue; // No escalation for this type (e.g., notifications)
                    }

                    String targetQueue = type.getEscalationTargetQueue() != null
                            ? type.getEscalationTargetQueue()
                            : DEFAULT_ESCALATION_QUEUE;
                    lifecycleService.escalateTask(task.getId(), targetQueue);
                    escalated++;
                } else {
                    // No task type defined — use default escalation
                    lifecycleService.escalateTask(task.getId(), DEFAULT_ESCALATION_QUEUE);
                    escalated++;
                }
            } catch (Exception e) {
                log.error("Error escalating task {}: {}", task.getId(), e.getMessage());
            }
        }

        // Check overdue RESERVED tasks (reserved but not completed within deadline)
        List<Task> overdueReserved = taskRepository.findOverdueReservedTasks(now);
        for (Task task : overdueReserved) {
            try {
                Optional<TaskType> typeOpt = task.getTaskTypeCode() != null
                        ? taskTypeRepository.findByTaskTypeCode(task.getTaskTypeCode())
                        : Optional.empty();

                if (typeOpt.isPresent()) {
                    TaskType type = typeOpt.get();
                    if (!type.isEscalationEnabled()) continue;

                    // For BOTH type, check completion deadline
                    if (type.getEscalationCheckType() == TaskType.EscalationCheckType.BOTH
                            || type.getEscalationCheckType() == TaskType.EscalationCheckType.NOT_COMPLETED) {

                        String targetQueue = type.getEscalationTargetQueue() != null
                                ? type.getEscalationTargetQueue()
                                : DEFAULT_ESCALATION_QUEUE;
                        lifecycleService.escalateTask(task.getId(), targetQueue);
                        escalated++;
                    }
                } else {
                    lifecycleService.escalateTask(task.getId(), DEFAULT_ESCALATION_QUEUE);
                    escalated++;
                }
            } catch (Exception e) {
                log.error("Error escalating reserved task {}: {}", task.getId(), e.getMessage());
            }
        }

        log.info("Escalation check complete: {} escalated, {} auto-closed", escalated, autoClosed);
    }
}
