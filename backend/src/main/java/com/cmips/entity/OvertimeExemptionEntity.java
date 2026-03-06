package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Overtime Exemption Entity - DSD Section 23 (CI-668111, CI-668117, CI-668118, CI-668113)
 *
 * Tracks exemptions from weekly overtime limits (SB 1066 / FLSA).
 * Providers can be exempt from the 90-hour/month IHSS or 60-hour/week WPCS thresholds
 * for Extraordinary Circumstances (SOC 2305) or Recipient needs.
 *
 * Key rules:
 * - Exemption applies to entire calendar month regardless of Begin/End dates
 * - Compliance: must not exceed 360 hours/month (pro-rated across recipients)
 * - Hours exceeding 360/month are marked as "Exemption Callback"
 * - Exemption inactivated when Begin/End expires or provider terminates
 */
@Entity
@Table(name = "overtime_exemptions")
public class OvertimeExemptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    // CI-668111: Create Overtime Violation Exemption fields
    @Column(name = "begin_date", nullable = false)
    private LocalDate beginDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "exemption_type", length = 100, nullable = false)
    // Values: EXTRAORDINARY_CIRCUMSTANCES (SOC 2305/2303), RECIPIENT_WAIVER, GENERAL
    private String exemptionType;

    @Column(name = "comments", columnDefinition = "TEXT")
    // Max 1,000 characters per DSD
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExemptionStatus status;

    // Inactivation tracking (CI-668113)
    @Column(name = "inactivated_date")
    private LocalDate inactivatedDate;

    @Column(name = "inactivated_by", length = 100)
    private String inactivatedBy;

    @Column(name = "inactivation_reason", length = 500)
    private String inactivationReason;

    // Compliance tracking - hours exceeded 360/month threshold
    @Column(name = "callback_hours")
    // Hours above 360/month that must be paid back
    private Double callbackHours;

    @Column(name = "callback_month")
    private Integer callbackMonth;

    @Column(name = "callback_year")
    private Integer callbackYear;

    @Column(name = "callback_processed")
    private Boolean callbackProcessed;

    // SOC form tracking
    @Column(name = "soc2305_received")
    // SOC 2305 – Request for Exemption from Workweek Limits for Extraordinary Circumstances
    private Boolean soc2305Received;

    @Column(name = "soc2305_date")
    private LocalDate soc2305Date;

    @Column(name = "soc2303_received")
    // SOC 2303 – SAR Request Form
    private Boolean soc2303Received;

    @Column(name = "soc2303_date")
    private LocalDate soc2303Date;

    // Approval workflow (County → CDSS CBCB for Extraordinary Circumstances)
    @Column(name = "county_approved")
    private Boolean countyApproved;

    @Column(name = "county_approved_date")
    private LocalDate countyApprovedDate;

    @Column(name = "county_approved_by", length = 100)
    private String countyApprovedBy;

    @Column(name = "cdss_approved")
    private Boolean cdssApproved;

    @Column(name = "cdss_approved_date")
    private LocalDate cdssApprovedDate;

    @Column(name = "cdss_approved_by", length = 100)
    private String cdssApprovedBy;

    @Column(name = "cdss_denial_reason", length = 500)
    private String cdssDenialReason;

    // Audit
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public OvertimeExemptionEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = ExemptionStatus.ACTIVE;
        if (callbackProcessed == null) callbackProcessed = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        if (status != ExemptionStatus.ACTIVE) return false;
        LocalDate today = LocalDate.now();
        if (endDate != null && endDate.isBefore(today)) return false;
        return !beginDate.isAfter(today);
    }

    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public enum ExemptionStatus {
        ACTIVE,
        INACTIVE,
        PENDING_APPROVAL,
        DENIED
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public LocalDate getBeginDate() { return beginDate; }
    public void setBeginDate(LocalDate beginDate) { this.beginDate = beginDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getExemptionType() { return exemptionType; }
    public void setExemptionType(String exemptionType) { this.exemptionType = exemptionType; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public ExemptionStatus getStatus() { return status; }
    public void setStatus(ExemptionStatus status) { this.status = status; }

    public LocalDate getInactivatedDate() { return inactivatedDate; }
    public void setInactivatedDate(LocalDate inactivatedDate) { this.inactivatedDate = inactivatedDate; }

    public String getInactivatedBy() { return inactivatedBy; }
    public void setInactivatedBy(String inactivatedBy) { this.inactivatedBy = inactivatedBy; }

    public String getInactivationReason() { return inactivationReason; }
    public void setInactivationReason(String inactivationReason) { this.inactivationReason = inactivationReason; }

    public Double getCallbackHours() { return callbackHours; }
    public void setCallbackHours(Double callbackHours) { this.callbackHours = callbackHours; }

    public Integer getCallbackMonth() { return callbackMonth; }
    public void setCallbackMonth(Integer callbackMonth) { this.callbackMonth = callbackMonth; }

    public Integer getCallbackYear() { return callbackYear; }
    public void setCallbackYear(Integer callbackYear) { this.callbackYear = callbackYear; }

    public Boolean getCallbackProcessed() { return callbackProcessed; }
    public void setCallbackProcessed(Boolean callbackProcessed) { this.callbackProcessed = callbackProcessed; }

    public Boolean getSoc2305Received() { return soc2305Received; }
    public void setSoc2305Received(Boolean soc2305Received) { this.soc2305Received = soc2305Received; }

    public LocalDate getSoc2305Date() { return soc2305Date; }
    public void setSoc2305Date(LocalDate soc2305Date) { this.soc2305Date = soc2305Date; }

    public Boolean getSoc2303Received() { return soc2303Received; }
    public void setSoc2303Received(Boolean soc2303Received) { this.soc2303Received = soc2303Received; }

    public LocalDate getSoc2303Date() { return soc2303Date; }
    public void setSoc2303Date(LocalDate soc2303Date) { this.soc2303Date = soc2303Date; }

    public Boolean getCountyApproved() { return countyApproved; }
    public void setCountyApproved(Boolean countyApproved) { this.countyApproved = countyApproved; }

    public LocalDate getCountyApprovedDate() { return countyApprovedDate; }
    public void setCountyApprovedDate(LocalDate countyApprovedDate) { this.countyApprovedDate = countyApprovedDate; }

    public String getCountyApprovedBy() { return countyApprovedBy; }
    public void setCountyApprovedBy(String countyApprovedBy) { this.countyApprovedBy = countyApprovedBy; }

    public Boolean getCdssApproved() { return cdssApproved; }
    public void setCdssApproved(Boolean cdssApproved) { this.cdssApproved = cdssApproved; }

    public LocalDate getCdssApprovedDate() { return cdssApprovedDate; }
    public void setCdssApprovedDate(LocalDate cdssApprovedDate) { this.cdssApprovedDate = cdssApprovedDate; }

    public String getCdssApprovedBy() { return cdssApprovedBy; }
    public void setCdssApprovedBy(String cdssApprovedBy) { this.cdssApprovedBy = cdssApprovedBy; }

    public String getCdssDenialReason() { return cdssDenialReason; }
    public void setCdssDenialReason(String cdssDenialReason) { this.cdssDenialReason = cdssDenialReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
