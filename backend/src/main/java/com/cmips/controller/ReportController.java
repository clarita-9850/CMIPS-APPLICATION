package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.ReportDefinitionEntity;
import com.cmips.entity.ReportExecutionEntity;
import com.cmips.repository.ReportDefinitionRepository;
import com.cmips.repository.ReportExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Report Management Controller — DSD Section 28.
 * Manages report definitions, scheduling, and execution history.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final ReportDefinitionRepository reportDefRepository;
    private final ReportExecutionRepository reportExecRepository;

    public ReportController(ReportDefinitionRepository reportDefRepository,
                           ReportExecutionRepository reportExecRepository) {
        this.reportDefRepository = reportDefRepository;
        this.reportExecRepository = reportExecRepository;
    }

    // ==================== REPORT DEFINITIONS ====================

    @GetMapping("/definitions")
    @RequirePermission(resource = "Report Resource", scope = "view")
    public ResponseEntity<?> getAllReportDefinitions() {
        try {
            return ResponseEntity.ok(reportDefRepository.findByStatusOrderByReportNameAsc("ACTIVE"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/definitions/category/{category}")
    @RequirePermission(resource = "Report Resource", scope = "view")
    public ResponseEntity<?> getReportsByCategory(@PathVariable String category) {
        try {
            return ResponseEntity.ok(reportDefRepository.findByReportCategory(category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/definitions/{reportCode}")
    @RequirePermission(resource = "Report Resource", scope = "view")
    public ResponseEntity<?> getReportDefinition(@PathVariable String reportCode) {
        try {
            return reportDefRepository.findByReportCode(reportCode)
                .map(r -> ResponseEntity.ok((Object) r))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/definitions")
    @RequirePermission(resource = "Report Resource", scope = "edit")
    public ResponseEntity<?> createReportDefinition(@RequestBody ReportDefinitionEntity def,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            def.setCreatedBy(userId);
            def.setStatus("ACTIVE");
            return ResponseEntity.status(HttpStatus.CREATED).body(reportDefRepository.save(def));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/definitions/{id}")
    @RequirePermission(resource = "Report Resource", scope = "edit")
    public ResponseEntity<?> updateReportDefinition(@PathVariable Long id,
            @RequestBody ReportDefinitionEntity updates,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var def = reportDefRepository.findById(id);
            if (def.isEmpty()) return ResponseEntity.notFound().build();
            var d = def.get();
            if (updates.getReportName() != null) d.setReportName(updates.getReportName());
            if (updates.getDescription() != null) d.setDescription(updates.getDescription());
            if (updates.getFrequency() != null) d.setFrequency(updates.getFrequency());
            if (updates.getOutputFormat() != null) d.setOutputFormat(updates.getOutputFormat());
            if (updates.getParameters() != null) d.setParameters(updates.getParameters());
            if (updates.getScheduleEnabled() != null) d.setScheduleEnabled(updates.getScheduleEnabled());
            if (updates.getScheduleCron() != null) d.setScheduleCron(updates.getScheduleCron());
            d.setUpdatedBy(userId);
            return ResponseEntity.ok(reportDefRepository.save(d));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/definitions/{id}/inactivate")
    @RequirePermission(resource = "Report Resource", scope = "edit")
    public ResponseEntity<?> inactivateReportDefinition(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var def = reportDefRepository.findById(id);
            if (def.isEmpty()) return ResponseEntity.notFound().build();
            var d = def.get();
            d.setStatus("INACTIVE");
            d.setUpdatedBy(userId);
            return ResponseEntity.ok(reportDefRepository.save(d));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== REPORT EXECUTIONS ====================

    @GetMapping("/executions/recent")
    @RequirePermission(resource = "Report Resource", scope = "view")
    public ResponseEntity<?> getRecentExecutions() {
        try {
            return ResponseEntity.ok(reportExecRepository.findTop20ByOrderByCreatedAtDesc());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/executions/report/{reportDefinitionId}")
    @RequirePermission(resource = "Report Resource", scope = "view")
    public ResponseEntity<?> getExecutionsByReport(@PathVariable Long reportDefinitionId) {
        try {
            return ResponseEntity.ok(reportExecRepository.findByReportDefinitionIdOrderByCreatedAtDesc(reportDefinitionId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/executions/{id}")
    @RequirePermission(resource = "Report Resource", scope = "view")
    public ResponseEntity<?> getExecution(@PathVariable Long id) {
        try {
            return reportExecRepository.findById(id)
                .map(r -> ResponseEntity.ok((Object) r))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST run a report — creates an execution record in RUNNING status */
    @PostMapping("/execute/{reportCode}")
    @RequirePermission(resource = "Report Resource", scope = "edit")
    public ResponseEntity<?> executeReport(@PathVariable String reportCode,
            @RequestBody(required = false) Map<String, Object> params,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var defOpt = reportDefRepository.findByReportCode(reportCode);
            if (defOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Report definition not found: " + reportCode));
            }
            var def = defOpt.get();
            var exec = new ReportExecutionEntity();
            exec.setReportDefinitionId(def.getId());
            exec.setReportCode(reportCode);
            exec.setReportName(def.getReportName());
            exec.setParameters(params != null ? params.toString() : def.getParameters());
            exec.setStartDate(LocalDateTime.now());
            exec.setStatus("RUNNING");
            exec.setRequestedBy(userId);
            var saved = reportExecRepository.save(exec);

            // Mock: immediately complete the report
            saved.setEndDate(LocalDateTime.now());
            saved.setStatus("COMPLETED");
            saved.setRowCount(0);
            saved.setExecutionTimeMs(100L);
            saved.setOutputFilePath("/reports/" + reportCode + "_" + saved.getId() + "." + def.getOutputFormat().toLowerCase());
            reportExecRepository.save(saved);

            // Update definition last run
            def.setLastRunDate(LocalDateTime.now());
            def.setLastRunBy(userId);
            def.setLastRunStatus("SUCCESS");
            reportDefRepository.save(def);

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/executions/running")
    @RequirePermission(resource = "Report Resource", scope = "view")
    public ResponseEntity<?> getRunningExecutions() {
        try {
            return ResponseEntity.ok(reportExecRepository.findByStatus("RUNNING"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
