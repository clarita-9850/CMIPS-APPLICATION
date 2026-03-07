package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DSD Section 24 — EVV Exception Approval Entity
 * Electronic Visit Verification exceptions when EVV data is unavailable
 * or does not match claimed hours. Requires county approval workflow.
 */
@Entity
@Table(name = "evv_exceptions", indexes = {
        @Index(name = "idx_evv_exc_ts", columnList = "timesheet_id"),
        @Index(name = "idx_evv_exc_provider", columnList = "provider_id"),
        @Index(name = "idx_evv_exc_status", columnList = "status")
})
public class EvvExceptionEntity {

    public enum EvvExceptionStatus {
        PENDING_REVIEW,
        APPROVED,
        DENIED,
        EXPIRED,
        CANCELLED
    }

    public enum EvvExceptionReason {
        SYSTEM_MALFUNCTION,        // EVV system was down
        PHONE_ISSUE,               // Provider phone not working
        NO_CELL_SIGNAL,            // No cell service at recipient location
        POWER_OUTAGE,              // Power outage at recipient home
        RECIPIENT_PHONE_ISSUE,     // Recipient landline not working (for telephony)
        FORGOT_TO_CLOCK,           // Provider forgot to clock in/out
        EMERGENCY,                 // Emergency situation prevented EVV use
        OTHER                      // Other reason (requires description)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exception_number", length = 30, unique = true)
    private String exceptionNumber;

    @Column(name = "timesheet_id")
    private Long timesheetId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "hours_claimed")
    private Double hoursClaimed;

    @Column(name = "evv_hours_recorded")
    private Double evvHoursRecorded;

    @Column(name = "hours_discrepancy")
    private Double hoursDiscrepancy;

    @Enumerated(EnumType.STRING)
    @Column(name = "exception_reason", length = 40)
    private EvvExceptionReason exceptionReason;

    @Column(name = "reason_description", length = 1000)
    private String reasonDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private EvvExceptionStatus status = EvvExceptionStatus.PENDING_REVIEW;

    @Column(name = "submitted_date")
    private LocalDate submittedDate;

    @Column(name = "reviewed_date")
    private LocalDate reviewedDate;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @Column(name = "denial_reason", length = 500)
    private String denialReason;

    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "soc_2261evv_generated")
    private Boolean soc2261evvGenerated = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (exceptionNumber == null) {
            exceptionNumber = "EVV-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExceptionNumber() { return exceptionNumber; }
    public void setExceptionNumber(String exceptionNumber) { this.exceptionNumber = exceptionNumber; }

    public Long getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }

    public Double getHoursClaimed() { return hoursClaimed; }
    public void setHoursClaimed(Double hoursClaimed) { this.hoursClaimed = hoursClaimed; }

    public Double getEvvHoursRecorded() { return evvHoursRecorded; }
    public void setEvvHoursRecorded(Double evvHoursRecorded) { this.evvHoursRecorded = evvHoursRecorded; }

    public Double getHoursDiscrepancy() { return hoursDiscrepancy; }
    public void setHoursDiscrepancy(Double hoursDiscrepancy) { this.hoursDiscrepancy = hoursDiscrepancy; }

    public EvvExceptionReason getExceptionReason() { return exceptionReason; }
    public void setExceptionReason(EvvExceptionReason exceptionReason) { this.exceptionReason = exceptionReason; }

    public String getReasonDescription() { return reasonDescription; }
    public void setReasonDescription(String reasonDescription) { this.reasonDescription = reasonDescription; }

    public EvvExceptionStatus getStatus() { return status; }
    public void setStatus(EvvExceptionStatus status) { this.status = status; }

    public LocalDate getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(LocalDate submittedDate) { this.submittedDate = submittedDate; }

    public LocalDate getReviewedDate() { return reviewedDate; }
    public void setReviewedDate(LocalDate reviewedDate) { this.reviewedDate = reviewedDate; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public String getDenialReason() { return denialReason; }
    public void setDenialReason(String denialReason) { this.denialReason = denialReason; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public Boolean getSoc2261evvGenerated() { return soc2261evvGenerated; }
    public void setSoc2261evvGenerated(Boolean soc2261evvGenerated) { this.soc2261evvGenerated = soc2261evvGenerated; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
