package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "travel_claim_time_entries")
public class TravelClaimTimeEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "travel_claim_id", nullable = false)
    private Long travelClaimId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "travel_hours_claimed")
    private Double travelHoursClaimed;

    @Column(name = "travel_hours_approved")
    private Double travelHoursApproved;

    @Column(name = "travel_hours_cutback")
    private Double travelHoursCutback;

    @Column(name = "cutback_reason", length = 500)
    private String cutbackReason;

    @Column(name = "has_paid_service_hours")
    private Boolean hasPaidServiceHours; // DSD: must have paid service hours same day

    @Column(name = "has_active_travel_record")
    private Boolean hasActiveTravelRecord; // DSD: must have active travel time record

    @Column(name = "work_week_number")
    private Integer workWeekNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (travelHoursApproved == null && travelHoursClaimed != null) {
            travelHoursApproved = travelHoursClaimed;
        }
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTravelClaimId() { return travelClaimId; }
    public void setTravelClaimId(Long travelClaimId) { this.travelClaimId = travelClaimId; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public Double getTravelHoursClaimed() { return travelHoursClaimed; }
    public void setTravelHoursClaimed(Double travelHoursClaimed) { this.travelHoursClaimed = travelHoursClaimed; }

    public Double getTravelHoursApproved() { return travelHoursApproved; }
    public void setTravelHoursApproved(Double travelHoursApproved) { this.travelHoursApproved = travelHoursApproved; }

    public Double getTravelHoursCutback() { return travelHoursCutback; }
    public void setTravelHoursCutback(Double travelHoursCutback) { this.travelHoursCutback = travelHoursCutback; }

    public String getCutbackReason() { return cutbackReason; }
    public void setCutbackReason(String cutbackReason) { this.cutbackReason = cutbackReason; }

    public Boolean getHasPaidServiceHours() { return hasPaidServiceHours; }
    public void setHasPaidServiceHours(Boolean hasPaidServiceHours) { this.hasPaidServiceHours = hasPaidServiceHours; }

    public Boolean getHasActiveTravelRecord() { return hasActiveTravelRecord; }
    public void setHasActiveTravelRecord(Boolean hasActiveTravelRecord) { this.hasActiveTravelRecord = hasActiveTravelRecord; }

    public Integer getWorkWeekNumber() { return workWeekNumber; }
    public void setWorkWeekNumber(Integer workWeekNumber) { this.workWeekNumber = workWeekNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
