package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Death Match Entity — DSD Section 26
 *
 * Records death match results from external data sources (CDPH, SCO, SSA, MEDS)
 * that flag a recipient or provider as potentially deceased.
 *
 * Match confidence: HIGH, MEDIUM, LOW (based on SSN + name + DOB match quality)
 *
 * Verification lifecycle:
 *   PENDING_VERIFICATION -> VERIFIED / FALSE_MATCH / UNRESOLVED
 *
 * Actions taken on verified matches:
 *   CASE_TERMINATED, PROVIDER_INELIGIBLE, NO_ACTION, PENDING
 */
@Entity
@Table(name = "death_matches", indexes = {
    @Index(name = "idx_dm_person", columnList = "person_id"),
    @Index(name = "idx_dm_status", columnList = "verification_status"),
    @Index(name = "idx_dm_source", columnList = "match_source")
})
public class DeathMatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "person_type", nullable = false, length = 20)
    private String personType;

    @Column(name = "person_name", length = 200)
    private String personName;

    @Column(name = "ssn", length = 11)
    private String ssn;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "date_of_death")
    private LocalDate dateOfDeath;

    @Column(name = "match_source", nullable = false, length = 30)
    private String matchSource;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "match_confidence", length = 20)
    private String matchConfidence;

    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_date")
    private LocalDate verifiedDate;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "action_taken", length = 50)
    private String actionTaken;

    @Column(name = "action_date")
    private LocalDate actionDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public DeathMatchEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (verificationStatus == null) verificationStatus = "PENDING_VERIFICATION";
        if (actionTaken == null) actionTaken = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }

    public String getPersonType() { return personType; }
    public void setPersonType(String personType) { this.personType = personType; }

    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public LocalDate getDateOfDeath() { return dateOfDeath; }
    public void setDateOfDeath(LocalDate dateOfDeath) { this.dateOfDeath = dateOfDeath; }

    public String getMatchSource() { return matchSource; }
    public void setMatchSource(String matchSource) { this.matchSource = matchSource; }

    public LocalDate getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }

    public String getMatchConfidence() { return matchConfidence; }
    public void setMatchConfidence(String matchConfidence) { this.matchConfidence = matchConfidence; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDate getVerifiedDate() { return verifiedDate; }
    public void setVerifiedDate(LocalDate verifiedDate) { this.verifiedDate = verifiedDate; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public LocalDate getActionDate() { return actionDate; }
    public void setActionDate(LocalDate actionDate) { this.actionDate = actionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
