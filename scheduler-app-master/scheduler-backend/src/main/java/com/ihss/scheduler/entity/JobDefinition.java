package com.ihss.scheduler.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "job_definition")
public class JobDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false, unique = true)
    private String jobName;

    @Column(name = "job_type", nullable = false)
    private String jobType;

    @Column(name = "description")
    private String description;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "timezone")
    private String timezone = "America/Los_Angeles";

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status = JobStatus.ACTIVE;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "priority")
    private Integer priority = 5;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 3600;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "job_parameters", columnDefinition = "jsonb")
    private Map<String, Object> jobParameters = new HashMap<>();

    @Column(name = "target_roles", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] targetRoles = new String[]{};

    @Column(name = "target_counties", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] targetCounties = new String[]{};

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Dependencies where this job depends on others
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobDependency> dependencies = new ArrayList<>();

    // Dependencies where others depend on this job
    @OneToMany(mappedBy = "dependsOnJob", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobDependency> dependents = new ArrayList<>();

    // Executions
    @OneToMany(mappedBy = "jobDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExecutionMapping> executions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Map<String, Object> getJobParameters() {
        return jobParameters;
    }

    public void setJobParameters(Map<String, Object> jobParameters) {
        this.jobParameters = jobParameters;
    }

    public String[] getTargetRoles() {
        return targetRoles;
    }

    public void setTargetRoles(String[] targetRoles) {
        this.targetRoles = targetRoles;
    }

    public String[] getTargetCounties() {
        return targetCounties;
    }

    public void setTargetCounties(String[] targetCounties) {
        this.targetCounties = targetCounties;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<JobDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<JobDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<JobDependency> getDependents() {
        return dependents;
    }

    public void setDependents(List<JobDependency> dependents) {
        this.dependents = dependents;
    }

    public List<ExecutionMapping> getExecutions() {
        return executions;
    }

    public void setExecutions(List<ExecutionMapping> executions) {
        this.executions = executions;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean canRun() {
        return enabled && status == JobStatus.ACTIVE && !isDeleted();
    }
}
