package com.ihss.scheduler.service;

import com.ihss.scheduler.dto.*;
import com.ihss.scheduler.entity.*;
import com.ihss.scheduler.exception.JobNotFoundException;
import com.ihss.scheduler.exception.DuplicateJobException;
import com.ihss.scheduler.repository.JobDefinitionRepository;
import com.ihss.scheduler.repository.JobDependencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(JobDefinitionService.class);

    private final JobDefinitionRepository jobRepository;
    private final JobDependencyRepository dependencyRepository;
    private final AuditService auditService;

    public JobDefinitionService(
            JobDefinitionRepository jobRepository,
            JobDependencyRepository dependencyRepository,
            AuditService auditService) {
        this.jobRepository = jobRepository;
        this.dependencyRepository = dependencyRepository;
        this.auditService = auditService;
    }

    public JobDefinitionDTO createJob(CreateJobRequest request, String createdBy) {
        log.info("Creating job: {} by user: {}", request.jobName(), createdBy);

        if (jobRepository.existsByJobNameAndDeletedAtIsNull(request.jobName())) {
            throw new DuplicateJobException("Job with name '" + request.jobName() + "' already exists");
        }

        JobDefinition job = new JobDefinition();
        job.setJobName(request.jobName());
        job.setJobType(request.jobType());
        job.setDescription(request.description());
        job.setCronExpression(request.cronExpression());
        job.setTimezone(request.timezone());
        job.setEnabled(request.enabled());
        job.setPriority(request.priority());
        job.setMaxRetries(request.maxRetries());
        job.setTimeoutSeconds(request.timeoutSeconds());
        job.setJobParameters(request.jobParameters());
        job.setTargetRoles(request.targetRoles());
        job.setTargetCounties(request.targetCounties());
        job.setCreatedBy(createdBy);
        job.setStatus(JobStatus.ACTIVE);

        JobDefinition saved = jobRepository.save(job);

        auditService.logAction(
            "JOB_DEFINITION",
            saved.getId(),
            AuditAction.CREATE,
            createdBy,
            null,
            saved,
            "Created job: " + saved.getJobName()
        );

        log.info("Created job with ID: {}", saved.getId());
        return JobDefinitionDTO.fromEntity(saved);
    }

    public JobDefinitionDTO updateJob(Long id, UpdateJobRequest request, String updatedBy) {
        log.info("Updating job ID: {} by user: {}", id, updatedBy);

        JobDefinition job = jobRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));

        JobDefinition previousState = copyJob(job);

        if (request.jobType() != null) job.setJobType(request.jobType());
        if (request.description() != null) job.setDescription(request.description());
        if (request.cronExpression() != null) job.setCronExpression(request.cronExpression());
        if (request.timezone() != null) job.setTimezone(request.timezone());
        if (request.enabled() != null) job.setEnabled(request.enabled());
        if (request.priority() != null) job.setPriority(request.priority());
        if (request.maxRetries() != null) job.setMaxRetries(request.maxRetries());
        if (request.timeoutSeconds() != null) job.setTimeoutSeconds(request.timeoutSeconds());
        if (request.jobParameters() != null) job.setJobParameters(request.jobParameters());
        if (request.targetRoles() != null) job.setTargetRoles(request.targetRoles());
        if (request.targetCounties() != null) job.setTargetCounties(request.targetCounties());
        job.setUpdatedBy(updatedBy);

        JobDefinition saved = jobRepository.save(job);

        auditService.logAction(
            "JOB_DEFINITION",
            saved.getId(),
            AuditAction.UPDATE,
            updatedBy,
            previousState,
            saved,
            "Updated job: " + saved.getJobName()
        );

        return JobDefinitionDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public JobDefinitionDTO getJob(Long id) {
        JobDefinition job = jobRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));

        List<DependencyDTO> dependencies = dependencyRepository.findDependenciesForJob(id)
            .stream()
            .map(DependencyDTO::fromEntity)
            .collect(Collectors.toList());

        List<DependencyDTO> dependents = dependencyRepository.findDependentsOfJob(id)
            .stream()
            .map(DependencyDTO::fromEntity)
            .collect(Collectors.toList());

        return JobDefinitionDTO.fromEntity(job).withDependencies(dependencies, dependents);
    }

    @Transactional(readOnly = true)
    public JobDefinitionDTO getJobByName(String jobName) {
        JobDefinition job = jobRepository.findByJobNameAndDeletedAtIsNull(jobName)
            .orElseThrow(() -> new JobNotFoundException("Job not found with name: " + jobName));
        return JobDefinitionDTO.fromEntity(job);
    }

    @Transactional(readOnly = true)
    public Page<JobDefinitionDTO> getAllJobs(Pageable pageable) {
        return jobRepository.findAllByDeletedAtIsNull(pageable)
            .map(JobDefinitionDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<JobDefinitionDTO> searchJobs(String search, Pageable pageable) {
        return jobRepository.searchJobs(search, pageable)
            .map(JobDefinitionDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<JobDefinitionDTO> filterJobs(JobStatus status, String jobType, Boolean enabled, Pageable pageable) {
        return jobRepository.findByFilters(status, jobType, enabled, pageable)
            .map(JobDefinitionDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<String> getJobTypes() {
        return jobRepository.findDistinctJobTypes();
    }

    public void deleteJob(Long id, String deletedBy) {
        log.info("Soft-deleting job ID: {} by user: {}", id, deletedBy);

        JobDefinition job = jobRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));

        job.setDeletedAt(LocalDateTime.now());
        job.setUpdatedBy(deletedBy);
        jobRepository.save(job);

        auditService.logAction(
            "JOB_DEFINITION",
            id,
            AuditAction.DELETE,
            deletedBy,
            job,
            null,
            "Deleted job: " + job.getJobName()
        );
    }

    public JobDefinitionDTO holdJob(Long id, String heldBy) {
        return updateJobStatus(id, JobStatus.ON_HOLD, heldBy, AuditAction.HOLD);
    }

    public JobDefinitionDTO iceJob(Long id, String icedBy) {
        return updateJobStatus(id, JobStatus.ON_ICE, icedBy, AuditAction.ICE);
    }

    public JobDefinitionDTO resumeJob(Long id, String resumedBy) {
        return updateJobStatus(id, JobStatus.ACTIVE, resumedBy, AuditAction.RESUME);
    }

    public JobDefinitionDTO enableJob(Long id, String enabledBy) {
        log.info("Enabling job ID: {} by user: {}", id, enabledBy);

        JobDefinition job = jobRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));

        job.setEnabled(true);
        job.setUpdatedBy(enabledBy);
        JobDefinition saved = jobRepository.save(job);

        auditService.logAction(
            "JOB_DEFINITION",
            id,
            AuditAction.ENABLE,
            enabledBy,
            null,
            null,
            "Enabled job: " + job.getJobName()
        );

        return JobDefinitionDTO.fromEntity(saved);
    }

    public JobDefinitionDTO disableJob(Long id, String disabledBy) {
        log.info("Disabling job ID: {} by user: {}", id, disabledBy);

        JobDefinition job = jobRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));

        job.setEnabled(false);
        job.setUpdatedBy(disabledBy);
        JobDefinition saved = jobRepository.save(job);

        auditService.logAction(
            "JOB_DEFINITION",
            id,
            AuditAction.DISABLE,
            disabledBy,
            null,
            null,
            "Disabled job: " + job.getJobName()
        );

        return JobDefinitionDTO.fromEntity(saved);
    }

    private JobDefinitionDTO updateJobStatus(Long id, JobStatus newStatus, String updatedBy, AuditAction action) {
        log.info("Updating job ID: {} status to {} by user: {}", id, newStatus, updatedBy);

        JobDefinition job = jobRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));

        JobStatus previousStatus = job.getStatus();
        job.setStatus(newStatus);
        job.setUpdatedBy(updatedBy);
        JobDefinition saved = jobRepository.save(job);

        auditService.logAction(
            "JOB_DEFINITION",
            id,
            action,
            updatedBy,
            null,
            null,
            "Changed status from " + previousStatus + " to " + newStatus
        );

        return JobDefinitionDTO.fromEntity(saved);
    }

    private JobDefinition copyJob(JobDefinition original) {
        JobDefinition copy = new JobDefinition();
        copy.setId(original.getId());
        copy.setJobName(original.getJobName());
        copy.setJobType(original.getJobType());
        copy.setDescription(original.getDescription());
        copy.setCronExpression(original.getCronExpression());
        copy.setTimezone(original.getTimezone());
        copy.setStatus(original.getStatus());
        copy.setEnabled(original.getEnabled());
        copy.setPriority(original.getPriority());
        copy.setMaxRetries(original.getMaxRetries());
        copy.setTimeoutSeconds(original.getTimeoutSeconds());
        return copy;
    }
}
