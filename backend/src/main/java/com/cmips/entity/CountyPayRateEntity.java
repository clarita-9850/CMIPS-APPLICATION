package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * County Pay Rate Entity — DSD Section 21/22
 * (CaseProvider.findDefaultCountyPayRateByDateAndCountyCode permission)
 *
 * County-specific IP (Individual Provider) pay rates by effective date.
 * Used to calculate share-of-cost, payroll estimates, and SOC spend-down.
 * Rates change over time; the applicable rate is found by effectiveDate lookup.
 */
@Entity
@Table(name = "county_pay_rate", indexes = {
        @Index(name = "idx_cpr_county_date", columnList = "county_code, effective_date"),
        @Index(name = "idx_cpr_status", columnList = "status")
})
public class CountyPayRateEntity {

    public enum RateType {
        STANDARD_IP,     // Standard Individual Provider rate
        WPCS_IP,         // WPCS Individual Provider rate
        ENHANCED_IP,     // Enhanced rate (live-in, paramedical, etc.)
        HOMEMAKER        // County Homemaker rate
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 2-digit California county code (01=Alameda, 19=Los Angeles, etc.) */
    @Column(name = "county_code", length = 3, nullable = false)
    private String countyCode;

    @Column(name = "county_name", length = 60)
    private String countyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", length = 20)
    private RateType rateType;

    /** Hourly IP rate in dollars */
    @Column(name = "hourly_rate", nullable = false)
    private Double hourlyRate;

    /** Overtime rate (typically 1.5 × hourlyRate — stored explicitly for payroll) */
    @Column(name = "overtime_rate")
    private Double overtimeRate;

    /** Date this rate became effective */
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    /** Date this rate was superseded (null = currently active) */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** ACTIVE or INACTIVE */
    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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
        if (status == null) status = "ACTIVE";
        if (rateType == null) rateType = RateType.STANDARD_IP;
        if (overtimeRate == null && hourlyRate != null) {
            overtimeRate = Math.round(hourlyRate * 1.5 * 100.0) / 100.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public String getCountyName() { return countyName; }
    public void setCountyName(String countyName) { this.countyName = countyName; }

    public RateType getRateType() { return rateType; }
    public void setRateType(RateType rateType) { this.rateType = rateType; }

    public Double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }

    public Double getOvertimeRate() { return overtimeRate; }
    public void setOvertimeRate(Double overtimeRate) { this.overtimeRate = overtimeRate; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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
