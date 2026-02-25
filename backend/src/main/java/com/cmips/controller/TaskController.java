package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.Task;
import com.cmips.entity.TaskHistory;
import com.cmips.entity.WorkQueue;
import com.cmips.repository.WorkQueueRepository;
import com.cmips.service.TaskService;
import com.cmips.service.TaskLifecycleService;
import com.cmips.service.WorkQueueSubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;
    private final TaskLifecycleService lifecycleService;
    private final WorkQueueSubscriptionService subscriptionService;
    private final WorkQueueRepository workQueueRepository;

    public TaskController(TaskService taskService,
                          TaskLifecycleService lifecycleService,
                          WorkQueueSubscriptionService subscriptionService,
                          WorkQueueRepository workQueueRepository) {
        this.taskService = taskService;
        this.lifecycleService = lifecycleService;
        this.subscriptionService = subscriptionService;
        this.workQueueRepository = workQueueRepository;
    }

    /**
     * Get tasks with flexible filtering
     * GET /api/tasks?assignedTo=user&status=OPEN
     * GET /api/tasks?reservedBy=user&status=RESERVED
     * GET /api/tasks?deferredBy=user&status=DEFERRED
     * GET /api/tasks?taskId=123
     * GET /api/tasks?caseNumber=CN123
     */
    @GetMapping
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<List<Task>> getTasks(
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String reservedBy,
            @RequestParam(required = false) String deferredBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String taskId,
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) String caseName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean includeSubscribedQueues) {

        if (taskId != null) {
            try {
                Long id = Long.parseLong(taskId);
                Optional<Task> task = taskService.getTaskById(id);
                return ResponseEntity.ok(task.map(List::of).orElse(List.of()));
            } catch (NumberFormatException e) {
                return ResponseEntity.ok(List.of());
            }
        }

        if (caseNumber != null) {
            return ResponseEntity.ok(taskService.getTasksByCaseNumber(caseNumber));
        }

        if (caseName != null) {
            return ResponseEntity.ok(taskService.searchTasks(caseName));
        }

        if (reservedBy != null) {
            if (status != null) {
                Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(taskService.getReservedTasksByStatus(reservedBy, taskStatus));
            }
            return ResponseEntity.ok(taskService.getReservedTasks(reservedBy));
        }

        if (deferredBy != null) {
            if (status != null) {
                Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(taskService.getDeferredTasksByStatus(deferredBy, taskStatus));
            }
            return ResponseEntity.ok(taskService.getDeferredTasks(deferredBy));
        }

        if (assignedTo != null) {
            if (status != null) {
                Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(taskService.getUserTasksByStatus(assignedTo, taskStatus));
            }
            return ResponseEntity.ok(taskService.getUserTasks(assignedTo));
        }

        if (username != null) {
            List<Task> tasks = taskService.getUserTasks(username);
            if (Boolean.TRUE.equals(includeSubscribedQueues)) {
                List<String> subscribedQueues = subscriptionService.getUserQueues(username);
                List<Task> queueTasks = taskService.getTasksFromSubscribedQueues(username, subscribedQueues);
                for (Task queueTask : queueTasks) {
                    if (tasks.stream().noneMatch(t -> t.getId().equals(queueTask.getId()))) {
                        tasks.add(queueTask);
                    }
                }
            }
            return ResponseEntity.ok(tasks);
        }

        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RequirePermission(resource = "Task Resource", scope = "create")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (task.getSubject() != null && (task.getTitle() == null || task.getTitle().isBlank())) {
            task.setTitle(task.getSubject());
        }
        if (task.getAssignedTo() == null || task.getAssignedTo().isBlank()) {
            task.setAssignedTo("UNASSIGNED");
        }
        // Auto-set workQueue if assignedTo matches a known queue name
        if (task.getWorkQueue() == null || task.getWorkQueue().isBlank()) {
            workQueueRepository.findByName(task.getAssignedTo())
                    .ifPresent(queue -> task.setWorkQueue(queue.getName()));
        }
        Task created = taskService.createTask(task);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return ResponseEntity.ok(taskService.updateTask(task));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // --- Task Lifecycle Endpoints ---

    @PostMapping("/{id}/reserve")
    @RequirePermission(resource = "Task Resource", scope = "reserve")
    public ResponseEntity<Task> reserveTask(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(lifecycleService.reserveTask(id, request.getOrDefault("username", "unknown")));
    }

    @PostMapping("/{id}/unreserve")
    @RequirePermission(resource = "Task Resource", scope = "reserve")
    public ResponseEntity<Task> unreserveTask(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        String username = request != null ? request.getOrDefault("username", "unknown") : "unknown";
        return ResponseEntity.ok(lifecycleService.unreserveTask(id, username));
    }

    @PostMapping("/{id}/forward")
    @RequirePermission(resource = "Task Resource", scope = "forward")
    public ResponseEntity<Task> forwardTask(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(lifecycleService.forwardTask(id,
                request.get("forwardTo"),
                request.getOrDefault("forwardedBy", "unknown"),
                request.get("comments")));
    }

    @PostMapping("/{id}/defer")
    @RequirePermission(resource = "Task Resource", scope = "defer")
    public ResponseEntity<Task> deferTask(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String restartDateStr = request.get("restartDate");
        LocalDateTime restartDate = restartDateStr != null ? LocalDateTime.parse(restartDateStr + "T00:00:00") : null;
        return ResponseEntity.ok(lifecycleService.deferTask(id,
                request.getOrDefault("username", "unknown"), restartDate, request.get("comment")));
    }

    @PostMapping("/{id}/close")
    @RequirePermission(resource = "Task Resource", scope = "close")
    public ResponseEntity<Task> closeTask(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        String username = request != null ? request.getOrDefault("username", "unknown") : "unknown";
        String comments = request != null ? request.get("comments") : null;
        return ResponseEntity.ok(lifecycleService.closeTask(id, username, comments));
    }

    @PostMapping("/{id}/restart")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<Task> restartTask(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        String username = request != null ? request.getOrDefault("username", "unknown") : "unknown";
        return ResponseEntity.ok(lifecycleService.restartTask(id, username));
    }

    @PostMapping("/{id}/reallocate")
    @RequirePermission(resource = "Task Resource", scope = "reallocate")
    public ResponseEntity<Task> reallocateTask(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        String username = request != null ? request.getOrDefault("username", "unknown") : "unknown";
        String comments = request != null ? request.get("comments") : null;
        return ResponseEntity.ok(lifecycleService.reallocateTask(id, username, comments));
    }

    @PostMapping("/{id}/add-comment")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> addComment(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        String username = request != null ? request.getOrDefault("username", "unknown") : "unknown";
        String comment = request != null ? request.getOrDefault("comment", "") : "";
        lifecycleService.addComment(id, username, comment);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{id}/history")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<List<TaskHistory>> getTaskHistory(@PathVariable Long id) {
        return ResponseEntity.ok(lifecycleService.getTaskHistory(id));
    }

    @GetMapping("/{id}/assignments")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<List<TaskHistory>> getAssignmentHistory(@PathVariable Long id) {
        return ResponseEntity.ok(lifecycleService.getAssignmentHistory(id));
    }

    // --- Legacy endpoints ---

    @PutMapping("/{id}/status")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(request.get("status").toUpperCase());
        return ResponseEntity.ok(taskService.updateTaskStatus(id, taskStatus));
    }

    @GetMapping("/status/{username}/{status}")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<List<Task>> getUserTasksByStatus(@PathVariable String username, @PathVariable String status) {
        return ResponseEntity.ok(taskService.getUserTasksByStatus(username, taskService.getTaskStatusByName(status)));
    }

    @GetMapping("/count/{username}")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<Map<String, Long>> getTaskCounts(@PathVariable String username) {
        return ResponseEntity.ok(Map.of(
                "open", taskService.getUserTaskCount(username),
                "reserved", taskService.getUserTaskCountByStatus(username, Task.TaskStatus.RESERVED),
                "closed", taskService.getUserTaskCountByStatus(username, Task.TaskStatus.CLOSED)));
    }

    @GetMapping("/queues")
    @RequirePermission(resource = "Task Resource", scope = "view")
    public ResponseEntity<Map<String, Long>> getAvailableQueues() {
        return ResponseEntity.ok(taskService.getAvailableQueues());
    }
}
