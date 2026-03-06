package com.ihss.scheduler.service;

import com.ihss.scheduler.dto.AddDependencyRequest;
import com.ihss.scheduler.dto.DependencyDTO;
import com.ihss.scheduler.entity.*;
import com.ihss.scheduler.exception.CyclicDependencyException;
import com.ihss.scheduler.exception.DependencyNotFoundException;
import com.ihss.scheduler.exception.JobNotFoundException;
import com.ihss.scheduler.repository.JobDefinitionRepository;
import com.ihss.scheduler.repository.JobDependencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DependencyService {

    private static final Logger log = LoggerFactory.getLogger(DependencyService.class);

    private final JobDependencyRepository dependencyRepository;
    private final JobDefinitionRepository jobRepository;
    private final AuditService auditService;

    public DependencyService(
            JobDependencyRepository dependencyRepository,
            JobDefinitionRepository jobRepository,
            AuditService auditService) {
        this.dependencyRepository = dependencyRepository;
        this.jobRepository = jobRepository;
        this.auditService = auditService;
    }

    public DependencyDTO addDependency(Long jobId, AddDependencyRequest request, String createdBy) {
        log.info("Adding dependency: job {} depends on {} by user: {}", jobId, request.dependsOnJobId(), createdBy);

        JobDefinition job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
            .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

        JobDefinition dependsOnJob = jobRepository.findByIdAndDeletedAtIsNull(request.dependsOnJobId())
            .orElseThrow(() -> new JobNotFoundException("Dependency job not found with ID: " + request.dependsOnJobId()));

        // Check for self-dependency
        if (jobId.equals(request.dependsOnJobId())) {
            throw new CyclicDependencyException("A job cannot depend on itself");
        }

        // Check for existing dependency
        if (dependencyRepository.existsByJobIdAndDependsOnJobId(jobId, request.dependsOnJobId())) {
            throw new IllegalStateException("Dependency already exists");
        }

        // Check for cyclic dependency
        if (wouldCreateCycle(jobId, request.dependsOnJobId())) {
            throw new CyclicDependencyException(
                "Adding this dependency would create a cycle: " +
                dependsOnJob.getJobName() + " already depends on " + job.getJobName() + " (directly or transitively)"
            );
        }

        JobDependency dependency = new JobDependency();
        dependency.setJob(job);
        dependency.setDependsOnJob(dependsOnJob);
        dependency.setDependencyType(request.dependencyType());
        dependency.setIsActive(true);
        dependency.setCreatedBy(createdBy);

        JobDependency saved = dependencyRepository.save(dependency);

        auditService.logAction(
            "JOB_DEPENDENCY",
            saved.getId(),
            AuditAction.ADD_DEPENDENCY,
            createdBy,
            null,
            null,
            job.getJobName() + " now depends on " + dependsOnJob.getJobName()
        );

        return DependencyDTO.fromEntity(saved);
    }

    public void removeDependency(Long jobId, Long dependsOnJobId, String removedBy) {
        log.info("Removing dependency: job {} depends on {} by user: {}", jobId, dependsOnJobId, removedBy);

        JobDependency dependency = dependencyRepository.findByJobIdAndDependsOnJobId(jobId, dependsOnJobId)
            .orElseThrow(() -> new DependencyNotFoundException("Dependency not found"));

        dependencyRepository.delete(dependency);

        auditService.logAction(
            "JOB_DEPENDENCY",
            dependency.getId(),
            AuditAction.REMOVE_DEPENDENCY,
            removedBy,
            null,
            null,
            "Removed dependency"
        );
    }

    @Transactional(readOnly = true)
    public List<DependencyDTO> getDependencies(Long jobId) {
        return dependencyRepository.findDependenciesForJob(jobId)
            .stream()
            .map(DependencyDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DependencyDTO> getDependents(Long jobId) {
        return dependencyRepository.findDependentsOfJob(jobId)
            .stream()
            .map(DependencyDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<Long, List<Long>> getFullDependencyGraph() {
        List<JobDependency> allDependencies = dependencyRepository.findAllActiveWithJobs();
        Map<Long, List<Long>> graph = new HashMap<>();

        for (JobDependency dep : allDependencies) {
            graph.computeIfAbsent(dep.getJob().getId(), k -> new ArrayList<>())
                .add(dep.getDependsOnJob().getId());
        }

        return graph;
    }

    @Transactional(readOnly = true)
    public List<Long> getExecutionOrder(List<Long> jobIds) {
        // Topological sort for execution order
        Map<Long, List<Long>> graph = new HashMap<>();
        Map<Long, Integer> inDegree = new HashMap<>();

        for (Long jobId : jobIds) {
            graph.put(jobId, new ArrayList<>());
            inDegree.put(jobId, 0);
        }

        for (Long jobId : jobIds) {
            List<JobDependency> deps = dependencyRepository.findDependenciesForJob(jobId);
            for (JobDependency dep : deps) {
                Long depJobId = dep.getDependsOnJob().getId();
                if (jobIds.contains(depJobId)) {
                    graph.get(depJobId).add(jobId);
                    inDegree.merge(jobId, 1, Integer::sum);
                }
            }
        }

        // Kahn's algorithm
        Queue<Long> queue = new LinkedList<>();
        for (Long jobId : jobIds) {
            if (inDegree.get(jobId) == 0) {
                queue.add(jobId);
            }
        }

        List<Long> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            result.add(current);

            for (Long neighbor : graph.get(current)) {
                inDegree.merge(neighbor, -1, Integer::sum);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (result.size() != jobIds.size()) {
            throw new CyclicDependencyException("Cycle detected in job dependencies");
        }

        return result;
    }

    @Transactional(readOnly = true)
    public boolean areAllDependenciesSatisfied(Long jobId, LocalDateTimeHolder asOf) {
        List<JobDependency> dependencies = dependencyRepository.findDependenciesForJob(jobId);

        if (dependencies.isEmpty()) {
            return true;
        }

        // This would need to check execution history - simplified for now
        // Real implementation would check if all dependency jobs have completed
        // successfully since the last scheduled run time
        return true;
    }

    private boolean wouldCreateCycle(Long jobId, Long newDependencyJobId) {
        // BFS to check if newDependencyJobId can reach jobId
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(newDependencyJobId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current.equals(jobId)) {
                return true;
            }

            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            List<JobDependency> deps = dependencyRepository.findDependenciesForJob(current);
            for (JobDependency dep : deps) {
                queue.add(dep.getDependsOnJob().getId());
            }
        }

        return false;
    }

    // Helper class for time handling
    public static class LocalDateTimeHolder {
        private final java.time.LocalDateTime value;
        public LocalDateTimeHolder(java.time.LocalDateTime value) {
            this.value = value;
        }
        public java.time.LocalDateTime getValue() {
            return value;
        }
    }
}
