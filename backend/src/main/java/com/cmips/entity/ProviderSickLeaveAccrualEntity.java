package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provider Sick Leave Accrual — DSD Section 23, Section 9.
 *
 * Tracks sick leave accrual and usage for IHSS providers.
 * Accrual rate: 1 hour per 30 hours worked, max accrual typically 48 hours,
 * usage cap 24 hours/year, eligible after 90 days.
 */
@Entity
@Table(name = "provider_sick_leave_accruals", indexes = {
        @Index(name = "idx_psla_provider", columnList = "provider_id")
})
public class ProviderSickLeaveAccrualEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "provider_number", length = 20)
    private String providerNumber;

    @Column(name = "accrual_year", nullable = false)
    private Integer accrualYear;

    /** Total hours accrued to date (stored as minutes) */
    @Column(name = "hours_accrued")
    private Integer hoursAccrued;

    /** Total hours used to date (stored as minutes) */
    @Column(name = "hours_used")
    private Integer hoursUsed;

    /** Hours carried over from prior year (stored as minutes) */
    @Column(name = "hours_carried_over")
    private Integer hoursCarriedOver;

    /** Maximum accrual cap (stored as minutes, typically 2880 = 48 hours) */
    @Column(name = "max_accrual_cap")
    private Integer maxAccrualCap;

    /** Annual usage cap (stored as minutes, typically 1440 = 24 hours) */
    @Column(name = "annual_usage_cap")
    private Integer annualUsageCap;

    /** Year-to-date hours used (stored as minutes) */
    @Column(name = "ytd_hours_used")
    private Integer ytdHoursUsed;

    /** Total hours worked that count toward accrual (stored as minutes) */
    @Column(name = "total_hours_worked")
    private Integer totalHoursWorked;

    @Column(name = "eligibility_date")
    private LocalDate eligibilityDate;

    @Column(name = "last_accrual_date")
    private LocalDate lastAccrualDate;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (hoursAccrued == null) hoursAccrued = 0;
        if (hoursUsed == null) hoursUsed = 0;
        if (hoursCarriedOver == null) hoursCarriedOver = 0;
        if (ytdHoursUsed == null) ytdHoursUsed = 0;
        if (totalHoursWorked == null) totalHoursWorked = 0;
        if (maxAccrualCap == null) maxAccrualCap = 2880;
        if (annualUsageCap == null) annualUsageCap = 1440;
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }

    public Integer getAccrualYear() { return accrualYear; }
    public void setAccrualYear(Integer accrualYear) { this.accrualYear = accrualYear; }

    public Integer getHoursAccrued() { return hoursAccrued; }
    public void setHoursAccrued(Integer hoursAccrued) { this.hoursAccrued = hoursAccrued; }

    public Integer getHoursUsed() { return hoursUsed; }
    public void setHoursUsed(Integer hoursUsed) { this.hoursUsed = hoursUsed; }

    public Integer getHoursCarriedOver() { return hoursCarriedOver; }
    public void setHoursCarriedOver(Integer hoursCarriedOver) { this.hoursCarriedOver = hoursCarriedOver; }

    public Integer getMaxAccrualCap() { return maxAccrualCap; }
    public void setMaxAccrualCap(Integer maxAccrualCap) { this.maxAccrualCap = maxAccrualCap; }

    public Integer getAnnualUsageCap() { return annualUsageCap; }
    public void setAnnualUsageCap(Integer annualUsageCap) { this.annualUsageCap = annualUsageCap; }

    public Integer getYtdHoursUsed() { return ytdHoursUsed; }
    public void setYtdHoursUsed(Integer ytdHoursUsed) { this.ytdHoursUsed = ytdHoursUsed; }

    public Integer getTotalHoursWorked() { return totalHoursWorked; }
    public void setTotalHoursWorked(Integer totalHoursWorked) { this.totalHoursWorked = totalHoursWorked; }

    public LocalDate getEligibilityDate() { return eligibilityDate; }
    public void setEligibilityDate(LocalDate eligibilityDate) { this.eligibilityDate = eligibilityDate; }

    public LocalDate getLastAccrualDate() { return lastAccrualDate; }
    public void setLastAccrualDate(LocalDate lastAccrualDate) { this.lastAccrualDate = lastAccrualDate; }

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
