package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Authorization Segment Entity — DSD Section 22
 *
 * Tracks date-bounded authorization periods for a case. Each segment represents
 * a discrete authorized period with specific hours and mode of service. Multiple
 * segments can exist for a case (initial, reassessment, change, etc.).
 * The ACTIVE segment drives current payroll calculations.
 */
@Entity
@Table(name = "authorization_segment", indexes = {
        @Index(name = "idx_as_case", columnList = "case_id"),
        @Index(name = "idx_as_assessment", columnList = "assessment_id"),
        @Index(name = "idx_as_status", columnList = "status"),
        @Index(name = "idx_as_dates", columnList = "segment_start_date, segment_end_date")
})
public class AuthorizationSegmentEntity {

    public enum SegmentStatus {
        ACTIVE,
        INACTIVE,
        SUPERSEDED,
        PENDING
    }

    public enum ModeOfService {
        IH,    // Individual Provider
        HM,    // Homemaker
        WPCS,  // Waiver Personal Care Services
        CFCO,  // Community First Choice Option
        IPO    // Independence Plus Option
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "assessment_id")
    private Long assessmentId;

    @Column(name = "segment_start_date", nullable = false)
    private LocalDate segmentStartDate;

    @Column(name = "segment_end_date")
    private LocalDate segmentEndDate;

    /** Total authorized hours per month for this segment */
    @Column(name = "authorized_hours_monthly")
    private Double authorizedHoursMonthly;

    /** Total authorized hours per week for this segment */
    @Column(name = "authorized_hours_weekly")
    private Double authorizedHoursWeekly;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_of_service", length = 10)
    private ModeOfService modeOfService;

    /** Funding source: IHSS, WPCS, CFCO, IPO, etc. */
    @Column(name = "funding_source", length = 20)
    private String fundingSource;

    /** County IP rate (hourly) at time of authorization */
    @Column(name = "county_ip_rate")
    private Double countyIpRate;

    /** Share of cost amount for this authorization period */
    @Column(name = "share_of_cost_amount")
    private Double shareOfCostAmount;

    /** Reason for this segment: INITIAL, REASSESSMENT, CHANGE, ICT, TELEHEALTH */
    @Column(name = "segment_reason", length = 30)
    private String segmentReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15)
    private SegmentStatus status;

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
        if (status == null) status = SegmentStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public LocalDate getSegmentStartDate() { return segmentStartDate; }
    public void setSegmentStartDate(LocalDate segmentStartDate) { this.segmentStartDate = segmentStartDate; }

    public LocalDate getSegmentEndDate() { return segmentEndDate; }
    public void setSegmentEndDate(LocalDate segmentEndDate) { this.segmentEndDate = segmentEndDate; }

    public Double getAuthorizedHoursMonthly() { return authorizedHoursMonthly; }
    public void setAuthorizedHoursMonthly(Double authorizedHoursMonthly) { this.authorizedHoursMonthly = authorizedHoursMonthly; }

    public Double getAuthorizedHoursWeekly() { return authorizedHoursWeekly; }
    public void setAuthorizedHoursWeekly(Double authorizedHoursWeekly) { this.authorizedHoursWeekly = authorizedHoursWeekly; }

    public ModeOfService getModeOfService() { return modeOfService; }
    public void setModeOfService(ModeOfService modeOfService) { this.modeOfService = modeOfService; }

    public String getFundingSource() { return fundingSource; }
    public void setFundingSource(String fundingSource) { this.fundingSource = fundingSource; }

    public Double getCountyIpRate() { return countyIpRate; }
    public void setCountyIpRate(Double countyIpRate) { this.countyIpRate = countyIpRate; }

    public Double getShareOfCostAmount() { return shareOfCostAmount; }
    public void setShareOfCostAmount(Double shareOfCostAmount) { this.shareOfCostAmount = shareOfCostAmount; }

    public String getSegmentReason() { return segmentReason; }
    public void setSegmentReason(String segmentReason) { this.segmentReason = segmentReason; }

    public SegmentStatus getStatus() { return status; }
    public void setStatus(SegmentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
