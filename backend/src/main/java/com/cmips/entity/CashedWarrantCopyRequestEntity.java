package com.cmips.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cashed Warrant Copy Request (DSD Section 27 CI-67327)
 *
 * When concerns arise that a warrant was cashed by someone other than
 * the payee, counties can request a copy from SCO.
 *
 * Request conveyed to SCO on the "Request Copies of Cashed Warrants by County/Office" report.
 * SCO sends the copy directly to the requesting county via address on the report.
 */
@Entity
@Table(name = "cashed_warrant_copy_requests", indexes = {
    @Index(name = "idx_cwcr_warrant", columnList = "warrant_id"),
    @Index(name = "idx_cwcr_status", columnList = "status")
})
@Data
@NoArgsConstructor
public class CashedWarrantCopyRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warrant_id", nullable = false)
    private Long warrantId;

    @Column(name = "warrant_number", nullable = false, length = 20)
    private String warrantNumber;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "sco_response_date")
    private LocalDate scoResponseDate;

    @Column(name = "sco_response_notes", length = 1000)
    private String scoResponseNotes;

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

    public enum RequestStatus {
        PENDING,     // Request submitted, awaiting SCO
        RECEIVED,    // Copy received from SCO
        CANCELLED    // Request cancelled
    }
}
