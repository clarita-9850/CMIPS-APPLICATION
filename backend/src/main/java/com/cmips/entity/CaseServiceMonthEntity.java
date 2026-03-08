package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Case Service Month Entity - DSD Section 22.
 *
 * Tracks monthly authorization and remaining hours for a case across all
 * delivery modes (IP, CC, HM) and WPCS. Each row represents one calendar
 * month (serviceMonth = first of month). Includes weekly cap, OT max,
 * SOC collected, and the count of active providers for the month.
 *
 * Status codes: OPEN, CLOSED, ADJUSTED.
 */
@Entity
@Table(name = "case_service_month", indexes = {
        @Index(name = "idx_csm_case", columnList = "case_id"),
        @Index(name = "idx_csm_svc_month", columnList = "service_month")
})
public class CaseServiceMonthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "service_month")
    private LocalDate serviceMonth;

    @Column(name = "auth_to_purchase_min")
    private Integer authToPurchaseMin;

    @Column(name = "ip_remaining_min")
    private Integer ipRemainingMin;

    @Column(name = "cc_remaining_min")
    private Integer ccRemainingMin;

    @Column(name = "hm_remaining_min")
    private Integer hmRemainingMin;

    @Column(name = "auth_to_purchase_remain_min")
    private Integer authToPurchaseRemainMin;

    @Column(name = "ihss_soc_collected_amt", precision = 10, scale = 2)
    private BigDecimal ihssSocCollectedAmt;

    @Column(name = "wpcs_auth_min")
    private Integer wpcsAuthMin;

    @Column(name = "wpcs_remaining_min")
    private Integer wpcsRemainingMin;

    @Column(name = "status_code", length = 20)
    private String statusCode;

    @Column(name = "weekly_cap_min")
    private Integer weeklyCapMin;

    @Column(name = "ot_max_min")
    private Integer otMaxMin;

    @Column(name = "active_providers")
    private Integer activeProviders;

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

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public LocalDate getServiceMonth() { return serviceMonth; }
    public void setServiceMonth(LocalDate serviceMonth) { this.serviceMonth = serviceMonth; }

    public Integer getAuthToPurchaseMin() { return authToPurchaseMin; }
    public void setAuthToPurchaseMin(Integer authToPurchaseMin) { this.authToPurchaseMin = authToPurchaseMin; }

    public Integer getIpRemainingMin() { return ipRemainingMin; }
    public void setIpRemainingMin(Integer ipRemainingMin) { this.ipRemainingMin = ipRemainingMin; }

    public Integer getCcRemainingMin() { return ccRemainingMin; }
    public void setCcRemainingMin(Integer ccRemainingMin) { this.ccRemainingMin = ccRemainingMin; }

    public Integer getHmRemainingMin() { return hmRemainingMin; }
    public void setHmRemainingMin(Integer hmRemainingMin) { this.hmRemainingMin = hmRemainingMin; }

    public Integer getAuthToPurchaseRemainMin() { return authToPurchaseRemainMin; }
    public void setAuthToPurchaseRemainMin(Integer authToPurchaseRemainMin) { this.authToPurchaseRemainMin = authToPurchaseRemainMin; }

    public BigDecimal getIhssSocCollectedAmt() { return ihssSocCollectedAmt; }
    public void setIhssSocCollectedAmt(BigDecimal ihssSocCollectedAmt) { this.ihssSocCollectedAmt = ihssSocCollectedAmt; }

    public Integer getWpcsAuthMin() { return wpcsAuthMin; }
    public void setWpcsAuthMin(Integer wpcsAuthMin) { this.wpcsAuthMin = wpcsAuthMin; }

    public Integer getWpcsRemainingMin() { return wpcsRemainingMin; }
    public void setWpcsRemainingMin(Integer wpcsRemainingMin) { this.wpcsRemainingMin = wpcsRemainingMin; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public Integer getWeeklyCapMin() { return weeklyCapMin; }
    public void setWeeklyCapMin(Integer weeklyCapMin) { this.weeklyCapMin = weeklyCapMin; }

    public Integer getOtMaxMin() { return otMaxMin; }
    public void setOtMaxMin(Integer otMaxMin) { this.otMaxMin = otMaxMin; }

    public Integer getActiveProviders() { return activeProviders; }
    public void setActiveProviders(Integer activeProviders) { this.activeProviders = activeProviders; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
