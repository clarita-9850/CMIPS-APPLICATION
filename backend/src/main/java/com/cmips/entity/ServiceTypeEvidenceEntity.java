package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Service Type Evidence Entity - IHSS Assessment service type details.
 *
 * One row per service type per assessment. Stores assessed need, deductions
 * (refused, alternate resources, voluntary), adjustments, and the resulting
 * net adjusted need in minutes. Child tasks are in ServiceTaskEvidenceEntity.
 *
 * Service type codes: DOMESTIC, MEAL_PREP, LAUNDRY, SHOPPING, AMBULATION,
 * BATHING, GROOMING, DRESSING, BOWEL_BLADDER, TRANSFER, FEEDING, RESPIRATION,
 * PARAMEDICAL, PROTECT_SUPER, MEAL_CLEANUP, HEAVY_CLEAN, YARD_HAZARD,
 * SNOW_REMOVAL, ACCOMP_MED, ACCOMP_ALT, MENSTRUAL, SKIN_CARE, RELATED,
 * TEACH_DEMO, PERSONAL_CARE.
 */
@Entity
@Table(name = "service_type_evidence", indexes = {
        @Index(name = "idx_ste_assessment", columnList = "assessment_evidence_id")
})
public class ServiceTypeEvidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_evidence_id", nullable = false)
    private Long assessmentEvidenceId;

    @Column(name = "service_type_code", length = 10)
    private String serviceTypeCode;

    @Column(name = "assessed_need_min")
    private Integer assessedNeedMin;

    @Column(name = "refused_services_min")
    private Integer refusedServicesMin;

    @Column(name = "alternate_resources_min")
    private Integer alternateResourcesMin;

    @Column(name = "voluntary_services_min")
    private Integer voluntaryServicesMin;

    @Column(name = "adjustments_min")
    private Integer adjustmentsMin;

    @Column(name = "ind_assessed_need_min")
    private Integer indAssessedNeedMin;

    @Column(name = "net_adj_need_min")
    private Integer netAdjNeedMin;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "number_of_months")
    private Integer numberOfMonths;

    @Column(name = "above_below_code", length = 10)
    private String aboveBelowCode;

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

    public String getServiceTypeCode() { return serviceTypeCode; }
    public void setServiceTypeCode(String serviceTypeCode) { this.serviceTypeCode = serviceTypeCode; }

    public Integer getAssessedNeedMin() { return assessedNeedMin; }
    public void setAssessedNeedMin(Integer assessedNeedMin) { this.assessedNeedMin = assessedNeedMin; }

    public Integer getRefusedServicesMin() { return refusedServicesMin; }
    public void setRefusedServicesMin(Integer refusedServicesMin) { this.refusedServicesMin = refusedServicesMin; }

    public Integer getAlternateResourcesMin() { return alternateResourcesMin; }
    public void setAlternateResourcesMin(Integer alternateResourcesMin) { this.alternateResourcesMin = alternateResourcesMin; }

    public Integer getVoluntaryServicesMin() { return voluntaryServicesMin; }
    public void setVoluntaryServicesMin(Integer voluntaryServicesMin) { this.voluntaryServicesMin = voluntaryServicesMin; }

    public Integer getAdjustmentsMin() { return adjustmentsMin; }
    public void setAdjustmentsMin(Integer adjustmentsMin) { this.adjustmentsMin = adjustmentsMin; }

    public Integer getIndAssessedNeedMin() { return indAssessedNeedMin; }
    public void setIndAssessedNeedMin(Integer indAssessedNeedMin) { this.indAssessedNeedMin = indAssessedNeedMin; }

    public Integer getNetAdjNeedMin() { return netAdjNeedMin; }
    public void setNetAdjNeedMin(Integer netAdjNeedMin) { this.netAdjNeedMin = netAdjNeedMin; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getNumberOfMonths() { return numberOfMonths; }
    public void setNumberOfMonths(Integer numberOfMonths) { this.numberOfMonths = numberOfMonths; }

    public String getAboveBelowCode() { return aboveBelowCode; }
    public void setAboveBelowCode(String aboveBelowCode) { this.aboveBelowCode = aboveBelowCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
