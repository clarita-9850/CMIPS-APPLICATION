package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IRS Live-In Provider Self-Certification Entity — DSD Section 32, CI-718021/718024
 *
 * Records self-certification forms from providers claiming "Live-In Excluded" status
 * for federal and state tax exclusion purposes (SOC 2298 / SOC 2299).
 */
@Entity
@Table(name = "live_in_self_certifications", indexes = {
        @Index(name = "idx_livein_provider", columnList = "provider_id"),
        @Index(name = "idx_livein_case", columnList = "case_id")
})
public class LiveInSelfCertificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "provider_number", length = 20, nullable = false)
    private String providerNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_number", length = 50, nullable = false)
    private String caseNumber;

    @Column(name = "recipient_id")
    private Long recipientId;

    /** "YES", "NO", or null (Blank) */
    @Column(name = "certification_status", length = 10)
    private String certificationStatus;

    @Column(name = "status_date")
    private LocalDate statusDate;

    /** "MANUAL" for entries via the screen, could be "BATCH" for future imports */
    @Column(name = "mode_of_entry", length = 20)
    private String modeOfEntry;

    @Column(name = "provider_county", length = 10)
    private String providerCounty;

    @Column(name = "provider_name", length = 200)
    private String providerName;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public LiveInSelfCertificationEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getCertificationStatus() { return certificationStatus; }
    public void setCertificationStatus(String certificationStatus) { this.certificationStatus = certificationStatus; }

    public LocalDate getStatusDate() { return statusDate; }
    public void setStatusDate(LocalDate statusDate) { this.statusDate = statusDate; }

    public String getModeOfEntry() { return modeOfEntry; }
    public void setModeOfEntry(String modeOfEntry) { this.modeOfEntry = modeOfEntry; }

    public String getProviderCounty() { return providerCounty; }
    public void setProviderCounty(String providerCounty) { this.providerCounty = providerCounty; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
