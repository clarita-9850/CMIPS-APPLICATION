package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Household Evidence Entity - IHSS Assessment household details.
 *
 * Tracks appliance indicators, living arrangement, residence type,
 * and room counts linked to a service eligibility assessment.
 */
@Entity
@Table(name = "household_evidence", indexes = {
        @Index(name = "idx_he_assessment", columnList = "assessment_evidence_id")
})
public class HouseholdEvidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "assessment_evidence_id")
    private Long assessmentEvidenceId;

    @Column(name = "stove_ind")
    private Boolean stoveInd;

    @Column(name = "refrigerator_ind")
    private Boolean refrigeratorInd;

    @Column(name = "washer_ind")
    private Boolean washerInd;

    @Column(name = "dryer_ind")
    private Boolean dryerInd;

    @Column(name = "yard_ind")
    private Boolean yardInd;

    @Column(name = "living_arrange_code", length = 10)
    private String livingArrangeCode;

    @Column(name = "residence_type_code", length = 10)
    private String residenceTypeCode;

    @Column(name = "rooms_private")
    private Integer roomsPrivate;

    @Column(name = "rooms_shared")
    private Integer roomsShared;

    @Column(name = "rooms_unused")
    private Integer roomsUnused;

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
        if (stoveInd == null) stoveInd = false;
        if (refrigeratorInd == null) refrigeratorInd = false;
        if (washerInd == null) washerInd = false;
        if (dryerInd == null) dryerInd = false;
        if (yardInd == null) yardInd = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getAssessmentEvidenceId() { return assessmentEvidenceId; }
    public void setAssessmentEvidenceId(Long assessmentEvidenceId) { this.assessmentEvidenceId = assessmentEvidenceId; }

    public Boolean getStoveInd() { return stoveInd; }
    public void setStoveInd(Boolean stoveInd) { this.stoveInd = stoveInd; }

    public Boolean getRefrigeratorInd() { return refrigeratorInd; }
    public void setRefrigeratorInd(Boolean refrigeratorInd) { this.refrigeratorInd = refrigeratorInd; }

    public Boolean getWasherInd() { return washerInd; }
    public void setWasherInd(Boolean washerInd) { this.washerInd = washerInd; }

    public Boolean getDryerInd() { return dryerInd; }
    public void setDryerInd(Boolean dryerInd) { this.dryerInd = dryerInd; }

    public Boolean getYardInd() { return yardInd; }
    public void setYardInd(Boolean yardInd) { this.yardInd = yardInd; }

    public String getLivingArrangeCode() { return livingArrangeCode; }
    public void setLivingArrangeCode(String livingArrangeCode) { this.livingArrangeCode = livingArrangeCode; }

    public String getResidenceTypeCode() { return residenceTypeCode; }
    public void setResidenceTypeCode(String residenceTypeCode) { this.residenceTypeCode = residenceTypeCode; }

    public Integer getRoomsPrivate() { return roomsPrivate; }
    public void setRoomsPrivate(Integer roomsPrivate) { this.roomsPrivate = roomsPrivate; }

    public Integer getRoomsShared() { return roomsShared; }
    public void setRoomsShared(Integer roomsShared) { this.roomsShared = roomsShared; }

    public Integer getRoomsUnused() { return roomsUnused; }
    public void setRoomsUnused(Integer roomsUnused) { this.roomsUnused = roomsUnused; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
