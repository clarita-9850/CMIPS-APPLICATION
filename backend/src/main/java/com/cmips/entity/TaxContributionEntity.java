package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tax Contribution Entity — DSD Section 18
 * Tracks provider tax withholding elections (W-4/DE-4), year-to-date tax amounts,
 * and W-2 generation status for IHSS providers.
 */
@Entity
@Table(name = "tax_contributions", indexes = {
    @Index(name = "idx_tc_provider", columnList = "provider_id"),
    @Index(name = "idx_tc_year", columnList = "tax_year")
})
public class TaxContributionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "provider_number", length = 20)
    private String providerNumber;

    @Column(name = "tax_year", nullable = false)
    private Integer taxYear;

    /** Quarter 1-4 */
    @Column(name = "tax_quarter", nullable = false)
    private Integer taxQuarter;

    /** SINGLE, MARRIED_JOINTLY, MARRIED_SEPARATELY, HEAD_OF_HOUSEHOLD */
    @Column(name = "federal_filing_status", length = 30)
    private String federalFilingStatus;

    @Column(name = "federal_allowances")
    private Integer federalAllowances;

    @Column(name = "additional_federal_withholding", precision = 12, scale = 2)
    private BigDecimal additionalFederalWithholding;

    @Column(name = "state_filing_status", length = 30)
    private String stateFilingStatus;

    @Column(name = "state_allowances")
    private Integer stateAllowances;

    @Column(name = "additional_state_withholding", precision = 12, scale = 2)
    private BigDecimal additionalStateWithholding;

    @Column(name = "exempt_from_federal_withholding")
    private Boolean exemptFromFederalWithholding;

    @Column(name = "exempt_from_state_withholding")
    private Boolean exemptFromStateWithholding;

    @Column(name = "gross_wages_ytd", precision = 12, scale = 2)
    private BigDecimal grossWagesYtd;

    @Column(name = "federal_tax_ytd", precision = 12, scale = 2)
    private BigDecimal federalTaxYtd;

    @Column(name = "state_tax_ytd", precision = 12, scale = 2)
    private BigDecimal stateTaxYtd;

    @Column(name = "fica_ytd", precision = 12, scale = 2)
    private BigDecimal ficaYtd;

    @Column(name = "medicare_ytd", precision = 12, scale = 2)
    private BigDecimal medicareYtd;

    @Column(name = "sdi_ytd", precision = 12, scale = 2)
    private BigDecimal sdiYtd;

    @Column(name = "w2_generated")
    private Boolean w2Generated;

    @Column(name = "w2_generated_date")
    private LocalDate w2GeneratedDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    /** ACTIVE, INACTIVE */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public TaxContributionEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (w2Generated == null) w2Generated = false;
        if (exemptFromFederalWithholding == null) exemptFromFederalWithholding = false;
        if (exemptFromStateWithholding == null) exemptFromStateWithholding = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }

    public Integer getTaxYear() { return taxYear; }
    public void setTaxYear(Integer taxYear) { this.taxYear = taxYear; }

    public Integer getTaxQuarter() { return taxQuarter; }
    public void setTaxQuarter(Integer taxQuarter) { this.taxQuarter = taxQuarter; }

    public String getFederalFilingStatus() { return federalFilingStatus; }
    public void setFederalFilingStatus(String federalFilingStatus) { this.federalFilingStatus = federalFilingStatus; }

    public Integer getFederalAllowances() { return federalAllowances; }
    public void setFederalAllowances(Integer federalAllowances) { this.federalAllowances = federalAllowances; }

    public BigDecimal getAdditionalFederalWithholding() { return additionalFederalWithholding; }
    public void setAdditionalFederalWithholding(BigDecimal additionalFederalWithholding) { this.additionalFederalWithholding = additionalFederalWithholding; }

    public String getStateFilingStatus() { return stateFilingStatus; }
    public void setStateFilingStatus(String stateFilingStatus) { this.stateFilingStatus = stateFilingStatus; }

    public Integer getStateAllowances() { return stateAllowances; }
    public void setStateAllowances(Integer stateAllowances) { this.stateAllowances = stateAllowances; }

    public BigDecimal getAdditionalStateWithholding() { return additionalStateWithholding; }
    public void setAdditionalStateWithholding(BigDecimal additionalStateWithholding) { this.additionalStateWithholding = additionalStateWithholding; }

    public Boolean getExemptFromFederalWithholding() { return exemptFromFederalWithholding; }
    public void setExemptFromFederalWithholding(Boolean exemptFromFederalWithholding) { this.exemptFromFederalWithholding = exemptFromFederalWithholding; }

    public Boolean getExemptFromStateWithholding() { return exemptFromStateWithholding; }
    public void setExemptFromStateWithholding(Boolean exemptFromStateWithholding) { this.exemptFromStateWithholding = exemptFromStateWithholding; }

    public BigDecimal getGrossWagesYtd() { return grossWagesYtd; }
    public void setGrossWagesYtd(BigDecimal grossWagesYtd) { this.grossWagesYtd = grossWagesYtd; }

    public BigDecimal getFederalTaxYtd() { return federalTaxYtd; }
    public void setFederalTaxYtd(BigDecimal federalTaxYtd) { this.federalTaxYtd = federalTaxYtd; }

    public BigDecimal getStateTaxYtd() { return stateTaxYtd; }
    public void setStateTaxYtd(BigDecimal stateTaxYtd) { this.stateTaxYtd = stateTaxYtd; }

    public BigDecimal getFicaYtd() { return ficaYtd; }
    public void setFicaYtd(BigDecimal ficaYtd) { this.ficaYtd = ficaYtd; }

    public BigDecimal getMedicareYtd() { return medicareYtd; }
    public void setMedicareYtd(BigDecimal medicareYtd) { this.medicareYtd = medicareYtd; }

    public BigDecimal getSdiYtd() { return sdiYtd; }
    public void setSdiYtd(BigDecimal sdiYtd) { this.sdiYtd = sdiYtd; }

    public Boolean getW2Generated() { return w2Generated; }
    public void setW2Generated(Boolean w2Generated) { this.w2Generated = w2Generated; }

    public LocalDate getW2GeneratedDate() { return w2GeneratedDate; }
    public void setW2GeneratedDate(LocalDate w2GeneratedDate) { this.w2GeneratedDate = w2GeneratedDate; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

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
