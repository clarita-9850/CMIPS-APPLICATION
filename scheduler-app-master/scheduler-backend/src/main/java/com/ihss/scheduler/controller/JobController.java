package com.ihss.scheduler.controller;

import com.ihss.scheduler.annotation.RequirePermission;
import com.ihss.scheduler.dto.*;
import com.ihss.scheduler.entity.JobStatus;
import com.ihss.scheduler.entity.TriggerType;
import com.ihss.scheduler.service.DependencyService;
import com.ihss.scheduler.service.ExecutionService;
import com.ihss.scheduler.service.JobDefinitionService;
import com.ihss.scheduler.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler/jobs")
@Tag(name = "Job Management", description = "APIs for managing job definitions")
public class JobController {

    private final JobDefinitionService jobService;
    private final DependencyService dependencyService;
    private final ExecutionService executionService;

    public JobController(
            JobDefinitionService jobService,
            DependencyService dependencyService,
            ExecutionService executionService) {
        this.jobService = jobService;
        this.dependencyService = dependencyService;
        this.executionService = executionService;
    }

    @GetMapping
    @Operation(summary = "Get all jobs", description = "Retrieve all job definitions with pagination")
    public ResponseEntity<Page<JobDefinitionDTO>> getAllJobs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(jobService.getAllJobs(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search jobs", description = "Search jobs by name or description")
    public ResponseEntity<Page<JobDefinitionDTO>> searchJobs(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(jobService.searchJobs(q, pageable));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter jobs", description = "Filter jobs by status, type, or enabled state")
    public ResponseEntity<Page<JobDefinitionDTO>> filterJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(jobService.filterJobs(status, jobType, enabled, pageable));
    }

    @GetMapping("/types")
    @Operation(summary = "Get job types", description = "Get all distinct job types")
    public ResponseEntity<List<String>> getJobTypes() {
        return ResponseEntity.ok(jobService.getJobTypes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID", description = "Retrieve a single job definition with dependencies")
    public ResponseEntity<JobDefinitionDTO> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @GetMapping("/name/{jobName}")
    @Operation(summary = "Get job by name", description = "Retrieve a job definition by its unique name")
    public ResponseEntity<JobDefinitionDTO> getJobByName(@PathVariable String jobName) {
        return ResponseEntity.ok(jobService.getJobByName(jobName));
    }

    @PostMapping
    @RequirePermission(resource = "Scheduler Job Resource", scope = "create", message = "You don't have permission to create jobs")
    @Operation(summary = "Create job", description = "Create a new job definition")
    public ResponseEntity<JobDefinitionDTO> createJob(
            @Valid @RequestBody CreateJobRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(jobService.createJob(request, username));
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "Scheduler Job Resource", scope = "edit", message = "You don't have permission to edit jobs")
    @Operation(summary = "Update job", description = "Update an existing job definition")
    public ResponseEntity<JobDefinitionDTO> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        return ResponseEntity.ok(jobService.updateJob(id, request, username));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resource = "Scheduler Job Resource", scope = "delete", message = "You don't have permission to delete jobs")
    @Operation(summary = "Delete job", description = "Soft delete a job definition")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        jobService.deleteJob(id, username);
        return ResponseEntity.noContent().build();
    }

    // Status Operations
    @PostMapping("/{id}/hold")
    @RequirePermission(resource = "Scheduler Job Resource", scope = "hold", message = "You don't have permission to hold jobs")
    @Operation(summary = "Hold job", description = "Put job on hold (blocks dependent jobs)")
    public ResponseEntity<JobDefinitionDTO> holdJob(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        return ResponseEntity.ok(jobService.holdJob(id, username));
    }

    @PostMapping("/{id}/ice")
    @RequirePermission(resource = "Scheduler Job Resource", scope = "ice", message = "You don't have permission to ice jobs")
    @Operation(summary = "Ice job", description = "Put job on ice (skips job but allows dependents)")
    public ResponseEntity<JobDefinitionDTO> iceJob(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        return ResponseEntity.ok(jobService.iceJob(id, username));
    }

    @PostMapping("/{id}/resume")
    @RequirePermission(resource = "Scheduler Job Resource", scope = "resume", message = "You don't have permission to resume jobs")
    @Operation(summary = "Resume job", description = "Resume a held or iced job")
    public ResponseEntity<JobDefinitionDTO> resumeJob(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        return ResponseEntity.ok(jobService.resumeJob(id, username));
    }

    @PostMapping("/{id}/enable")
    @RequirePermission(resource = "Scheduler Job Resource", scope = "enable", message = "You don't have permission to enable jobs")
    @Operation(summary = "Enable job", description = "Enable a disabled job")
    public ResponseEntity<JobDefinitionDTO> enableJob(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        return ResponseEntity.ok(jobService.enableJob(id, username));
    }

    @PostMapping("/{id}/disable")
    @RequirePermission(resource = "Scheduler Job Resource", scope = "disable", message = "You don't have permission to disable jobs")
    @Operation(summary = "Disable job", description = "Disable a job")
    public ResponseEntity<JobDefinitionDTO> disableJob(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        return ResponseEntity.ok(jobService.disableJob(id, username));
    }

    // Dependency Operations
    @GetMapping("/{id}/dependencies")
    @Operation(summary = "Get job dependencies", description = "Get all jobs this job depends on")
    public ResponseEntity<List<DependencyDTO>> getDependencies(@PathVariable Long id) {
        return ResponseEntity.ok(dependencyService.getDependencies(id));
    }

    @GetMapping("/{id}/dependents")
    @Operation(summary = "Get job dependents", description = "Get all jobs that depend on this job")
    public ResponseEntity<List<DependencyDTO>> getDependents(@PathVariable Long id) {
        return ResponseEntity.ok(dependencyService.getDependents(id));
    }

    @PostMapping("/{id}/dependencies")
    @RequirePermission(resource = "Scheduler Job Resource", scope = "edit", message = "You don't have permission to manage job dependencies")
    @Operation(summary = "Add dependency", description = "Add a dependency to a job")
    public ResponseEntity<DependencyDTO> addDependency(
            @PathVariable Long id,
            @Valid @RequestBody AddDependencyRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(dependencyService.addDependency(id, request, username));
    }

    @DeleteMapping("/{id}/dependencies/{dependsOnJobId}")
    @RequirePermission(resource = "Scheduler Job Resource", scope = "edit", message = "You don't have permission to manage job dependencies")
    @Operation(summary = "Remove dependency", description = "Remove a dependency from a job")
    public ResponseEntity<Void> removeDependency(
            @PathVariable Long id,
            @PathVariable Long dependsOnJobId,
            @AuthenticationPrincipal Jwt jwt) {
        String username = SecurityUtils.getUsername(jwt);
        dependencyService.removeDependency(id, dependsOnJobId, username);
        return ResponseEntity.noContent().build();
    }

    // Execution Operations
    @GetMapping("/{id}/executions")
    @Operation(summary = "Get job executions", description = "Get execution history for a job")
    public ResponseEntity<Page<ExecutionSummaryDTO>> getExecutions(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "triggeredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(executionService.getExecutionsForJob(id, pageable));
    }
}
