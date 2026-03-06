package com.ihss.scheduler.controller;

import com.ihss.scheduler.dto.DashboardStatsDTO;
import com.ihss.scheduler.dto.ExecutionSummaryDTO;
import com.ihss.scheduler.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler/dashboard")
@Tag(name = "Dashboard", description = "APIs for dashboard statistics and overview")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Get overview statistics for the dashboard")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent executions", description = "Get the most recent job executions")
    public ResponseEntity<List<ExecutionSummaryDTO>> getRecentExecutions(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentExecutions(limit));
    }

    @GetMapping("/running")
    @Operation(summary = "Get running executions", description = "Get all currently running executions")
    public ResponseEntity<List<ExecutionSummaryDTO>> getRunningExecutions() {
        return ResponseEntity.ok(dashboardService.getRunningExecutions());
    }
}
