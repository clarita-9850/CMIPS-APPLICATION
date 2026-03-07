package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DSD Section 24 — Timesheet Issuance Workflow Entity
 * Tracks the lifecycle of timesheet issuance from generation through mailing/delivery.
 * Timesheets are issued per pay period per provider-recipient assignment.
 */
@Entity
@Table(name = "timesheet_issuances", indexes = {
        @Index(name = "idx_tsi_case", columnList = "case_id"),
        @Index(name = "idx_tsi_provider", columnList = "provider_id"),
        @Index(name = "idx_tsi_period", columnList = "pay_period_start"),
        @Index(name = "idx_tsi_status", columnList = "status")
})
public class TimesheetIssuanceEntity {

    public enum IssuanceStatus {
        PENDING_GENERATION,     // Awaiting batch generation
        GENERATED,              // TS number assigned, form generated
        PENDING_MAIL,           // In print queue for mailing
        MAILED,                 // Mailed to provider
        DELIVERED_ELECTRONIC,   // Delivered via ESP portal
        DELIVERED_IN_PERSON,    // Handed to provider in office
        CANCELLED,              // Issuance cancelled (provider terminated, etc.)
        REISSUED                // Original voided, new one issued
    }

    public enum IssuanceMethod {
        MAIL,                   // Standard USPS mail
        ELECTRONIC,             // ESP portal delivery
        IN_PERSON,              // County office pickup
        BATCH_PRINT             // Nightly batch print
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issuance_number", length = 30, unique = true)
    private String issuanceNumber;

    @Column(name = "timesheet_id")
    private Long timesheetId;

    @Column(name = "timesheet_number", length = 20)
    private String timesheetNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    @Column(name = "timesheet_type", length = 30)
    private String timesheetType; // STANDARD, LARGE_FONT, EVV_EXCEPTION

    @Column(name = "program_type", length = 10)
    private String programType; // IHSS, WPCS

    @Enumerated(EnumType.STRING)
    @Column(name = "issuance_method", length = 30)
    private IssuanceMethod issuanceMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private IssuanceStatus status = IssuanceStatus.PENDING_GENERATION;

    @Column(name = "generation_date")
    private LocalDate generationDate;

    @Column(name = "mail_date")
    private LocalDate mailDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate; // Pay period end + 30 days

    @Column(name = "is_reissue")
    private Boolean isReissue = false;

    @Column(name = "original_issuance_id")
    private Long originalIssuanceId;

    @Column(name = "reissue_reason", length = 500)
    private String reissueReason;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "batch_id", length = 30)
    private String batchId;

    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "provider_address", length = 500)
    private String providerAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (issuanceNumber == null) {
            issuanceNumber = "ISS-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIssuanceNumber() { return issuanceNumber; }
    public void setIssuanceNumber(String issuanceNumber) { this.issuanceNumber = issuanceNumber; }

    public Long getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }

    public String getTimesheetNumber() { return timesheetNumber; }
    public void setTimesheetNumber(String timesheetNumber) { this.timesheetNumber = timesheetNumber; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public LocalDate getPayPeriodStart() { return payPeriodStart; }
    public void setPayPeriodStart(LocalDate payPeriodStart) { this.payPeriodStart = payPeriodStart; }

    public LocalDate getPayPeriodEnd() { return payPeriodEnd; }
    public void setPayPeriodEnd(LocalDate payPeriodEnd) { this.payPeriodEnd = payPeriodEnd; }

    public String getTimesheetType() { return timesheetType; }
    public void setTimesheetType(String timesheetType) { this.timesheetType = timesheetType; }

    public String getProgramType() { return programType; }
    public void setProgramType(String programType) { this.programType = programType; }

    public IssuanceMethod getIssuanceMethod() { return issuanceMethod; }
    public void setIssuanceMethod(IssuanceMethod issuanceMethod) { this.issuanceMethod = issuanceMethod; }

    public IssuanceStatus getStatus() { return status; }
    public void setStatus(IssuanceStatus status) { this.status = status; }

    public LocalDate getGenerationDate() { return generationDate; }
    public void setGenerationDate(LocalDate generationDate) { this.generationDate = generationDate; }

    public LocalDate getMailDate() { return mailDate; }
    public void setMailDate(LocalDate mailDate) { this.mailDate = mailDate; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }

    public Boolean getIsReissue() { return isReissue; }
    public void setIsReissue(Boolean isReissue) { this.isReissue = isReissue; }

    public Long getOriginalIssuanceId() { return originalIssuanceId; }
    public void setOriginalIssuanceId(Long originalIssuanceId) { this.originalIssuanceId = originalIssuanceId; }

    public String getReissueReason() { return reissueReason; }
    public void setReissueReason(String reissueReason) { this.reissueReason = reissueReason; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public String getProviderAddress() { return providerAddress; }
    public void setProviderAddress(String providerAddress) { this.providerAddress = providerAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
