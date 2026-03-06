package com.ihss.scheduler.controller;

import com.ihss.scheduler.annotation.RequirePermission;
import com.ihss.scheduler.dto.ExecutionSummaryDTO;
import com.ihss.scheduler.dto.TriggerJobRequest;
import com.ihss.scheduler.entity.TriggerType;
import com.ihss.scheduler.service.ExecutionService;
import com.ihss.scheduler.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler/trigger")
@Tag(name = "Job Triggering", description = "APIs for triggering and controlling job executions")
public class TriggerController {

    private final ExecutionService executionService;

    public TriggerController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/{jobId}")
    @RequirePermission(resource = "Scheduler Trigger Resource", scope = "trigger", message = "You don't have permission to trigger jobs")
    @Operation(summary = "Trigger job", description = "Manually trigger a job execution")
    public ResponseEntity<ExecutionSummaryDTO> triggerJob(
            @PathVariable Long jobId,
            @RequestBody(required = false) TriggerJobRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);

        if (request == null) {
            request = new TriggerJobRequest(null, false);
        }

        ExecutionSummaryDTO execution = executionService.triggerJob(
            jobId,
            request,
            username,
            TriggerType.MANUAL
        );

        return ResponseEntity.ok(execution);
    }

    @PostMapping("/stop/{triggerId}")
    @RequirePermission(resource = "Scheduler Trigger Resource", scope = "stop", message = "You don't have permission to stop job executions")
    @Operation(summary = "Stop execution", description = "Stop a running job execution")
    public ResponseEntity<Void> stopExecution(
            @PathVariable String triggerId,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        executionService.stopExecution(triggerId, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{triggerId}")
    @Operation(summary = "Get execution status", description = "Get the current status of an execution")
    public ResponseEntity<ExecutionSummaryDTO> getExecutionStatus(@PathVariable String triggerId) {
        return ResponseEntity.ok(executionService.getExecution(triggerId));
    }

    @GetMapping("/running")
    @Operation(summary = "Get running executions", description = "Get all currently running job executions")
    public ResponseEntity<List<ExecutionSummaryDTO>> getRunningExecutions() {
        return ResponseEntity.ok(executionService.getRunningExecutions());
    }
}
