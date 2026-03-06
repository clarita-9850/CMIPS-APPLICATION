package com.cmips.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment Correction (DSD Section 27 CI-67321)
 *
 * Corrects payments to providers when a timesheet was processed for the
 * incorrect number of hours, erred out entirely, or an Advance Pay reconciling
 * timesheet needs to be reissued.
 *
 * All corrections require Payroll Approver secondary approval.
 * Time entries must be entered on a day-by-day basis.
 *
 * Pay Types:
 *  1. OVER_REPORTED_HOURS (Negative Hours) - Correct advance pay provider credited for too many hours
 *  2. PRIOR_UNDERPAYMENT - Pay additional hours to previously underpaid provider
 *  3. TIMESHEET_EXCEPTION - Pay provider for a timesheet erroneously exceptioned
 *  4. WPCS_RECIPIENT_ON_LEAVE - Pay WPCS provider up to 7 days while recipient is on leave
 */
@Entity
@Table(name = "payment_corrections", indexes = {
    @Index(name = "idx_paycorr_case", columnList = "case_id"),
    @Index(name = "idx_paycorr_status", columnList = "status"),
    @Index(name = "idx_paycorr_pay_period", columnList = "pay_period_start")
})
@Data
@NoArgsConstructor
public class PaymentCorrectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "provider_id", length = 50)
    private String providerId;

    @Column(name = "provider_name", length = 200)
    private String providerName;

    /**
     * OVER_REPORTED_HOURS, PRIOR_UNDERPAYMENT, TIMESHEET_EXCEPTION, WPCS_RECIPIENT_ON_LEAVE
     */
    @Column(name = "correction_type", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private CorrectionType correctionType;

    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    /** Total hours corrected (in minutes) - positive for add, negative for deduct */
    @Column(name = "hours_corrected_minutes")
    private Integer hoursCorrectedMinutes;

    /** Original timesheet ID if applicable (for TIMESHEET_EXCEPTION) */
    @Column(name = "original_timesheet_id")
    private Long originalTimesheetId;

    /** Day-by-day time entries stored as JSON string */
    @Column(name = "daily_time_entries", length = 4000)
    private String dailyTimeEntries;

    @Column(name = "program", length = 10)
    @Enumerated(EnumType.STRING)
    private Program program;

    @Column(name = "taxable_income")
    private Boolean taxableIncome = true;

    @Column(name = "soc_spend_down")
    private Boolean socSpendDown = false;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private CorrectionStatus status = CorrectionStatus.PENDING;

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

    public enum CorrectionType {
        OVER_REPORTED_HOURS,
        PRIOR_UNDERPAYMENT,
        TIMESHEET_EXCEPTION,
        WPCS_RECIPIENT_ON_LEAVE
    }

    public enum Program { IHSS, WPCS }

    public enum CorrectionStatus {
        PENDING,
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        CANCELLED,
        PROCESSED
    }
}
