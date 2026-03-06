package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * County Contractor Entity - Represents a County Contractor rate record.
 * Based on DSD 21, Lines 6499-6515 (CountyContractor table).
 */
@Entity
@Table(name = "county_contractors")
public class CountyContractorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "contractor_name", length = 60)
    private String contractorName;

    @Column(name = "rate_amt", precision = 31, scale = 2)
    private BigDecimal rateAmt;

    @Column(name = "wage_amt", nullable = false, precision = 31, scale = 2)
    private BigDecimal wageAmt;

    @Column(name = "macr_amt", precision = 31, scale = 2)
    private BigDecimal macrAmt;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "county_contractor_number", length = 6)
    private String countyContractorNumber;

    @Column(name = "bi_monthly_invoice_ind", nullable = false, length = 1)
    private String biMonthlyInvoiceInd = "N";

    // Audit fields
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

    public CountyContractorEntity() {}

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public String getContractorName() { return contractorName; }
    public void setContractorName(String contractorName) { this.contractorName = contractorName; }

    public BigDecimal getRateAmt() { return rateAmt; }
    public void setRateAmt(BigDecimal rateAmt) { this.rateAmt = rateAmt; }

    public BigDecimal getWageAmt() { return wageAmt; }
    public void setWageAmt(BigDecimal wageAmt) { this.wageAmt = wageAmt; }

    public BigDecimal getMacrAmt() { return macrAmt; }
    public void setMacrAmt(BigDecimal macrAmt) { this.macrAmt = macrAmt; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public String getCountyContractorNumber() { return countyContractorNumber; }
    public void setCountyContractorNumber(String countyContractorNumber) { this.countyContractorNumber = countyContractorNumber; }

    public String getBiMonthlyInvoiceInd() { return biMonthlyInvoiceInd; }
    public void setBiMonthlyInvoiceInd(String biMonthlyInvoiceInd) { this.biMonthlyInvoiceInd = biMonthlyInvoiceInd; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
