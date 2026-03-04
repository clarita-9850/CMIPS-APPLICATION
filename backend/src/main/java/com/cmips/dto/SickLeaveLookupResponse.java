package com.cmips.dto;

import java.time.LocalDate;

/**
 * Response DTO for Sick Leave Claim Manual Entry lookup (CI-790531).
 * Contains provider/recipient details for the Time Entries screen.
 */
public class SickLeaveLookupResponse {

    private Long providerId;
    private String providerNumber;
    private String providerName;
    private String providerType; // "IHSS" or "WPCS"
    private Long caseId;
    private String caseNumber;
    private Long recipientId;
    private String recipientName;
    private LocalDate servicePeriodFrom;

    public SickLeaveLookupResponse() {}

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public LocalDate getServicePeriodFrom() { return servicePeriodFrom; }
    public void setServicePeriodFrom(LocalDate servicePeriodFrom) { this.servicePeriodFrom = servicePeriodFrom; }
}
