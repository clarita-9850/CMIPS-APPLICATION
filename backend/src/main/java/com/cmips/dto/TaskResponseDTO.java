package com.cmips.dto;

import com.cmips.entity.Task;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DSD GAP 7 — Field-based security DTO for Task responses.
 * <p>
 * Per DSD Section 9, certain task fields should be hidden based on user role.
 * This DTO masks sensitive fields (SSN references, financial details) unless
 * the user has the appropriate role.
 */
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String assignedTo;
    private String createdBy;
    private LocalDateTime dueDate;
    private LocalDateTime escalationDate;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String workQueue;
    private String queueRole;
    private String actionLink;
    private String taskTypeCode;
    private String caseNumber;
    private String county;
    private String districtOffice;
    private String subject;
    private String creationType;
    private Boolean isNotification;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Lifecycle fields — visible to all
    private String reservedBy;
    private LocalDateTime reservedDate;
    private String deferredBy;
    private LocalDateTime deferredDate;
    private LocalDateTime restartDate;
    private String forwardedTo;
    private String forwardedBy;
    private LocalDateTime forwardedDate;
    private String closedBy;
    private LocalDateTime closedDate;
    private String closeComments;

    // Sensitive fields — masked unless authorized
    private String triggerCondition;
    private String caseParticipant;
    private Integer timeWorked;

    // Closure info
    private String requiredActionForClosure;

    /**
     * Create a DTO from a Task entity with field-level masking based on roles.
     *
     * @param task  the source task
     * @param roles the user's Keycloak roles
     * @param requiredAction the required action text from TaskType (nullable)
     */
    public static TaskResponseDTO fromTask(Task task, Set<String> roles, String requiredAction) {
        TaskResponseDTO dto = new TaskResponseDTO();

        // Always visible
        dto.id = task.getId();
        dto.title = task.getTitle();
        dto.status = task.getStatus() != null ? task.getStatus().name() : null;
        dto.priority = task.getPriority() != null ? task.getPriority().name() : null;
        dto.assignedTo = task.getAssignedTo();
        dto.createdBy = task.getCreatedBy();
        dto.dueDate = task.getDueDate();
        dto.escalationDate = task.getEscalationDate();
        dto.relatedEntityType = task.getRelatedEntityType();
        dto.relatedEntityId = task.getRelatedEntityId();
        dto.workQueue = task.getWorkQueue();
        dto.queueRole = task.getQueueRole();
        dto.actionLink = task.getActionLink();
        dto.taskTypeCode = task.getTaskTypeCode();
        dto.caseNumber = task.getCaseNumber();
        dto.county = task.getCounty();
        dto.districtOffice = task.getDistrictOffice();
        dto.subject = task.getSubject();
        dto.creationType = task.getCreationType() != null ? task.getCreationType().name() : "SYSTEM";
        dto.isNotification = task.isNotification();
        dto.createdAt = task.getCreatedAt();
        dto.updatedAt = task.getUpdatedAt();

        // Lifecycle fields
        dto.reservedBy = task.getReservedBy();
        dto.reservedDate = task.getReservedDate();
        dto.deferredBy = task.getDeferredBy();
        dto.deferredDate = task.getDeferredDate();
        dto.restartDate = task.getRestartDate();
        dto.forwardedTo = task.getForwardedTo();
        dto.forwardedBy = task.getForwardedBy();
        dto.forwardedDate = task.getForwardedDate();
        dto.closedBy = task.getClosedBy();
        dto.closedDate = task.getClosedDate();
        dto.closeComments = task.getCloseComments();

        // Sensitive fields — mask for restricted roles
        boolean hasFullAccess = roles != null && roles.stream().anyMatch(r ->
                r != null && (r.toUpperCase().contains("SUPERVISOR")
                || "CASEMANAGEMENTROLE".equalsIgnoreCase(r)
                || "ADMIN".equalsIgnoreCase(r)));

        if (hasFullAccess) {
            dto.description = task.getDescription();
            dto.triggerCondition = task.getTriggerCondition();
            dto.caseParticipant = task.getCaseParticipant();
            dto.timeWorked = task.getTimeWorked();
        } else {
            dto.description = task.getDescription();
            dto.triggerCondition = null; // Hide trigger details from basic roles
            dto.caseParticipant = maskSensitiveField(task.getCaseParticipant());
            dto.timeWorked = task.getTimeWorked();
        }

        dto.requiredActionForClosure = requiredAction;

        return dto;
    }

    /**
     * Simple factory without role masking (for internal/system use).
     */
    public static TaskResponseDTO fromTask(Task task) {
        return fromTask(task, Set.of("ADMIN"), null);
    }

    private static String maskSensitiveField(String value) {
        if (value == null || value.length() <= 4) return value;
        return "***" + value.substring(value.length() - 4);
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getAssignedTo() { return assignedTo; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getDueDate() { return dueDate; }
    public LocalDateTime getEscalationDate() { return escalationDate; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public Long getRelatedEntityId() { return relatedEntityId; }
    public String getWorkQueue() { return workQueue; }
    public String getQueueRole() { return queueRole; }
    public String getActionLink() { return actionLink; }
    public String getTaskTypeCode() { return taskTypeCode; }
    public String getCaseNumber() { return caseNumber; }
    public String getCounty() { return county; }
    public String getDistrictOffice() { return districtOffice; }
    public String getSubject() { return subject; }
    public String getCreationType() { return creationType; }
    public Boolean getIsNotification() { return isNotification; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getReservedBy() { return reservedBy; }
    public LocalDateTime getReservedDate() { return reservedDate; }
    public String getDeferredBy() { return deferredBy; }
    public LocalDateTime getDeferredDate() { return deferredDate; }
    public LocalDateTime getRestartDate() { return restartDate; }
    public String getForwardedTo() { return forwardedTo; }
    public String getForwardedBy() { return forwardedBy; }
    public LocalDateTime getForwardedDate() { return forwardedDate; }
    public String getClosedBy() { return closedBy; }
    public LocalDateTime getClosedDate() { return closedDate; }
    public String getCloseComments() { return closeComments; }
    public String getTriggerCondition() { return triggerCondition; }
    public String getCaseParticipant() { return caseParticipant; }
    public Integer getTimeWorked() { return timeWorked; }
    public String getRequiredActionForClosure() { return requiredActionForClosure; }
}
