package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Earnings Statement Entity — DSD Section 17 (Pay Stubs)
 * Provider pay stub detail for each pay period, including gross/net pay,
 * all tax and deduction breakdowns, hours by type, and YTD totals.
 */
@Entity
@Table(name = "earnings_statements", indexes = {
    @Index(name = "idx_es_provider", columnList = "provider_id"),
    @Index(name = "idx_es_warrant", columnList = "warrant_id")
})
public class EarningsStatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "provider_number", length = 20)
    private String providerNumber;

    @Column(name = "warrant_id")
    private Long warrantId;

    @Column(name = "warrant_number", length = 30)
    private String warrantNumber;

    @Column(name = "pay_period_begin_date", nullable = false)
    private LocalDate payPeriodBeginDate;

    @Column(name = "pay_period_end_date", nullable = false)
    private LocalDate payPeriodEndDate;

    @Column(name = "gross_pay", precision = 12, scale = 2)
    private BigDecimal grossPay;

    @Column(name = "federal_tax", precision = 12, scale = 2)
    private BigDecimal federalTax;

    @Column(name = "state_tax", precision = 12, scale = 2)
    private BigDecimal stateTax;

    @Column(name = "fica_tax", precision = 12, scale = 2)
    private BigDecimal ficaTax;

    @Column(name = "medicare_tax", precision = 12, scale = 2)
    private BigDecimal medicareTax;

    @Column(name = "sdi_tax", precision = 12, scale = 2)
    private BigDecimal sdiTax;

    @Column(name = "health_benefits", precision = 12, scale = 2)
    private BigDecimal healthBenefits;

    @Column(name = "union_dues", precision = 12, scale = 2)
    private BigDecimal unionDues;

    @Column(name = "garnishments", precision = 12, scale = 2)
    private BigDecimal garnishments;

    @Column(name = "other_deductions", precision = 12, scale = 2)
    private BigDecimal otherDeductions;

    @Column(name = "total_deductions", precision = 12, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "net_pay", precision = 12, scale = 2)
    private BigDecimal netPay;

    /** Regular hours in minutes */
    @Column(name = "regular_hours")
    private Integer regularHours;

    /** Overtime hours in minutes */
    @Column(name = "overtime_hours")
    private Integer overtimeHours;

    /** Sick leave hours in minutes */
    @Column(name = "sick_leave_hours")
    private Integer sickLeaveHours;

    /** Travel hours in minutes */
    @Column(name = "travel_hours")
    private Integer travelHours;

    @Column(name = "regular_rate", precision = 8, scale = 2)
    private BigDecimal regularRate;

    @Column(name = "overtime_rate", precision = 8, scale = 2)
    private BigDecimal overtimeRate;

    @Column(name = "ytd_gross_pay", precision = 14, scale = 2)
    private BigDecimal ytdGrossPay;

    @Column(name = "ytd_federal_tax", precision = 14, scale = 2)
    private BigDecimal ytdFederalTax;

    @Column(name = "ytd_state_tax", precision = 14, scale = 2)
    private BigDecimal ytdStateTax;

    @Column(name = "ytd_net_pay", precision = 14, scale = 2)
    private BigDecimal ytdNetPay;

    @Column(name = "generated_date")
    private LocalDate generatedDate;

    /** GENERATED, MAILED, AVAILABLE */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public EarningsStatementEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "GENERATED";
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

    public Long getWarrantId() { return warrantId; }
    public void setWarrantId(Long warrantId) { this.warrantId = warrantId; }

    public String getWarrantNumber() { return warrantNumber; }
    public void setWarrantNumber(String warrantNumber) { this.warrantNumber = warrantNumber; }

    public LocalDate getPayPeriodBeginDate() { return payPeriodBeginDate; }
    public void setPayPeriodBeginDate(LocalDate payPeriodBeginDate) { this.payPeriodBeginDate = payPeriodBeginDate; }

    public LocalDate getPayPeriodEndDate() { return payPeriodEndDate; }
    public void setPayPeriodEndDate(LocalDate payPeriodEndDate) { this.payPeriodEndDate = payPeriodEndDate; }

    public BigDecimal getGrossPay() { return grossPay; }
    public void setGrossPay(BigDecimal grossPay) { this.grossPay = grossPay; }

    public BigDecimal getFederalTax() { return federalTax; }
    public void setFederalTax(BigDecimal federalTax) { this.federalTax = federalTax; }

    public BigDecimal getStateTax() { return stateTax; }
    public void setStateTax(BigDecimal stateTax) { this.stateTax = stateTax; }

    public BigDecimal getFicaTax() { return ficaTax; }
    public void setFicaTax(BigDecimal ficaTax) { this.ficaTax = ficaTax; }

    public BigDecimal getMedicareTax() { return medicareTax; }
    public void setMedicareTax(BigDecimal medicareTax) { this.medicareTax = medicareTax; }

    public BigDecimal getSdiTax() { return sdiTax; }
    public void setSdiTax(BigDecimal sdiTax) { this.sdiTax = sdiTax; }

    public BigDecimal getHealthBenefits() { return healthBenefits; }
    public void setHealthBenefits(BigDecimal healthBenefits) { this.healthBenefits = healthBenefits; }

    public BigDecimal getUnionDues() { return unionDues; }
    public void setUnionDues(BigDecimal unionDues) { this.unionDues = unionDues; }

    public BigDecimal getGarnishments() { return garnishments; }
    public void setGarnishments(BigDecimal garnishments) { this.garnishments = garnishments; }

    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }

    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

    public BigDecimal getNetPay() { return netPay; }
    public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }

    public Integer getRegularHours() { return regularHours; }
    public void setRegularHours(Integer regularHours) { this.regularHours = regularHours; }

    public Integer getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(Integer overtimeHours) { this.overtimeHours = overtimeHours; }

    public Integer getSickLeaveHours() { return sickLeaveHours; }
    public void setSickLeaveHours(Integer sickLeaveHours) { this.sickLeaveHours = sickLeaveHours; }

    public Integer getTravelHours() { return travelHours; }
    public void setTravelHours(Integer travelHours) { this.travelHours = travelHours; }

    public BigDecimal getRegularRate() { return regularRate; }
    public void setRegularRate(BigDecimal regularRate) { this.regularRate = regularRate; }

    public BigDecimal getOvertimeRate() { return overtimeRate; }
    public void setOvertimeRate(BigDecimal overtimeRate) { this.overtimeRate = overtimeRate; }

    public BigDecimal getYtdGrossPay() { return ytdGrossPay; }
    public void setYtdGrossPay(BigDecimal ytdGrossPay) { this.ytdGrossPay = ytdGrossPay; }

    public BigDecimal getYtdFederalTax() { return ytdFederalTax; }
    public void setYtdFederalTax(BigDecimal ytdFederalTax) { this.ytdFederalTax = ytdFederalTax; }

    public BigDecimal getYtdStateTax() { return ytdStateTax; }
    public void setYtdStateTax(BigDecimal ytdStateTax) { this.ytdStateTax = ytdStateTax; }

    public BigDecimal getYtdNetPay() { return ytdNetPay; }
    public void setYtdNetPay(BigDecimal ytdNetPay) { this.ytdNetPay = ytdNetPay; }

    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

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
