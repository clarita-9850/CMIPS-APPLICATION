package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Backup Provider Hours Entity — DSD Section 23, CI-117646/CI-117647
 *
 * Tracks authorized backup provider hours per recipient case.
 * A backup provider can be authorized to provide services when the primary
 * provider is unavailable. Backup hours are tracked separately from
 * primary authorized hours.
 */
@Entity
@Table(name = "backup_provider_hours", indexes = {
        @Index(name = "idx_bph_provider", columnList = "provider_id"),
        @Index(name = "idx_bph_case", columnList = "case_id"),
        @Index(name = "idx_bph_status", columnList = "status")
})
public class BackupProviderHoursEntity {

    public enum BackupStatus { ACTIVE, INACTIVE, TERMINATED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The backup provider */
    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    /** The primary provider this backup covers */
    @Column(name = "primary_provider_id")
    private Long primaryProviderId;

    @Column(name = "primary_provider_name", length = 200)
    private String primaryProviderName;

    /** The recipient case for which backup hours are authorized */
    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "case_number", length = 20)
    private String caseNumber;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    /** Authorized backup hours per week */
    @Column(name = "authorized_hours_weekly", nullable = false)
    private Double authorizedHoursWeekly;

    /** Authorized backup hours per month (if monthly tracking applies) */
    @Column(name = "authorized_hours_monthly")
    private Double authorizedHoursMonthly;

    @Column(name = "begin_date", nullable = false)
    private LocalDate beginDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private BackupStatus status;

    @Column(name = "termination_reason", length = 500)
    private String terminationReason;

    @Column(name = "terminated_date")
    private LocalDate terminatedDate;

    /** Pay type for backup services (IHSS or WPCS) */
    @Column(name = "program_type", length = 10)
    private String programType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public BackupProviderHoursEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = BackupStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Long getPrimaryProviderId() { return primaryProviderId; }
    public void setPrimaryProviderId(Long primaryProviderId) { this.primaryProviderId = primaryProviderId; }

    public String getPrimaryProviderName() { return primaryProviderName; }
    public void setPrimaryProviderName(String primaryProviderName) { this.primaryProviderName = primaryProviderName; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public Double getAuthorizedHoursWeekly() { return authorizedHoursWeekly; }
    public void setAuthorizedHoursWeekly(Double authorizedHoursWeekly) { this.authorizedHoursWeekly = authorizedHoursWeekly; }

    public Double getAuthorizedHoursMonthly() { return authorizedHoursMonthly; }
    public void setAuthorizedHoursMonthly(Double authorizedHoursMonthly) { this.authorizedHoursMonthly = authorizedHoursMonthly; }

    public LocalDate getBeginDate() { return beginDate; }
    public void setBeginDate(LocalDate beginDate) { this.beginDate = beginDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BackupStatus getStatus() { return status; }
    public void setStatus(BackupStatus status) { this.status = status; }

    public String getTerminationReason() { return terminationReason; }
    public void setTerminationReason(String terminationReason) { this.terminationReason = terminationReason; }

    public LocalDate getTerminatedDate() { return terminatedDate; }
    public void setTerminatedDate(LocalDate terminatedDate) { this.terminatedDate = terminatedDate; }

    public String getProgramType() { return programType; }
    public void setProgramType(String programType) { this.programType = programType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
