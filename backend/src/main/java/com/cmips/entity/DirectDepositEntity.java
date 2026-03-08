package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Direct Deposit Entity — DSD Section 32 (Internal Operations)
 *
 * Manages provider direct deposit (ACH) enrollment for IHSS payments.
 * Account types: CHECKING, SAVINGS
 * Deposit types: FULL (entire payment) or PARTIAL (fixed amount or percentage)
 *
 * Prenote verification lifecycle:
 *   PENDING -> VERIFIED / FAILED
 *
 * Status lifecycle:
 *   PENDING_VERIFICATION -> ACTIVE -> INACTIVE
 *
 * Prenote is a zero-dollar test transaction sent to the bank
 * to validate routing/account numbers before live deposits begin.
 */
@Entity
@Table(name = "direct_deposits", indexes = {
    @Index(name = "idx_dd_provider", columnList = "provider_id"),
    @Index(name = "idx_dd_status", columnList = "status")
})
public class DirectDepositEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "provider_number", length = 20)
    private String providerNumber;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "routing_number", nullable = false, length = 9)
    private String routingNumber;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @Column(name = "deposit_type", nullable = false, length = 20)
    private String depositType;

    @Column(name = "deposit_amount", precision = 12, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "deposit_percentage", precision = 5, scale = 2)
    private BigDecimal depositPercentage;

    @Column(name = "prenote_status", length = 30)
    private String prenoteStatus;

    @Column(name = "prenote_date")
    private LocalDate prenoteDate;

    @Column(name = "prenote_verified_date")
    private LocalDate prenoteVerifiedDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "inactivated_date")
    private LocalDate inactivatedDate;

    @Column(name = "inactivated_by", length = 100)
    private String inactivatedBy;

    @Column(name = "inactivated_reason", length = 200)
    private String inactivatedReason;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public DirectDepositEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING_VERIFICATION";
        if (prenoteStatus == null) prenoteStatus = "PENDING";
        if (depositType == null) depositType = "FULL";
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

    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getRoutingNumber() { return routingNumber; }
    public void setRoutingNumber(String routingNumber) { this.routingNumber = routingNumber; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getDepositType() { return depositType; }
    public void setDepositType(String depositType) { this.depositType = depositType; }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public BigDecimal getDepositPercentage() { return depositPercentage; }
    public void setDepositPercentage(BigDecimal depositPercentage) { this.depositPercentage = depositPercentage; }

    public String getPrenoteStatus() { return prenoteStatus; }
    public void setPrenoteStatus(String prenoteStatus) { this.prenoteStatus = prenoteStatus; }

    public LocalDate getPrenoteDate() { return prenoteDate; }
    public void setPrenoteDate(LocalDate prenoteDate) { this.prenoteDate = prenoteDate; }

    public LocalDate getPrenoteVerifiedDate() { return prenoteVerifiedDate; }
    public void setPrenoteVerifiedDate(LocalDate prenoteVerifiedDate) { this.prenoteVerifiedDate = prenoteVerifiedDate; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getInactivatedDate() { return inactivatedDate; }
    public void setInactivatedDate(LocalDate inactivatedDate) { this.inactivatedDate = inactivatedDate; }

    public String getInactivatedBy() { return inactivatedBy; }
    public void setInactivatedBy(String inactivatedBy) { this.inactivatedBy = inactivatedBy; }

    public String getInactivatedReason() { return inactivatedReason; }
    public void setInactivatedReason(String inactivatedReason) { this.inactivatedReason = inactivatedReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
