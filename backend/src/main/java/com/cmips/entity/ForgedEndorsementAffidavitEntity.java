package com.cmips.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Forged Endorsement Affidavit (DSD Section 27 CI-67317)
 *
 * When a payee claims they did not cash a warrant, they must complete and sign
 * STO CA 0034 – Forged Endorsement Affidavit to initiate a replacement.
 *
 * Counties record: affidavit signed date, submitted to SCO date,
 * SCO response date, and SCO response.
 *
 * The Create/Modify screens allow printing the pre-populated form (Box 1 pre-filled).
 */
@Entity
@Table(name = "forged_endorsement_affidavits", indexes = {
    @Index(name = "idx_fea_warrant", columnList = "warrant_id"),
    @Index(name = "idx_fea_status", columnList = "status")
})
@Data
@NoArgsConstructor
public class ForgedEndorsementAffidavitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warrant_id", nullable = false)
    private Long warrantId;

    @Column(name = "warrant_number", nullable = false, length = 20)
    private String warrantNumber;

    @Column(name = "case_id")
    private Long caseId;

    /** Date the payee signed the STO CA 0034 affidavit */
    @Column(name = "affidavit_signed_date")
    private LocalDate affidavitSignedDate;

    /** Date the affidavit was submitted to SCO */
    @Column(name = "submitted_to_sco_date")
    private LocalDate submittedToScoDate;

    /** Date SCO responded */
    @Column(name = "sco_response_date")
    private LocalDate scoResponseDate;

    /** SCO's response (from code table: APPROVED, DENIED, PENDING) */
    @Column(name = "sco_response", length = 30)
    @Enumerated(EnumType.STRING)
    private ScoResponse scoResponse;

    @Column(name = "sco_response_notes", length = 1000)
    private String scoResponseNotes;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AffidavitStatus status = AffidavitStatus.ACTIVE;

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

    public enum ScoResponse {
        APPROVED,   // SCO approved the replacement
        DENIED,     // SCO denied the claim
        PENDING     // Awaiting SCO decision
    }

    public enum AffidavitStatus {
        ACTIVE,
        CANCELLED
    }
}
