package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fraud Case Entity — DSD Section 26
 *
 * Tracks fraud investigations for IHSS cases, recipients, and providers.
 * Fraud types: TIMESHEET_FRAUD, IDENTITY_FRAUD, OVERPAYMENT_FRAUD,
 *              ELIGIBILITY_FRAUD, PROVIDER_FRAUD, OTHER
 *
 * Investigation lifecycle:
 *   REPORTED -> UNDER_INVESTIGATION -> SUBSTANTIATED/UNSUBSTANTIATED -> REFERRED_TO_DA/CLOSED
 *
 * Tracks monetary amounts involved, recovery amounts, DA referrals,
 * and resulting actions (case termination, provider ineligibility).
 */
@Entity
@Table(name = "fraud_cases", indexes = {
    @Index(name = "idx_fc_case", columnList = "case_id"),
    @Index(name = "idx_fc_provider", columnList = "provider_id"),
    @Index(name = "idx_fc_status", columnList = "investigation_status")
})
public class FraudCaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "fraud_type", nullable = false, length = 50)
    private String fraudType;

    @Column(name = "referral_source", length = 50)
    private String referralSource;

    @Column(name = "allegation_summary", length = 2000)
    private String allegationSummary;

    @Column(name = "investigation_status", nullable = false, length = 30)
    private String investigationStatus;

    @Column(name = "investigator_name", length = 200)
    private String investigatorName;

    @Column(name = "investigator_id", length = 50)
    private String investigatorId;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "investigation_start_date")
    private LocalDate investigationStartDate;

    @Column(name = "investigation_end_date")
    private LocalDate investigationEndDate;

    @Column(name = "finding_summary", length = 2000)
    private String findingSummary;

    @Column(name = "amount_involved", precision = 12, scale = 2)
    private BigDecimal amountInvolved;

    @Column(name = "recovery_amount", precision = 12, scale = 2)
    private BigDecimal recoveryAmount;

    @Column(name = "recovered_to_date", precision = 12, scale = 2)
    private BigDecimal recoveredToDate;

    @Column(name = "referred_to_da")
    private Boolean referredToDA;

    @Column(name = "da_referral_date")
    private LocalDate daReferralDate;

    @Column(name = "da_case_number", length = 50)
    private String daCaseNumber;

    @Column(name = "criminal_charges_filed")
    private Boolean criminalChargesFiled;

    @Column(name = "provider_ineligible_result")
    private Boolean providerIneligibleResult;

    @Column(name = "case_terminated_result")
    private Boolean caseTerminatedResult;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public FraudCaseEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (investigationStatus == null) investigationStatus = "REPORTED";
        if (referredToDA == null) referredToDA = Boolean.FALSE;
        if (criminalChargesFiled == null) criminalChargesFiled = Boolean.FALSE;
        if (providerIneligibleResult == null) providerIneligibleResult = Boolean.FALSE;
        if (caseTerminatedResult == null) caseTerminatedResult = Boolean.FALSE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getFraudType() { return fraudType; }
    public void setFraudType(String fraudType) { this.fraudType = fraudType; }

    public String getReferralSource() { return referralSource; }
    public void setReferralSource(String referralSource) { this.referralSource = referralSource; }

    public String getAllegationSummary() { return allegationSummary; }
    public void setAllegationSummary(String allegationSummary) { this.allegationSummary = allegationSummary; }

    public String getInvestigationStatus() { return investigationStatus; }
    public void setInvestigationStatus(String investigationStatus) { this.investigationStatus = investigationStatus; }

    public String getInvestigatorName() { return investigatorName; }
    public void setInvestigatorName(String investigatorName) { this.investigatorName = investigatorName; }

    public String getInvestigatorId() { return investigatorId; }
    public void setInvestigatorId(String investigatorId) { this.investigatorId = investigatorId; }

    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }

    public LocalDate getInvestigationStartDate() { return investigationStartDate; }
    public void setInvestigationStartDate(LocalDate investigationStartDate) { this.investigationStartDate = investigationStartDate; }

    public LocalDate getInvestigationEndDate() { return investigationEndDate; }
    public void setInvestigationEndDate(LocalDate investigationEndDate) { this.investigationEndDate = investigationEndDate; }

    public String getFindingSummary() { return findingSummary; }
    public void setFindingSummary(String findingSummary) { this.findingSummary = findingSummary; }

    public BigDecimal getAmountInvolved() { return amountInvolved; }
    public void setAmountInvolved(BigDecimal amountInvolved) { this.amountInvolved = amountInvolved; }

    public BigDecimal getRecoveryAmount() { return recoveryAmount; }
    public void setRecoveryAmount(BigDecimal recoveryAmount) { this.recoveryAmount = recoveryAmount; }

    public BigDecimal getRecoveredToDate() { return recoveredToDate; }
    public void setRecoveredToDate(BigDecimal recoveredToDate) { this.recoveredToDate = recoveredToDate; }

    public Boolean getReferredToDA() { return referredToDA; }
    public void setReferredToDA(Boolean referredToDA) { this.referredToDA = referredToDA; }

    public LocalDate getDaReferralDate() { return daReferralDate; }
    public void setDaReferralDate(LocalDate daReferralDate) { this.daReferralDate = daReferralDate; }

    public String getDaCaseNumber() { return daCaseNumber; }
    public void setDaCaseNumber(String daCaseNumber) { this.daCaseNumber = daCaseNumber; }

    public Boolean getCriminalChargesFiled() { return criminalChargesFiled; }
    public void setCriminalChargesFiled(Boolean criminalChargesFiled) { this.criminalChargesFiled = criminalChargesFiled; }

    public Boolean getProviderIneligibleResult() { return providerIneligibleResult; }
    public void setProviderIneligibleResult(Boolean providerIneligibleResult) { this.providerIneligibleResult = providerIneligibleResult; }

    public Boolean getCaseTerminatedResult() { return caseTerminatedResult; }
    public void setCaseTerminatedResult(Boolean caseTerminatedResult) { this.caseTerminatedResult = caseTerminatedResult; }

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
