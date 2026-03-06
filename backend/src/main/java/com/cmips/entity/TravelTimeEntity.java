package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Travel Time Entity - DSD Section 23 (CI-480867)
 *
 * Tracks travel time agreements for providers who serve multiple recipients.
 * The 7-hour/week rule: seven hours of travel time per week inclusive of
 * travel duration across recipients/programs for IHSS and WPCS.
 *
 * Key rules:
 * - Travel time counts toward overtime calculation (7-hour rule)
 * - "Traveling From" recipient must be specified
 * - Multiple travel time records per provider allowed (one per recipient pair)
 * - Inactivatable with history preserved
 */
@Entity
@Table(name = "travel_times")
public class TravelTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    // The recipient the provider is traveling TO
    @Column(name = "to_recipient_id")
    private Long toRecipientId;

    @Column(name = "to_recipient_name", length = 200)
    private String toRecipientName;

    @Column(name = "to_case_number", length = 20)
    private String toCaseNumber;

    // The recipient the provider is traveling FROM (CI-480867: "Select Traveling From Recipient")
    @Column(name = "from_recipient_id")
    private Long fromRecipientId;

    @Column(name = "from_recipient_name", length = 200)
    private String fromRecipientName;

    @Column(name = "from_case_number", length = 20)
    private String fromCaseNumber;

    // Travel time details
    @Column(name = "travel_hours_weekly")
    // Weekly travel hours between these two recipients
    private Double travelHoursWeekly;

    @Column(name = "travel_minutes")
    // One-way travel time in minutes
    private Integer travelMinutes;

    @Column(name = "begin_date", nullable = false)
    private LocalDate beginDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TravelTimeStatus status;

    // Program type (affects 7-hour rule calculation)
    @Column(name = "program_type", length = 10)
    // Values: IHSS, WPCS
    private String programType;

    // Inactivation tracking
    @Column(name = "inactivated_date")
    private LocalDate inactivatedDate;

    @Column(name = "inactivated_by", length = 100)
    private String inactivatedBy;

    // Audit
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public TravelTimeEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = TravelTimeStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == TravelTimeStatus.ACTIVE;
    }

    public enum TravelTimeStatus {
        ACTIVE, INACTIVE
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Long getToRecipientId() { return toRecipientId; }
    public void setToRecipientId(Long toRecipientId) { this.toRecipientId = toRecipientId; }

    public String getToRecipientName() { return toRecipientName; }
    public void setToRecipientName(String toRecipientName) { this.toRecipientName = toRecipientName; }

    public String getToCaseNumber() { return toCaseNumber; }
    public void setToCaseNumber(String toCaseNumber) { this.toCaseNumber = toCaseNumber; }

    public Long getFromRecipientId() { return fromRecipientId; }
    public void setFromRecipientId(Long fromRecipientId) { this.fromRecipientId = fromRecipientId; }

    public String getFromRecipientName() { return fromRecipientName; }
    public void setFromRecipientName(String fromRecipientName) { this.fromRecipientName = fromRecipientName; }

    public String getFromCaseNumber() { return fromCaseNumber; }
    public void setFromCaseNumber(String fromCaseNumber) { this.fromCaseNumber = fromCaseNumber; }

    public Double getTravelHoursWeekly() { return travelHoursWeekly; }
    public void setTravelHoursWeekly(Double travelHoursWeekly) { this.travelHoursWeekly = travelHoursWeekly; }

    public Integer getTravelMinutes() { return travelMinutes; }
    public void setTravelMinutes(Integer travelMinutes) { this.travelMinutes = travelMinutes; }

    public LocalDate getBeginDate() { return beginDate; }
    public void setBeginDate(LocalDate beginDate) { this.beginDate = beginDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public TravelTimeStatus getStatus() { return status; }
    public void setStatus(TravelTimeStatus status) { this.status = status; }

    public String getProgramType() { return programType; }
    public void setProgramType(String programType) { this.programType = programType; }

    public LocalDate getInactivatedDate() { return inactivatedDate; }
    public void setInactivatedDate(LocalDate inactivatedDate) { this.inactivatedDate = inactivatedDate; }

    public String getInactivatedBy() { return inactivatedBy; }
    public void setInactivatedBy(String inactivatedBy) { this.inactivatedBy = inactivatedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
