package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Case SOC Hours Entity - DSD Section 22.
 *
 * Monthly share-of-cost (SOC) hours tracking for a case. Stores the
 * IHSS authorized hours, calculated spend-down amount, SOC authorized
 * amount, SOC hours, auth-to-pay, advance pay indicator, and breakdowns
 * by delivery mode (IP paid, recipient paid, CC, HM).
 */
@Entity
@Table(name = "case_soc_hours", indexes = {
        @Index(name = "idx_csh_case", columnList = "case_id"),
        @Index(name = "idx_csh_svc_month", columnList = "service_month")
})
public class CaseSOCHoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "service_month")
    private LocalDate serviceMonth;

    @Column(name = "ihss_auth_hours")
    private Integer ihssAuthHours;

    @Column(name = "calculated_spend_down_amt", precision = 10, scale = 2)
    private BigDecimal calculatedSpendDownAmt;

    @Column(name = "soc_auth_amt", precision = 10, scale = 2)
    private BigDecimal socAuthAmt;

    @Column(name = "soc_hours")
    private Integer socHours;

    @Column(name = "auth_to_pay")
    private Integer authToPay;

    @Column(name = "ap_ind")
    private Boolean apInd;

    @Column(name = "paid_mins")
    private Integer paidMins;

    @Column(name = "recipient_paid_mins")
    private Integer recipientPaidMins;

    @Column(name = "ip_mins")
    private Integer ipMins;

    @Column(name = "cc_mins")
    private Integer ccMins;

    @Column(name = "hm_mins")
    private Integer hmMins;

    @Column(name = "available_hours")
    private Integer availableHours;

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

    public Integer getIhssAuthHours() { return ihssAuthHours; }
    public void setIhssAuthHours(Integer ihssAuthHours) { this.ihssAuthHours = ihssAuthHours; }

    public BigDecimal getCalculatedSpendDownAmt() { return calculatedSpendDownAmt; }
    public void setCalculatedSpendDownAmt(BigDecimal calculatedSpendDownAmt) { this.calculatedSpendDownAmt = calculatedSpendDownAmt; }

    public BigDecimal getSocAuthAmt() { return socAuthAmt; }
    public void setSocAuthAmt(BigDecimal socAuthAmt) { this.socAuthAmt = socAuthAmt; }

    public Integer getSocHours() { return socHours; }
    public void setSocHours(Integer socHours) { this.socHours = socHours; }

    public Integer getAuthToPay() { return authToPay; }
    public void setAuthToPay(Integer authToPay) { this.authToPay = authToPay; }

    public Boolean getApInd() { return apInd; }
    public void setApInd(Boolean apInd) { this.apInd = apInd; }

    public Integer getPaidMins() { return paidMins; }
    public void setPaidMins(Integer paidMins) { this.paidMins = paidMins; }

    public Integer getRecipientPaidMins() { return recipientPaidMins; }
    public void setRecipientPaidMins(Integer recipientPaidMins) { this.recipientPaidMins = recipientPaidMins; }

    public Integer getIpMins() { return ipMins; }
    public void setIpMins(Integer ipMins) { this.ipMins = ipMins; }

    public Integer getCcMins() { return ccMins; }
    public void setCcMins(Integer ccMins) { this.ccMins = ccMins; }

    public Integer getHmMins() { return hmMins; }
    public void setHmMins(Integer hmMins) { this.hmMins = hmMins; }

    public Integer getAvailableHours() { return availableHours; }
    public void setAvailableHours(Integer availableHours) { this.availableHours = availableHours; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
