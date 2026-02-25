package com.ihss.scheduler.controller;

import com.ihss.scheduler.annotation.RequirePermission;
import com.ihss.scheduler.service.CmipsBackendClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduler/admin")
@Tag(name = "Admin", description = "Administrative APIs for scheduler management")
public class AdminController {

    private final Scheduler quartzScheduler;
    private final CmipsBackendClient cmipsBackendClient;

    public AdminController(Scheduler quartzScheduler, CmipsBackendClient cmipsBackendClient) {
        this.quartzScheduler = quartzScheduler;
        this.cmipsBackendClient = cmipsBackendClient;
    }

    @GetMapping("/status")
    @RequirePermission(resource = "Scheduler Admin Resource", scope = "status", message = "You don't have permission to view scheduler status")
    @Operation(summary = "Get scheduler status", description = "Get the current status of the scheduler")
    public ResponseEntity<Map<String, Object>> getStatus() throws SchedulerException {
        Map<String, Object> status = new HashMap<>();

        status.put("schedulerName", quartzScheduler.getSchedulerName());
        status.put("schedulerInstanceId", quartzScheduler.getSchedulerInstanceId());
        status.put("isStarted", quartzScheduler.isStarted());
        status.put("isShutdown", quartzScheduler.isShutdown());
        status.put("isStandby", quartzScheduler.isInStandbyMode());
        status.put("cmipsBackendHealthy", cmipsBackendClient.healthCheck());

        return ResponseEntity.ok(status);
    }

    @PostMapping("/pause")
    @RequirePermission(resource = "Scheduler Admin Resource", scope = "pause", message = "You don't have permission to pause the scheduler")
    @Operation(summary = "Pause scheduler", description = "Pause all scheduled job execution")
    public ResponseEntity<Map<String, String>> pauseScheduler() throws SchedulerException {
        quartzScheduler.standby();
        return ResponseEntity.ok(Map.of("status", "paused", "message", "Scheduler is now in standby mode"));
    }

    @PostMapping("/resume")
    @RequirePermission(resource = "Scheduler Admin Resource", scope = "resume", message = "You don't have permission to resume the scheduler")
    @Operation(summary = "Resume scheduler", description = "Resume scheduled job execution")
    public ResponseEntity<Map<String, String>> resumeScheduler() throws SchedulerException {
        quartzScheduler.start();
        return ResponseEntity.ok(Map.of("status", "running", "message", "Scheduler has been resumed"));
    }

    @GetMapping("/health/cmips-backend")
    @RequirePermission(resource = "Scheduler Admin Resource", scope = "status", message = "You don't have permission to check health status")
    @Operation(summary = "Check CMIPS backend health", description = "Check if CMIPS backend is reachable")
    public ResponseEntity<Map<String, Object>> checkCmipsBackendHealth() {
        boolean healthy = cmipsBackendClient.healthCheck();
        Map<String, Object> response = new HashMap<>();
        response.put("healthy", healthy);
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics")
    @RequirePermission(resource = "Scheduler Admin Resource", scope = "metrics", message = "You don't have permission to view scheduler metrics")
    @Operation(summary = "Get scheduler metrics", description = "Get metrics about the scheduler")
    public ResponseEntity<Map<String, Object>> getMetrics() throws SchedulerException {
        Map<String, Object> metrics = new HashMap<>();

        var meta = quartzScheduler.getMetaData();
        metrics.put("numberOfJobsExecuted", meta.getNumberOfJobsExecuted());
        metrics.put("runningSince", meta.getRunningSince());
        metrics.put("threadPoolSize", meta.getThreadPoolSize());
        metrics.put("jobStoreClustered", meta.isJobStoreClustered());
        metrics.put("schedulerRemote", meta.isSchedulerRemote());

        return ResponseEntity.ok(metrics);
    }
}
