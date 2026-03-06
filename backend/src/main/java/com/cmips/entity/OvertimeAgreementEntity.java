package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Overtime Agreement Entity — DSD Section 25, CI-480922/480921/480926/480918
 *
 * Records recipient acknowledgment of overtime rules per FLSA.
 * OVERTIME_EXEMPTION: companion/live-in providers may be exempt from FLSA OT rules.
 * Date Received must be >= 11/1/2014 and not in the future.
 */
@Entity
@Table(name = "overtime_agreements", indexes = {
        @Index(name = "idx_oa_case", columnList = "case_id"),
        @Index(name = "idx_oa_provider", columnList = "provider_id")
})
public class OvertimeAgreementEntity {

    public enum AgreementType { OVERTIME, OVERTIME_EXEMPTION }
    public enum AgreementStatus { ACTIVE, INACTIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "provider_number", length = 20)
    private String providerNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", length = 30, nullable = false)
    private AgreementType agreementType;

    @Column(name = "date_received", nullable = false)
    private LocalDate dateReceived;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AgreementStatus status = AgreementStatus.ACTIVE;

    @Column(name = "inactivated_date")
    private LocalDate inactivatedDate;

    @Column(name = "inactivated_by", length = 100)
    private String inactivatedBy;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }
    public AgreementType getAgreementType() { return agreementType; }
    public void setAgreementType(AgreementType agreementType) { this.agreementType = agreementType; }
    public LocalDate getDateReceived() { return dateReceived; }
    public void setDateReceived(LocalDate dateReceived) { this.dateReceived = dateReceived; }
    public AgreementStatus getStatus() { return status; }
    public void setStatus(AgreementStatus status) { this.status = status; }
    public LocalDate getInactivatedDate() { return inactivatedDate; }
    public void setInactivatedDate(LocalDate inactivatedDate) { this.inactivatedDate = inactivatedDate; }
    public String getInactivatedBy() { return inactivatedBy; }
    public void setInactivatedBy(String inactivatedBy) { this.inactivatedBy = inactivatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
