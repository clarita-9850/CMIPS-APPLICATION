package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Share of Cost Evidence Entity - IHSS Assessment SOC calculation details.
 *
 * Stores SSI/SSP linkage, benefit level, dependents, countable income,
 * and the computed IHSS share-of-cost amount. Child income rows are
 * in IncomeEvidenceEntity.
 */
@Entity
@Table(name = "share_of_cost_evidence", indexes = {
        @Index(name = "idx_soce_assessment", columnList = "assessment_evidence_id")
})
public class ShareOfCostEvidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_evidence_id", nullable = false)
    private Long assessmentEvidenceId;

    @Column(name = "share_of_cost_linkage_code", length = 10)
    private String shareOfCostLinkageCode;

    @Column(name = "benefit_level_code", length = 10)
    private String benefitLevelCode;

    @Column(name = "dependents")
    private Integer dependents;

    @Column(name = "countable_income_amt")
    private BigDecimal countableIncomeAmt;

    @Column(name = "ihss_share_of_cost_amt")
    private BigDecimal ihssShareOfCostAmt;

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

    public Long getAssessmentEvidenceId() { return assessmentEvidenceId; }
    public void setAssessmentEvidenceId(Long assessmentEvidenceId) { this.assessmentEvidenceId = assessmentEvidenceId; }

    public String getShareOfCostLinkageCode() { return shareOfCostLinkageCode; }
    public void setShareOfCostLinkageCode(String shareOfCostLinkageCode) { this.shareOfCostLinkageCode = shareOfCostLinkageCode; }

    public String getBenefitLevelCode() { return benefitLevelCode; }
    public void setBenefitLevelCode(String benefitLevelCode) { this.benefitLevelCode = benefitLevelCode; }

    public Integer getDependents() { return dependents; }
    public void setDependents(Integer dependents) { this.dependents = dependents; }

    public BigDecimal getCountableIncomeAmt() { return countableIncomeAmt; }
    public void setCountableIncomeAmt(BigDecimal countableIncomeAmt) { this.countableIncomeAmt = countableIncomeAmt; }

    public BigDecimal getIhssShareOfCostAmt() { return ihssShareOfCostAmt; }
    public void setIhssShareOfCostAmt(BigDecimal ihssShareOfCostAmt) { this.ihssShareOfCostAmt = ihssShareOfCostAmt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
