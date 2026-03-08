package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Functional Index Evidence Entity - IHSS Assessment functional rankings.
 *
 * Stores the functional rank (1-6) for each assessed area:
 *   1 = Independent
 *   2 = Able to perform but needs verbal assistance
 *   3 = Can perform with some human assistance
 *   4 = Can perform only with substantial human assistance
 *   5 = Cannot perform without complete human assistance
 *   6 = Totally dependent
 */
@Entity
@Table(name = "functional_index_evidence", indexes = {
        @Index(name = "idx_fie_assessment", columnList = "assessment_evidence_id")
})
public class FunctionalIndexEvidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_evidence_id", nullable = false)
    private Long assessmentEvidenceId;

    @Column(name = "housework", length = 10)
    private String housework;

    @Column(name = "laundry", length = 10)
    private String laundry;

    @Column(name = "shopping_and_errands", length = 10)
    private String shoppingAndErrands;

    @Column(name = "meal_prep_and_cleanup", length = 10)
    private String mealPrepAndCleanup;

    @Column(name = "ambulation", length = 10)
    private String ambulation;

    @Column(name = "bathing_and_grooming", length = 10)
    private String bathingAndGrooming;

    @Column(name = "dressing", length = 10)
    private String dressing;

    @Column(name = "bowel_bladder_and_menstrual_care", length = 10)
    private String bowelBladderAndMenstrualCare;

    @Column(name = "transfer", length = 10)
    private String transfer;

    @Column(name = "feeding", length = 10)
    private String feeding;

    @Column(name = "respiration", length = 10)
    private String respiration;

    @Column(name = "memory", length = 10)
    private String memory;

    @Column(name = "orientation", length = 10)
    private String orientation;

    @Column(name = "judgment", length = 10)
    private String judgment;

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

    public String getHousework() { return housework; }
    public void setHousework(String housework) { this.housework = housework; }

    public String getLaundry() { return laundry; }
    public void setLaundry(String laundry) { this.laundry = laundry; }

    public String getShoppingAndErrands() { return shoppingAndErrands; }
    public void setShoppingAndErrands(String shoppingAndErrands) { this.shoppingAndErrands = shoppingAndErrands; }

    public String getMealPrepAndCleanup() { return mealPrepAndCleanup; }
    public void setMealPrepAndCleanup(String mealPrepAndCleanup) { this.mealPrepAndCleanup = mealPrepAndCleanup; }

    public String getAmbulation() { return ambulation; }
    public void setAmbulation(String ambulation) { this.ambulation = ambulation; }

    public String getBathingAndGrooming() { return bathingAndGrooming; }
    public void setBathingAndGrooming(String bathingAndGrooming) { this.bathingAndGrooming = bathingAndGrooming; }

    public String getDressing() { return dressing; }
    public void setDressing(String dressing) { this.dressing = dressing; }

    public String getBowelBladderAndMenstrualCare() { return bowelBladderAndMenstrualCare; }
    public void setBowelBladderAndMenstrualCare(String bowelBladderAndMenstrualCare) { this.bowelBladderAndMenstrualCare = bowelBladderAndMenstrualCare; }

    public String getTransfer() { return transfer; }
    public void setTransfer(String transfer) { this.transfer = transfer; }

    public String getFeeding() { return feeding; }
    public void setFeeding(String feeding) { this.feeding = feeding; }

    public String getRespiration() { return respiration; }
    public void setRespiration(String respiration) { this.respiration = respiration; }

    public String getMemory() { return memory; }
    public void setMemory(String memory) { this.memory = memory; }

    public String getOrientation() { return orientation; }
    public void setOrientation(String orientation) { this.orientation = orientation; }

    public String getJudgment() { return judgment; }
    public void setJudgment(String judgment) { this.judgment = judgment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
