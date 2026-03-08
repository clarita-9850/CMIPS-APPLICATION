package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mode of Service Snapshot Entity - DSD Section 22.
 *
 * Immutable point-in-time snapshot of a ModeOfServiceEntity record.
 * Created whenever a mode-of-service is superseded, voided, or adjusted
 * to preserve audit history. Only has createdAt/createdBy (no updates).
 */
@Entity
@Table(name = "mode_of_service_snapshot", indexes = {
        @Index(name = "idx_mos_snap_mos", columnList = "mode_of_service_id")
})
public class ModeOfServiceSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mode_of_service_id", nullable = false)
    private Long modeOfServiceId;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "ip_hours_min")
    private Integer ipHoursMin;

    @Column(name = "cc_hours_min")
    private Integer ccHoursMin;

    @Column(name = "hm_hours_min")
    private Integer hmHoursMin;

    @Column(name = "mode_of_service_start_date")
    private LocalDate modeOfServiceStartDate;

    @Column(name = "mode_of_service_end_date")
    private LocalDate modeOfServiceEndDate;

    @Column(name = "status_code", length = 20)
    private String statusCode;

    @Column(name = "case_cost", precision = 10, scale = 2)
    private BigDecimal caseCost;

    @Column(name = "snapshot_date")
    private LocalDateTime snapshotDate;

    @Column(name = "snapshot_reason", length = 100)
    private String snapshotReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getModeOfServiceId() { return modeOfServiceId; }
    public void setModeOfServiceId(Long modeOfServiceId) { this.modeOfServiceId = modeOfServiceId; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Integer getIpHoursMin() { return ipHoursMin; }
    public void setIpHoursMin(Integer ipHoursMin) { this.ipHoursMin = ipHoursMin; }

    public Integer getCcHoursMin() { return ccHoursMin; }
    public void setCcHoursMin(Integer ccHoursMin) { this.ccHoursMin = ccHoursMin; }

    public Integer getHmHoursMin() { return hmHoursMin; }
    public void setHmHoursMin(Integer hmHoursMin) { this.hmHoursMin = hmHoursMin; }

    public LocalDate getModeOfServiceStartDate() { return modeOfServiceStartDate; }
    public void setModeOfServiceStartDate(LocalDate modeOfServiceStartDate) { this.modeOfServiceStartDate = modeOfServiceStartDate; }

    public LocalDate getModeOfServiceEndDate() { return modeOfServiceEndDate; }
    public void setModeOfServiceEndDate(LocalDate modeOfServiceEndDate) { this.modeOfServiceEndDate = modeOfServiceEndDate; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public BigDecimal getCaseCost() { return caseCost; }
    public void setCaseCost(BigDecimal caseCost) { this.caseCost = caseCost; }

    public LocalDateTime getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDateTime snapshotDate) { this.snapshotDate = snapshotDate; }

    public String getSnapshotReason() { return snapshotReason; }
    public void setSnapshotReason(String snapshotReason) { this.snapshotReason = snapshotReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
