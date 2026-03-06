package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.Task;
import com.cmips.entity.WorkQueue;
import com.cmips.entity.WorkQueueSubscription;
import com.cmips.repository.WorkQueueRepository;
import com.cmips.service.KeycloakPolicyEvaluationService;
import com.cmips.service.TaskLifecycleService;
import com.cmips.service.TaskService;
import com.cmips.service.WorkQueueSubscriptionService;
import com.cmips.service.KeycloakAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-queues")
@CrossOrigin(origins = "*")
public class WorkQueueController {

    private static final Logger log = LoggerFactory.getLogger(WorkQueueController.class);

    private final WorkQueueRepository workQueueRepository;
    private final TaskService taskService;
    private final TaskLifecycleService lifecycleService;
    private final WorkQueueSubscriptionService subscriptionService;
    private final KeycloakAdminService keycloakAdminService;
    private final KeycloakPolicyEvaluationService policyEvaluationService;

    public WorkQueueController(WorkQueueRepository workQueueRepository,
                               TaskService taskService,
                               TaskLifecycleService lifecycleService,
                               WorkQueueSubscriptionService subscriptionService,
                               KeycloakAdminService keycloakAdminService,
                               KeycloakPolicyEvaluationService policyEvaluationService) {
        this.workQueueRepository = workQueueRepository;
        this.taskService = taskService;
        this.lifecycleService = lifecycleService;
        this.subscriptionService = subscriptionService;
        this.keycloakAdminService = keycloakAdminService;
        this.policyEvaluationService = policyEvaluationService;
    }

    /**
     * List all active work queues
     * GET /api/work-queues
     */
    @GetMapping
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<List<WorkQueue>> getAllQueues() {
        List<WorkQueue> queues = workQueueRepository.findByActiveTrue();
        return ResponseEntity.ok(queues);
    }

    /**
     * Get work queue by ID
     * GET /api/work-queues/{id}
     */
    @GetMapping("/{id}")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<WorkQueue> getQueueById(@PathVariable Long id) {
        return workQueueRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get tasks in a specific queue
     * GET /api/work-queues/{id}/tasks
     * Supervisor-only queues require supervisor role.
     */
    @GetMapping("/{id}/tasks")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<?> getQueueTasks(@PathVariable Long id) {
        return workQueueRepository.findById(id)
                .map(queue -> {
                    if (queue.isSupervisorOnly()) {
                        java.util.Set<String> roles = policyEvaluationService.getCurrentUserRoles();
                        if (roles == null || !roles.stream().anyMatch(r -> "SUPERVISORROLE".equalsIgnoreCase(r) || "SUPERVISOR".equalsIgnoreCase(r))) {
                            return ResponseEntity.status(403).body(Map.of("error", "Access denied", "message", "This queue is restricted to supervisors"));
                        }
                    }
                    List<Task> tasks = taskService.getQueueTasks(queue.getName());
                    return ResponseEntity.ok(tasks);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Reserve next N tasks from a queue
     * POST /api/work-queues/{id}/reserve
     * Body: { "count": 1, "username": "user1" }
     * Supervisor-only queues require supervisor role.
     */
    @PostMapping("/{id}/reserve")
    @RequirePermission(resource = "Work Queue Resource", scope = "reserve")
    public ResponseEntity<?> reserveFromQueue(@PathVariable Long id,
                                              @RequestBody Map<String, Object> request) {
        return workQueueRepository.findById(id)
                .map(queue -> {
                    if (queue.isSupervisorOnly()) {
                        java.util.Set<String> roles = policyEvaluationService.getCurrentUserRoles();
                        if (roles == null || !roles.stream().anyMatch(r -> "SUPERVISORROLE".equalsIgnoreCase(r) || "SUPERVISOR".equalsIgnoreCase(r))) {
                            return ResponseEntity.status(403).body(Map.of("error", "Access denied", "message", "This queue is restricted to supervisors"));
                        }
                    }
                    String username = (String) request.get("username");
                    int count = request.containsKey("count") ? ((Number) request.get("count")).intValue() : 1;

                    if (username == null || username.isBlank()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "username is required"));
                    }

                    List<Task> reserved = lifecycleService.reserveNextTasks(queue.getName(), username, count);
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "reserved", reserved.size(),
                            "tasks", reserved
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get subscribers for a queue
     * GET /api/work-queues/{id}/subscribers
     */
    @GetMapping("/{id}/subscribers")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<List<WorkQueueSubscription>> getQueueSubscribers(@PathVariable Long id) {
        return workQueueRepository.findById(id)
                .map(queue -> {
                    List<WorkQueueSubscription> subs = subscriptionService.getQueueSubscriptions(queue.getName());
                    return ResponseEntity.ok(subs);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Subscribe a user to a queue
     * POST /api/work-queues/{id}/subscribe
     * Body: { "username": "user1", "subscribedBy": "admin" }
     */
    @PostMapping("/{id}/subscribe")
    @RequirePermission(resource = "Work Queue Resource", scope = "subscribe")
    public ResponseEntity<?> subscribeToQueue(@PathVariable Long id,
                                              @RequestBody Map<String, String> request) {
        return workQueueRepository.findById(id)
                .map(queue -> {
                    String username = request.get("username");
                    String subscribedBy = request.get("subscribedBy");

                    if (username == null) {
                        return ResponseEntity.badRequest().body(Map.of("error", "username is required"));
                    }

                    try {
                        WorkQueueSubscription sub = subscriptionService.subscribeUserToQueue(
                                username, queue.getName(), subscribedBy);
                        return ResponseEntity.ok(Map.of("success", true, "subscription", sub));
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Unsubscribe a user from a queue
     * DELETE /api/work-queues/{id}/subscribe
     */
    @DeleteMapping("/{id}/subscribe")
    @RequirePermission(resource = "Work Queue Resource", scope = "subscribe")
    public ResponseEntity<?> unsubscribeFromQueue(@PathVariable Long id,
                                                  @RequestBody Map<String, String> request) {
        return workQueueRepository.findById(id)
                .map(queue -> {
                    String username = request.get("username");
                    if (username == null) {
                        return ResponseEntity.badRequest().body(Map.of("error", "username is required"));
                    }
                    subscriptionService.unsubscribeUserFromQueue(username, queue.getName());
                    return ResponseEntity.ok(Map.of("success", true));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get queue summary with task counts
     * GET /api/work-queues/summary
     */
    @GetMapping("/summary")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<List<Map<String, Object>>> getQueuesSummary() {
        List<WorkQueue> queues = workQueueRepository.findByActiveTrue();
        List<Map<String, Object>> summary = new java.util.ArrayList<>();

        for (WorkQueue queue : queues) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", queue.getId());
            item.put("name", queue.getName());
            item.put("displayName", queue.getDisplayName());
            item.put("category", queue.getQueueCategory());
            item.put("openTasks", taskService.getQueueTaskCountByStatus(queue.getName(), Task.TaskStatus.OPEN));
            summary.add(item);
        }

        return ResponseEntity.ok(summary);
    }

    /**
     * Get all users (for subscription management)
     * GET /api/work-queues/users
     */
    @GetMapping("/users")
    @RequirePermission(resource = "Work Queue Resource", scope = "manage")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<Map<String, Object>> users = keycloakAdminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(List.of());
        }
    }

    // --- Legacy endpoints for backward compatibility ---

    @GetMapping("/catalog")
    @RequirePermission(resource = "Work Queue Resource", scope = "view")
    public ResponseEntity<List<WorkQueue>> getQueueCatalog() {
        return getAllQueues();
    }
}
