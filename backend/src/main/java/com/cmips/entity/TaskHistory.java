package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskAction action;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt = LocalDateTime.now();

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(columnDefinition = "TEXT")
    private String details;

    public TaskHistory() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public TaskAction getAction() { return action; }
    public void setAction(TaskAction action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public enum TaskAction {
        CREATED,
        RESERVED,
        UNRESERVED,
        ASSIGNED,
        FORWARDED,
        DEFERRED,
        CLOSED,
        ESCALATED,
        RESTARTED,
        REALLOCATED,
        COMMENT_ADDED,
        TIME_MODIFIED,
        STATUS_CHANGED,
        AUTO_CLOSED
    }
}
