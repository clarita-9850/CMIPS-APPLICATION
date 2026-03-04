package com.cmips.dto;

import java.time.LocalDate;

/**
 * Response DTO for IRS Live-In Provider Self-Certification Entry screen (CI-718024).
 * Contains read-only detail fields + current certification status.
 */
public class LiveInCertEntryResponse {

    private Long providerId;
    private String providerNumber;
    private String providerName;
    private String providerCounty;
    private Long caseId;
    private String caseNumber;
    private Long recipientId;
    private String recipientName;
    private String currentCertificationStatus;
    private LocalDate statusDate;

    public LiveInCertEntryResponse() {}

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderCounty() { return providerCounty; }
    public void setProviderCounty(String providerCounty) { this.providerCounty = providerCounty; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getCurrentCertificationStatus() { return currentCertificationStatus; }
    public void setCurrentCertificationStatus(String currentCertificationStatus) { this.currentCertificationStatus = currentCertificationStatus; }

    public LocalDate getStatusDate() { return statusDate; }
    public void setStatusDate(LocalDate statusDate) { this.statusDate = statusDate; }
}
