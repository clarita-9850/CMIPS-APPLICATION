package com.cmips.service;

import com.cmips.entity.Task;
import com.cmips.entity.TaskHistory;
import com.cmips.entity.Notification;
import com.cmips.repository.TaskRepository;
import com.cmips.repository.TaskHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TaskLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(TaskLifecycleService.class);

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository historyRepository;
    private final NotificationService notificationService;

    public TaskLifecycleService(TaskRepository taskRepository,
                                TaskHistoryRepository historyRepository,
                                NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
    }

    /**
     * Reserve a single task — pull from queue, lock to user
     * OPEN → RESERVED
     */
    @Transactional
    public Task reserveTask(Long taskId, String username) {
        Task task = getTaskOrThrow(taskId);
        validateStatus(task, Task.TaskStatus.OPEN, "reserve");

        String previousStatus = task.getStatus().name();
        task.setStatus(Task.TaskStatus.RESERVED);
        task.setReservedBy(username);
        task.setReservedDate(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.RESERVED, username, previousStatus, "RESERVED", null,
                "Reserved by " + username);
        log.info("Task {} reserved by {}", taskId, username);
        return saved;
    }

    /**
     * Reserve next N tasks from a queue for a user
     */
    @Transactional
    public List<Task> reserveNextTasks(String queueName, String username, int count) {
        List<Task> available = taskRepository.findOpenTasksInQueueByPriority(queueName);
        int toReserve = Math.min(count, available.size());

        List<Task> reserved = new java.util.ArrayList<>();
        for (int i = 0; i < toReserve; i++) {
            Task task = available.get(i);
            task.setStatus(Task.TaskStatus.RESERVED);
            task.setReservedBy(username);
            task.setReservedDate(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            reserved.add(taskRepository.save(task));

            recordHistory(task.getId(), TaskHistory.TaskAction.RESERVED, username, "OPEN", "RESERVED", null,
                    "Reserved from queue " + queueName);
        }
        log.info("Reserved {} tasks from queue {} for user {}", reserved.size(), queueName, username);
        return reserved;
    }

    /**
     * Reserve selected tasks by IDs
     */
    @Transactional
    public List<Task> reserveSelectedTasks(List<Long> taskIds, String username) {
        List<Task> reserved = new java.util.ArrayList<>();
        for (Long taskId : taskIds) {
            Task task = getTaskOrThrow(taskId);
            if (task.getStatus() == Task.TaskStatus.OPEN) {
                task.setStatus(Task.TaskStatus.RESERVED);
                task.setReservedBy(username);
                task.setReservedDate(LocalDateTime.now());
                task.setUpdatedAt(LocalDateTime.now());
                reserved.add(taskRepository.save(task));

                recordHistory(taskId, TaskHistory.TaskAction.RESERVED, username, "OPEN", "RESERVED", null,
                        "Selected and reserved");
            }
        }
        log.info("Reserved {} selected tasks for user {}", reserved.size(), username);
        return reserved;
    }

    /**
     * Un-reserve — release task back to queue
     * RESERVED → OPEN
     */
    @Transactional
    public Task unreserveTask(Long taskId, String username) {
        Task task = getTaskOrThrow(taskId);
        validateStatus(task, Task.TaskStatus.RESERVED, "unreserve");

        String previousStatus = task.getStatus().name();
        task.setStatus(Task.TaskStatus.OPEN);
        task.setReservedBy(null);
        task.setReservedDate(null);
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.UNRESERVED, username, previousStatus, "OPEN", null,
                "Un-reserved by " + username);
        log.info("Task {} un-reserved by {}", taskId, username);
        return saved;
    }

    /**
     * Forward task to another user or queue
     */
    @Transactional
    public Task forwardTask(Long taskId, String forwardTo, String forwardedBy, String comments) {
        Task task = getTaskOrThrow(taskId);

        String previousStatus = task.getStatus().name();
        task.setStatus(Task.TaskStatus.OPEN);
        task.setForwardedTo(forwardTo);
        task.setForwardedBy(forwardedBy);
        task.setForwardedDate(LocalDateTime.now());
        task.setReservedBy(null);
        task.setReservedDate(null);
        task.setAssignedTo(forwardTo);
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.FORWARDED, forwardedBy, previousStatus, "OPEN", comments,
                "Forwarded to " + forwardTo);

        // Notify the target
        createNotification(forwardTo, "Task forwarded to you: " + task.getTitle(),
                "/tasks/" + taskId, Notification.NotificationType.INFO);

        log.info("Task {} forwarded from {} to {}", taskId, forwardedBy, forwardTo);
        return saved;
    }

    /**
     * Defer task with a restart date
     * RESERVED/ASSIGNED → DEFERRED
     */
    @Transactional
    public Task deferTask(Long taskId, String username, LocalDateTime restartDate, String comment) {
        Task task = getTaskOrThrow(taskId);
        if (task.getStatus() != Task.TaskStatus.RESERVED && task.getStatus() != Task.TaskStatus.ASSIGNED) {
            throw new IllegalStateException("Cannot defer task in status: " + task.getStatus());
        }

        String previousStatus = task.getStatus().name();
        task.setStatus(Task.TaskStatus.DEFERRED);
        task.setDeferredBy(username);
        task.setDeferredDate(LocalDateTime.now());
        task.setRestartDate(restartDate);
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.DEFERRED, username, previousStatus, "DEFERRED", comment,
                "Deferred until " + restartDate);
        log.info("Task {} deferred by {} until {}", taskId, username, restartDate);
        return saved;
    }

    /**
     * Close task
     */
    @Transactional
    public Task closeTask(Long taskId, String username, String comments) {
        Task task = getTaskOrThrow(taskId);
        if (task.getStatus() == Task.TaskStatus.CLOSED) {
            throw new IllegalStateException("Task is already closed");
        }

        String previousStatus = task.getStatus().name();
        task.setStatus(Task.TaskStatus.CLOSED);
        task.setClosedBy(username);
        task.setClosedDate(LocalDateTime.now());
        task.setCloseComments(comments);
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.CLOSED, username, previousStatus, "CLOSED", comments,
                "Closed by " + username);
        log.info("Task {} closed by {}", taskId, username);
        return saved;
    }

    /**
     * Restart a closed task
     * CLOSED → OPEN
     */
    @Transactional
    public Task restartTask(Long taskId, String username) {
        Task task = getTaskOrThrow(taskId);
        if (task.getStatus() != Task.TaskStatus.CLOSED
                && task.getStatus() != Task.TaskStatus.ESCALATED
                && task.getStatus() != Task.TaskStatus.DEFERRED) {
            throw new IllegalStateException("Cannot restart task in status: " + task.getStatus());
        }

        String previousStatus = task.getStatus().name();
        task.setStatus(Task.TaskStatus.OPEN);
        task.setClosedBy(null);
        task.setClosedDate(null);
        task.setCloseComments(null);
        task.setReservedBy(null);
        task.setReservedDate(null);
        task.setDeferredBy(null);
        task.setDeferredDate(null);
        task.setRestartDate(null);
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.RESTARTED, username, previousStatus, "OPEN", null,
                "Restarted by " + username);
        log.info("Task {} restarted by {}", taskId, username);
        return saved;
    }

    /**
     * Reallocate — return reserved/assigned task to queue
     * RESERVED/ASSIGNED → OPEN
     */
    @Transactional
    public Task reallocateTask(Long taskId, String username, String comments) {
        Task task = getTaskOrThrow(taskId);
        if (task.getStatus() != Task.TaskStatus.RESERVED && task.getStatus() != Task.TaskStatus.ASSIGNED) {
            throw new IllegalStateException("Cannot reallocate task in status: " + task.getStatus());
        }

        String previousStatus = task.getStatus().name();
        task.setStatus(Task.TaskStatus.OPEN);
        task.setReservedBy(null);
        task.setReservedDate(null);
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.REALLOCATED, username, previousStatus, "OPEN", comments,
                "Reallocated by " + username);

        if (task.getAssignedTo() != null && !task.getAssignedTo().equals(username)) {
            createNotification(task.getAssignedTo(), "Task has been reallocated: " + task.getTitle(),
                    "/tasks/" + taskId, Notification.NotificationType.INFO);
        }

        log.info("Task {} reallocated by {}", taskId, username);
        return saved;
    }

    /**
     * Assign task directly to a user (push model)
     * OPEN → ASSIGNED
     */
    @Transactional
    public Task assignTask(Long taskId, String assignTo, String assignedBy) {
        Task task = getTaskOrThrow(taskId);
        validateStatus(task, Task.TaskStatus.OPEN, "assign");

        String previousStatus = task.getStatus().name();
        task.setStatus(Task.TaskStatus.ASSIGNED);
        task.setAssignedTo(assignTo);
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.ASSIGNED, assignedBy, previousStatus, "ASSIGNED", null,
                "Assigned to " + assignTo + " by " + assignedBy);

        createNotification(assignTo, "Task assigned to you: " + task.getTitle(),
                "/tasks/" + taskId, Notification.NotificationType.INFO);

        log.info("Task {} assigned to {} by {}", taskId, assignTo, assignedBy);
        return saved;
    }

    /**
     * Add comment to task
     */
    @Transactional
    public void addComment(Long taskId, String username, String comment) {
        getTaskOrThrow(taskId);
        recordHistory(taskId, TaskHistory.TaskAction.COMMENT_ADDED, username,
                null, null, comment, null);
        log.info("Comment added to task {} by {}", taskId, username);
    }

    /**
     * Modify time worked
     */
    @Transactional
    public Task modifyTimeWorked(Long taskId, String username, int minutes) {
        Task task = getTaskOrThrow(taskId);
        task.setTimeWorked((task.getTimeWorked() != null ? task.getTimeWorked() : 0) + minutes);
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.TIME_MODIFIED, username, null, null, null,
                "Time worked updated to " + task.getTimeWorked() + " minutes");
        return saved;
    }

    /**
     * Escalate a task to supervisor queue
     */
    @Transactional
    public Task escalateTask(Long taskId, String escalationQueue) {
        Task task = getTaskOrThrow(taskId);

        String previousStatus = task.getStatus().name();
        task.setStatus(Task.TaskStatus.ESCALATED);
        task.setWorkQueue(escalationQueue);
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.ESCALATED, "SYSTEM", previousStatus, "ESCALATED", null,
                "Escalated to " + escalationQueue + " — deadline exceeded");
        log.info("Task {} escalated to queue {}", taskId, escalationQueue);
        return saved;
    }

    /**
     * Auto-close a task (system action)
     */
    @Transactional
    public Task autoCloseTask(Long taskId) {
        Task task = getTaskOrThrow(taskId);
        String previousStatus = task.getStatus().name();
        task.setStatus(Task.TaskStatus.CLOSED);
        task.setClosedBy("SYSTEM");
        task.setClosedDate(LocalDateTime.now());
        task.setCloseComments("Auto-closed after deadline");
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        recordHistory(taskId, TaskHistory.TaskAction.AUTO_CLOSED, "SYSTEM", previousStatus, "CLOSED", null,
                "Auto-closed — deadline exceeded without escalation");
        log.info("Task {} auto-closed", taskId);
        return saved;
    }

    /**
     * Get task history
     */
    public List<TaskHistory> getTaskHistory(Long taskId) {
        return historyRepository.findByTaskIdOrderByPerformedAtDesc(taskId);
    }

    /**
     * Get assignment history for a task
     */
    public List<TaskHistory> getAssignmentHistory(Long taskId) {
        return historyRepository.findByTaskId(taskId).stream()
                .filter(h -> h.getAction() == TaskHistory.TaskAction.ASSIGNED
                        || h.getAction() == TaskHistory.TaskAction.RESERVED
                        || h.getAction() == TaskHistory.TaskAction.FORWARDED
                        || h.getAction() == TaskHistory.TaskAction.REALLOCATED)
                .toList();
    }

    // --- Helpers ---

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
    }

    private void validateStatus(Task task, Task.TaskStatus expected, String operation) {
        if (task.getStatus() != expected) {
            throw new IllegalStateException(
                    "Cannot " + operation + " task — expected status " + expected + " but was " + task.getStatus());
        }
    }

    private void recordHistory(Long taskId, TaskHistory.TaskAction action, String performedBy,
                               String previousStatus, String newStatus, String comments, String details) {
        TaskHistory history = new TaskHistory();
        history.setTaskId(taskId);
        history.setAction(action);
        history.setPerformedBy(performedBy);
        history.setPerformedAt(LocalDateTime.now());
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setComments(comments);
        history.setDetails(details);
        historyRepository.save(history);
    }

    private void createNotification(String userId, String message, String actionLink,
                                    Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .notificationType(type)
                .actionLink(actionLink)
                .readStatus(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationService.createNotification(notification);
    }
}
