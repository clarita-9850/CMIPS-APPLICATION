package com.cmips.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Special Transaction (DSD Section 27 CI-67322)
 *
 * One-time payments and deductions requested by County, CDSS, Vendor, or System.
 * Requires Payroll Approver secondary approval (except Vendor travel claims).
 * Interfaced to Payroll at end of day after approval.
 *
 * Pay Types: ADVANCE_PAY_ADDITIONAL, ADVANCE_PAY_INITIAL, ADVANCE_PAY_RECOVERY_REFUND,
 *   LEGISLATIVE_CHANGE, OVERPAYMENT_RECOVERY_REFUND, PARAMEDICAL_REIMBURSEMENT,
 *   RESTAURANT_MEALS_INITIAL, SERVICE_AUTH_REIMBURSEMENT, STATE_HEARING_DECISION,
 *   WRIT_OF_ADMIN_MANDAMUS, ADVANCE_PAY_OVERTIME, ARREARS_TRAVEL, PROVIDER_MEDI_CAL_SOC_REIMBURSEMENT,
 *   NON_FPO_ELIGIBLE_OVERTIME, CONLAN_REIMBURSEMENT, RETROACTIVE_OVERTIME_PAY,
 *   TRAVEL_CLAIM, TRAVEL_CLAIM_SUPPLEMENTAL, OVERTIME_EXEMPTION_PAY_OVER_LIMIT,
 *   FUNDING_SOURCE_HOURS_PAYMENT, SICK_LEAVE, SICK_LEAVE_EMERGENCY, COVID_SICK_LEAVE_STATE,
 *   COVID_MEDICAL_APPOINTMENT, ADVANCE_PAY_BACKUP_PROVIDER,
 *   BUY_OUT_REIMBURSEMENT, HEALTH_BENEFIT_DEDUCTION, HEALTH_BENEFIT_REFUND,
 *   SHARE_OF_COST_REFUND, PROVIDER_MEDI_CAL_SOC_REIMBURSEMENT_NEGATIVE
 */
@Entity
@Table(name = "special_transactions", indexes = {
    @Index(name = "idx_sptxn_case", columnList = "case_id"),
    @Index(name = "idx_sptxn_status", columnList = "status"),
    @Index(name = "idx_sptxn_pay_type", columnList = "pay_type"),
    @Index(name = "idx_sptxn_service_period", columnList = "service_period_from")
})
@Data
@NoArgsConstructor
public class SpecialTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    /** County, CDSS, Vendor, or System */
    @Column(name = "transaction_source", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionSource transactionSource;

    /** Pay type from DSD table (33 pay types + 8 deduction types) */
    @Column(name = "pay_type", nullable = false, length = 60)
    private String payType;

    /** PAYMENT or DEDUCTION */
    @Column(name = "transaction_direction", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private TransactionDirection transactionDirection;

    /** RECIPIENT or PROVIDER */
    @Column(name = "payee_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PayeeType payeeType;

    @Column(name = "payee_id", length = 50)
    private String payeeId;

    @Column(name = "payee_name", length = 200)
    private String payeeName;

    /** Dollars or Hours */
    @Column(name = "amount_type", length = 10)
    @Enumerated(EnumType.STRING)
    private AmountType amountType;

    /** Amount in dollars (if amountType=DOLLARS) */
    @Column(name = "amount_dollars", precision = 12, scale = 2)
    private BigDecimal amountDollars;

    /** Hours (in minutes, if amountType=HOURS) */
    @Column(name = "amount_minutes")
    private Integer amountMinutes;

    @Column(name = "service_period_from")
    private LocalDate servicePeriodFrom;

    @Column(name = "service_period_to")
    private LocalDate servicePeriodTo;

    @Column(name = "program", length = 10)
    @Enumerated(EnumType.STRING)
    private Program program;

    @Column(name = "funding_source", length = 30)
    private String fundingSource;

    /** Adjusts authorized hours in case */
    @Column(name = "adjusts_hours_in_case")
    private Boolean adjustsHoursInCase = false;

    @Column(name = "bypass_hours")
    private Boolean bypassHours = false;

    @Column(name = "taxable_income")
    private Boolean taxableIncome = true;

    @Column(name = "soc_spend_down")
    private Boolean socSpendDown = false;

    @Column(name = "rate_override", precision = 10, scale = 4)
    private BigDecimal rateOverride;

    @Column(name = "notes", length = 2000)
    private String notes;

    /** For Travel Claim additional options */
    @Column(name = "is_travel_claim")
    private Boolean isTravelClaim = false;

    @Column(name = "travel_claim_form_number", length = 50)
    private String travelClaimFormNumber;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    /** Payroll batch transaction ID once interfaced */
    @Column(name = "payroll_txn_id", length = 50)
    private String payrollTxnId;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransactionSource { COUNTY, CDSS, VENDOR, SYSTEM }
    public enum TransactionDirection { PAYMENT, DEDUCTION }
    public enum PayeeType { RECIPIENT, PROVIDER, AP_PROVIDER, WPCS_PROVIDER }
    public enum AmountType { DOLLARS, HOURS }
    public enum Program { IHSS, WPCS }

    public enum TransactionStatus {
        PENDING,           // Just created, not yet submitted
        PENDING_APPROVAL,  // Submitted, awaiting second Payroll Approver
        APPROVED,          // Approved, interfaced to Payroll nightly batch
        REJECTED,          // Rejected by approver
        CANCELLED,         // Cancelled by requestor before approval
        PROCESSED          // Interfaced and processed by Payroll
    }
}
