package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * DSD Section 24 — Timesheet Code Tables
 * Stores configurable lookup values for timesheet processing.
 * 21 code table types per DSD specification.
 */
@Entity
@Table(name = "timesheet_code_tables", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ts_ct", columnNames = {"table_type", "code"})
})
public class TimesheetCodeTableEntity {

    public enum TableType {
        TIMESHEET_TYPE,             // CT-01: Standard, Large Font, EVV, Advance Pay, etc.
        TIMESHEET_STATUS,           // CT-02: Pending Issuance, Issued, Received, etc.
        SOURCE_TYPE,                // CT-03: TPF Paper, Electronic ESP, Manual, EVV Mobile
        PROGRAM_TYPE,               // CT-04: IHSS, WPCS
        EXCEPTION_TYPE,             // CT-05: Hard Edit, Soft Edit, Hold Condition
        EXCEPTION_SEVERITY,         // CT-06: Block, Warning, Hold
        PAY_PERIOD_TYPE,            // CT-07: Semi-monthly (1st-15th, 16th-EOM)
        HOLD_REASON,                // CT-08: Early, Late, BVI, Excessive, Sampling, Flagged
        CUTBACK_REASON,             // CT-09: Leave, Ineligible, OT, Authorized Exceeded
        REJECTION_REASON,           // CT-10: Invalid TS#, Duplicate, No Signature, etc.
        VOID_REASON,                // CT-11: Payment Correction, Administrative, Error
        FUNDING_SOURCE_AID_CODE,    // CT-12: 2K, 2L, 2M, 2N, etc.
        SOC_INDICATOR,              // CT-13: Y, N
        BVI_REVIEW_STATUS,          // CT-14: Pending, Approved, Rejected
        EVV_EXCEPTION_REASON,       // CT-15: System Malfunction, Phone Issue, etc.
        TRAVEL_CLAIM_STATUS,        // CT-16: Submitted, Hold, Approved, Rejected
        OVERTIME_TYPE,              // CT-17: Weekly 66:00, Weekly 70:45, Monthly Max
        ISSUANCE_METHOD,            // CT-18: Mail, Electronic, In-Person
        PAYMENT_CORRECTION_TYPE,    // CT-19: Void, Void/Reissue, Supplemental
        SAMPLING_STATUS,            // CT-20: Selected, Verified, Failed
        COUNTY_CODE                 // CT-21: 58 California counties
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_type", length = 40, nullable = false)
    private TableType tableType;

    @Column(name = "code", length = 30, nullable = false)
    private String code;

    @Column(name = "description", length = 200, nullable = false)
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "effective_date")
    private java.time.LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private java.time.LocalDate expirationDate;

    @Column(name = "parent_code", length = 30)
    private String parentCode;

    @Column(name = "metadata", length = 500)
    private String metadata; // JSON for extra config (e.g., max hours for OT type)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TableType getTableType() { return tableType; }
    public void setTableType(TableType tableType) { this.tableType = tableType; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public java.time.LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(java.time.LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public java.time.LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(java.time.LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
