package com.cmips.dto;

/**
 * Request DTO for IRS Live-In Provider Self-Certification Search "Continue" action (CI-718023).
 */
public class LiveInCertLookupRequest {

    private String providerNumber;
    private String caseNumber;

    public LiveInCertLookupRequest() {}

    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
}
