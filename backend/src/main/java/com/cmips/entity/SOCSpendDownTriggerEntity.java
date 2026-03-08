package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SOC Spend Down Trigger Entity - DSD Section 22.
 *
 * Captures share-of-cost spend-down trigger events for a case/month.
 * Each trigger calculates regular and OT spend-down amounts, SOC hours,
 * available hours, and delivery mode breakdowns (IP, HM, CC). Triggers
 * can be INITIAL, RECALC, or MANUAL and are processed in batch.
 *
 * Trigger types: INITIAL, RECALC, MANUAL.
 * Status codes: PENDING, PROCESSED, ERROR.
 * Record status: ACTIVE, VOID.
 */
@Entity
@Table(name = "soc_spend_down_trigger", indexes = {
        @Index(name = "idx_ssdt_case", columnList = "case_id"),
        @Index(name = "idx_ssdt_svc_month", columnList = "service_month")
})
public class SOCSpendDownTriggerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "service_month")
    private LocalDate serviceMonth;

    @Column(name = "ihss_authorization_id")
    private Long ihssAuthorizationId;

    @Column(name = "medical_elig_info_id")
    private Long medicalEligInfoId;

    @Column(name = "ap_ind")
    private Boolean apInd;

    @Column(name = "ihss_auth_hours")
    private Integer ihssAuthHours;

    @Column(name = "ihss_spend_down_amt", precision = 10, scale = 2)
    private BigDecimal ihssSpendDownAmt;

    @Column(name = "soc_amt", precision = 10, scale = 2)
    private BigDecimal socAmt;

    @Column(name = "soc_hours")
    private Integer socHours;

    @Column(name = "available_hours")
    private Integer availableHours;

    @Column(name = "ihss_ot_hours")
    private Integer ihssOTHours;

    @Column(name = "ihss_ot_spend_down_amt", precision = 10, scale = 2)
    private BigDecimal ihssOTSpendDownAmt;

    @Column(name = "soc_ot_amt", precision = 10, scale = 2)
    private BigDecimal socOTAmt;

    @Column(name = "soc_ot_hours")
    private Integer socOTHours;

    @Column(name = "available_ot_hours")
    private Integer availableOTHours;

    @Column(name = "trigger_date")
    private LocalDate triggerDate;

    @Column(name = "trigger_type", length = 20)
    private String triggerType;

    @Column(name = "status_code", length = 20)
    private String statusCode;

    @Column(name = "batch_date")
    private LocalDate batchDate;

    @Column(name = "record_status", length = 10)
    private String recordStatus;

    @Column(name = "ip_mins")
    private Integer ipMins;

    @Column(name = "hm_mins")
    private Integer hmMins;

    @Column(name = "cc_mins")
    private Integer ccMins;

    @Column(name = "error", length = 2000)
    private String error;

    @Column(name = "is_ihssr")
    private Boolean isIHSSR;

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

    public Long getIhssAuthorizationId() { return ihssAuthorizationId; }
    public void setIhssAuthorizationId(Long ihssAuthorizationId) { this.ihssAuthorizationId = ihssAuthorizationId; }

    public Long getMedicalEligInfoId() { return medicalEligInfoId; }
    public void setMedicalEligInfoId(Long medicalEligInfoId) { this.medicalEligInfoId = medicalEligInfoId; }

    public Boolean getApInd() { return apInd; }
    public void setApInd(Boolean apInd) { this.apInd = apInd; }

    public Integer getIhssAuthHours() { return ihssAuthHours; }
    public void setIhssAuthHours(Integer ihssAuthHours) { this.ihssAuthHours = ihssAuthHours; }

    public BigDecimal getIhssSpendDownAmt() { return ihssSpendDownAmt; }
    public void setIhssSpendDownAmt(BigDecimal ihssSpendDownAmt) { this.ihssSpendDownAmt = ihssSpendDownAmt; }

    public BigDecimal getSocAmt() { return socAmt; }
    public void setSocAmt(BigDecimal socAmt) { this.socAmt = socAmt; }

    public Integer getSocHours() { return socHours; }
    public void setSocHours(Integer socHours) { this.socHours = socHours; }

    public Integer getAvailableHours() { return availableHours; }
    public void setAvailableHours(Integer availableHours) { this.availableHours = availableHours; }

    public Integer getIhssOTHours() { return ihssOTHours; }
    public void setIhssOTHours(Integer ihssOTHours) { this.ihssOTHours = ihssOTHours; }

    public BigDecimal getIhssOTSpendDownAmt() { return ihssOTSpendDownAmt; }
    public void setIhssOTSpendDownAmt(BigDecimal ihssOTSpendDownAmt) { this.ihssOTSpendDownAmt = ihssOTSpendDownAmt; }

    public BigDecimal getSocOTAmt() { return socOTAmt; }
    public void setSocOTAmt(BigDecimal socOTAmt) { this.socOTAmt = socOTAmt; }

    public Integer getSocOTHours() { return socOTHours; }
    public void setSocOTHours(Integer socOTHours) { this.socOTHours = socOTHours; }

    public Integer getAvailableOTHours() { return availableOTHours; }
    public void setAvailableOTHours(Integer availableOTHours) { this.availableOTHours = availableOTHours; }

    public LocalDate getTriggerDate() { return triggerDate; }
    public void setTriggerDate(LocalDate triggerDate) { this.triggerDate = triggerDate; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public LocalDate getBatchDate() { return batchDate; }
    public void setBatchDate(LocalDate batchDate) { this.batchDate = batchDate; }

    public String getRecordStatus() { return recordStatus; }
    public void setRecordStatus(String recordStatus) { this.recordStatus = recordStatus; }

    public Integer getIpMins() { return ipMins; }
    public void setIpMins(Integer ipMins) { this.ipMins = ipMins; }

    public Integer getHmMins() { return hmMins; }
    public void setHmMins(Integer hmMins) { this.hmMins = hmMins; }

    public Integer getCcMins() { return ccMins; }
    public void setCcMins(Integer ccMins) { this.ccMins = ccMins; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Boolean getIsIHSSR() { return isIHSSR; }
    public void setIsIHSSR(Boolean isIHSSR) { this.isIHSSR = isIHSSR; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
