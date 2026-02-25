package com.ihss.scheduler.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "execution_mapping")
public class ExecutionMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_definition_id", nullable = false)
    private JobDefinition jobDefinition;

    @Column(name = "trigger_id", nullable = false, unique = true)
    private String triggerId;

    @Column(name = "cmips_execution_id")
    private Long cmipsExecutionId;

    @Column(name = "spring_batch_execution_id")
    private Long springBatchExecutionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExecutionStatus status = ExecutionStatus.TRIGGERED;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Column(name = "progress_message")
    private String progressMessage;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "triggered_by")
    private String triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType = TriggerType.SCHEDULED;

    @PrePersist
    protected void onCreate() {
        if (triggeredAt == null) {
            triggeredAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobDefinition getJobDefinition() {
        return jobDefinition;
    }

    public void setJobDefinition(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public Long getCmipsExecutionId() {
        return cmipsExecutionId;
    }

    public void setCmipsExecutionId(Long cmipsExecutionId) {
        this.cmipsExecutionId = cmipsExecutionId;
    }

    public Long getSpringBatchExecutionId() {
        return springBatchExecutionId;
    }

    public void setSpringBatchExecutionId(Long springBatchExecutionId) {
        this.springBatchExecutionId = springBatchExecutionId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public boolean isRunning() {
        return status == ExecutionStatus.TRIGGERED ||
               status == ExecutionStatus.QUEUED ||
               status == ExecutionStatus.STARTING ||
               status == ExecutionStatus.RUNNING;
    }

    public boolean isCompleted() {
        return status == ExecutionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == ExecutionStatus.FAILED ||
               status == ExecutionStatus.ABANDONED;
    }

    public boolean isTerminal() {
        return status == ExecutionStatus.COMPLETED ||
               status == ExecutionStatus.FAILED ||
               status == ExecutionStatus.STOPPED ||
               status == ExecutionStatus.ABANDONED;
    }
}
