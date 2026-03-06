package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CC Invoice Details Entity - Line items for a County Contractor Invoice.
 * Based on DSD 20, Lines 14084-14095 (CCInvoiceDetails table).
 */
@Entity
@Table(name = "cc_invoice_details")
public class CCInvoiceDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "county_contractor_invoice_id", nullable = false)
    private Long countyContractorInvoiceId;

    @Column(name = "case_count", nullable = false)
    private Integer caseCount;

    @Column(name = "funding_aid_code", length = 10)
    private String fundingAidCode;

    @Column(name = "service_month")
    private LocalDate serviceMonth;

    @Column(name = "amount", nullable = false, precision = 31, scale = 2)
    private BigDecimal amount;

    public CCInvoiceDetailsEntity() {}

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCountyContractorInvoiceId() { return countyContractorInvoiceId; }
    public void setCountyContractorInvoiceId(Long countyContractorInvoiceId) { this.countyContractorInvoiceId = countyContractorInvoiceId; }

    public Integer getCaseCount() { return caseCount; }
    public void setCaseCount(Integer caseCount) { this.caseCount = caseCount; }

    public String getFundingAidCode() { return fundingAidCode; }
    public void setFundingAidCode(String fundingAidCode) { this.fundingAidCode = fundingAidCode; }

    public LocalDate getServiceMonth() { return serviceMonth; }
    public void setServiceMonth(LocalDate serviceMonth) { this.serviceMonth = serviceMonth; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
