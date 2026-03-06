package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * County Contractor Invoice Entity
 * Based on DSD 20, Lines 14058-14080 (CountyContractorInvoice table).
 */
@Entity
@Table(name = "county_contractor_invoices")
public class CountyContractorInvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "county_contractor_id", nullable = false)
    private Long countyContractorId;

    @Column(name = "invoice_number", length = 6)
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "service_period", length = 10)
    private String servicePeriod;

    @Column(name = "processed_date")
    private LocalDate processedDate;

    @Column(name = "bill_rate", nullable = false, precision = 31, scale = 2)
    private BigDecimal billRate;

    @Column(name = "billing_month")
    private LocalDate billingMonth;

    @Column(name = "original_amt", nullable = false, precision = 31, scale = 2)
    private BigDecimal originalAmt;

    @Column(name = "rejected_amt", nullable = false, precision = 31, scale = 2)
    private BigDecimal rejectedAmt;

    @Column(name = "cut_back_amt", nullable = false, precision = 31, scale = 2)
    private BigDecimal cutBackAmt;

    @Column(name = "soc_collected_amt", nullable = false, precision = 31, scale = 2)
    private BigDecimal socCollectedAmt;

    @Column(name = "authorized_amt", nullable = false, precision = 31, scale = 2)
    private BigDecimal authorizedAmt;

    @Column(name = "warrant_number", length = 30)
    private String warrantNumber;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "status", length = 20)
    private String status = "Pending";

    @Column(name = "communication_id")
    private Long communicationId;

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

    public CountyContractorInvoiceEntity() {}

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCountyContractorId() { return countyContractorId; }
    public void setCountyContractorId(Long countyContractorId) { this.countyContractorId = countyContractorId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public String getServicePeriod() { return servicePeriod; }
    public void setServicePeriod(String servicePeriod) { this.servicePeriod = servicePeriod; }

    public LocalDate getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDate processedDate) { this.processedDate = processedDate; }

    public BigDecimal getBillRate() { return billRate; }
    public void setBillRate(BigDecimal billRate) { this.billRate = billRate; }

    public LocalDate getBillingMonth() { return billingMonth; }
    public void setBillingMonth(LocalDate billingMonth) { this.billingMonth = billingMonth; }

    public BigDecimal getOriginalAmt() { return originalAmt; }
    public void setOriginalAmt(BigDecimal originalAmt) { this.originalAmt = originalAmt; }

    public BigDecimal getRejectedAmt() { return rejectedAmt; }
    public void setRejectedAmt(BigDecimal rejectedAmt) { this.rejectedAmt = rejectedAmt; }

    public BigDecimal getCutBackAmt() { return cutBackAmt; }
    public void setCutBackAmt(BigDecimal cutBackAmt) { this.cutBackAmt = cutBackAmt; }

    public BigDecimal getSocCollectedAmt() { return socCollectedAmt; }
    public void setSocCollectedAmt(BigDecimal socCollectedAmt) { this.socCollectedAmt = socCollectedAmt; }

    public BigDecimal getAuthorizedAmt() { return authorizedAmt; }
    public void setAuthorizedAmt(BigDecimal authorizedAmt) { this.authorizedAmt = authorizedAmt; }

    public String getWarrantNumber() { return warrantNumber; }
    public void setWarrantNumber(String warrantNumber) { this.warrantNumber = warrantNumber; }

    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCommunicationId() { return communicationId; }
    public void setCommunicationId(Long communicationId) { this.communicationId = communicationId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
