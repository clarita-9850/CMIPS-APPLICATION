package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sick Leave Claim Entity — DSD Section 32, CI-790531/790532/794527-794530
 *
 * Records sick leave claims entered by TPF staff from SOC forms.
 * Time entries are stored as a JSON string of daily {date, minutes} objects.
 */
@Entity
@Table(name = "sick_leave_claims", indexes = {
        @Index(name = "idx_slc_provider", columnList = "provider_id"),
        @Index(name = "idx_slc_case", columnList = "case_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_slc_claim_number", columnNames = "claim_number")
})
public class SickLeaveClaimEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** System-generated claim number: SLC-YYYYMMDD-XXXXX */
    @Column(name = "claim_number", length = 30, nullable = false, unique = true)
    private String claimNumber;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "provider_number", length = 20, nullable = false)
    private String providerNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_number", length = 50, nullable = false)
    private String caseNumber;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "provider_name", length = 200)
    private String providerName;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    /** "IHSS" or "WPCS" */
    @Column(name = "provider_type", length = 10)
    private String providerType;

    @Column(name = "pay_period_begin_date", nullable = false)
    private LocalDate payPeriodBeginDate;

    @Column(name = "service_period_from")
    private LocalDate servicePeriodFrom;

    /** Total claimed time in minutes (for HH:MM display) */
    @Column(name = "claimed_hours")
    private Integer claimedHours;

    /** JSON array: [{"date":"2026-03-01","minutes":60}, ...] */
    @Column(name = "time_entries", length = 4000)
    private String timeEntries;

    /** "MANUAL" for screen entries, "ELECTRONIC" for batch */
    @Column(name = "mode_of_entry", length = 20)
    private String modeOfEntry;

    @Column(name = "claim_entered_date")
    private LocalDate claimEnteredDate;

    /** Date SCO issued warrant — null until processed */
    @Column(name = "issued_date")
    private LocalDate issuedDate;

    /** "ACTIVE" or "CANCELLED" */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public SickLeaveClaimEntity() {}

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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }

    public LocalDate getPayPeriodBeginDate() { return payPeriodBeginDate; }
    public void setPayPeriodBeginDate(LocalDate payPeriodBeginDate) { this.payPeriodBeginDate = payPeriodBeginDate; }

    public LocalDate getServicePeriodFrom() { return servicePeriodFrom; }
    public void setServicePeriodFrom(LocalDate servicePeriodFrom) { this.servicePeriodFrom = servicePeriodFrom; }

    public Integer getClaimedHours() { return claimedHours; }
    public void setClaimedHours(Integer claimedHours) { this.claimedHours = claimedHours; }

    public String getTimeEntries() { return timeEntries; }
    public void setTimeEntries(String timeEntries) { this.timeEntries = timeEntries; }

    public String getModeOfEntry() { return modeOfEntry; }
    public void setModeOfEntry(String modeOfEntry) { this.modeOfEntry = modeOfEntry; }

    public LocalDate getClaimEnteredDate() { return claimEnteredDate; }
    public void setClaimEnteredDate(LocalDate claimEnteredDate) { this.claimEnteredDate = claimEnteredDate; }

    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
