package com.cmips.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Void/Reissue/Replacement Request (DSD Section 27 CI-67323/67325/67326/67318)
 *
 * Tracks requests to void, reissue, or replace warrants:
 *
 * STOP (Void, no replacement):
 *   Reasons: CANCELLED, INCORRECT_INFORMATION, PAYEE_INELIGIBLE
 *   Sent to SCO on Void/Stop Payment report daily.
 *   On SCO confirmation: Pay Status → Void; triggers SOC reversal if applicable.
 *
 * REISSUE (Stop + new warrant):
 *   Reasons: DAMAGED (provider/recipient reports damaged warrant)
 *   Two-pronged: stop original + new payment in nightly payroll cycle.
 *
 * REPLACEMENT (Stop + duplicate warrant, no new warrant number):
 *   Reasons: LOST, STOLEN, DESTROYED, NEVER_RECEIVED
 *   SCO replaces the original; CGI Back Office manually enters Replacement Date.
 *
 * REDEPOSIT (Undeliverable warrant):
 *   Returned directly to SCO. Void Type = Redeposit.
 *   SOC reversal processed + hours increased.
 */
@Entity
@Table(name = "void_reissue_requests", indexes = {
    @Index(name = "idx_vrr_warrant", columnList = "warrant_id"),
    @Index(name = "idx_vrr_status", columnList = "status"),
    @Index(name = "idx_vrr_type", columnList = "request_type")
})
@Data
@NoArgsConstructor
public class VoidReissueRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warrant_id", nullable = false)
    private Long warrantId;

    @Column(name = "warrant_number", nullable = false, length = 20)
    private String warrantNumber;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "request_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    /** CANCELLED, INCORRECT_INFORMATION, PAYEE_INELIGIBLE (stop); DAMAGED (reissue);
     *  LOST, STOLEN, DESTROYED, NEVER_RECEIVED (replacement); UNDELIVERABLE (redeposit) */
    @Column(name = "void_reason", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private VoidReason voidReason;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    /** Date SCO confirmed void/stop payment */
    @Column(name = "sco_confirmation_date")
    private LocalDate scoConfirmationDate;

    /** New warrant number if reissued */
    @Column(name = "replacement_warrant_id")
    private Long replacementWarrantId;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RequestType {
        STOP,        // Void, funds redeposited — no replacement
        REISSUE,     // Stop + new warrant number generated
        REPLACEMENT, // Stop + duplicate warrant same number (STD 435)
        REDEPOSIT    // Undeliverable warrant returned to SCO
    }

    public enum VoidReason {
        CANCELLED,
        INCORRECT_INFORMATION,
        PAYEE_INELIGIBLE,
        DAMAGED,
        LOST,
        STOLEN,
        DESTROYED,
        NEVER_RECEIVED,
        UNDELIVERABLE
    }

    public enum RequestStatus {
        PENDING,   // Request recorded, not yet sent to SCO
        SENT,      // Transmitted on Void/Stop Payment report
        CONFIRMED, // SCO confirmed void processing
        CANCELLED  // Request cancelled before sending
    }
}
