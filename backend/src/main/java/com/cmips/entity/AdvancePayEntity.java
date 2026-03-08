package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Advance Pay Entity — DSD Section 14
 * Tracks advance payments to IHSS providers including initial, ongoing, and supplemental advances.
 * Recovery tracking for overpaid advances via payroll deductions.
 */
@Entity
@Table(name = "advance_pays", indexes = {
    @Index(name = "idx_ap_case", columnList = "case_id"),
    @Index(name = "idx_ap_provider", columnList = "provider_id")
})
public class AdvancePayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "provider_number", length = 20)
    private String providerNumber;

    @Column(name = "provider_name", length = 200)
    private String providerName;

    /** INITIAL, ONGOING, SUPPLEMENTAL */
    @Column(name = "advance_pay_type", nullable = false, length = 30)
    private String advancePayType;

    @Column(name = "payment_amount", precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "pay_period_begin_date")
    private LocalDate payPeriodBeginDate;

    @Column(name = "pay_period_end_date")
    private LocalDate payPeriodEndDate;

    /** Authorized hours in minutes */
    @Column(name = "authorized_hours")
    private Integer authorizedHours;

    @Column(name = "pay_rate", precision = 8, scale = 2)
    private BigDecimal payRate;

    /** Overtime hours in minutes */
    @Column(name = "overtime_hours")
    private Integer overtimeHours;

    @Column(name = "overtime_rate", precision = 8, scale = 2)
    private BigDecimal overtimeRate;

    /** Travel hours in minutes */
    @Column(name = "travel_hours")
    private Integer travelHours;

    @Column(name = "withholding_amount", precision = 12, scale = 2)
    private BigDecimal withholdingAmount;

    @Column(name = "net_pay_amount", precision = 12, scale = 2)
    private BigDecimal netPayAmount;

    @Column(name = "warrant_number", length = 30)
    private String warrantNumber;

    @Column(name = "warrant_date")
    private LocalDate warrantDate;

    /** PENDING, ISSUED, RECOVERED, CANCELLED */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "recovery_start_date")
    private LocalDate recoveryStartDate;

    @Column(name = "recovery_amount", precision = 12, scale = 2)
    private BigDecimal recoveryAmount;

    @Column(name = "recovered_to_date", precision = 12, scale = 2)
    private BigDecimal recoveredToDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public AdvancePayEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
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

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getAdvancePayType() { return advancePayType; }
    public void setAdvancePayType(String advancePayType) { this.advancePayType = advancePayType; }

    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }

    public LocalDate getPayPeriodBeginDate() { return payPeriodBeginDate; }
    public void setPayPeriodBeginDate(LocalDate payPeriodBeginDate) { this.payPeriodBeginDate = payPeriodBeginDate; }

    public LocalDate getPayPeriodEndDate() { return payPeriodEndDate; }
    public void setPayPeriodEndDate(LocalDate payPeriodEndDate) { this.payPeriodEndDate = payPeriodEndDate; }

    public Integer getAuthorizedHours() { return authorizedHours; }
    public void setAuthorizedHours(Integer authorizedHours) { this.authorizedHours = authorizedHours; }

    public BigDecimal getPayRate() { return payRate; }
    public void setPayRate(BigDecimal payRate) { this.payRate = payRate; }

    public Integer getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(Integer overtimeHours) { this.overtimeHours = overtimeHours; }

    public BigDecimal getOvertimeRate() { return overtimeRate; }
    public void setOvertimeRate(BigDecimal overtimeRate) { this.overtimeRate = overtimeRate; }

    public Integer getTravelHours() { return travelHours; }
    public void setTravelHours(Integer travelHours) { this.travelHours = travelHours; }

    public BigDecimal getWithholdingAmount() { return withholdingAmount; }
    public void setWithholdingAmount(BigDecimal withholdingAmount) { this.withholdingAmount = withholdingAmount; }

    public BigDecimal getNetPayAmount() { return netPayAmount; }
    public void setNetPayAmount(BigDecimal netPayAmount) { this.netPayAmount = netPayAmount; }

    public String getWarrantNumber() { return warrantNumber; }
    public void setWarrantNumber(String warrantNumber) { this.warrantNumber = warrantNumber; }

    public LocalDate getWarrantDate() { return warrantDate; }
    public void setWarrantDate(LocalDate warrantDate) { this.warrantDate = warrantDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getRecoveryStartDate() { return recoveryStartDate; }
    public void setRecoveryStartDate(LocalDate recoveryStartDate) { this.recoveryStartDate = recoveryStartDate; }

    public BigDecimal getRecoveryAmount() { return recoveryAmount; }
    public void setRecoveryAmount(BigDecimal recoveryAmount) { this.recoveryAmount = recoveryAmount; }

    public BigDecimal getRecoveredToDate() { return recoveredToDate; }
    public void setRecoveredToDate(BigDecimal recoveredToDate) { this.recoveredToDate = recoveredToDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
