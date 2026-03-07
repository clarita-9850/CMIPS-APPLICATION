package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DSD Section 24 — BVI (Blind/Visually Impaired) Review Entity
 * TVP Rules 62, 63, 64, 74.
 * When a BVI recipient has registered with TTS (Telephone Timesheet System),
 * unsigned timesheets go to BVI queue for electronic review instead of hard edit.
 */
@Entity
@Table(name = "bvi_reviews", indexes = {
        @Index(name = "idx_bvi_ts", columnList = "timesheet_id"),
        @Index(name = "idx_bvi_recipient", columnList = "recipient_id"),
        @Index(name = "idx_bvi_status", columnList = "status")
})
public class BviReviewEntity {

    public enum BviReviewStatus {
        PENDING_RECIPIENT_REVIEW,   // Waiting for TTS electronic confirmation
        APPROVED_BY_TTS,            // TTS confirmed — process for payment
        REJECTED_BY_TTS,            // TTS rejected — hard edit exception
        EXPIRED,                    // 10 business day auto-expire
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_number", length = 30, unique = true)
    private String reviewNumber;

    @Column(name = "timesheet_id", nullable = false)
    private Long timesheetId;

    @Column(name = "timesheet_number", length = 20)
    private String timesheetNumber;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "pay_period_start")
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end")
    private LocalDate payPeriodEnd;

    @Column(name = "total_hours_claimed")
    private Double totalHoursClaimed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40, nullable = false)
    private BviReviewStatus status = BviReviewStatus.PENDING_RECIPIENT_REVIEW;

    @Column(name = "tts_registered")
    private Boolean ttsRegistered = false;

    @Column(name = "tts_confirmation_date")
    private LocalDate ttsConfirmationDate;

    @Column(name = "tts_confirmation_code", length = 30)
    private String ttsConfirmationCode;

    @Column(name = "tts_rejection_reason", length = 500)
    private String ttsRejectionReason;

    @Column(name = "review_deadline")
    private LocalDate reviewDeadline; // 10 business days from creation

    @Column(name = "early_submission")
    private Boolean earlySubmission = false;

    @Column(name = "late_submission")
    private Boolean lateSubmission = false;

    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (reviewNumber == null) {
            reviewNumber = "BVI-" + System.currentTimeMillis();
        }
        if (reviewDeadline == null) {
            reviewDeadline = LocalDate.now().plusDays(14); // ~10 business days
        }
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReviewNumber() { return reviewNumber; }
    public void setReviewNumber(String reviewNumber) { this.reviewNumber = reviewNumber; }

    public Long getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }

    public String getTimesheetNumber() { return timesheetNumber; }
    public void setTimesheetNumber(String timesheetNumber) { this.timesheetNumber = timesheetNumber; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public LocalDate getPayPeriodStart() { return payPeriodStart; }
    public void setPayPeriodStart(LocalDate payPeriodStart) { this.payPeriodStart = payPeriodStart; }

    public LocalDate getPayPeriodEnd() { return payPeriodEnd; }
    public void setPayPeriodEnd(LocalDate payPeriodEnd) { this.payPeriodEnd = payPeriodEnd; }

    public Double getTotalHoursClaimed() { return totalHoursClaimed; }
    public void setTotalHoursClaimed(Double totalHoursClaimed) { this.totalHoursClaimed = totalHoursClaimed; }

    public BviReviewStatus getStatus() { return status; }
    public void setStatus(BviReviewStatus status) { this.status = status; }

    public Boolean getTtsRegistered() { return ttsRegistered; }
    public void setTtsRegistered(Boolean ttsRegistered) { this.ttsRegistered = ttsRegistered; }

    public LocalDate getTtsConfirmationDate() { return ttsConfirmationDate; }
    public void setTtsConfirmationDate(LocalDate ttsConfirmationDate) { this.ttsConfirmationDate = ttsConfirmationDate; }

    public String getTtsConfirmationCode() { return ttsConfirmationCode; }
    public void setTtsConfirmationCode(String ttsConfirmationCode) { this.ttsConfirmationCode = ttsConfirmationCode; }

    public String getTtsRejectionReason() { return ttsRejectionReason; }
    public void setTtsRejectionReason(String ttsRejectionReason) { this.ttsRejectionReason = ttsRejectionReason; }

    public LocalDate getReviewDeadline() { return reviewDeadline; }
    public void setReviewDeadline(LocalDate reviewDeadline) { this.reviewDeadline = reviewDeadline; }

    public Boolean getEarlySubmission() { return earlySubmission; }
    public void setEarlySubmission(Boolean earlySubmission) { this.earlySubmission = earlySubmission; }

    public Boolean getLateSubmission() { return lateSubmission; }
    public void setLateSubmission(Boolean lateSubmission) { this.lateSubmission = lateSubmission; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
