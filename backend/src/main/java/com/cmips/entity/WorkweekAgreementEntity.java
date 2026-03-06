package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Workweek Agreement Entity - DSD Section 23 (CI-480910)
 *
 * Documents the Provider Workweek Agreement where a provider agrees to a specific
 * workweek schedule across one or more recipients. Required for providers serving
 * multiple recipients under FLSA (Fair Labor Standards Act) post-2015 rules.
 *
 * Key rules (BR PVM 73-76):
 * - One active workweek agreement per provider at a time
 * - Effective immediately when saved
 * - Inactivated when provider terminates or new agreement created
 * - Only Active status records displayed on main screen
 * - Inactive history available through "View Inactive Provider Workweek Agreement History"
 */
@Entity
@Table(name = "workweek_agreements")
public class WorkweekAgreementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    // The recipient this workweek agreement links (can be primary or secondary)
    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(name = "case_number", length = 20)
    private String caseNumber;

    // Workweek schedule: FLSA-defined 7-consecutive-day period
    @Column(name = "workweek_start_day", length = 20)
    // Values: SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    private String workweekStartDay;

    @Column(name = "agreed_hours_weekly")
    // Total agreed hours per FLSA workweek across all recipients
    private Double agreedHoursWeekly;

    @Column(name = "begin_date", nullable = false)
    private LocalDate beginDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AgreementStatus status;

    // Travel time connection (BR PVM 67-70)
    @Column(name = "includes_travel_time")
    private Boolean includesTravelTime;

    @Column(name = "travel_hours_weekly")
    private Double travelHoursWeekly;

    // Inactivation tracking
    @Column(name = "inactivated_date")
    private LocalDate inactivatedDate;

    @Column(name = "inactivated_by", length = 100)
    private String inactivatedBy;

    @Column(name = "inactivation_reason", length = 500)
    private String inactivationReason;

    // Audit
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public WorkweekAgreementEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = AgreementStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == AgreementStatus.ACTIVE;
    }

    public enum AgreementStatus {
        ACTIVE, INACTIVE
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getWorkweekStartDay() { return workweekStartDay; }
    public void setWorkweekStartDay(String workweekStartDay) { this.workweekStartDay = workweekStartDay; }

    public Double getAgreedHoursWeekly() { return agreedHoursWeekly; }
    public void setAgreedHoursWeekly(Double agreedHoursWeekly) { this.agreedHoursWeekly = agreedHoursWeekly; }

    public LocalDate getBeginDate() { return beginDate; }
    public void setBeginDate(LocalDate beginDate) { this.beginDate = beginDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public AgreementStatus getStatus() { return status; }
    public void setStatus(AgreementStatus status) { this.status = status; }

    public Boolean getIncludesTravelTime() { return includesTravelTime; }
    public void setIncludesTravelTime(Boolean includesTravelTime) { this.includesTravelTime = includesTravelTime; }

    public Double getTravelHoursWeekly() { return travelHoursWeekly; }
    public void setTravelHoursWeekly(Double travelHoursWeekly) { this.travelHoursWeekly = travelHoursWeekly; }

    public LocalDate getInactivatedDate() { return inactivatedDate; }
    public void setInactivatedDate(LocalDate inactivatedDate) { this.inactivatedDate = inactivatedDate; }

    public String getInactivatedBy() { return inactivatedBy; }
    public void setInactivatedBy(String inactivatedBy) { this.inactivatedBy = inactivatedBy; }

    public String getInactivationReason() { return inactivationReason; }
    public void setInactivationReason(String inactivationReason) { this.inactivationReason = inactivationReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
