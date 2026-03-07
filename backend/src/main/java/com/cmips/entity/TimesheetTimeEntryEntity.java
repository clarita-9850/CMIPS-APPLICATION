package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "timesheet_time_entries")
public class TimesheetTimeEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timesheet_id", nullable = false)
    private Long timesheetId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "day_of_period")
    private Integer dayOfPeriod; // 1-16 (pay period day number)

    @Column(name = "hours_claimed")
    private Double hoursClaimed;

    @Column(name = "minutes_claimed")
    private Integer minutesClaimed;

    @Column(name = "hours_approved")
    private Double hoursApproved;

    @Column(name = "hours_cutback")
    private Double hoursCutback;

    @Column(name = "cutback_reason", length = 500)
    private String cutbackReason;

    // Eligibility flags per day
    @Column(name = "recipient_eligible")
    private Boolean recipientEligible = true;

    @Column(name = "provider_eligible")
    private Boolean providerEligible = true;

    @Column(name = "recipient_on_leave")
    private Boolean recipientOnLeave = false;

    @Column(name = "provider_on_leave")
    private Boolean providerOnLeave = false;

    @Column(name = "is_future_day")
    private Boolean isFutureDay = false;

    @Column(name = "is_weekend")
    private Boolean isWeekend = false;

    // Week tracking for FLSA overtime
    @Column(name = "work_week_number")
    private Integer workWeekNumber; // 1, 2, or 3 within pay period

    @Column(name = "day_of_week")
    private Integer dayOfWeek; // 1=Sun, 7=Sat

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (hoursApproved == null && hoursClaimed != null) {
            hoursApproved = hoursClaimed;
        }
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public Integer getDayOfPeriod() { return dayOfPeriod; }
    public void setDayOfPeriod(Integer dayOfPeriod) { this.dayOfPeriod = dayOfPeriod; }

    public Double getHoursClaimed() { return hoursClaimed; }
    public void setHoursClaimed(Double hoursClaimed) { this.hoursClaimed = hoursClaimed; }

    public Integer getMinutesClaimed() { return minutesClaimed; }
    public void setMinutesClaimed(Integer minutesClaimed) { this.minutesClaimed = minutesClaimed; }

    public Double getHoursApproved() { return hoursApproved; }
    public void setHoursApproved(Double hoursApproved) { this.hoursApproved = hoursApproved; }

    public Double getHoursCutback() { return hoursCutback; }
    public void setHoursCutback(Double hoursCutback) { this.hoursCutback = hoursCutback; }

    public String getCutbackReason() { return cutbackReason; }
    public void setCutbackReason(String cutbackReason) { this.cutbackReason = cutbackReason; }

    public Boolean getRecipientEligible() { return recipientEligible; }
    public void setRecipientEligible(Boolean recipientEligible) { this.recipientEligible = recipientEligible; }

    public Boolean getProviderEligible() { return providerEligible; }
    public void setProviderEligible(Boolean providerEligible) { this.providerEligible = providerEligible; }

    public Boolean getRecipientOnLeave() { return recipientOnLeave; }
    public void setRecipientOnLeave(Boolean recipientOnLeave) { this.recipientOnLeave = recipientOnLeave; }

    public Boolean getProviderOnLeave() { return providerOnLeave; }
    public void setProviderOnLeave(Boolean providerOnLeave) { this.providerOnLeave = providerOnLeave; }

    public Boolean getIsFutureDay() { return isFutureDay; }
    public void setIsFutureDay(Boolean isFutureDay) { this.isFutureDay = isFutureDay; }

    public Boolean getIsWeekend() { return isWeekend; }
    public void setIsWeekend(Boolean isWeekend) { this.isWeekend = isWeekend; }

    public Integer getWorkWeekNumber() { return workWeekNumber; }
    public void setWorkWeekNumber(Integer workWeekNumber) { this.workWeekNumber = workWeekNumber; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
