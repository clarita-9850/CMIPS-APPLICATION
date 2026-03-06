package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Program Evidence Entity — DSD Section 21
 *
 * Tracks program-level eligibility factors for a case: which IHSS program
 * applies, Medi-Cal aid code, funding category, and effective period.
 * Supports IHSS, WPCS, CFCO, and IPO programs.
 */
@Entity
@Table(name = "program_evidence", indexes = {
        @Index(name = "idx_pe_case", columnList = "case_id"),
        @Index(name = "idx_pe_status", columnList = "status")
})
public class ProgramEvidenceEntity {

    public enum ProgramType {
        IHSS,   // In-Home Supportive Services (standard)
        WPCS,   // Waiver Personal Care Services
        CFCO,   // Community First Choice Option
        IPO     // Independence Plus Option
    }

    public enum FundingCategory {
        STATE,
        COUNTY,
        FEDERAL_WAIVER,
        STATE_AND_COUNTY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_type", length = 10, nullable = false)
    private ProgramType programType;

    @Enumerated(EnumType.STRING)
    @Column(name = "funding_category", length = 20)
    private FundingCategory fundingCategory;

    /** Medi-Cal aid code (2-digit) — determines eligibility for IHSS */
    @Column(name = "medi_cal_aid_code", length = 4)
    private String mediCalAidCode;

    /** Whether recipient has active Medi-Cal enrollment */
    @Column(name = "medi_cal_active")
    private Boolean mediCalActive;

    /** Whether recipient meets IHSS functional need criteria */
    @Column(name = "functional_need_met")
    private Boolean functionalNeedMet;

    /** Whether recipient meets IHSS financial eligibility criteria */
    @Column(name = "financial_eligibility_met")
    private Boolean financialEligibilityMet;

    /** Reason for program eligibility or denial */
    @Column(name = "eligibility_reason", columnDefinition = "TEXT")
    private String eligibilityReason;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", length = 20)
    private String status; // ACTIVE, INACTIVE, PENDING

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
        if (status == null) status = "ACTIVE";
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

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public ProgramType getProgramType() { return programType; }
    public void setProgramType(ProgramType programType) { this.programType = programType; }

    public FundingCategory getFundingCategory() { return fundingCategory; }
    public void setFundingCategory(FundingCategory fundingCategory) { this.fundingCategory = fundingCategory; }

    public String getMediCalAidCode() { return mediCalAidCode; }
    public void setMediCalAidCode(String mediCalAidCode) { this.mediCalAidCode = mediCalAidCode; }

    public Boolean getMediCalActive() { return mediCalActive; }
    public void setMediCalActive(Boolean mediCalActive) { this.mediCalActive = mediCalActive; }

    public Boolean getFunctionalNeedMet() { return functionalNeedMet; }
    public void setFunctionalNeedMet(Boolean functionalNeedMet) { this.functionalNeedMet = functionalNeedMet; }

    public Boolean getFinancialEligibilityMet() { return financialEligibilityMet; }
    public void setFinancialEligibilityMet(Boolean financialEligibilityMet) { this.financialEligibilityMet = financialEligibilityMet; }

    public String getEligibilityReason() { return eligibilityReason; }
    public void setEligibilityReason(String eligibilityReason) { this.eligibilityReason = eligibilityReason; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
