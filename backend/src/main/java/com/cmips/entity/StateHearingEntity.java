package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * State Hearing Entity — models a State Hearing (Case Appeal) record
 * Per DSD Section 20 (CI-67779) and Section 25 State Hearings
 *
 * Status is auto-calculated per BR CM 04/05/06/06a:
 *   - Requested: hearing request date exists, no scheduled date, no outcome
 *   - Scheduled: scheduled hearing date exists, no outcome
 *   - Resolved: outcome and outcome date exist
 *
 * Search State Hearing Status (SSHS) values used for search:
 *   SSHS001=Requested, SSHS002=Scheduled, SSHS003=Resolved, SSHS004=Requested And Scheduled
 */
@Entity
@Table(name = "state_hearings")
public class StateHearingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DSD: appealNumber (CHAR 20) — user-entered State Hearing Number, required on Create
    @Column(name = "appeal_number", length = 20)
    private String appealNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(name = "county_code", length = 50)
    private String countyCode;

    // BR CM 04-06a: status is auto-calculated from dates/outcome
    // Values: REQUESTED, SCHEDULED, RESOLVED
    @Column(name = "status", length = 30, nullable = false)
    private String status;

    // Hearing Request Date — when the recipient filed the hearing request
    @Column(name = "hearing_request_date", nullable = false)
    private LocalDate hearingRequestDate;

    // Scheduled Hearing Date — when the hearing is scheduled
    @Column(name = "scheduled_hearing_date")
    private LocalDate scheduledHearingDate;

    // Hearing Outcome: Granted, Denied, Withdrawn, Dismissed, Modified, Remanded, Partial Grant, Other
    @Column(name = "hearing_outcome", length = 50)
    private String hearingOutcome;

    // Outcome Date — when the outcome was determined
    @Column(name = "outcome_date")
    private LocalDate outcomeDate;

    // Rescheduled Reason: County Request, Recipient Request, ALJ Request
    @Column(name = "rescheduled_reason", length = 50)
    private String rescheduledReason;

    // Issue description — what the hearing is about
    @Column(name = "issue", length = 500)
    private String issue;

    // Notes
    @Column(name = "notes", length = 2000)
    private String notes;

    // Compliance Form Sent Date
    @Column(name = "compliance_form_sent_date")
    private LocalDate complianceFormSentDate;

    // Audit Fields
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
        recalculateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        recalculateStatus();
    }

    /**
     * Auto-calculate status per DSD BR CM 04/05/06/06a:
     *  - BR CM 06a: If outcome other than Pending (EO501) + outcomeDate exist → RESOLVED
     *  - BR CM 05/06: If scheduledHearingDate exists → SCHEDULED
     *  - BR CM 04: Otherwise → REQUESTED
     */
    public void recalculateStatus() {
        if (hearingOutcome != null && !"EO501".equals(hearingOutcome) && outcomeDate != null) {
            this.status = "RESOLVED";
        } else if (scheduledHearingDate != null) {
            this.status = "SCHEDULED";
        } else {
            this.status = "REQUESTED";
        }
    }

    /**
     * Check if this hearing is "resolved" and fields should be locked (BR CM 06a).
     * All fields except Compliance Form Sent Date are non-editable when resolved.
     */
    public boolean isResolved() {
        return "RESOLVED".equals(this.status);
    }

    /**
     * Maps the auto-calculated status to the SSHS search code
     */
    public String getSearchStatusCode() {
        return switch (status) {
            case "REQUESTED" -> "SSHS001";
            case "SCHEDULED" -> "SSHS002";
            case "RESOLVED" -> "SSHS003";
            default -> "SSHS001";
        };
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAppealNumber() { return appealNumber; }
    public void setAppealNumber(String appealNumber) { this.appealNumber = appealNumber; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getHearingRequestDate() { return hearingRequestDate; }
    public void setHearingRequestDate(LocalDate hearingRequestDate) { this.hearingRequestDate = hearingRequestDate; }

    public LocalDate getScheduledHearingDate() { return scheduledHearingDate; }
    public void setScheduledHearingDate(LocalDate scheduledHearingDate) { this.scheduledHearingDate = scheduledHearingDate; }

    public String getHearingOutcome() { return hearingOutcome; }
    public void setHearingOutcome(String hearingOutcome) { this.hearingOutcome = hearingOutcome; }

    public LocalDate getOutcomeDate() { return outcomeDate; }
    public void setOutcomeDate(LocalDate outcomeDate) { this.outcomeDate = outcomeDate; }

    public String getRescheduledReason() { return rescheduledReason; }
    public void setRescheduledReason(String rescheduledReason) { this.rescheduledReason = rescheduledReason; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getComplianceFormSentDate() { return complianceFormSentDate; }
    public void setComplianceFormSentDate(LocalDate complianceFormSentDate) { this.complianceFormSentDate = complianceFormSentDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
