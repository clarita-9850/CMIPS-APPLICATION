package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provider Training Entity
 * Tracks provider orientation and annual refresher training completions.
 * DSD Section 23 — CI-117545 / CI-67804
 *
 * Training hour requirements by fiscal year:
 *   7/1/2018 – 6/30/2020: 8 hours/year
 *   7/1/2020 – 6/30/2022: 16 hours/year
 *   7/1/2022+:             24 hours/year
 */
@Entity
@Table(name = "provider_training")
public class ProviderTrainingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    /**
     * Training type:
     *   INITIAL_ORIENTATION   — one-time enrollment requirement (SOC enrollment orientation)
     *   ANNUAL_REFRESHER      — annual continuing education hours
     *   VIOLATION_REMEDIATION — remediation training required after 2nd overtime violation
     */
    @Column(name = "training_type", length = 30, nullable = false)
    private String trainingType;

    /**
     * Fiscal year string, e.g. "2022-2023" (July 1 – June 30).
     * Null for INITIAL_ORIENTATION records.
     */
    @Column(name = "fiscal_year", length = 10)
    private String fiscalYear;

    /** Total hours completed (stored as minutes for precision, e.g. 480 = 8 hrs) */
    @Column(name = "hours_completed_minutes")
    private Integer hoursCompletedMinutes;

    /** Required hours for the fiscal year (minutes) — 480 / 960 / 1440 */
    @Column(name = "hours_required_minutes")
    private Integer hoursRequiredMinutes;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "certificate_number", length = 100)
    private String certificateNumber;

    /** ACTIVE = valid, EXPIRED = past expiration, WAIVED = county waiver granted */
    @Column(name = "status", length = 20)
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

    public ProviderTrainingEntity() {}

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

    /** Returns required hours in minutes for a fiscal year begin date per DSD Section 23 */
    public static int requiredHoursMinutesForFiscalYearStart(LocalDate fiscalYearStart) {
        LocalDate threshold1 = LocalDate.of(2020, 7, 1);
        LocalDate threshold2 = LocalDate.of(2022, 7, 1);
        if (fiscalYearStart.isBefore(threshold1)) return 480;  // 8 hrs
        if (fiscalYearStart.isBefore(threshold2)) return 960;  // 16 hrs
        return 1440; // 24 hrs
    }

    /** Returns the fiscal year string (e.g. "2022-2023") for a given date */
    public static String fiscalYearFor(LocalDate date) {
        int year = date.getMonthValue() >= 7 ? date.getYear() : date.getYear() - 1;
        return year + "-" + (year + 1);
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getTrainingType() { return trainingType; }
    public void setTrainingType(String trainingType) { this.trainingType = trainingType; }

    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public Integer getHoursCompletedMinutes() { return hoursCompletedMinutes; }
    public void setHoursCompletedMinutes(Integer hoursCompletedMinutes) { this.hoursCompletedMinutes = hoursCompletedMinutes; }

    public Integer getHoursRequiredMinutes() { return hoursRequiredMinutes; }
    public void setHoursRequiredMinutes(Integer hoursRequiredMinutes) { this.hoursRequiredMinutes = hoursRequiredMinutes; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }

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
