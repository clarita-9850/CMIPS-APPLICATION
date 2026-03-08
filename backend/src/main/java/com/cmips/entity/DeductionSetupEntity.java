package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Deduction Setup Entity — DSD Section 15
 * Configures payroll deductions for IHSS providers including taxes, benefits,
 * union dues, garnishments, and other withholdings.
 */
@Entity
@Table(name = "deduction_setups", indexes = {
    @Index(name = "idx_ds_provider", columnList = "provider_id"),
    @Index(name = "idx_ds_type", columnList = "deduction_type")
})
public class DeductionSetupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "provider_number", length = 20)
    private String providerNumber;

    /** FEDERAL_TAX, STATE_TAX, FICA, MEDICARE, SDI, HEALTH_BENEFITS, UNION_DUES, GARNISHMENT, RETIREMENT, OTHER */
    @Column(name = "deduction_type", nullable = false, length = 50)
    private String deductionType;

    @Column(name = "deduction_code", length = 20)
    private String deductionCode;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "percentage", precision = 8, scale = 4)
    private BigDecimal percentage;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** PER_PAY_PERIOD, MONTHLY, ANNUAL */
    @Column(name = "frequency", length = 20)
    private String frequency;

    @Column(name = "max_amount", precision = 12, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "ytd_amount", precision = 12, scale = 2)
    private BigDecimal ytdAmount;

    /** ACTIVE, INACTIVE, SUSPENDED */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** For garnishment deductions */
    @Column(name = "garnishment_case_number", length = 50)
    private String garnishmentCaseNumber;

    /** For garnishment deductions */
    @Column(name = "court_order_date")
    private LocalDate courtOrderDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public DeductionSetupEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
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

    public String getDeductionType() { return deductionType; }
    public void setDeductionType(String deductionType) { this.deductionType = deductionType; }

    public String getDeductionCode() { return deductionCode; }
    public void setDeductionCode(String deductionCode) { this.deductionCode = deductionCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public BigDecimal getYtdAmount() { return ytdAmount; }
    public void setYtdAmount(BigDecimal ytdAmount) { this.ytdAmount = ytdAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGarnishmentCaseNumber() { return garnishmentCaseNumber; }
    public void setGarnishmentCaseNumber(String garnishmentCaseNumber) { this.garnishmentCaseNumber = garnishmentCaseNumber; }

    public LocalDate getCourtOrderDate() { return courtOrderDate; }
    public void setCourtOrderDate(LocalDate courtOrderDate) { this.courtOrderDate = courtOrderDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
