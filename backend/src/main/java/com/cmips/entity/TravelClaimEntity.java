package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "travel_claims")
public class TravelClaimEntity {

    public enum TravelClaimStatus {
        PENDING_ISSUANCE,
        ISSUED,
        RECEIVED,
        HOLD_TIMESHEET_NOT_PROCESSED,
        VALIDATING,
        EXCEPTION,
        APPROVED_FOR_PAYROLL,
        SENT_TO_PAYROLL,
        PROCESSED,
        REJECTED,
        VOID
    }

    public enum ProgramType {
        IHSS,
        WPCS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "travel_claim_number", unique = true, length = 20)
    private String travelClaimNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "timesheet_id")
    private Long timesheetId; // Linked timesheet (required for payment)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40, nullable = false)
    private TravelClaimStatus status = TravelClaimStatus.PENDING_ISSUANCE;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_type", length = 10, nullable = false)
    private ProgramType programType = ProgramType.IHSS;

    // --- Pay Period ---
    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    // --- Travel Hours ---
    @Column(name = "total_travel_hours_claimed")
    private Double totalTravelHoursClaimed;

    @Column(name = "total_travel_hours_approved")
    private Double totalTravelHoursApproved;

    @Column(name = "travel_hours_cutback")
    private Double travelHoursCutback;

    // DSD: 7 hrs/week max; 14 hrs/week absolute max before OT violation
    @Column(name = "weekly_travel_hours_cap")
    private Double weeklyTravelHoursCap = 7.0;

    // --- Validation ---
    @Column(name = "has_hard_edit")
    private Boolean hasHardEdit = false;

    @Column(name = "has_soft_edit")
    private Boolean hasSoftEdit = false;

    @Column(name = "exception_count")
    private Integer exceptionCount = 0;

    @Column(name = "provider_eligible_for_travel")
    private Boolean providerEligibleForTravel;

    // --- Dates ---
    @Column(name = "date_received")
    private LocalDate dateReceived;

    @Column(name = "date_issued")
    private LocalDate dateIssued;

    @Column(name = "date_sent_to_payroll")
    private LocalDateTime dateSentToPayroll;

    @Column(name = "hold_release_date")
    private LocalDateTime holdReleaseDate;

    // --- Metadata ---
    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (travelClaimNumber == null) {
            travelClaimNumber = "TC-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTravelClaimNumber() { return travelClaimNumber; }
    public void setTravelClaimNumber(String travelClaimNumber) { this.travelClaimNumber = travelClaimNumber; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Long getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }

    public TravelClaimStatus getStatus() { return status; }
    public void setStatus(TravelClaimStatus status) { this.status = status; }

    public ProgramType getProgramType() { return programType; }
    public void setProgramType(ProgramType programType) { this.programType = programType; }

    public LocalDate getPayPeriodStart() { return payPeriodStart; }
    public void setPayPeriodStart(LocalDate payPeriodStart) { this.payPeriodStart = payPeriodStart; }

    public LocalDate getPayPeriodEnd() { return payPeriodEnd; }
    public void setPayPeriodEnd(LocalDate payPeriodEnd) { this.payPeriodEnd = payPeriodEnd; }

    public Double getTotalTravelHoursClaimed() { return totalTravelHoursClaimed; }
    public void setTotalTravelHoursClaimed(Double totalTravelHoursClaimed) { this.totalTravelHoursClaimed = totalTravelHoursClaimed; }

    public Double getTotalTravelHoursApproved() { return totalTravelHoursApproved; }
    public void setTotalTravelHoursApproved(Double totalTravelHoursApproved) { this.totalTravelHoursApproved = totalTravelHoursApproved; }

    public Double getTravelHoursCutback() { return travelHoursCutback; }
    public void setTravelHoursCutback(Double travelHoursCutback) { this.travelHoursCutback = travelHoursCutback; }

    public Double getWeeklyTravelHoursCap() { return weeklyTravelHoursCap; }
    public void setWeeklyTravelHoursCap(Double weeklyTravelHoursCap) { this.weeklyTravelHoursCap = weeklyTravelHoursCap; }

    public Boolean getHasHardEdit() { return hasHardEdit; }
    public void setHasHardEdit(Boolean hasHardEdit) { this.hasHardEdit = hasHardEdit; }

    public Boolean getHasSoftEdit() { return hasSoftEdit; }
    public void setHasSoftEdit(Boolean hasSoftEdit) { this.hasSoftEdit = hasSoftEdit; }

    public Integer getExceptionCount() { return exceptionCount; }
    public void setExceptionCount(Integer exceptionCount) { this.exceptionCount = exceptionCount; }

    public Boolean getProviderEligibleForTravel() { return providerEligibleForTravel; }
    public void setProviderEligibleForTravel(Boolean providerEligibleForTravel) { this.providerEligibleForTravel = providerEligibleForTravel; }

    public LocalDate getDateReceived() { return dateReceived; }
    public void setDateReceived(LocalDate dateReceived) { this.dateReceived = dateReceived; }

    public LocalDate getDateIssued() { return dateIssued; }
    public void setDateIssued(LocalDate dateIssued) { this.dateIssued = dateIssued; }

    public LocalDateTime getDateSentToPayroll() { return dateSentToPayroll; }
    public void setDateSentToPayroll(LocalDateTime dateSentToPayroll) { this.dateSentToPayroll = dateSentToPayroll; }

    public LocalDateTime getHoldReleaseDate() { return holdReleaseDate; }
    public void setHoldReleaseDate(LocalDateTime holdReleaseDate) { this.holdReleaseDate = holdReleaseDate; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
