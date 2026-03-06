package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provider Benefit/Deduction Entity - DSD Section 23 (CI-117534)
 *
 * Tracks health benefit deductions for providers enrolled in IHSS Public Authority
 * health benefit plans. Deductions are taken from provider paychecks.
 *
 * Key rules (DSD Section 6 / PR00905A, PR00906A interfaces):
 * - Only providers enrolled with PA (Public Authority) are eligible
 * - Benefit deductions tied to plan enrollment dates
 * - Termination tracked with effective date
 * - Linked to payroll via interface PROO905A (view) / PROO906A (add/terminate)
 */
@Entity
@Table(name = "provider_benefits")
public class ProviderBenefitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    // Benefit plan details
    @Column(name = "benefit_type", length = 100, nullable = false)
    // Values: HEALTH, DENTAL, VISION, LIFE_INSURANCE, SDI, CALPERS
    private String benefitType;

    @Column(name = "plan_name", length = 200)
    private String planName;

    @Column(name = "plan_code", length = 50)
    private String planCode;

    // Coverage
    @Column(name = "coverage_type", length = 50)
    // Values: SINGLE, EMPLOYEE_PLUS_ONE, FAMILY
    private String coverageType;

    // Deduction amounts
    @Column(name = "monthly_deduction_amount", precision = 10, scale = 2)
    private BigDecimal monthlyDeductionAmount;

    @Column(name = "employee_contribution", precision = 10, scale = 2)
    private BigDecimal employeeContribution;

    @Column(name = "employer_contribution", precision = 10, scale = 2)
    private BigDecimal employerContribution;

    // Dates
    @Column(name = "begin_date", nullable = false)
    private LocalDate beginDate;

    @Column(name = "end_date")
    // Set when benefit is terminated
    private LocalDate endDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BenefitStatus status;

    // Termination
    @Column(name = "termination_reason", length = 200)
    private String terminationReason;

    @Column(name = "terminated_by", length = 100)
    private String terminatedBy;

    @Column(name = "terminated_date")
    private LocalDate terminatedDate;

    // SDI (State Disability Insurance) specific fields
    @Column(name = "elective_sdi")
    private Boolean electiveSdi;

    @Column(name = "sdi_begin_date")
    private LocalDate sdiBeginDate;

    @Column(name = "sdi_end_date")
    private LocalDate sdiEndDate;

    // CalSavers
    @Column(name = "calsavers_status", length = 50)
    private String calsaversStatus;

    @Column(name = "calsavers_amount", precision = 10, scale = 2)
    private BigDecimal calsaversAmount;

    // Payroll interface tracking (PROO905A / PROO906A)
    @Column(name = "payroll_updated")
    private Boolean payrollUpdated;

    @Column(name = "payroll_update_date")
    private LocalDate payrollUpdateDate;

    // Audit
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public ProviderBenefitEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = BenefitStatus.ACTIVE;
        if (payrollUpdated == null) payrollUpdated = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == BenefitStatus.ACTIVE;
    }

    public enum BenefitStatus {
        ACTIVE, TERMINATED, PENDING
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getBenefitType() { return benefitType; }
    public void setBenefitType(String benefitType) { this.benefitType = benefitType; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }

    public String getCoverageType() { return coverageType; }
    public void setCoverageType(String coverageType) { this.coverageType = coverageType; }

    public BigDecimal getMonthlyDeductionAmount() { return monthlyDeductionAmount; }
    public void setMonthlyDeductionAmount(BigDecimal monthlyDeductionAmount) { this.monthlyDeductionAmount = monthlyDeductionAmount; }

    public BigDecimal getEmployeeContribution() { return employeeContribution; }
    public void setEmployeeContribution(BigDecimal employeeContribution) { this.employeeContribution = employeeContribution; }

    public BigDecimal getEmployerContribution() { return employerContribution; }
    public void setEmployerContribution(BigDecimal employerContribution) { this.employerContribution = employerContribution; }

    public LocalDate getBeginDate() { return beginDate; }
    public void setBeginDate(LocalDate beginDate) { this.beginDate = beginDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public BenefitStatus getStatus() { return status; }
    public void setStatus(BenefitStatus status) { this.status = status; }

    public String getTerminationReason() { return terminationReason; }
    public void setTerminationReason(String terminationReason) { this.terminationReason = terminationReason; }

    public String getTerminatedBy() { return terminatedBy; }
    public void setTerminatedBy(String terminatedBy) { this.terminatedBy = terminatedBy; }

    public LocalDate getTerminatedDate() { return terminatedDate; }
    public void setTerminatedDate(LocalDate terminatedDate) { this.terminatedDate = terminatedDate; }

    public Boolean getElectiveSdi() { return electiveSdi; }
    public void setElectiveSdi(Boolean electiveSdi) { this.electiveSdi = electiveSdi; }

    public LocalDate getSdiBeginDate() { return sdiBeginDate; }
    public void setSdiBeginDate(LocalDate sdiBeginDate) { this.sdiBeginDate = sdiBeginDate; }

    public LocalDate getSdiEndDate() { return sdiEndDate; }
    public void setSdiEndDate(LocalDate sdiEndDate) { this.sdiEndDate = sdiEndDate; }

    public String getCalsaversStatus() { return calsaversStatus; }
    public void setCalsaversStatus(String calsaversStatus) { this.calsaversStatus = calsaversStatus; }

    public BigDecimal getCalsaversAmount() { return calsaversAmount; }
    public void setCalsaversAmount(BigDecimal calsaversAmount) { this.calsaversAmount = calsaversAmount; }

    public Boolean getPayrollUpdated() { return payrollUpdated; }
    public void setPayrollUpdated(Boolean payrollUpdated) { this.payrollUpdated = payrollUpdated; }

    public LocalDate getPayrollUpdateDate() { return payrollUpdateDate; }
    public void setPayrollUpdateDate(LocalDate payrollUpdateDate) { this.payrollUpdateDate = payrollUpdateDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
