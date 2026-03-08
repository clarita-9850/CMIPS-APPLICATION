package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Protective Supervision Evidence Entity - IHSS protective supervision details.
 *
 * Child of ServiceTypeEvidenceEntity (for service type PROTECT_SUPER).
 * Tracks form dates and pending-info status for protective supervision
 * authorization.
 */
@Entity
@Table(name = "protective_supervision_evidence", indexes = {
        @Index(name = "idx_pse_service_type", columnList = "service_type_evidence_id")
})
public class ProtectiveSupervisionEvidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_type_evidence_id", nullable = false)
    private Long serviceTypeEvidenceId;

    @Column(name = "assessment_evidence_id")
    private Long assessmentEvidenceId;

    @Column(name = "pending_additional_info_ind")
    private Boolean pendingAdditionalInfoInd;

    @Column(name = "form_sent_date")
    private LocalDate formSentDate;

    @Column(name = "form_received_date")
    private LocalDate formReceivedDate;

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
        if (pendingAdditionalInfoInd == null) pendingAdditionalInfoInd = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getServiceTypeEvidenceId() { return serviceTypeEvidenceId; }
    public void setServiceTypeEvidenceId(Long serviceTypeEvidenceId) { this.serviceTypeEvidenceId = serviceTypeEvidenceId; }

    public Long getAssessmentEvidenceId() { return assessmentEvidenceId; }
    public void setAssessmentEvidenceId(Long assessmentEvidenceId) { this.assessmentEvidenceId = assessmentEvidenceId; }

    public Boolean getPendingAdditionalInfoInd() { return pendingAdditionalInfoInd; }
    public void setPendingAdditionalInfoInd(Boolean pendingAdditionalInfoInd) { this.pendingAdditionalInfoInd = pendingAdditionalInfoInd; }

    public LocalDate getFormSentDate() { return formSentDate; }
    public void setFormSentDate(LocalDate formSentDate) { this.formSentDate = formSentDate; }

    public LocalDate getFormReceivedDate() { return formReceivedDate; }
    public void setFormReceivedDate(LocalDate formReceivedDate) { this.formReceivedDate = formReceivedDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
