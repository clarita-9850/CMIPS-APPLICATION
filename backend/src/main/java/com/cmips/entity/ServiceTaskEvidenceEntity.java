package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Service Task Evidence Entity - IHSS Assessment individual task details.
 *
 * Child of ServiceTypeEvidenceEntity. Each row represents one task within
 * a service type, with frequency, quantity, duration, and proration for
 * shared household members.
 */
@Entity
@Table(name = "service_task_evidence", indexes = {
        @Index(name = "idx_stask_service_type", columnList = "service_type_evidence_id")
})
public class ServiceTaskEvidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_type_evidence_id", nullable = false)
    private Long serviceTypeEvidenceId;

    @Column(name = "service_task_code", length = 10)
    private String serviceTaskCode;

    @Column(name = "service_task_free_text", length = 50)
    private String serviceTaskFreeText;

    @Column(name = "frequency_code", length = 10)
    private String frequencyCode;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "duration_min")
    private Integer durationMin;

    @Column(name = "proration")
    private Integer proration;

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

    public Long getServiceTypeEvidenceId() { return serviceTypeEvidenceId; }
    public void setServiceTypeEvidenceId(Long serviceTypeEvidenceId) { this.serviceTypeEvidenceId = serviceTypeEvidenceId; }

    public String getServiceTaskCode() { return serviceTaskCode; }
    public void setServiceTaskCode(String serviceTaskCode) { this.serviceTaskCode = serviceTaskCode; }

    public String getServiceTaskFreeText() { return serviceTaskFreeText; }
    public void setServiceTaskFreeText(String serviceTaskFreeText) { this.serviceTaskFreeText = serviceTaskFreeText; }

    public String getFrequencyCode() { return frequencyCode; }
    public void setFrequencyCode(String frequencyCode) { this.frequencyCode = frequencyCode; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }

    public Integer getProration() { return proration; }
    public void setProration(Integer proration) { this.proration = proration; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
