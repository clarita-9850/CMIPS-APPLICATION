package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CaseAppealHearingHistory — tracks previously scheduled hearing dates
 * Per DSD Section 25 Database Entities (p.690-691):
 *   - caseAppealHearingHistoryID (PK, BIGINT)
 *   - appealId (FK → state_hearings.id)
 *   - hearingDate (DATE)
 *   - rescheduledReason (CHAR 50)
 *
 * When a Scheduled Hearing Date is changed on Modify, the old date + reason
 * is captured here. Displayed in the "Previously Scheduled Hearings" section
 * on the View State Hearing screen.
 */
@Entity
@Table(name = "case_appeal_hearing_history")
public class CaseAppealHearingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appeal_id", nullable = false)
    private Long appealId;

    @Column(name = "hearing_date", nullable = false)
    private LocalDate hearingDate;

    @Column(name = "rescheduled_reason", length = 50)
    private String rescheduledReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAppealId() { return appealId; }
    public void setAppealId(Long appealId) { this.appealId = appealId; }

    public LocalDate getHearingDate() { return hearingDate; }
    public void setHearingDate(LocalDate hearingDate) { this.hearingDate = hearingDate; }

    public String getRescheduledReason() { return rescheduledReason; }
    public void setRescheduledReason(String rescheduledReason) { this.rescheduledReason = rescheduledReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
