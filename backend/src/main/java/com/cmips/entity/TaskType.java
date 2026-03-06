package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_types")
public class TaskType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_type_code", nullable = false, unique = true)
    private String taskTypeCode;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_queue")
    private String targetQueue;

    @Column(name = "is_notification")
    private boolean isNotification = false;

    @Column(name = "deadline_business_days")
    private int deadlineBusinessDays = 5;

    @Column(name = "escalation_enabled")
    private boolean escalationEnabled = true;

    @Column(name = "escalation_target_queue")
    private String escalationTargetQueue;

    @Column(name = "escalation_check_type")
    @Enumerated(EnumType.STRING)
    private EscalationCheckType escalationCheckType = EscalationCheckType.NOT_RESERVED;

    @Column(name = "reserve_deadline_days")
    private Integer reserveDeadlineDays;

    @Column(name = "completion_deadline_days")
    private Integer completionDeadlineDays;

    @Column(name = "auto_close_enabled")
    private boolean autoCloseEnabled = false;

    @Column(name = "auto_close_days")
    private Integer autoCloseDays;

    @Column(name = "priority_default")
    @Enumerated(EnumType.STRING)
    private Task.TaskPriority priorityDefault = Task.TaskPriority.MEDIUM;

    @Column(name = "trigger_event")
    private String triggerEvent;

    @Column(name = "action_link_template")
    private String actionLinkTemplate;

    @Column(name = "functional_area")
    private String functionalArea;

    @Column
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public TaskType() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTaskTypeCode() { return taskTypeCode; }
    public void setTaskTypeCode(String taskTypeCode) { this.taskTypeCode = taskTypeCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTargetQueue() { return targetQueue; }
    public void setTargetQueue(String targetQueue) { this.targetQueue = targetQueue; }

    public boolean isNotification() { return isNotification; }
    public void setNotification(boolean notification) { isNotification = notification; }

    public int getDeadlineBusinessDays() { return deadlineBusinessDays; }
    public void setDeadlineBusinessDays(int deadlineBusinessDays) { this.deadlineBusinessDays = deadlineBusinessDays; }

    public boolean isEscalationEnabled() { return escalationEnabled; }
    public void setEscalationEnabled(boolean escalationEnabled) { this.escalationEnabled = escalationEnabled; }

    public String getEscalationTargetQueue() { return escalationTargetQueue; }
    public void setEscalationTargetQueue(String escalationTargetQueue) { this.escalationTargetQueue = escalationTargetQueue; }

    public EscalationCheckType getEscalationCheckType() { return escalationCheckType; }
    public void setEscalationCheckType(EscalationCheckType escalationCheckType) { this.escalationCheckType = escalationCheckType; }

    public Integer getReserveDeadlineDays() { return reserveDeadlineDays; }
    public void setReserveDeadlineDays(Integer reserveDeadlineDays) { this.reserveDeadlineDays = reserveDeadlineDays; }

    public Integer getCompletionDeadlineDays() { return completionDeadlineDays; }
    public void setCompletionDeadlineDays(Integer completionDeadlineDays) { this.completionDeadlineDays = completionDeadlineDays; }

    public boolean isAutoCloseEnabled() { return autoCloseEnabled; }
    public void setAutoCloseEnabled(boolean autoCloseEnabled) { this.autoCloseEnabled = autoCloseEnabled; }

    public Integer getAutoCloseDays() { return autoCloseDays; }
    public void setAutoCloseDays(Integer autoCloseDays) { this.autoCloseDays = autoCloseDays; }

    public Task.TaskPriority getPriorityDefault() { return priorityDefault; }
    public void setPriorityDefault(Task.TaskPriority priorityDefault) { this.priorityDefault = priorityDefault; }

    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; }

    public String getActionLinkTemplate() { return actionLinkTemplate; }
    public void setActionLinkTemplate(String actionLinkTemplate) { this.actionLinkTemplate = actionLinkTemplate; }

    public String getFunctionalArea() { return functionalArea; }
    public void setFunctionalArea(String functionalArea) { this.functionalArea = functionalArea; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public enum EscalationCheckType {
        NOT_RESERVED,
        NOT_COMPLETED,
        BOTH
    }
}
