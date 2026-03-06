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
 * Overpayment Recovery (DSD Section 27 CI-67319)
 *
 * Tracks overpayments of IHSS/WPCS funds and their recovery through
 * payroll deductions or personal payments.
 *
 * Always set up from the recipient case prospective.
 * Recovery can span up to 12 consecutive calendar months.
 *
 * 14 Overpayment Recovery Types:
 *  1. Advance Pay – Recipient Payroll Deductions
 *  2. Advance Pay – Other
 *  3. Excess Compensation – Hours
 *  4. Excess Compensation – Rate
 *  5. Restaurant Meals
 *  6. Share of Cost
 *  7. Special Transaction (Dollars)
 *  8. Special Transaction (Hours)
 *  9. Special Transaction (Provider)
 *  10. Special Transaction (Provider AP)
 *  11. Converted Overpayment
 *  12. Legacy Special Transaction (Recipient/Provider)
 *  13. Legacy Special Transaction (Provider)
 *  14. Excess Compensation – Travel
 */
@Entity
@Table(name = "overpayments", indexes = {
    @Index(name = "idx_overpay_case", columnList = "case_id"),
    @Index(name = "idx_overpay_status", columnList = "status"),
    @Index(name = "idx_overpay_overpaid_payee", columnList = "overpaid_payee_id"),
    @Index(name = "idx_overpay_recovery_payee", columnList = "recovery_payee_id")
})
@Data
@NoArgsConstructor
public class OverpaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    /** 14 recovery types per DSD table */
    @Column(name = "overpayment_type", nullable = false, length = 60)
    @Enumerated(EnumType.STRING)
    private OverpaymentType overpaymentType;

    @Column(name = "program", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Program program;

    /** Who received the overpayment */
    @Column(name = "overpaid_payee_type", length = 20)
    @Enumerated(EnumType.STRING)
    private PayeeType overpaidPayeeType;

    @Column(name = "overpaid_payee_id", length = 50)
    private String overpaidPayeeId;

    @Column(name = "overpaid_payee_name", length = 200)
    private String overpaidPayeeName;

    /** Who pays back (may differ for cross-county) */
    @Column(name = "recovery_payee_type", length = 20)
    @Enumerated(EnumType.STRING)
    private PayeeType recoveryPayeeType;

    @Column(name = "recovery_payee_id", length = 50)
    private String recoveryPayeeId;

    @Column(name = "recovery_payee_name", length = 200)
    private String recoveryPayeeName;

    /** DOLLARS or HOURS */
    @Column(name = "amount_type", length = 10)
    @Enumerated(EnumType.STRING)
    private AmountType amountType;

    /** Service period start (overpayment occurred) */
    @Column(name = "service_period_from", nullable = false)
    private LocalDate servicePeriodFrom;

    /** Service period end — may not exceed 12 consecutive months */
    @Column(name = "service_period_to", nullable = false)
    private LocalDate servicePeriodTo;

    /** Calculated net overpayment from Payroll (dollars) */
    @Column(name = "total_net_overpayment", precision = 12, scale = 2)
    private BigDecimal totalNetOverpayment;

    /** Overpaid hours (in minutes) */
    @Column(name = "overpaid_hours_minutes")
    private Integer overpaidHoursMinutes;

    /** Negotiated recovery amount (may be less than total net overpayment) */
    @Column(name = "recovery_amount", precision = 12, scale = 2)
    private BigDecimal recoveryAmount;

    /** Outstanding balance remaining */
    @Column(name = "balance", precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /** PAYROLL_DEDUCTION or PERSONAL_PAYMENT or BOTH */
    @Column(name = "recovery_method", length = 30)
    @Enumerated(EnumType.STRING)
    private RecoveryMethod recoveryMethod;

    /** For NEGOTIATED or OTHER installment types */
    @Column(name = "installment_type", length = 30)
    @Enumerated(EnumType.STRING)
    private InstallmentType installmentType;

    @Column(name = "installment_amount", precision = 12, scale = 2)
    private BigDecimal installmentAmount;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "comments", length = 2000)
    private String comments;

    /** If linked to a special transaction that caused the overpayment */
    @Column(name = "source_special_transaction_id")
    private Long sourceSpecialTransactionId;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private OverpaymentStatus status = OverpaymentStatus.PENDING;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Column(name = "stopped_at")
    private LocalDateTime stoppedAt;

    @Column(name = "stopped_by", length = 100)
    private String stoppedBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OverpaymentType {
        ADVANCE_PAY_RECIPIENT_PAYROLL_DEDUCTIONS,
        ADVANCE_PAY_OTHER,
        EXCESS_COMPENSATION_HOURS,
        EXCESS_COMPENSATION_RATE,
        RESTAURANT_MEALS,
        SHARE_OF_COST,
        SPECIAL_TRANSACTION_DOLLARS_RECIPIENT_OR_PROVIDER,
        SPECIAL_TRANSACTION_HOURS_RECIPIENT_OR_PROVIDER,
        SPECIAL_TRANSACTION_DOLLARS_PROVIDER,
        SPECIAL_TRANSACTION_HOURS_PROVIDER_AP,
        CONVERTED_OVERPAYMENT,
        LEGACY_SPECIAL_TRANSACTION_RECIPIENT_OR_PROVIDER,
        LEGACY_SPECIAL_TRANSACTION_PROVIDER,
        EXCESS_COMPENSATION_TRAVEL
    }

    public enum Program { IHSS, WPCS }
    public enum PayeeType { RECIPIENT, PROVIDER }
    public enum AmountType { DOLLARS, HOURS }

    public enum RecoveryMethod {
        PAYROLL_DEDUCTION,
        PERSONAL_PAYMENT
    }

    public enum InstallmentType {
        NEGOTIATED,
        AUTOMATIC,
        OTHER
    }

    public enum OverpaymentStatus {
        PENDING,          // Created, pay periods being set up, not yet submitted
        PENDING_PAYROLL,  // Submitted, flagged for nightly batch (Payroll Deduction method)
        ACTIVE,           // Deductions ongoing or personal payments being collected
        STOPPED,          // Stop Collection action taken
        CANCELLED,        // Cancel Recovery action taken
        CLOSED            // Balance reduced to $0.00 (fully collected)
    }
}
