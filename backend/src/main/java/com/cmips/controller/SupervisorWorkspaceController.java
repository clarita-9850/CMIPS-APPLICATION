package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.Task;
import com.cmips.entity.WorkQueue;
import com.cmips.entity.WorkQueueSubscription;
import com.cmips.repository.TaskRepository;
import com.cmips.repository.WorkQueueRepository;
import com.cmips.service.KeycloakPolicyEvaluationService;
import com.cmips.service.TaskLifecycleService;
import com.cmips.service.TaskService;
import com.cmips.service.WorkQueueSubscriptionService;
import com.cmips.util.BusinessDayCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DSD GAP 5 — Supervisor Workspace Controller.
 * <p>
 * Per DSD Section 7, supervisors need 20+ screens for team management:
 * - View team workload
 * - Reassign tasks between workers
 * - Block/unblock task allocation
 * - Monitor escalated tasks
 * - Override task deadlines
 * - View worker queue subscriptions
 * <p>
 * All endpoints require SUPERVISORROLE.
 */
@RestController
@RequestMapping("/api/supervisor")
@CrossOrigin(origins = "*")
public class SupervisorWorkspaceController {

    private static final Logger log = LoggerFactory.getLogger(SupervisorWorkspaceController.class);

    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final TaskLifecycleService lifecycleService;
    private final WorkQueueRepository workQueueRepository;
    private final WorkQueueSubscriptionService subscriptionService;
    private final KeycloakPolicyEvaluationService policyEvaluationService;
    private final BusinessDayCalculator businessDayCalc;

    public SupervisorWorkspaceController(TaskRepository taskRepository,
                                          TaskService taskService,
                                          TaskLifecycleService lifecycleService,
                                          WorkQueueRepository workQueueRepository,
                                          WorkQueueSubscriptionService subscriptionService,
                                          KeycloakPolicyEvaluationService policyEvaluationService,
                                          BusinessDayCalculator businessDayCalc) {
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.lifecycleService = lifecycleService;
        this.workQueueRepository = workQueueRepository;
        this.subscriptionService = subscriptionService;
        this.policyEvaluationService = policyEvaluationService;
        this.businessDayCalc = businessDayCalc;
    }

    // ==================== Team Workload ====================

    /**
     * Get team workload summary — tasks per worker with status breakdown.
     * GET /api/supervisor/team-workload?workers=user1,user2,user3
     */
    @GetMapping("/team-workload")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<?> getTeamWorkload(@RequestParam List<String> workers) {
        requireSupervisorRole();

        List<Map<String, Object>> workload = new ArrayList<>();
        for (String worker : workers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("username", worker);
            item.put("openTasks", taskService.getUserTaskCountByStatus(worker, Task.TaskStatus.OPEN));
            item.put("reservedTasks", taskService.getUserTaskCountByStatus(worker, Task.TaskStatus.RESERVED));
            item.put("assignedTasks", taskService.getUserTaskCountByStatus(worker, Task.TaskStatus.ASSIGNED));
            item.put("deferredTasks", taskService.getUserTaskCountByStatus(worker, Task.TaskStatus.DEFERRED));
            item.put("escalatedTasks", taskService.getUserTaskCountByStatus(worker, Task.TaskStatus.ESCALATED));

            // Overdue count
            List<Task> userTasks = taskService.getUserTasks(worker);
            long overdue = userTasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now()))
                    .filter(t -> t.getStatus() != Task.TaskStatus.CLOSED)
                    .count();
            item.put("overdueTasks", overdue);

            // Queue subscriptions
            List<String> queues = subscriptionService.getUserQueues(worker);
            item.put("subscribedQueues", queues);
            item.put("queueCount", queues.size());

