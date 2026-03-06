package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * County Contractor Invoice Entity — DSD Section 25, CI-67732
 *
 * Tracks invoices from contracted agencies for IHSS services.
 * Authorization generates SOC 432 (Claim for Reimbursement).
 * Payment info passed to payroll system on authorization.
 */
@Entity
@Table(name = "county_contractor_invoices", indexes = {
        @Index(name = "idx_cci_case", columnList = "case_id"),
        @Index(name = "idx_cci_status", columnList = "status")
})
public class CountyContractorInvoiceEntity {

    public enum InvoiceStatus { PENDING, AUTHORIZED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "contractor_name", length = 200, nullable = false)
    private String contractorName;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "service_period_from")
    private LocalDate servicePeriodFrom;

    @Column(name = "service_period_to")
    private LocalDate servicePeriodTo;

    @Column(name = "invoice_amount", precision = 10, scale = 2)
    private BigDecimal invoiceAmount;

    @Column(name = "warrant_number", length = 50)
    private String warrantNumber;

    @Column(name = "warrant_date")
    private LocalDate warrantDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public String getContractorName() { return contractorName; }
    public void setContractorName(String contractorName) { this.contractorName = contractorName; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDate getServicePeriodFrom() { return servicePeriodFrom; }
    public void setServicePeriodFrom(LocalDate servicePeriodFrom) { this.servicePeriodFrom = servicePeriodFrom; }
    public LocalDate getServicePeriodTo() { return servicePeriodTo; }
    public void setServicePeriodTo(LocalDate servicePeriodTo) { this.servicePeriodTo = servicePeriodTo; }
    public BigDecimal getInvoiceAmount() { return invoiceAmount; }
    public void setInvoiceAmount(BigDecimal invoiceAmount) { this.invoiceAmount = invoiceAmount; }
    public String getWarrantNumber() { return warrantNumber; }
    public void setWarrantNumber(String warrantNumber) { this.warrantNumber = warrantNumber; }
    public LocalDate getWarrantDate() { return warrantDate; }
    public void setWarrantDate(LocalDate warrantDate) { this.warrantDate = warrantDate; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
