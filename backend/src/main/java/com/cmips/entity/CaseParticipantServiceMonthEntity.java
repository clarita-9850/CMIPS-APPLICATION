package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Case Participant Service Month Entity - DSD Section 22.
 *
 * Tracks per-provider monthly hour allocations and remaining balances within
 * a CaseServiceMonth. Each row represents one provider's allocation for a
 * given service month, including their weekly cap, OT max, WPCS remaining,
 * and cross-case totals (activeCases, caseWeeklyCap, caseOTMax).
 */
@Entity
@Table(name = "case_participant_service_month", indexes = {
        @Index(name = "idx_cpsm_csm", columnList = "case_service_month_id")
})
public class CaseParticipantServiceMonthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_service_month_id", nullable = false)
    private Long caseServiceMonthId;

    @Column(name = "case_participant_role_id")
    private Long caseParticipantRoleId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "remaining_min")
    private Integer remainingMin;

    @Column(name = "status_code", length = 20)
    private String statusCode;

    @Column(name = "wpcs_remaining_min")
    private Integer wpcsRemainingMin;

    @Column(name = "weekly_cap_min")
    private Integer weeklyCapMin;

    @Column(name = "ot_max_min")
    private Integer otMaxMin;

    @Column(name = "case_weekly_cap")
    private Integer caseWeeklyCap;

    @Column(name = "case_ot_max")
    private Integer caseOTMax;

    @Column(name = "un_paid_adv_pay_ot")
    private Integer unPaidAdvPayOT;

    @Column(name = "active_cases")
    private Integer activeCases;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseServiceMonthId() { return caseServiceMonthId; }
    public void setCaseServiceMonthId(Long caseServiceMonthId) { this.caseServiceMonthId = caseServiceMonthId; }

    public Long getCaseParticipantRoleId() { return caseParticipantRoleId; }
    public void setCaseParticipantRoleId(Long caseParticipantRoleId) { this.caseParticipantRoleId = caseParticipantRoleId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Integer getRemainingMin() { return remainingMin; }
    public void setRemainingMin(Integer remainingMin) { this.remainingMin = remainingMin; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public Integer getWpcsRemainingMin() { return wpcsRemainingMin; }
    public void setWpcsRemainingMin(Integer wpcsRemainingMin) { this.wpcsRemainingMin = wpcsRemainingMin; }

    public Integer getWeeklyCapMin() { return weeklyCapMin; }
    public void setWeeklyCapMin(Integer weeklyCapMin) { this.weeklyCapMin = weeklyCapMin; }

    public Integer getOtMaxMin() { return otMaxMin; }
    public void setOtMaxMin(Integer otMaxMin) { this.otMaxMin = otMaxMin; }

    public Integer getCaseWeeklyCap() { return caseWeeklyCap; }
    public void setCaseWeeklyCap(Integer caseWeeklyCap) { this.caseWeeklyCap = caseWeeklyCap; }

    public Integer getCaseOTMax() { return caseOTMax; }
    public void setCaseOTMax(Integer caseOTMax) { this.caseOTMax = caseOTMax; }

    public Integer getUnPaidAdvPayOT() { return unPaidAdvPayOT; }
    public void setUnPaidAdvPayOT(Integer unPaidAdvPayOT) { this.unPaidAdvPayOT = unPaidAdvPayOT; }

    public Integer getActiveCases() { return activeCases; }
    public void setActiveCases(Integer activeCases) { this.activeCases = activeCases; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
