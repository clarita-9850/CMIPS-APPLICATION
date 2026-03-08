package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * IHSS Authorized Service Entity - DSD Section 22.
 *
 * Child of IHSSAuthorizationEntity. One row per service type that has been
 * authorized for purchase. Links back to the service type evidence record
 * from the assessment and captures the unmet need and authorized-to-purchase
 * minutes for that specific service type.
 */
@Entity
@Table(name = "ihss_authorized_service", indexes = {
        @Index(name = "idx_ihss_auth_svc_auth", columnList = "ihss_authorization_id")
})
public class IHSSAuthorizedServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ihss_authorization_id", nullable = false)
    private Long ihssAuthorizationId;

    @Column(name = "service_type_evidence_id")
    private Long serviceTypeEvidenceId;

    @Column(name = "service_type_code", length = 20)
    private String serviceTypeCode;

    @Column(name = "unmet_need_min")
    private Integer unmetNeedMin;

    @Column(name = "auth_to_purchase_min")
    private Integer authToPurchaseMin;

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

    public Long getIhssAuthorizationId() { return ihssAuthorizationId; }
    public void setIhssAuthorizationId(Long ihssAuthorizationId) { this.ihssAuthorizationId = ihssAuthorizationId; }

    public Long getServiceTypeEvidenceId() { return serviceTypeEvidenceId; }
    public void setServiceTypeEvidenceId(Long serviceTypeEvidenceId) { this.serviceTypeEvidenceId = serviceTypeEvidenceId; }

    public String getServiceTypeCode() { return serviceTypeCode; }
    public void setServiceTypeCode(String serviceTypeCode) { this.serviceTypeCode = serviceTypeCode; }

    public Integer getUnmetNeedMin() { return unmetNeedMin; }
    public void setUnmetNeedMin(Integer unmetNeedMin) { this.unmetNeedMin = unmetNeedMin; }

    public Integer getAuthToPurchaseMin() { return authToPurchaseMin; }
    public void setAuthToPurchaseMin(Integer authToPurchaseMin) { this.authToPurchaseMin = authToPurchaseMin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
