package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "timesheet_exceptions")
public class TimesheetExceptionEntity {

    public enum ExceptionType {
        HARD_EDIT,
        SOFT_EDIT,
        HOLD_CONDITION
    }

    public enum ExceptionSeverity {
        BLOCK,      // Prevents payment (hard edit)
        WARNING,    // Does not prevent payment (soft edit)
        HOLD        // Requires review before payment (hold)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timesheet_id")
    private Long timesheetId;

    @Column(name = "travel_claim_id")
    private Long travelClaimId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exception_type", length = 20)
    private ExceptionType exceptionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private ExceptionSeverity severity;

    @Column(name = "rule_number")
    private Integer ruleNumber; // DSD Rule 4-64

    @Column(name = "error_code", length = 20)
    private String errorCode; // e.g. 11960, 12002, 12004

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "affected_date")
    private String affectedDate; // Specific day(s) affected

    @Column(name = "resolved")
    private Boolean resolved = false;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }

    public Long getTravelClaimId() { return travelClaimId; }
    public void setTravelClaimId(Long travelClaimId) { this.travelClaimId = travelClaimId; }

    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }

    public ExceptionSeverity getSeverity() { return severity; }
    public void setSeverity(ExceptionSeverity severity) { this.severity = severity; }

    public Integer getRuleNumber() { return ruleNumber; }
    public void setRuleNumber(Integer ruleNumber) { this.ruleNumber = ruleNumber; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getAffectedDate() { return affectedDate; }
    public void setAffectedDate(String affectedDate) { this.affectedDate = affectedDate; }

    public Boolean getResolved() { return resolved; }
    public void setResolved(Boolean resolved) { this.resolved = resolved; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
