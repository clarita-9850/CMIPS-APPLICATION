package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mode of Service Entity - DSD Section 22.
 *
 * Tracks the distribution of authorized hours across delivery modes:
 * Individual Provider (IP), County Contractor (CC), and Homemaker (HM).
 * Each record represents a mode-of-service period for an authorization.
 *
 * Status codes: ACTIVE, INACTIVE, SUPERSEDED.
 */
@Entity
@Table(name = "mode_of_service", indexes = {
        @Index(name = "idx_mos_auth", columnList = "ihss_authorization_id")
})
public class ModeOfServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ihss_authorization_id", nullable = false)
    private Long ihssAuthorizationId;

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

    public Long getIhssAuthorizationId() { return ihssAuthorizationId; }
    public void setIhssAuthorizationId(Long ihssAuthorizationId) { this.ihssAuthorizationId = ihssAuthorizationId; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
