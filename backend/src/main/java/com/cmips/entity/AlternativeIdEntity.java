package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Alternative ID Entity - Audit trail for merged SSN records.
 * Per DSD CI-446456 BR-38: On Save, create Alternative ID record
 * with type "Duplicate SSN" and audit comment.
 */
@Entity
@Table(name = "alternative_ids")
public class AlternativeIdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    // RECIPIENT or PROVIDER
    @Column(name = "person_type", nullable = false, length = 20)
    private String personType;

    // e.g. "DUPLICATE_SSN"
    @Column(name = "alternative_id_type", nullable = false, length = 50)
    private String alternativeIdType;

    @Column(name = "original_ssn", length = 11)
    private String originalSsn;

    @Column(name = "master_cin", length = 20)
    private String masterCin;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public AlternativeIdEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }

    public String getPersonType() { return personType; }
    public void setPersonType(String personType) { this.personType = personType; }

    public String getAlternativeIdType() { return alternativeIdType; }
    public void setAlternativeIdType(String alternativeIdType) { this.alternativeIdType = alternativeIdType; }

    public String getOriginalSsn() { return originalSsn; }
    public void setOriginalSsn(String originalSsn) { this.originalSsn = originalSsn; }

    public String getMasterCin() { return masterCin; }
    public void setMasterCin(String masterCin) { this.masterCin = masterCin; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
