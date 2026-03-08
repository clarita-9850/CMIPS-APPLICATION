package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Disaster Preparedness Entity - IHSS recipient disaster preparedness record.
 *
 * Linked to a case (not an assessment). Tracks the degree of contact needed,
 * life support needs, special impairments, extreme weather concerns,
 * and active/inactive status.
 */
@Entity
@Table(name = "disaster_preparedness", indexes = {
        @Index(name = "idx_dp_case", columnList = "case_id")
})
public class DisasterPreparednessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "assessment_evidence_id")
    private Long assessmentEvidenceId;

    @Column(name = "degree_of_contact_code", length = 10)
    private String degreeOfContactCode;

    @Column(name = "life_support_need_code", length = 10)
    private String lifeSupportNeedCode;

    @Column(name = "special_impairment_code", length = 10)
    private String specialImpairmentCode;

    @Column(name = "extreme_weather_code", length = 10)
    private String extremeWeatherCode;

    @Column(name = "comment", length = 50)
    private String comment;

    @Column(name = "status", length = 10)
    private String status;

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

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getAssessmentEvidenceId() { return assessmentEvidenceId; }
    public void setAssessmentEvidenceId(Long assessmentEvidenceId) { this.assessmentEvidenceId = assessmentEvidenceId; }

    public String getDegreeOfContactCode() { return degreeOfContactCode; }
    public void setDegreeOfContactCode(String degreeOfContactCode) { this.degreeOfContactCode = degreeOfContactCode; }

    public String getLifeSupportNeedCode() { return lifeSupportNeedCode; }
    public void setLifeSupportNeedCode(String lifeSupportNeedCode) { this.lifeSupportNeedCode = lifeSupportNeedCode; }

    public String getSpecialImpairmentCode() { return specialImpairmentCode; }
    public void setSpecialImpairmentCode(String specialImpairmentCode) { this.specialImpairmentCode = specialImpairmentCode; }

    public String getExtremeWeatherCode() { return extremeWeatherCode; }
    public void setExtremeWeatherCode(String extremeWeatherCode) { this.extremeWeatherCode = extremeWeatherCode; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
