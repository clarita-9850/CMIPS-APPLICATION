package com.ihss.scheduler.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Column(name = "action_timestamp", nullable = false)
    private LocalDateTime actionTimestamp;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(name = "performed_by_role")
    private String performedByRole;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "previous_state", columnDefinition = "jsonb")
    private Map<String, Object> previousState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_state", columnDefinition = "jsonb")
    private Map<String, Object> newState;

    @Column(name = "change_summary")
    private String changeSummary;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        if (actionTimestamp == null) {
            actionTimestamp = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public LocalDateTime getActionTimestamp() {
        return actionTimestamp;
    }

    public void setActionTimestamp(LocalDateTime actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getPerformedByRole() {
        return performedByRole;
    }

    public void setPerformedByRole(String performedByRole) {
        this.performedByRole = performedByRole;
    }

    public Map<String, Object> getPreviousState() {
        return previousState;
    }

    public void setPreviousState(Map<String, Object> previousState) {
        this.previousState = previousState;
    }

    public Map<String, Object> getNewState() {
        return newState;
    }

    public void setNewState(Map<String, Object> newState) {
        this.newState = newState;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    // Builder pattern for easier construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AuditLog auditLog = new AuditLog();

        public Builder entityType(String entityType) {
            auditLog.entityType = entityType;
            return this;
        }

        public Builder entityId(Long entityId) {
            auditLog.entityId = entityId;
            return this;
        }

        public Builder action(AuditAction action) {
            auditLog.action = action;
            return this;
        }

        public Builder performedBy(String performedBy) {
            auditLog.performedBy = performedBy;
            return this;
        }

        public Builder performedByRole(String performedByRole) {
            auditLog.performedByRole = performedByRole;
            return this;
        }

        public Builder previousState(Map<String, Object> previousState) {
            auditLog.previousState = previousState;
            return this;
        }

        public Builder newState(Map<String, Object> newState) {
            auditLog.newState = newState;
            return this;
        }

        public Builder changeSummary(String changeSummary) {
            auditLog.changeSummary = changeSummary;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            auditLog.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            auditLog.userAgent = userAgent;
            return this;
        }

        public AuditLog build() {
            return auditLog;
        }
    }
}
