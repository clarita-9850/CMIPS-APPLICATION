package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Income Evidence Entity - IHSS Assessment income source details.
 *
 * Child of ShareOfCostEvidenceEntity. Each row represents one income
 * source with monthly amount and deduction for SOC computation.
 */
@Entity
@Table(name = "income_evidence", indexes = {
        @Index(name = "idx_ie_soc", columnList = "share_of_cost_evidence_id")
})
public class IncomeEvidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "share_of_cost_evidence_id", nullable = false)
    private Long shareOfCostEvidenceId;

    @Column(name = "income_source_code", length = 10)
    private String incomeSourceCode;

    @Column(name = "monthly_income_amt")
    private BigDecimal monthlyIncomeAmt;

    @Column(name = "deduction_amt")
    private BigDecimal deductionAmt;

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

    public Long getShareOfCostEvidenceId() { return shareOfCostEvidenceId; }
    public void setShareOfCostEvidenceId(Long shareOfCostEvidenceId) { this.shareOfCostEvidenceId = shareOfCostEvidenceId; }

    public String getIncomeSourceCode() { return incomeSourceCode; }
    public void setIncomeSourceCode(String incomeSourceCode) { this.incomeSourceCode = incomeSourceCode; }

    public BigDecimal getMonthlyIncomeAmt() { return monthlyIncomeAmt; }
    public void setMonthlyIncomeAmt(BigDecimal monthlyIncomeAmt) { this.monthlyIncomeAmt = monthlyIncomeAmt; }

    public BigDecimal getDeductionAmt() { return deductionAmt; }
    public void setDeductionAmt(BigDecimal deductionAmt) { this.deductionAmt = deductionAmt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
