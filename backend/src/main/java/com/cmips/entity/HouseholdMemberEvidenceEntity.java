package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Household Member Evidence Entity - IHSS Assessment household member details.
 *
 * Tracks individual household members associated with a service eligibility
 * assessment, including relationship, companion case links, and protective
 * supervision indicators.
 */
@Entity
@Table(name = "household_member_evidence", indexes = {
        @Index(name = "idx_hme_assessment", columnList = "assessment_evidence_id")
})
public class HouseholdMemberEvidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_evidence_id", nullable = false)
    private Long assessmentEvidenceId;

    @Column(name = "last_name", length = 30)
    private String lastName;

    @Column(name = "first_name", length = 25)
    private String firstName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "parent_spouse_code", length = 10)
    private String parentSpouseCode;

    @Column(name = "relationship_code", length = 10)
    private String relationshipCode;

    @Column(name = "companion_case_id")
    private Long companionCaseId;

    @Column(name = "protect_super_ind")
    private Boolean protectSuperInd;

    @Column(name = "include_protect_super_ind")
    private Boolean includeProtectSuperInd;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (protectSuperInd == null) protectSuperInd = false;
        if (includeProtectSuperInd == null) includeProtectSuperInd = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssessmentEvidenceId() { return assessmentEvidenceId; }
    public void setAssessmentEvidenceId(Long assessmentEvidenceId) { this.assessmentEvidenceId = assessmentEvidenceId; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getParentSpouseCode() { return parentSpouseCode; }
    public void setParentSpouseCode(String parentSpouseCode) { this.parentSpouseCode = parentSpouseCode; }

    public String getRelationshipCode() { return relationshipCode; }
    public void setRelationshipCode(String relationshipCode) { this.relationshipCode = relationshipCode; }

    public Long getCompanionCaseId() { return companionCaseId; }
    public void setCompanionCaseId(Long companionCaseId) { this.companionCaseId = companionCaseId; }

    public Boolean getProtectSuperInd() { return protectSuperInd; }
    public void setProtectSuperInd(Boolean protectSuperInd) { this.protectSuperInd = protectSuperInd; }

    public Boolean getIncludeProtectSuperInd() { return includeProtectSuperInd; }
    public void setIncludeProtectSuperInd(Boolean includeProtectSuperInd) { this.includeProtectSuperInd = includeProtectSuperInd; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
