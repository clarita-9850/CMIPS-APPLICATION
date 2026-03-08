package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payroll Batch Run Entity — DSD Section 17
 * Tracks payroll batch processing runs including regular, supplemental,
 * correction, and advance batch types. Integrates with SCO file generation.
 */
@Entity
@Table(name = "payroll_batch_runs", indexes = {
    @Index(name = "idx_pbr_status", columnList = "status"),
    @Index(name = "idx_pbr_period", columnList = "pay_period_begin_date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_pbr_batch_number", columnNames = {"batch_number"})
})
public class PayrollBatchRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_number", nullable = false, unique = true, length = 30)
    private String batchNumber;

    /** REGULAR, SUPPLEMENTAL, CORRECTION, ADVANCE */
    @Column(name = "batch_type", nullable = false, length = 30)
    private String batchType;

    @Column(name = "pay_period_begin_date", nullable = false)
    private LocalDate payPeriodBeginDate;

    @Column(name = "pay_period_end_date", nullable = false)
    private LocalDate payPeriodEndDate;

    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "total_timesheets")
    private Integer totalTimesheets;

    @Column(name = "total_gross_pay", precision = 14, scale = 2)
    private BigDecimal totalGrossPay;

    @Column(name = "total_deductions", precision = 14, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "total_net_pay", precision = 14, scale = 2)
    private BigDecimal totalNetPay;

    /** Total overtime hours in minutes */
    @Column(name = "total_overtime_hours")
    private Integer totalOvertimeHours;

    /** Total regular hours in minutes */
    @Column(name = "total_regular_hours")
    private Integer totalRegularHours;

    /** PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED */
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "error_details", length = 4000)
    private String errorDetails;

    @Column(name = "sco_file_generated")
    private Boolean scoFileGenerated;

    @Column(name = "sco_file_name", length = 200)
    private String scoFileName;

    @Column(name = "sco_submitted_date")
    private LocalDateTime scoSubmittedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public PayrollBatchRunEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
        if (scoFileGenerated == null) scoFileGenerated = false;
        if (errorCount == null) errorCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getBatchType() { return batchType; }
    public void setBatchType(String batchType) { this.batchType = batchType; }

    public LocalDate getPayPeriodBeginDate() { return payPeriodBeginDate; }
    public void setPayPeriodBeginDate(LocalDate payPeriodBeginDate) { this.payPeriodBeginDate = payPeriodBeginDate; }

    public LocalDate getPayPeriodEndDate() { return payPeriodEndDate; }
    public void setPayPeriodEndDate(LocalDate payPeriodEndDate) { this.payPeriodEndDate = payPeriodEndDate; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public Integer getTotalTimesheets() { return totalTimesheets; }
    public void setTotalTimesheets(Integer totalTimesheets) { this.totalTimesheets = totalTimesheets; }

    public BigDecimal getTotalGrossPay() { return totalGrossPay; }
    public void setTotalGrossPay(BigDecimal totalGrossPay) { this.totalGrossPay = totalGrossPay; }

    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

    public BigDecimal getTotalNetPay() { return totalNetPay; }
    public void setTotalNetPay(BigDecimal totalNetPay) { this.totalNetPay = totalNetPay; }

    public Integer getTotalOvertimeHours() { return totalOvertimeHours; }
    public void setTotalOvertimeHours(Integer totalOvertimeHours) { this.totalOvertimeHours = totalOvertimeHours; }

    public Integer getTotalRegularHours() { return totalRegularHours; }
    public void setTotalRegularHours(Integer totalRegularHours) { this.totalRegularHours = totalRegularHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(LocalDateTime submittedDate) { this.submittedDate = submittedDate; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }

    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }

    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }

    public Boolean getScoFileGenerated() { return scoFileGenerated; }
    public void setScoFileGenerated(Boolean scoFileGenerated) { this.scoFileGenerated = scoFileGenerated; }

    public String getScoFileName() { return scoFileName; }
    public void setScoFileName(String scoFileName) { this.scoFileName = scoFileName; }

    public LocalDateTime getScoSubmittedDate() { return scoSubmittedDate; }
    public void setScoSubmittedDate(LocalDateTime scoSubmittedDate) { this.scoSubmittedDate = scoSubmittedDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
