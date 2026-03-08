package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Garnishment Entity — DSD Section 32 (Internal Operations)
 *
 * Tracks wage garnishments against IHSS provider payments.
 * Types: CHILD_SUPPORT, TAX_LEVY, STUDENT_LOAN, CREDITOR, BANKRUPTCY, OTHER
 *
 * Priority ordering determines deduction sequence when multiple
 * garnishments exist for the same provider.
 *
 * Lifecycle: ACTIVE -> SATISFIED/SUSPENDED/TERMINATED
 *
 * Tracks court order details, amounts withheld, payee information,
 * and percentage/fixed-amount caps per pay period.
 */
@Entity
@Table(name = "garnishments", indexes = {
    @Index(name = "idx_g_provider", columnList = "provider_id"),
    @Index(name = "idx_g_status", columnList = "status"),
    @Index(name = "idx_g_type", columnList = "garnishment_type")
})
public class GarnishmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "provider_number", length = 20)
    private String providerNumber;

    @Column(name = "provider_name", length = 200)
    private String providerName;

    @Column(name = "garnishment_type", nullable = false, length = 50)
    private String garnishmentType;

    @Column(name = "court_order_number", length = 50)
    private String courtOrderNumber;

    @Column(name = "court_order_date")
    private LocalDate courtOrderDate;

    @Column(name = "issuing_authority", length = 200)
    private String issuingAuthority;

    @Column(name = "garnishment_amount", precision = 12, scale = 2)
    private BigDecimal garnishmentAmount;

    @Column(name = "garnishment_percentage", precision = 5, scale = 2)
    private BigDecimal garnishmentPercentage;

    @Column(name = "max_per_pay_period", precision = 12, scale = 2)
    private BigDecimal maxPerPayPeriod;

    @Column(name = "total_ordered_amount", precision = 12, scale = 2)
    private BigDecimal totalOrderedAmount;

    @Column(name = "total_withheld_to_date", precision = 12, scale = 2)
    private BigDecimal totalWithheldToDate;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "payee_info", length = 500)
    private String payeeInfo;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "suspended_reason", length = 200)
    private String suspendedReason;

    @Column(name = "notes", length = 1000)
    private String notes;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public GarnishmentEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (totalWithheldToDate == null) totalWithheldToDate = BigDecimal.ZERO;
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

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getGarnishmentType() { return garnishmentType; }
    public void setGarnishmentType(String garnishmentType) { this.garnishmentType = garnishmentType; }

    public String getCourtOrderNumber() { return courtOrderNumber; }
    public void setCourtOrderNumber(String courtOrderNumber) { this.courtOrderNumber = courtOrderNumber; }

    public LocalDate getCourtOrderDate() { return courtOrderDate; }
    public void setCourtOrderDate(LocalDate courtOrderDate) { this.courtOrderDate = courtOrderDate; }

    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }

    public BigDecimal getGarnishmentAmount() { return garnishmentAmount; }
    public void setGarnishmentAmount(BigDecimal garnishmentAmount) { this.garnishmentAmount = garnishmentAmount; }

    public BigDecimal getGarnishmentPercentage() { return garnishmentPercentage; }
    public void setGarnishmentPercentage(BigDecimal garnishmentPercentage) { this.garnishmentPercentage = garnishmentPercentage; }

    public BigDecimal getMaxPerPayPeriod() { return maxPerPayPeriod; }
    public void setMaxPerPayPeriod(BigDecimal maxPerPayPeriod) { this.maxPerPayPeriod = maxPerPayPeriod; }

    public BigDecimal getTotalOrderedAmount() { return totalOrderedAmount; }
    public void setTotalOrderedAmount(BigDecimal totalOrderedAmount) { this.totalOrderedAmount = totalOrderedAmount; }

    public BigDecimal getTotalWithheldToDate() { return totalWithheldToDate; }
    public void setTotalWithheldToDate(BigDecimal totalWithheldToDate) { this.totalWithheldToDate = totalWithheldToDate; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getPayeeInfo() { return payeeInfo; }
    public void setPayeeInfo(String payeeInfo) { this.payeeInfo = payeeInfo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSuspendedReason() { return suspendedReason; }
    public void setSuspendedReason(String suspendedReason) { this.suspendedReason = suspendedReason; }

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