            workload.add(item);
        }

        return ResponseEntity.ok(workload);
    }

    // ==================== Escalated Tasks ====================

    /**
     * Get all escalated tasks visible to supervisor.
     * GET /api/supervisor/escalated-tasks
     */
    @GetMapping("/escalated-tasks")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<?> getEscalatedTasks(@RequestParam(required = false) String county) {
        requireSupervisorRole();

        List<Task> escalated = taskRepository.findByStatus(Task.TaskStatus.ESCALATED);

        if (county != null && !county.isBlank()) {
            escalated = escalated.stream()
                    .filter(t -> county.equalsIgnoreCase(t.getCounty()))
                    .toList();
        }

        return ResponseEntity.ok(escalated);
    }

    // ==================== Bulk Reassign ====================

    /**
     * Bulk reassign tasks from one worker to another.
     * POST /api/supervisor/bulk-reassign
     * Body: { "fromWorker": "user1", "toWorker": "user2", "taskIds": [1,2,3] }
     * If taskIds is empty/null, reassigns ALL open/reserved tasks from fromWorker.
     */
    @PostMapping("/bulk-reassign")
    @RequirePermission(resource = "Work Queue Resource", scope = "manage")
    public ResponseEntity<?> bulkReassign(@RequestBody Map<String, Object> request) {
        requireSupervisorRole();

        String fromWorker = (String) request.get("fromWorker");
        String toWorker = (String) request.get("toWorker");
        @SuppressWarnings("unchecked")
        List<Number> taskIdNumbers = (List<Number>) request.get("taskIds");
        String comments = (String) request.get("comments");

        if (fromWorker == null || toWorker == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "fromWorker and toWorker are required"));
        }

        List<Task> tasksToReassign;
        if (taskIdNumbers != null && !taskIdNumbers.isEmpty()) {
            List<Long> taskIds = taskIdNumbers.stream().map(Number::longValue).toList();
            tasksToReassign = taskIds.stream()
                    .map(id -> taskRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .filter(t -> fromWorker.equals(t.getAssignedTo()) || fromWorker.equals(t.getReservedBy()))
                    .toList();
        } else {
            tasksToReassign = taskService.getUserTasks(fromWorker).stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.OPEN
                            || t.getStatus() == Task.TaskStatus.RESERVED
                            || t.getStatus() == Task.TaskStatus.ASSIGNED)
                    .toList();
        }

        int reassigned = 0;
        for (Task task : tasksToReassign) {
            try {
                lifecycleService.forwardTask(task.getId(), toWorker, "SUPERVISOR",
                        comments != null ? comments : "Supervisor bulk reassignment from " + fromWorker);
                reassigned++;
            } catch (Exception e) {
                log.error("Failed to reassign task {}: {}", task.getId(), e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "reassigned", reassigned,
                "fromWorker", fromWorker,
                "toWorker", toWorker
        ));
    }

    // ==================== Task Deadline Override ====================

    /**
     * Override a task's deadline (extend or shorten).
     * PUT /api/supervisor/tasks/{taskId}/deadline
     * Body: { "newDueDate": "2026-03-20T17:00:00", "reason": "..." }
     * OR: { "extendBusinessDays": 5, "reason": "..." }
     */
    @PutMapping("/tasks/{taskId}/deadline")
    @RequirePermission(resource = "Work Queue Resource", scope = "manage")
    public ResponseEntity<?> overrideDeadline(@PathVariable Long taskId,
                                               @RequestBody Map<String, Object> request) {
        requireSupervisorRole();

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return ResponseEntity.notFound().build();
        if (task.getStatus() == Task.TaskStatus.CLOSED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot modify deadline of a closed task"));
        }

        String reason = (String) request.get("reason");

        if (request.containsKey("newDueDate")) {
            LocalDateTime newDue = LocalDateTime.parse((String) request.get("newDueDate"));
            task.setDueDate(newDue);
        } else if (request.containsKey("extendBusinessDays")) {
            int days = ((Number) request.get("extendBusinessDays")).intValue();
            LocalDateTime base = task.getDueDate() != null ? task.getDueDate() : LocalDateTime.now();
            task.setDueDate(businessDayCalc.addBusinessDays(base, days));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Provide newDueDate or extendBusinessDays"));
        }

        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        log.info("Supervisor overrode deadline for task {} — new due: {}, reason: {}",
                taskId, task.getDueDate(), reason);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "taskId", taskId,
                "newDueDate", task.getDueDate().toString(),
                "reason", reason != null ? reason : ""
        ));
    }

    // ==================== Queue Oversight ====================

    /**
     * Get all queues with detailed statistics for supervisor.
     * GET /api/supervisor/queue-overview
     */
    @GetMapping("/queue-overview")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<?> getQueueOverview() {
        requireSupervisorRole();

        List<WorkQueue> queues = workQueueRepository.findByActiveTrue();
        List<Map<String, Object>> overview = new ArrayList<>();

        for (WorkQueue queue : queues) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", queue.getId());
            item.put("name", queue.getName());
            item.put("displayName", queue.getDisplayName());
            item.put("category", queue.getQueueCategory());
            item.put("supervisorOnly", queue.isSupervisorOnly());
            item.put("openTasks", taskService.getQueueTaskCountByStatus(queue.getName(), Task.TaskStatus.OPEN));
            item.put("reservedTasks", taskService.getQueueTaskCountByStatus(queue.getName(), Task.TaskStatus.RESERVED));
            item.put("escalatedTasks", taskService.getQueueTaskCountByStatus(queue.getName(), Task.TaskStatus.ESCALATED));

            // Subscriber count
            List<WorkQueueSubscription> subs = subscriptionService.getQueueSubscriptions(queue.getName());
            item.put("subscriberCount", subs.size());
            item.put("subscribers", subs.stream().map(WorkQueueSubscription::getUsername).toList());

            // Overdue in queue
            List<Task> queueTasks = taskService.getQueueTasks(queue.getName());
            long overdue = queueTasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now()))
                    .filter(t -> t.getStatus() != Task.TaskStatus.CLOSED)
                    .count();
            item.put("overdueTasks", overdue);

            overview.add(item);
        }

        return ResponseEntity.ok(overview);
    }

    // ==================== Worker Queue Management ====================

    /**
     * Get a worker's queue subscriptions and task counts.
     * GET /api/supervisor/worker/{username}/queues
     */
    @GetMapping("/worker/{username}/queues")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<?> getWorkerQueues(@PathVariable String username) {
        requireSupervisorRole();

        List<WorkQueueSubscription> subscriptions = subscriptionService.getUserSubscriptions(username);
        List<Map<String, Object>> result = new ArrayList<>();

        for (WorkQueueSubscription sub : subscriptions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("queueName", sub.getWorkQueue());
            item.put("subscribedAt", sub.getCreatedAt());
            item.put("subscribedBy", sub.getSubscribedBy());
            item.put("openTasks", taskService.getQueueTaskCountByStatus(sub.getWorkQueue(), Task.TaskStatus.OPEN));
            result.add(item);
        }

        return ResponseEntity.ok(Map.of(
                "username", username,
                "subscriptions", result,
                "totalOpenTasks", taskService.getUserTaskCount(username),
                "totalReservedTasks", taskService.getUserTaskCountByStatus(username, Task.TaskStatus.RESERVED)
        ));
    }

    /**
     * Get a worker's tasks with status breakdown.
     * GET /api/supervisor/worker/{username}/tasks?status=OPEN
     */
    @GetMapping("/worker/{username}/tasks")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<?> getWorkerTasks(@PathVariable String username,
                                             @RequestParam(required = false) String status) {
        requireSupervisorRole();

        if (status != null) {
            Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(taskService.getUserTasksByStatus(username, taskStatus));
        }

        return ResponseEntity.ok(taskService.getUserTasks(username));
    }

    // ==================== Allocation Blocking ====================

    /**
     * Block/unblock a queue's subscription (prevent new workers from subscribing).
     * PUT /api/supervisor/queues/{queueId}/subscription-allowed
     * Body: { "allowed": false, "reason": "..." }
     */
    @PutMapping("/queues/{queueId}/subscription-allowed")
    @RequirePermission(resource = "Work Queue Resource", scope = "manage")
    public ResponseEntity<?> setSubscriptionAllowed(@PathVariable Long queueId,
                                                     @RequestBody Map<String, Object> request) {
        requireSupervisorRole();

        return workQueueRepository.findById(queueId)
                .map(queue -> {
                    Boolean allowed = (Boolean) request.get("allowed");
                    if (allowed == null) {
                        return ResponseEntity.badRequest().body(Map.of("error", "allowed (boolean) is required"));
                    }
                    queue.setSubscriptionAllowed(allowed);
                    queue.setUpdatedAt(LocalDateTime.now());
                    workQueueRepository.save(queue);

                    log.info("Supervisor {} subscription for queue {} — {}",
                            allowed ? "enabled" : "blocked", queue.getName(), request.get("reason"));

                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "queueName", queue.getName(),
                            "subscriptionAllowed", allowed
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Dashboard Summary ====================

    /**
     * Overall supervisor dashboard summary.
     * GET /api/supervisor/dashboard
     */
    @GetMapping("/dashboard")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<?> getDashboard() {
        requireSupervisorRole();

        Map<String, Object> dashboard = new LinkedHashMap<>();

        // Escalated tasks count
        long escalatedCount = taskRepository.findByStatus(Task.TaskStatus.ESCALATED).size();
        dashboard.put("escalatedTasks", escalatedCount);

        // Overdue open tasks
        List<Task> overdueOpen = taskRepository.findOverdueOpenTasks(LocalDateTime.now());
        dashboard.put("overdueOpenTasks", overdueOpen.size());

        // Overdue reserved tasks
        List<Task> overdueReserved = taskRepository.findOverdueReservedTasks(LocalDateTime.now());
        dashboard.put("overdueReservedTasks", overdueReserved.size());

        // Active queues count
        List<WorkQueue> activeQueues = workQueueRepository.findByActiveTrue();
        dashboard.put("activeQueues", activeQueues.size());

        // Total open tasks across all queues
        long totalOpen = activeQueues.stream()
                .mapToLong(q -> taskService.getQueueTaskCountByStatus(q.getName(), Task.TaskStatus.OPEN))
                .sum();
        dashboard.put("totalOpenTasks", totalOpen);

        // Deferred tasks ready for restart
        List<Task> readyToRestart = taskRepository.findDeferredTasksReadyToRestart(LocalDateTime.now());
        dashboard.put("deferredReadyToRestart", readyToRestart.size());

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Helpers ====================

    private void requireSupervisorRole() {
        Set<String> roles = policyEvaluationService.getCurrentUserRoles();
        boolean isSupervisor = roles != null && roles.stream()
                .anyMatch(r -> r != null && r.toUpperCase().contains("SUPERVISOR"));
        if (!isSupervisor) {
            throw new org.springframework.security.access.AccessDeniedException("Supervisor role required");
        }
    }
}
