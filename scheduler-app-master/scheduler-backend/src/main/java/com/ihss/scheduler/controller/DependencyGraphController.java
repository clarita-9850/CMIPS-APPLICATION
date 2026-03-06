package com.ihss.scheduler.controller;

import com.ihss.scheduler.dto.DependencyDTO;
import com.ihss.scheduler.dto.JobDefinitionDTO;
import com.ihss.scheduler.service.DependencyService;
import com.ihss.scheduler.service.JobDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scheduler/graph")
@Tag(name = "Dependency Graph", description = "APIs for viewing and managing the job dependency graph")
public class DependencyGraphController {

    private final DependencyService dependencyService;
    private final JobDefinitionService jobService;

    public DependencyGraphController(
            DependencyService dependencyService,
            JobDefinitionService jobService) {
        this.dependencyService = dependencyService;
        this.jobService = jobService;
    }

    @GetMapping
    @Operation(summary = "Get full dependency graph", description = "Get the complete job dependency graph for visualization")
    public ResponseEntity<GraphDTO> getGraph() {
        Map<Long, List<Long>> graph = dependencyService.getFullDependencyGraph();

        // Get all job info
        List<JobDefinitionDTO> allJobs = jobService.getAllJobs(Pageable.unpaged()).getContent();

        List<NodeDTO> nodes = allJobs.stream()
            .map(job -> new NodeDTO(
                job.id().toString(),
                job.jobName(),
                job.jobType(),
                job.status().name(),
                job.enabled()
            ))
            .collect(Collectors.toList());

        List<EdgeDTO> edges = graph.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .map(depId -> new EdgeDTO(
                    depId.toString(),
                    entry.getKey().toString()
                )))
            .collect(Collectors.toList());

        return ResponseEntity.ok(new GraphDTO(nodes, edges));
    }

    @GetMapping("/subgraph/{jobId}")
    @Operation(summary = "Get job subgraph", description = "Get dependency subgraph centered on a specific job")
    public ResponseEntity<GraphDTO> getSubgraph(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "2") int depth) {

        // Get the job and its dependencies/dependents up to specified depth
        JobDefinitionDTO centerJob = jobService.getJob(jobId);

        List<DependencyDTO> dependencies = dependencyService.getDependencies(jobId);
        List<DependencyDTO> dependents = dependencyService.getDependents(jobId);

        // Build subgraph (simplified - full implementation would recursively expand)
        List<NodeDTO> nodes = new java.util.ArrayList<>();
        List<EdgeDTO> edges = new java.util.ArrayList<>();

        // Add center node
        nodes.add(new NodeDTO(
            centerJob.id().toString(),
            centerJob.jobName(),
            centerJob.jobType(),
            centerJob.status().name(),
            centerJob.enabled()
        ));

        // Add dependency nodes and edges
        for (DependencyDTO dep : dependencies) {
            nodes.add(new NodeDTO(
                dep.dependsOnJobId().toString(),
                dep.dependsOnJobName(),
                null,
                null,
                null
            ));
            edges.add(new EdgeDTO(
                dep.dependsOnJobId().toString(),
                dep.jobId().toString()
            ));
        }

        // Add dependent nodes and edges
        for (DependencyDTO dep : dependents) {
            nodes.add(new NodeDTO(
                dep.jobId().toString(),
                dep.jobName(),
                null,
                null,
                null
            ));
            edges.add(new EdgeDTO(
                dep.dependsOnJobId().toString(),
                dep.jobId().toString()
            ));
        }

        return ResponseEntity.ok(new GraphDTO(nodes, edges));
    }

    @GetMapping("/execution-order")
    @Operation(summary = "Get execution order", description = "Get the topologically sorted execution order for given jobs")
    public ResponseEntity<List<Long>> getExecutionOrder(@RequestParam List<Long> jobIds) {
        return ResponseEntity.ok(dependencyService.getExecutionOrder(jobIds));
    }

    // DTOs for graph visualization
    public record GraphDTO(List<NodeDTO> nodes, List<EdgeDTO> edges) {}

    public record NodeDTO(
        String id,
        String label,
        String type,
        String status,
        Boolean enabled
    ) {}

    public record EdgeDTO(
        String source,
        String target
    ) {}
}
